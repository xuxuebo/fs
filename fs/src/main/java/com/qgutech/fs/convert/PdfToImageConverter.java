package com.qgutech.fs.convert;

import com.qgutech.fs.utils.FsConstants;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class PdfToImageConverter extends AbstractConverter {

    private int pdfSplitSize = DEFAULT_PDF_SPLIT_SIZE;
    private int semaphoreCnt = DEFAULT_SEMAPHORE_CNT;
    private ThreadPoolTaskExecutor taskExecutor;
    private static final int DEFAULT_PDF_SPLIT_SIZE = 100;
    private static final int DEFAULT_SEMAPHORE_CNT = Runtime.getRuntime().availableProcessors() / 2 + 1;

    @Override
    protected File windowsConvert(String inputFilePath, final String targetFileDirPath) throws Exception {
        List<String> pdfFilePathList = splitPdf(inputFilePath);
        if (pdfFilePathList.size() == 1) {
            return super.windowsConvert(inputFilePath, targetFileDirPath);
        }

        final Semaphore semaphore = new Semaphore(getSemaphoreCnt());
        List<Future<File>> futures = new ArrayList<Future<File>>(pdfFilePathList.size());
        try {
            for (String filePath : pdfFilePathList) {
                semaphore.acquire();
                final String pdfFilePath = filePath;
                futures.add(getTaskExecutor().submit(new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        try {
                            return superWindowsConvert(pdfFilePath, targetFileDirPath);
                        } finally {
                            semaphore.release();
                        }
                    }
                }));
            }

            File targetFile = null;
            for (Future<File> future : futures) {
                File file = future.get();
                if (targetFile == null) {
                    targetFile = file;
                }
            }

            return targetFile;
        } catch (Exception e) {
            for (Future<File> future : futures) {
                future.cancel(true);
            }

            throw e;
        } finally {
            FileUtils.deleteQuietly(new File(pdfFilePathList.get(0)).getParentFile());
        }
    }

    final protected File superWindowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        return super.windowsConvert(inputFilePath, targetFileDirPath);
    }

    protected List<String> splitPdf(String pdfFile) throws Exception {
        File file = new File(pdfFile);
        PDDocument pdDocument = null;
        File tempDir = null;
        List<PDDocument> pdDocuments = null;
        try {
            pdDocument = PDDocument.load(file);
            int numberOfPages = pdDocument.getNumberOfPages();
            if (numberOfPages <= getPdfSplitSize()) {
                return Arrays.asList(pdfFile);
            }

            tempDir = new File(file.getParentFile(), UUID.randomUUID().toString());
            if (!tempDir.mkdirs()) {
                throw new IOException("cannot create directory[" + tempDir.getAbsolutePath() + "]!");
            }

            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(getPdfSplitSize());
            pdDocuments = splitter.split(pdDocument);
            List<String> subFiles = new ArrayList<String>(pdDocuments.size());
            for (int i = 0; i < pdDocuments.size(); i++) {
                PDDocument document = pdDocuments.get(i);
                File subPdfFile = new File(tempDir, i + FsConstants.PDF_SUFFIX);
                subFiles.add(subPdfFile.getAbsolutePath());
                document.save(subPdfFile);
            }

            return subFiles;
        } catch (Exception e) {
            if (tempDir != null) {
                FileUtils.deleteQuietly(tempDir);
            }

            throw e;
        } finally {
            if (pdDocuments != null && pdDocuments.size() > 0) {
                for (PDDocument document : pdDocuments) {
                    closePDDocument(document);
                }
            }

            closePDDocument(pdDocument);
        }
    }

    private void closePDDocument(PDDocument document) {
        if (document != null) {
            try {
                document.close();
            } catch (Exception e) {
                //not need process
            }
        }
    }

    public int getPdfSplitSize() {
        return pdfSplitSize;
    }

    public void setPdfSplitSize(int pdfSplitSize) {
        this.pdfSplitSize = pdfSplitSize;
    }

    public int getSemaphoreCnt() {
        return semaphoreCnt;
    }

    public void setSemaphoreCnt(int semaphoreCnt) {
        this.semaphoreCnt = semaphoreCnt;
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
}

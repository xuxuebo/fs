package com.qgutech.fs.convert;


import com.qgutech.fs.utils.ConvertUtils;
import com.qgutech.fs.utils.FsConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.UUID;

public class DocToPdfConverter extends AbstractConverter {

    private String getFileExtension(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isEmpty(extension)) {
            return FsConstants.DOC_TYPE_DOC;
        }

        return extension.toLowerCase();
    }

    @Override
    protected File windowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        String extension = getFileExtension(inputFilePath);
        File targetFile = getTargetFile(targetFileDirPath);
        if (FsConstants.DOC_TYPE_PDF.equals(extension)) {
            FileUtils.copyFile(new File(inputFilePath), targetFile);
        } else if (FsConstants.DOC_TYPE_DOC.equals(extension)
                || FsConstants.DOC_TYPE_DOCX.equals(extension)
                || FsConstants.DOC_TYPE_TXT.equals(extension)) {
            ConvertUtils.wordToPdf(inputFilePath, targetFile.getAbsolutePath());
        } else if (FsConstants.DOC_TYPE_PPT.equals(extension)
                || FsConstants.DOC_TYPE_PPTX.equals(extension)) {
            ConvertUtils.excelToPdf(inputFilePath, targetFile.getAbsolutePath());
        } else if (FsConstants.DOC_TYPE_XLS.equals(extension)
                || FsConstants.DOC_TYPE_XLSX.equals(extension)) {
            ConvertUtils.pptToPdf(inputFilePath, targetFile.getAbsolutePath());
        } else {
            throw new RuntimeException("InputFile[path:" + inputFilePath
                    + "]'s extension[" + extension + "] is not supported!");
        }

        return targetFile;
    }

    @Override
    protected File getTargetFile(String targetFilePath) {
        return new File(targetFilePath, FsConstants.PDF_PREFIX
                + UUID.randomUUID().toString().replace("-", "") + "." + FsConstants.DOC_TYPE_PDF);
    }
}

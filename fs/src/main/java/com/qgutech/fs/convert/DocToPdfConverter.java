package com.qgutech.fs.convert;


import com.qgutech.fs.domain.DocTypeEnum;
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
            return DocTypeEnum.DOC.docType();
        }

        return extension.toLowerCase();
    }

    @Override
    protected File windowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        String extension = getFileExtension(inputFilePath);
        File targetFile = getTargetFile(targetFileDirPath);
        if (DocTypeEnum.PDF.docType().equals(extension)) {
            FileUtils.copyFile(new File(inputFilePath), targetFile);
        } else if (DocTypeEnum.DOC.docType().equals(extension)
                || DocTypeEnum.DOCX.docType().equals(extension)
                || DocTypeEnum.TXT.docType().equals(extension)) {
            ConvertUtils.wordToPdf(inputFilePath, targetFile.getAbsolutePath());
        } else if (DocTypeEnum.PPT.docType().equals(extension)
                || DocTypeEnum.PPTX.docType().equals(extension)) {
            ConvertUtils.excelToPdf(inputFilePath, targetFile.getAbsolutePath());
        } else if (DocTypeEnum.XLS.docType().equals(extension)
                || DocTypeEnum.XLSX.docType().equals(extension)) {
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
                + UUID.randomUUID().toString().replace("-", "") + FsConstants.PDF_SUFFIX);
    }
}

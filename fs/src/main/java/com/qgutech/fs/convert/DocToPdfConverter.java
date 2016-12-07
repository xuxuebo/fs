package com.qgutech.fs.convert;


import com.qgutech.fs.domain.DocTypeEnum;
import com.qgutech.fs.utils.ConvertUtils;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;

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
        if (DocTypeEnum.PDF.docType().equalsIgnoreCase(extension)) {
            FileUtils.copyFile(new File(inputFilePath), targetFile);
        } else if (DocTypeEnum.DOC.docType().equalsIgnoreCase(extension)
                || DocTypeEnum.DOCX.docType().equalsIgnoreCase(extension)
                || DocTypeEnum.TXT.docType().equalsIgnoreCase(extension)) {
            ConvertUtils.wordToPdf(inputFilePath, targetFile.getAbsolutePath());
        } else if (DocTypeEnum.PPT.docType().equalsIgnoreCase(extension)
                || DocTypeEnum.PPTX.docType().equalsIgnoreCase(extension)) {
            ConvertUtils.pptToPdf(inputFilePath, targetFile.getAbsolutePath());
        } else if (DocTypeEnum.XLS.docType().equalsIgnoreCase(extension)
                || DocTypeEnum.XLSX.docType().equalsIgnoreCase(extension)) {
            ConvertUtils.excelToPdf(inputFilePath, targetFile.getAbsolutePath());
        } else {
            throw new RuntimeException("InputFile[path:" + inputFilePath
                    + "]'s extension[" + extension + "] is not supported!");
        }

        return targetFile;
    }

    @Override
    protected File getTargetFile(String targetFilePath) {
        return new File(targetFilePath, FsConstants.PDF_PREFIX
                + FsUtils.generateUUID() + FsConstants.PDF_SUFFIX);
    }
}

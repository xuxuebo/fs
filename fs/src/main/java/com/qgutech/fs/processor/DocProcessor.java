package com.qgutech.fs.processor;


import com.qgutech.fs.domain.DocTypeEnum;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class DocProcessor extends AbstractProcessor {

    protected boolean validateFile(FsFile fsFile, String tmpFilePath) throws Exception {
        MultipartFile file = fsFile.getFile();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            if (inputStream == null) {
                return false;
            }

            if (!DocTypeEnum.isDoc(fsFile.getSuffix())) {
                return false;
            }

            outputStream = new FileOutputStream(tmpFilePath);
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        return true;
    }

    protected String getOriginFilePath(FsFile fsFile) {
        StringBuilder builder = new StringBuilder();
        builder.append(PropertiesUtils.getFileStoreDir()).append(fsFile.getCorpCode())
                .append(File.separator).append(fsFile.getAppCode())
                .append(File.separator).append(FsConstants.FILE_DIR_SRC);
        String businessDir = fsFile.getBusinessDir();
        if (StringUtils.isNotEmpty(businessDir)) {
            builder.append(File.separator).append(businessDir);
        }

        builder.append(File.separator).append(DocTypeEnum.DOC.docType())
                .append(File.separator).append(FsUtils.formatDateToYYMM(new Date()))
                .append(File.separator).append(fsFile.getBusinessId())
                .append(File.separator).append(fsFile.getId())
                .append(FsConstants.POINT).append(fsFile.getSuffix());

        return builder.toString();
    }

}

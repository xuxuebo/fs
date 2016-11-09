package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.HttpUtils;
import com.qgutech.fs.utils.PropertiesUtils;
import com.qgutech.fs.utils.Signer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractProcessor implements Processor {


    @Override
    public FsFile beforeProcess(FsFile fsFile) throws Exception {
        if (fsFile == null || fsFile.uploadValidate()) {
            fsFile = new FsFile();
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            return fsFile;
        }

        MultipartFile file = fsFile.getFile();
        if (file == null || StringUtils.isEmpty(file.getOriginalFilename())
              /*  || file.getInputStream() == null*/) {
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            return fsFile;
        }

        String originalFilename = file.getOriginalFilename();
        fsFile.setStoredFileName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        if (StringUtils.isEmpty(extension)) {
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            return fsFile;
        }
        fsFile.setSuffix(extension);
        fsFile.setFileSize(file.getSize());
        fsFile.setServerCode(PropertiesUtils.getServerCode());
        fsFile.setServerHost(PropertiesUtils.getServerHost());

        //如果是压缩包，还要检查压缩包中的文件是否合法
        String unZipFile;
        if ("zip".equals(extension) || "rar".equals(extension)) {
            unZipFile = unZip(file);
        } else {
            unZipFile = null;
        }

        OutputStream outputStream = null;
        InputStream inputStream = null;
        String originFilePath = null;
        try {
            inputStream = file.getInputStream();
            originFilePath = getOriginFilePath(fsFile);
            outputStream = new FileOutputStream(originFilePath);
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        if (needAsync(fsFile)) {
            fsFile.setStatus(ProcessStatusEnum.PROCESSING);
            String fsFileId = saveFile(fsFile);
            fsFile.setId(fsFileId);
            submitToRedis(fsFile, unZipFile, originFilePath);
        } else {
            process(fsFile);
            fsFile.setStatus(ProcessStatusEnum.SUCCESS);
            String fsFileId = saveFile(fsFile);
            fsFile.setId(fsFileId);
        }

        return fsFile;
    }

    protected boolean validateFile(FsFile fsFile, String tmpFile) throws Exception {
        return true;
    }

    protected String unZip(MultipartFile file) {
        return null;
    }

    protected void submitToRedis(FsFile fsFile, String unZipFile, String originFilePath) {

    }

    protected boolean needAsync(FsFile fsFile) {
        return true;
    }

    private String saveFile(FsFile fsFile) {
        long timestamp = System.currentTimeMillis();
        fsFile.setTimestamp(timestamp);
        fsFile.setSign(Signer.sign(PropertiesUtils.getServerHost()
                , PropertiesUtils.getServerSecret(), timestamp));
        return HttpUtils.doPost(PropertiesUtils.getSaveFileUrl(), fsFile.toMap());
    }

    private String updateFile(FsFile fsFile) {
        long timestamp = System.currentTimeMillis();
        fsFile.setTimestamp(timestamp);
        fsFile.setSign(Signer.sign(fsFile.getId(), PropertiesUtils.getServerHost()
                , PropertiesUtils.getServerSecret(), timestamp));
        return HttpUtils.doPost(PropertiesUtils.getUpdateFileUrl(), fsFile.toMap());
    }

    protected String getOriginFilePath(FsFile fsFile) {
        throw new UnsupportedOperationException("This method should be override by the subClass!");
    }

    @Override
    public void process(FsFile fsFile) throws Exception {

    }

    @Override
    public void afterProcess(FsFile fsFile) throws Exception {

    }
}

package com.qgutech.fs.utils;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


public class FsMultipartFile implements MultipartFile {
    private String name;
    private File file;
    private String originalFileName;
    private String contentType;
    private InputStream inputStream;

    public FsMultipartFile(String name, File file
            , String originalFileName, String contentType) {
        this.name = name;
        this.file = file;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFileName;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return file.length() == 0;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public byte[] getBytes() throws IOException {
        InputStream inputStream = getInputStream();
        if (inputStream == null) {
            return new byte[0];
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new FileInputStream(file);
        }

        return inputStream;
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        if (dest.exists() && !dest.delete()) {
            throw new IOException("Destination file [" + dest.getAbsolutePath()
                    + "] already exists and could not be deleted!");
        }

        FileUtils.copyFile(file, dest);
    }

    public void cleanup() {
        IOUtils.closeQuietly(inputStream);
        //todo 是否需要删除文件
        //file.delete();
    }
}

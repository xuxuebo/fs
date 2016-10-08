package com.qgutech.fs.convert;


import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public abstract class AbstractConverter implements Converter {

    protected String serverType = SERVER_TYPE_WINDOWS;
    protected String convertToolPath;
    protected static final String SERVER_TYPE_WINDOWS = "windows";
    protected static final String SERVER_TYPE_LINUX = "linux";
    protected final Log LOG = LogFactory.getLog(getClass());

    @Override
    public File convert(String inputFilePath, String targetFileDirPath
            , ResultProcess resultProcess) throws Exception {
        Assert.hasText(inputFilePath, "inputFilePath is empty!");
        Assert.hasText(targetFileDirPath, "targetFileDirPath is empty!");

        File input = new File(inputFilePath);
        Assert.isTrue(input.exists(), "inputFile[absolutePath:" + inputFilePath + "] not exists!");
        File directory = new File(targetFileDirPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Create directory[path:" + targetFileDirPath + "] failed!");
            }
        }

        if (SERVER_TYPE_LINUX.equals(serverType)) {
            return windowsConvert(inputFilePath, targetFileDirPath, resultProcess);
        } else if (SERVER_TYPE_WINDOWS.equals(serverType)) {
            return linuxConvert(inputFilePath, targetFileDirPath, resultProcess);
        } else {
            throw new RuntimeException("ServerType[" + serverType + "] is not supported!");
        }
    }

    protected String getCommand(String srcFilePath, String targetFilePath) {
        StringBuilder builder = new StringBuilder("\"" + convertToolPath);
        builder.append("\" -srcFile \"");
        builder.append(srcFilePath.replace("\\", "\\\\"));
        builder.append("\" -tarFile \"");
        builder.append(targetFilePath.replace("\\", "\\\\"));
        builder.append("\"");

        return builder.toString();
    }

    protected File getTargetFile(String targetFilePath) {
        return new File(targetFilePath);
    }

    protected File windowsConvert(String inputFilePath, String targetFileDirPath
            , ResultProcess resultProcess) throws Exception {
        File targetFile = getTargetFile(targetFileDirPath);
        BufferedReader bufferReader = null;
        Process process = null;
        StringBuilder builder = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    getCommand(inputFilePath, targetFile.getAbsolutePath()));
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                LOG.info(line.trim());
                if (builder.length() > 0) {
                    builder.append("\r\n");
                }

                builder.append(line.trim());
            }

            process.waitFor();
        } finally {
            IOUtils.closeQuietly(bufferReader);
            if (process != null) {
                process.destroy();
            }
        }

        String result = builder.toString();
        if (resultProcess != null) {
            resultProcess.processResult(result);
        } else if (result.length() > 0) {
            throw new RuntimeException("Execute cmd["
                    + getCommand(inputFilePath, targetFile.getAbsolutePath()) + "] failed! " + result);
        }

        return targetFile;
    }

    protected File linuxConvert(String inputFilePath, String targetFileDirPath, ResultProcess resultProcess) {
        throw new RuntimeException("linuxConvert is not supported!");
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getConvertToolPath() {
        return convertToolPath;
    }

    public void setConvertToolPath(String convertToolPath) {
        this.convertToolPath = convertToolPath;
    }
}

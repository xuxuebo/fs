package com.qgutech.fs.convert;


import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public abstract class AbstractConverter implements Converter {

    protected String serverType = SERVER_TYPE_LINUX;
    protected String convertToolPath;
    protected static final String SERVER_TYPE_WINDOWS = "windows";
    protected static final String SERVER_TYPE_LINUX = "linux";
    protected final Log LOG = LogFactory.getLog(getClass());

    @Override
    public File convert(String inputFilePath, String targetFileDirPath) throws Exception {
        Assert.hasText(inputFilePath, "inputFilePath is empty!");
        Assert.hasText(targetFileDirPath, "targetFileDirPath is empty!");

        File inputFile = new File(inputFilePath);
        Assert.isTrue(inputFile.exists(), "inputFile[absolutePath:" + inputFilePath + "] not exists!");
        File targetDir = new File(targetFileDirPath);
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new RuntimeException("Create targetDir[path:" + targetFileDirPath + "] failed!");
            }
        }

        if (SERVER_TYPE_WINDOWS.equals(serverType)) {
            return windowsConvert(inputFilePath, targetFileDirPath);
        } else if (SERVER_TYPE_LINUX.equals(serverType)) {
            return linuxConvert(inputFilePath, targetFileDirPath);
        } else {
            throw new RuntimeException("ServerType[" + serverType + "] is not supported!");
        }
    }

    protected String getCommand(String srcFilePath, String targetFilePath) {
        return "\"" + convertToolPath + "\" -srcFile \""
                + srcFilePath.replace("\\", "\\\\") + "\" -tarFile \""
                + targetFilePath.replace("\\", "\\\\") + "\"";
    }

    protected File getTargetFile(String targetFilePath) {
        return new File(targetFilePath);
    }

    protected File windowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        File targetFile = getTargetFile(targetFileDirPath);
        StringBuilder builder = executeCmd(inputFilePath, targetFile.getAbsolutePath());
        String result = builder.toString();
        if (result.length() > 0) {
            throw new RuntimeException("Execute cmd["
                    + getCommand(inputFilePath, targetFile.getAbsolutePath()) + "] failed! " + result);
        }

        return targetFile;
    }

    protected StringBuilder executeCmd(String inputFilePath, String targetFilePath) throws Exception {
        BufferedReader bufferReader = null;
        Process process = null;
        StringBuilder builder = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(getCommand(inputFilePath, targetFilePath));
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

        return builder;
    }

    protected File linuxConvert(String inputFilePath, String targetFileDirPath) {
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

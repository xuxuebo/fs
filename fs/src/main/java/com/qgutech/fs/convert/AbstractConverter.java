package com.qgutech.fs.convert;


import com.qgutech.fs.utils.FsUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.File;

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
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new RuntimeException("Create targetDir[path:" + targetFileDirPath + "] failed!");
        }

        if (SERVER_TYPE_WINDOWS.equals(serverType)) {
            return windowsConvert(inputFilePath, targetFileDirPath);
        } else if (SERVER_TYPE_LINUX.equals(serverType)) {
            return linuxConvert(inputFilePath, targetFileDirPath);
        } else {
            throw new RuntimeException("ServerType[" + serverType + "] is not supported!");
        }
    }

    protected String[] getCommands(String srcFilePath, String targetFilePath) {
        return new String[]{convertToolPath, "-srcFile"
                , srcFilePath.replace("\\", "\\\\"), "-tarFile"
                , targetFilePath.replace("\\", "\\\\")};
    }

    protected File getTargetFile(String targetFilePath) {
        return new File(targetFilePath);
    }

    protected File windowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        File targetFile = getTargetFile(targetFileDirPath);
        String[] commands = getCommands(inputFilePath, targetFile.getAbsolutePath());
        String result = FsUtils.executeCommand(commands);
        if (result.length() > 0) {
            LOG.info("Executing command[" + FsUtils.toString(commands)
                    + "] and result message is:" + result);
        }

        return targetFile;
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

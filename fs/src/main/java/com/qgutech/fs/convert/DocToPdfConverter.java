package com.qgutech.fs.convert;


import com.qgutech.fs.domain.DocTypeEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DocToPdfConverter extends AbstractDocToPdfConverter {

    protected long timerPeriod;
    protected long timerDelay;
    protected boolean enableTimer;
    protected JedisCommands commonJedis;

    public void init() {
        if (!SERVER_TYPE_WINDOWS.equals(serverType) && enableTimer) {
            return;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                killWindowsProcesses(FsConstants.IMAGE_NAME_WIN_WORD_EXE);
                killWindowsProcesses(FsConstants.IMAGE_NAME_POWER_PNT_EXE);
                killWindowsProcesses(FsConstants.IMAGE_NAME_EXCEL_EXE);
            }
        }, timerDelay, timerPeriod);
    }

    private void killWindowsProcesses(String imageName) {
        try {
            List<String> windowsPids = FsUtils.getWindowsPids(imageName);
            if (CollectionUtils.isEmpty(windowsPids)) {
                return;
            }

            for (String windowsPid : windowsPids) {
                String key = RedisKey.FS_WINDOWS_PID_ + imageName + FsConstants.UNDERLINE
                        + PropertiesUtils.getServerHost() + FsConstants.UNDERLINE + windowsPid;
                String timestamp = commonJedis.get(key);
                long currentTimeMillis = System.currentTimeMillis();
                if (StringUtils.isEmpty(timestamp)) {
                    commonJedis.setex(key, (int) timerPeriod / 1000 * 2, currentTimeMillis + "");
                    continue;
                }

                long executeTime = currentTimeMillis - Long.parseLong(timestamp);
                if (executeTime >= timerPeriod) {
                    FsUtils.killWindowsProcess(windowsPid);
                    commonJedis.expire(key, 0);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private String getFileExtension(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isEmpty(extension)) {
            return DocTypeEnum.DOC.docType();
        }

        return extension.toLowerCase();
    }

    @Override
    protected File doWindowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
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

    public long getTimerDelay() {
        return timerDelay;
    }

    public void setTimerDelay(long timerDelay) {
        this.timerDelay = timerDelay;
    }

    public long getTimerPeriod() {
        return timerPeriod;
    }

    public void setTimerPeriod(long timerPeriod) {
        this.timerPeriod = timerPeriod;
    }

    public boolean isEnableTimer() {
        return enableTimer;
    }

    public void setEnableTimer(boolean enableTimer) {
        this.enableTimer = enableTimer;
    }

    public JedisCommands getCommonJedis() {
        return commonJedis;
    }

    public void setCommonJedis(JedisCommands commonJedis) {
        this.commonJedis = commonJedis;
    }
}

package com.qgutech.fs.utils;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FsUtils {

    private static final Log LOG = LogFactory.getLog(FsUtils.class);

    public static String formatDateToYYMM(Date date) {
        Assert.notNull(date, "Date is null!");
        return new SimpleDateFormat("yyMM").format(date);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("_", "");
    }

    public static String executeCommand(String command) throws Exception {
        Assert.hasText(command, "Command is empty!");
        BufferedReader bufferReader = null;
        Process process = null;
        StringBuilder builder = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(command);
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

        return builder.toString();
    }

    public static String executeErrorCommand(String command) throws Exception {
        Assert.hasText(command, "Command is empty!");
        BufferedReader bufferReader = null;
        Process process = null;
        StringBuilder builder = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(command);
            bufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
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

        return builder.toString();
    }

    public static String getImageResolution(String filePath) throws Exception {
        Assert.hasText(filePath, "filePath is empty!");
        String command = "ffmpeg -i " + filePath;
        String result = executeErrorCommand(command);
        if (StringUtils.isEmpty(result)) {
            throw new RuntimeException("File[" + filePath + "] not exist or is not an image!");
        }

        Matcher matcher = Pattern.compile("[^0-9]+([0-9]+x[0-9]+)[^0-9]+").matcher(result);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("File[" + filePath + "] not exist or is not an image!");
        }
    }
}

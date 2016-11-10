package com.qgutech.fs.utils;


import com.qgutech.fs.domain.FsFile;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.*;
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

    public static String toString(String[] array) {
        Assert.notEmpty(array, "Array is empty!");
        StringBuilder builder = new StringBuilder();
        for (String command : array) {
            builder.append(command).append(" ");
        }

        return builder.toString();
    }

    public static String executeErrorCommand(String command) throws Exception {
        Assert.hasText(command, "Command is empty!");
        BufferedReader bufferReader = null;
        StringBuilder builder = new StringBuilder();
        Process process = Runtime.getRuntime().exec(command);
        try {
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

    public static String executeCommand(String[] commands) throws Exception {
        Assert.notEmpty(commands, "Commands is empty!");
        BufferedReader bufferReader = null;
        StringBuilder builder = new StringBuilder();
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try {
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

    public static String getImageResolution(String filePath) throws Exception {
        Assert.hasText(filePath, "filePath is empty!");
        String result = executeCommand(new String[]{"ffmpeg", "-i", filePath});
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

    public static void decompress(String compressFilePath, String decompressDirPath) throws Exception {
        Assert.hasText(compressFilePath, "CompressFilePath is empty!");
        Assert.hasText(decompressDirPath, "DecompressDirPath is empty!");
        File compressFile = new File(compressFilePath);
        if (!compressFile.exists()) {
            throw new RuntimeException("CompressFile[" + compressFilePath + "] not exist!");
        }

        if (!compressFile.isFile()) {
            throw new RuntimeException("CompressFile[" + compressFilePath + "] is not a file!");
        }

        File decompressDir = new File(decompressDirPath);
        if (decompressDir.exists() && !decompressDir.isDirectory()) {
            throw new RuntimeException("DecompressDir[" + decompressDirPath + "] is not a directory!");
        }

        if (!decompressDir.exists() && !decompressDir.mkdirs()) {
            throw new RuntimeException("Creating directory[" + decompressDirPath + "] failed!");
        }


        InputStream is = null;
        ArArchiveInputStream ais = null;
        try {
            is = new FileInputStream(compressFile);
            ais = new ArArchiveInputStream(is);
            ArchiveEntry archiveEntry;
            while ((archiveEntry = ais.getNextEntry()) != null) {
                if (archiveEntry.isDirectory()) {
                    File entryDirFile = new File(decompressDir, archiveEntry.getName());
                    if (!entryDirFile.exists() && !entryDirFile.mkdirs()) {
                        throw new IOException("Creating directory["
                                + entryDirFile.getAbsolutePath() + "] failed!");
                    }

                    continue;
                }

                OutputStream outputStream = null;
                try {
                    File entryFile = new File(decompressDir, archiveEntry.getName());
                    if (!entryFile.exists() && !entryFile.createNewFile()) {
                        throw new IOException("Creating file[" + entryFile.getAbsolutePath() + "] failed!");
                    }

                    outputStream = new FileOutputStream(entryFile);
                    IOUtils.copy(ais, outputStream);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }
            }
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(ais);
        }

    }

    private static void unZip(String compressFilePath, String decompressDirPath) throws Exception {
        Assert.hasText(compressFilePath, "CompressFilePath is empty!");
        Assert.hasText(decompressDirPath, "DecompressDirPath is empty!");
        File compressFile = new File(compressFilePath);
        if (!compressFile.exists()) {
            throw new RuntimeException("CompressFile[" + compressFilePath + "] not exist!");
        }

        if (!compressFile.isFile()) {
            throw new RuntimeException("CompressFile[" + compressFilePath + "] is not a file!");
        }

        File decompressDir = new File(decompressDirPath);
        if (decompressDir.exists() && !decompressDir.isDirectory()) {
            throw new RuntimeException("DecompressDir[" + decompressDirPath + "] is not a directory!");
        }

        if (!decompressDir.exists() && !decompressDir.mkdirs()) {
            throw new RuntimeException("Creating directory[" + decompressDirPath + "] failed!");
        }

        InputStream is = null;
        ZipArchiveInputStream zis = null;
        try {
            is = new FileInputStream(compressFile);
            zis = new ZipArchiveInputStream(is);
            ArchiveEntry archiveEntry;
            while ((archiveEntry = zis.getNextEntry()) != null) {
                if (archiveEntry.isDirectory()) {
                    File entryDirFile = new File(decompressDir, archiveEntry.getName());
                    if (!entryDirFile.exists() && !entryDirFile.mkdirs()) {
                        throw new IOException("Creating directory["
                                + entryDirFile.getAbsolutePath() + "] failed!");
                    }

                    continue;
                }

                OutputStream outputStream = null;
                try {
                    File entryFile = new File(decompressDir, archiveEntry.getName());
                    if (!entryFile.exists() && !entryFile.createNewFile()) {
                        throw new IOException("Creating file[" + entryFile.getAbsolutePath() + "] failed!");
                    }

                    outputStream = new FileOutputStream(entryFile);
                    IOUtils.copy(zis, outputStream);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }
            }
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(zis);
        }
    }

    public static String compress(FsFile fsFile) {
        return null;
    }

    public static void main(String[] args) throws Exception {
        decompress("C:\\\\Users\\\\Administrator\\\\Desktop\\\\test\\\\mp41.rar"
                , "C:\\\\Users\\\\Administrator\\\\Desktop\\\\test\\\\my");
        //System.out.println(getImageResolution("C:\\\\Users\\\\Administrator\\\\Desktop\\\\test\\\\2.png"));
    }


}

package com.qgutech.fs.utils;


import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FsUtils {

    private static final Log LOG = LogFactory.getLog(FsUtils.class);

    public static void deleteFile(String filePath) {
        if (StringUtils.isNotEmpty(filePath)) {
            deleteFile(new File(filePath));
        }
    }

    public static void deleteFile(File file) {
        try {
            if (file != null && file.exists()) {
                FileUtils.forceDelete(file);
            }
        } catch (Exception e) {
            //not need process
        }
    }

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
                LOG.debug(line.trim());
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
                LOG.debug(line.trim());
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
        String result = executeCommand(new String[]{FsConstants.FFMPEG, "-i", filePath});
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

        String extension = FilenameUtils.getExtension(compressFile.getName());
        if (FsConstants.COMPRESS_FILE_SUFFIX_ZIP.equalsIgnoreCase(extension)) {
            unZip(compressFilePath, decompressDirPath);
        } else if (FsConstants.COMPRESS_FILE_SUFFIX_7Z.equalsIgnoreCase(extension)) {
            unSevenZ(compressFilePath, decompressDirPath);
        } else if (FsConstants.COMPRESS_FILE_SUFFIX_RAR.equalsIgnoreCase(extension)) {
            unRar(compressFilePath, decompressDirPath);
        } else {
            throw new RuntimeException("extension[" + extension + "] is not support decompression!");
        }

    }

    private static void unRar(String compressFilePath, String decompressDirPath) throws Exception {
        Archive archive = null;
        try {
            String baseName = FilenameUtils.getBaseName(compressFilePath);
            archive = new Archive(new File(compressFilePath));
            FileHeader fh;
            while ((fh = archive.nextFileHeader()) != null) {
                String fileName = fh.getFileNameW().isEmpty() ?
                        fh.getFileNameString() : fh.getFileNameW();
                if (fh.isDirectory() && (fileName.equals(baseName)
                        || fileName.equals(baseName + FsConstants.PATH_SEPARATOR))) {
                    continue;
                }

                int indexOf = fileName.indexOf(baseName);
                if (indexOf > -1) {
                    fileName = fileName.substring(indexOf + baseName.length());
                }

                if (fh.isDirectory()) {
                    File entryDirFile = new File(decompressDirPath, fileName);
                    if (!entryDirFile.exists() && !entryDirFile.mkdirs()) {
                        throw new IOException("Creating directory["
                                + entryDirFile.getAbsolutePath() + "] failed!");
                    }

                    continue;
                }

                OutputStream outputStream = null;
                try {
                    File entryFile = new File(decompressDirPath, fileName);
                    File parentFile = entryFile.getParentFile();
                    if (!parentFile.exists() && !parentFile.mkdirs()) {
                        throw new IOException("Creating directory["
                                + parentFile.getAbsolutePath() + "] failed!");
                    }

                    if (!entryFile.exists() && !entryFile.createNewFile()) {
                        throw new IOException("Creating file[" + entryFile.getAbsolutePath() + "] failed!");
                    }

                    outputStream = new FileOutputStream(entryFile);
                    archive.extractFile(fh, outputStream);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }
            }
        } finally {
            if (archive != null) {
                try {
                    archive.close();
                } catch (Exception e) {
                    //np
                }
            }
        }
    }

    private static void unZip(String compressFilePath, String decompressDirPath) throws Exception {
        InputStream is = null;
        ArchiveInputStream zis = null;
        try {
            String baseName = FilenameUtils.getBaseName(compressFilePath);
            is = new FileInputStream(compressFilePath);
            zis = new ZipArchiveInputStream(is);
            ArchiveEntry archiveEntry;
            while ((archiveEntry = zis.getNextEntry()) != null) {
                String name = archiveEntry.getName();
                if (archiveEntry.isDirectory() && (name.equals(baseName)
                        || name.equals(baseName + FsConstants.PATH_SEPARATOR))) {
                    continue;
                }

                int indexOf = name.indexOf(baseName);
                if (indexOf > -1) {
                    name = name.substring(indexOf + baseName.length());
                }

                if (archiveEntry.isDirectory()) {
                    File entryDirFile = new File(decompressDirPath, name);
                    if (!entryDirFile.exists() && !entryDirFile.mkdirs()) {
                        throw new IOException("Creating directory["
                                + entryDirFile.getAbsolutePath() + "] failed!");
                    }

                    continue;
                }

                OutputStream outputStream = null;
                try {
                    File entryFile = new File(decompressDirPath, name);
                    File parentFile = entryFile.getParentFile();
                    if (!parentFile.exists() && !parentFile.mkdirs()) {
                        throw new IOException("Creating directory["
                                + parentFile.getAbsolutePath() + "] failed!");
                    }

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

    private static void unSevenZ(String compressFilePath, String decompressDirPath) throws Exception {
        SevenZFile sevenZFile = null;
        try {
            sevenZFile = new SevenZFile(new File(compressFilePath));
            ArchiveEntry archiveEntry;
            while ((archiveEntry = sevenZFile.getNextEntry()) != null) {
                if (archiveEntry.isDirectory()) {
                    File entryDirFile = new File(decompressDirPath, archiveEntry.getName());
                    if (!entryDirFile.exists() && !entryDirFile.mkdirs()) {
                        throw new IOException("Creating directory["
                                + entryDirFile.getAbsolutePath() + "] failed!");
                    }

                    continue;
                }

                OutputStream outputStream = null;
                try {
                    File entryFile = new File(decompressDirPath, archiveEntry.getName());
                    File parentFile = entryFile.getParentFile();
                    if (!parentFile.exists() && !parentFile.mkdirs()) {
                        throw new IOException("Creating directory["
                                + parentFile.getAbsolutePath() + "] failed!");
                    }

                    if (!entryFile.exists() && !entryFile.createNewFile()) {
                        throw new IOException("Creating file[" + entryFile.getAbsolutePath() + "] failed!");
                    }

                    outputStream = new FileOutputStream(entryFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = sevenZFile.read(buffer)) > -1) {
                        outputStream.write(buffer, 0, len);
                    }

                    outputStream.flush();
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }
            }
        } finally {
            if (sevenZFile != null) {
                try {
                    sevenZFile.close();
                } catch (Exception e) {
                    //np
                }
            }
        }
    }

    public static void compress(String srcFilePath, String compressFilePath) throws Exception {
        Assert.hasText(compressFilePath, "CompressFilePath is empty!");
        String extension = FilenameUtils.getExtension(compressFilePath);
        if (FsConstants.COMPRESS_FILE_SUFFIX_ZIP.equalsIgnoreCase(extension)) {
            compressToZip(srcFilePath, compressFilePath);
        } else if (FsConstants.COMPRESS_FILE_SUFFIX_7Z.equalsIgnoreCase(extension)) {
            compressTo7Z(srcFilePath, compressFilePath);
        } else if (FsConstants.COMPRESS_FILE_SUFFIX_RAR.equalsIgnoreCase(extension)) {
            compressToRar(srcFilePath, compressFilePath);
        } else {
            throw new RuntimeException("extension[" + extension + "] is not support compress!");
        }
    }

    public static void compressToZip(String srcFilePath, String compressFilePath) throws Exception {
        Assert.hasText(srcFilePath, "SrcFilePath is empty!");
        Assert.hasText(compressFilePath, "CompressFilePath is empty!");
        File srcFile = new File(srcFilePath);
        if (!srcFile.exists()) {
            throw new RuntimeException("SrcFile[" + srcFilePath + "] not exist!");
        }

        File[] files = srcFile.listFiles();
        if (srcFile.isDirectory() && (files == null || files.length == 0)) {
            throw new RuntimeException("SrcFile[" + srcFilePath + "] is a directory but not has sub file!");
        }

        File compressFile = new File(compressFilePath);
        if (compressFile.exists() && compressFile.isDirectory()) {
            throw new RuntimeException("compressFile[" + compressFilePath + "] must not be directory!");
        }

        if (compressFile.exists() && !compressFilePath.endsWith(FsConstants.COMPRESS_FILE_SUFFIX_ZIP)) {
            throw new RuntimeException("compressFile[" + compressFilePath + "]'s suffix must not end with zip!");
        }

        if (!compressFilePath.endsWith(FsConstants.COMPRESS_FILE_SUFFIX_ZIP)) {
            compressFilePath = compressFilePath + FsConstants.POINT + FsConstants.COMPRESS_FILE_SUFFIX_ZIP;
            compressFile = new File(compressFilePath);
        }

        File parentFile = compressFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("Creating directory[" + parentFile.getAbsolutePath() + "] failed!");
        }

        if (!compressFile.exists() && !compressFile.createNewFile()) {
            throw new IOException("Creating file[" + compressFile.getAbsolutePath() + "] failed!");
        }

        ZipArchiveOutputStream zos = null;
        try {
            zos = new ZipArchiveOutputStream(compressFile);
            zos.setUseZip64(Zip64Mode.AsNeeded);
            ZipArchiveEntry zipArchiveEntry = null;
            InputStream inputStream = null;
            List<String> subFiles = getSubFiles(srcFilePath, null);
            for (String subFile : subFiles) {
                try {
                    File inputFile = srcFile.isFile() ? srcFile : new File(srcFilePath, subFile);
                    zipArchiveEntry = new ZipArchiveEntry(inputFile, subFile);
                    zos.putArchiveEntry(zipArchiveEntry);
                    if (inputFile.isDirectory()) {
                        continue;
                    }

                    inputStream = new FileInputStream(inputFile);
                    IOUtils.copy(inputStream, zos);
                } finally {
                    if (zipArchiveEntry != null) {
                        try {
                            zos.closeArchiveEntry();
                        } catch (Exception e) {
                            //np
                        }
                    }
                    IOUtils.closeQuietly(inputStream);
                }
            }
        } finally {
            IOUtils.closeQuietly(zos);
        }
    }

    public static void compressTo7Z(String srcFilePath, String compressFilePath) throws Exception {
        Assert.hasText(srcFilePath, "SrcFilePath is empty!");
        Assert.hasText(compressFilePath, "CompressFilePath is empty!");
        File srcFile = new File(srcFilePath);
        if (!srcFile.exists()) {
            throw new RuntimeException("SrcFile[" + srcFilePath + "] not exist!");
        }

        File[] files = srcFile.listFiles();
        if (srcFile.isDirectory() && (files == null || files.length == 0)) {
            throw new RuntimeException("SrcFile[" + srcFilePath + "] is a directory but not has sub file!");
        }

        File compressFile = new File(compressFilePath);
        if (compressFile.exists() && compressFile.isDirectory()) {
            throw new RuntimeException("compressFile[" + compressFilePath + "] must not be directory!");
        }

        if (compressFile.exists() && !compressFilePath.endsWith(FsConstants.COMPRESS_FILE_SUFFIX_7Z)) {
            throw new RuntimeException("compressFile[" + compressFilePath + "]'s suffix must not end with 7z!");
        }

        if (!compressFilePath.endsWith(FsConstants.COMPRESS_FILE_SUFFIX_7Z)) {
            compressFilePath = compressFilePath + FsConstants.POINT + FsConstants.COMPRESS_FILE_SUFFIX_7Z;
            compressFile = new File(compressFilePath);
        }

        File parentFile = compressFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("Creating directory[" + parentFile.getAbsolutePath() + "] failed!");
        }

        if (!compressFile.exists() && !compressFile.createNewFile()) {
            throw new IOException("Creating file[" + compressFile.getAbsolutePath() + "] failed!");
        }


        SevenZOutputFile sevenZOutputFile = null;
        try {
            sevenZOutputFile = new SevenZOutputFile(compressFile);
            SevenZArchiveEntry archiveEntry = null;
            InputStream inputStream = null;
            List<String> subFiles = getSubFiles(srcFilePath, null);
            for (String subFile : subFiles) {
                try {
                    File inputFile = srcFile.isFile() ? srcFile : new File(srcFilePath, subFile);
                    archiveEntry = sevenZOutputFile.createArchiveEntry(inputFile, subFile);
                    sevenZOutputFile.putArchiveEntry(archiveEntry);
                    if (inputFile.isDirectory()) {
                        continue;
                    }

                    inputStream = new FileInputStream(inputFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) > -1) {
                        sevenZOutputFile.write(buffer, 0, len);
                    }
                } finally {
                    if (archiveEntry != null) {
                        try {
                            sevenZOutputFile.closeArchiveEntry();
                        } catch (Exception e) {
                            //np
                        }
                    }
                    IOUtils.closeQuietly(inputStream);
                }
            }
        } finally {
            try {
                if (sevenZOutputFile != null) {
                    sevenZOutputFile.close();
                }
            } catch (Exception e) {
                //np
            }
        }
    }

    public static void compressToRar(String srcFilePath, String compressFilePath) throws Exception {
        throw new RuntimeException("Compress rar is not support!");
    }

    private static List<String> getSubFiles(String filePath, String prefix) {
        Assert.hasText(filePath, "FilePath is empty!");
        File file = new File(filePath);
        if (file.isFile()) {
            List<String> subFiles = new ArrayList<String>(1);
            subFiles.add(file.getName());
            return subFiles;
        }

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return new ArrayList<String>(0);
        }

        List<String> subFiles = new ArrayList<String>();
        for (File f : files) {
            String name = StringUtils.isEmpty(prefix) ? f.getName()
                    : prefix + FsConstants.PATH_SEPARATOR + f.getName();
            subFiles.add(name);
            if (f.isDirectory()) {
                subFiles.addAll(getSubFiles(f.getAbsolutePath(), name));
            }
        }

        return subFiles;
    }

    public static Video getVideo(String videoPath) throws Exception {
        Assert.hasText(videoPath, "VideoPath is empty!");
        String result = executeCommand(new String[]{FsConstants.FFMPEG, "-i", videoPath});
        Video video = new Video();
        Matcher matcher = Pattern.compile("Duration:[^\\d:.]+([\\d:.]+)[^\\d:.]+").matcher(result);
        if (matcher.find()) {
            video.setDuration(matcher.group(1));
        } else {
            throw new RuntimeException("File[" + videoPath + "] not exist or not a video!");
        }

        matcher = Pattern.compile("bitrate:[^\\d]+(\\d+)[^\\d]+").matcher(result);
        if (matcher.find()) {
            video.setBitRate(Integer.parseInt(matcher.group(1)));
        } else {
            throw new RuntimeException("File[" + videoPath + "] not exist or not a video!");
        }

        matcher = Pattern.compile("Video:.*[^\\d]+(\\d+x\\d+)[^\\d]+").matcher(result);
        if (matcher.find()) {
            String resolution = matcher.group(1);
            video.setResolution(resolution);
            int indexOf = resolution.indexOf("x");
            video.setWidth(Integer.parseInt(resolution.substring(0, indexOf)));
            video.setHeight(Integer.parseInt(resolution.substring(indexOf + 1)));
        } else {
            throw new RuntimeException("File[" + videoPath + "] not exist or not a video!");
        }

        return video;
    }

    public static Audio getAudio(String audioPath) throws Exception {
        Assert.hasText(audioPath, "AudioPath is empty!");
        String result = executeCommand(new String[]{FsConstants.FFMPEG, "-i", audioPath});
        Audio audio = new Audio();

        Matcher matcher = Pattern.compile("Duration:[^\\d:.]+([\\d:.]+)[^\\d:.]+").matcher(result);
        if (matcher.find()) {
            audio.setDuration(matcher.group(1));
        } else {
            throw new RuntimeException("File[" + audioPath + "] not exist or not an audio!");
        }

        matcher = Pattern.compile("bitrate:[^\\d]+(\\d+)[^\\d]+").matcher(result);
        if (matcher.find()) {
            audio.setBitRate(Integer.parseInt(matcher.group(1)));
        } else {
            throw new RuntimeException("File[" + audioPath + "] not exist or not an audio!");
        }

        return audio;
    }

    public static long parseStringTimeToLong(String time) {
        if (StringUtils.isBlank(time)) {
            return 0l;
        }

        String[] times = time.split(":");
        if (times.length != 3) {
            throw new RuntimeException("Time[" + time
                    + "]'s format is invalid,it must be like HH:mm:ss.SSS!");
        }

        String hour = StringUtils.trim(times[0]);
        String minute = StringUtils.trim(times[1]);
        String second = StringUtils.trim(times[2]);
        if (StringUtils.length(hour) == 0 || StringUtils.length(hour) > 2
                || StringUtils.length(minute) == 0 || StringUtils.length(minute) > 2
                || StringUtils.length(second) == 0) {
            throw new RuntimeException("Time[" + time
                    + "]'s format is invalid,it must be like HH:mm:ss.SSS!");
        }

        long result = Integer.parseInt(hour) * 60 * 60 * 1000 + Integer.parseInt(minute) * 60 * 1000;
        if (!second.contains(".")) {
            if (StringUtils.length(second) > 2) {
                throw new RuntimeException("Time[" + time
                        + "]'s format is invalid,it must be like HH:mm:ss.SSS!");
            }

            result += Integer.parseInt(second) * 1000;
        } else {
            String[] seconds = second.split("\\.");
            String secondPart = StringUtils.trim(seconds[0]);
            String millPart = StringUtils.trim(seconds[1]);
            if (StringUtils.length(secondPart) == 0
                    || StringUtils.length(secondPart) > 2
                    || (StringUtils.length(millPart) == 0
                    || StringUtils.length(millPart) > 3)) {
                throw new RuntimeException("Time[" + time
                        + "]'s format is invalid,it must be like HH:mm:ss.SSS!");
            }

            result += Integer.parseInt(secondPart) * 1000;
            result += Integer.parseInt(millPart);
        }

        return result;
    }

    public static String parseLongToDayTime(Long time) {
        if (time == null || time <= 0) {
            return "00:00:00";
        }

        String result = "";
        long hour = time % (24 * 60 * 60 * 1000) / (60 * 60 * 1000);
        if (hour == 0) {
            result += "00";
        } else if (hour < 9) {
            result += "0" + hour;
        } else {
            result += hour;
        }

        long minute = time % (60 * 60 * 1000) / (60 * 1000);
        if (minute == 0) {
            result += ":00";
        } else if (minute < 9) {
            result += ":0" + minute;
        } else {
            result += ":" + minute;
        }

        long second = time % (60 * 1000) / 1000;
        if (second == 0) {
            result += ":00";
        } else if (second < 9) {
            result += ":0" + second;
        } else {
            result += ":" + second;
        }

        long millSecond = time % 1000;
        if (millSecond != 0) {
            result += "." + millSecond;
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getVideo("E:\\1.mkv"));
        executeCommand(new String[]{FsConstants.FFMPEG, "-i", "E:\\1.mkv", "-ss"
                , "00:10:00", "-y", "E:\\1.png"});
    /*    decompress("C:/Users/Administrator/Desktop/test/mp41.rar"
                , "C:/Users/Administrator/Desktop/test/my");*/
       /* compressTo7Z("C:/Users/Administrator/Desktop/test/mp41"
                , "C:/Users/Administrator/Desktop/test/my/my.7z");*/
        //System.out.println(getImageResolution("C:\\\\Users\\\\Administrator\\\\Desktop\\\\test\\\\2.png"));
    }


}

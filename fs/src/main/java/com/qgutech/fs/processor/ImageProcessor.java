package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ImageTypeEnum;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ImageProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateImage(fsFile.getSuffix());
    }

    @Override
    protected boolean needAsync(FsFile fsFile) {
        return false;
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String genFilePath = getGenFilePath(fsFile);
        File genFile = new File(genFilePath);
        FsUtils.deleteFile(genFilePath);
        if (!genFile.exists() && !genFile.mkdirs() && !genFile.exists()) {
            throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
        }

        ImageTypeEnum[] values = ImageTypeEnum.values();
        String tmpFilePath = fsFile.getTmpFilePath();
        String resolution = FsUtils.getImageResolution(tmpFilePath);
        int width = Integer.parseInt(resolution.substring(0, resolution.indexOf("x")));
        int height = Integer.parseInt(resolution.substring(resolution.indexOf("x") + 1));
        List<Future<String>> futures = new ArrayList<Future<String>>(4);
        for (ImageTypeEnum value : values) {
            final List<String> commands = new ArrayList<String>(7);
            int w = value.getW();
            int h = value.getH();
            commands.add(FsConstants.FFMPEG);
            commands.add("-i");
            commands.add(tmpFilePath);
            if (w > 0 && h > 0) {
                if (w > width) {
                    w = width;
                }

                if (h > height) {
                    h = height;
                }

                commands.add("-s");
                commands.add(w + "*" + h);
            }

            commands.add("-y");
            commands.add(genFilePath + File.separator
                    + value.name().toLowerCase() + FsConstants.DEFAULT_IMAGE_SUFFIX);
            futures.add(taskExecutor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return FsUtils.executeCommand(commands.toArray(new String[commands.size()]));
                }
            }));
        }

        getFutures(futures);
        afterProcess(fsFile);
    }
}

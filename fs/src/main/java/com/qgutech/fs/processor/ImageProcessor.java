package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ImageTypeEnum;
import com.qgutech.fs.domain.Point;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsPathUtils;
import com.qgutech.fs.utils.FsUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ImageProcessor extends AbstractProcessor {

    public static final int C_WHITE = 255;
    public static final int C_BLACK = 0;

    public static List<Point> extractPoints(FsFile fsFile) throws Exception {
        try {
            String tmpFilePath = fsFile.getTmpFilePath();
            if (StringUtils.isEmpty(fsFile.getTmpFilePath())) {
                wordToImage(fsFile);
            } else {
                File imageFile = new File(FsPathUtils.getImportTmpDirPath(FsUtils.generateUUID()
                        + fsFile.getSuffix()));
                fsFile.setTmpFilePath(imageFile.getAbsolutePath());
                File parentFile = imageFile.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs() && !parentFile.exists()) {
                    throw new RuntimeException("Creating directory["
                            + parentFile.getAbsolutePath() + "] failed!");
                }
                FileUtils.copyFile(new File(tmpFilePath), imageFile);
            }

            tmpFilePath = fsFile.getTmpFilePath();
            BufferedImage image = ImageIO.read(new File(tmpFilePath));
            Integer cell = fsFile.getCell();
            image = getShadowImage(image, fsFile.getW(), fsFile.getH(), cell, cell);
            int[][] shadow = getShadow(image);
            return getCellStartPoints(shadow, fsFile.getW() / cell, fsFile.getH() / cell, C_WHITE);
        } finally {
            FsUtils.deleteFile(fsFile.getTmpFilePath());
        }
    }

    public static void wordToImage(FsFile fsFile) throws Exception {
        String text = fsFile.getText();
        int cell = fsFile.getCell();
        int width = fsFile.getW() / cell;
        int height = fsFile.getH() / cell;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);
        g.setClip(0, 0, width, height);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.green);
        int len = text.length();
        if (width >= height) {
            int x = width / 100;
            int w = (width - 2 * x) / len;
            int h = height / 2;
            int size = Math.min(w, h);
            Font font = new Font(fsFile.getFamily(), fsFile.getStyle(), size);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            int y = (g.getClipBounds().height - (fm.getAscent() + fm.getDescent())) / 2 + fm.getAscent();
            int center = (width - 2 * x) / 2;
            center += len % 2 == 0 ? 0 : size / 2;
            int ll = len % 2 == 0 ? len / 2 : (len / 2 + 1);
            int rl = len / 2;
            for (int i = 0; i < ll; i++) {
                g.drawString(text.charAt(ll - 1 - i) + "", center - (i + 1) * size, y);
            }

            for (int i = 0; i < rl; i++) {
                g.drawString(text.charAt(i + ll) + "", center + i * size, y);
            }
        } else {
            int y = height / 100;
            int h = (height - 2 * y) / len;
            int w = width / 2;
            int size = Math.min(w, h);
            Font font = new Font(fsFile.getFamily(), fsFile.getStyle(), size);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            int x = (g.getClipBounds().width - (fm.getAscent() + fm.getDescent())) / 2;
            int center = (height - 2 * y) / 2;
            center += len % 2 == 0 ? 0 : size / 2;
            int ul = len % 2 == 0 ? len / 2 : (len / 2 + 1);
            int dl = len / 2;
            for (int i = 0; i < ul; i++) {
                g.drawString(text.charAt(ul - 1 - i) + "", x, center - i * size);
            }

            for (int i = 0; i < dl; i++) {
                g.drawString(text.charAt(i + ul) + "", x, center + (i + 1) * size);
            }
        }
        g.dispose();

        File png = new File(FsPathUtils.getImportTmpDirPath(FsUtils.generateUUID()
                + FsConstants.DEFAULT_IMAGE_SUFFIX));
        fsFile.setTmpFilePath(png.getAbsolutePath());
        File parentFile = png.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs() && !parentFile.exists()) {
            throw new RuntimeException("Creating directory[" + parentFile.getAbsolutePath() + "] failed!");
        }

        ImageIO.write(image, FsConstants.DEFAULT_IMAGE_TYPE, png);
    }

    public static BufferedImage getShadowImage(BufferedImage sample, int canvasWidth,
                                               int canvasHeight, int cellWidth, int cellHeight) {
        int sampleHeight = sample.getHeight();
        int sampleWidth = sample.getWidth();

        int width, height;
        if (canvasHeight * 1.0f / sampleHeight < canvasWidth * 1.0f / sampleWidth) {
            height = canvasHeight;
            width = canvasHeight * sampleWidth / sampleHeight;
        } else {
            width = canvasWidth;
            height = canvasWidth * sampleHeight / sampleWidth;
        }

        int shadowWidth = width / cellWidth;
        int shadowHeight = height / cellHeight;
        BufferedImage shadowImage = new BufferedImage(shadowWidth, shadowHeight, sample.getType());
        Graphics graphics = shadowImage.getGraphics();
        graphics.drawImage(sample, 0, 0, shadowWidth, shadowHeight, null);
        graphics.dispose();

        return shadowImage;
    }

    public static int[][] getShadow(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] colorSpace = new int[256];
        int[][] shadow = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = getGray(rgb);
                shadow[y][x] = gray;
                colorSpace[gray] = colorSpace[gray] + 1;
            }
        }

        int firstIndex = 0, secondIndex = 0;
        if (colorSpace[0] > colorSpace[1]) {
            firstIndex = 0;
            secondIndex = 1;
        }

        for (int i = 2; i < colorSpace.length; i++) {
            if (colorSpace[i] > colorSpace[firstIndex]) {
                secondIndex = firstIndex;
                firstIndex = i;
            } else if (colorSpace[i] > colorSpace[secondIndex]) {
                secondIndex = i;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = shadow[y][x];
                if (Math.abs(color - firstIndex) <= Math.abs(color - secondIndex)) {
                    shadow[y][x] = C_BLACK;
                } else {
                    shadow[y][x] = C_WHITE;
                }
            }
        }

        return shadow;
    }

    //从彩色变为灰度图
    public static int getGray(int rgb) {
        Color pixel = new Color(rgb);
        int red = pixel.getRed();
        int green = pixel.getGreen();
        int blue = pixel.getBlue();

        return (red * 30 + green * 60 + blue * 10) / 100;
    }

    public static List<Point> getCellStartPoints(int[][] shadow, int width, int height, int target) {
        List<Point> points = new ArrayList<Point>();
        for (int y = 0; y < shadow.length; y++) {
            for (int x = 0; x < shadow[y].length; x++) {
                if (shadow[y][x] != target) {
                    continue;
                }

                Point point = new Point(x, y);
                points.add(point);
            }
        }

        if (CollectionUtils.isEmpty(points)) {
            return points;
        }

        Point point = points.get(0);
        int sx = point.getX(), sy = point.getY();
        int ex = point.getX(), ey = point.getY();
        for (Point p : points) {
            if (p.getX() < sx) {
                sx = p.getX();
            }

            if (p.getX() > ex) {
                ex = p.getX();
            }

            if (p.getY() < sy) {
                sy = p.getY();
            }

            if (p.getY() > ey) {
                ey = p.getY();
            }
        }

        int my = (height - (ey - sy)) / 2;
        int mx = (width - (ex - sx)) / 2;
        for (Point p : points) {
            p.setX(p.getX() - sx + mx);
            p.setY(p.getY() - sy + my);
        }

        return points;
    }

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
                if (w >= width || h >= height) {
                    w = width;
                    h = height;
                } else {
                    w = width <= height ? w : (width * h / height);
                    h = width >= height ? h : (height * w / width);
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

package com.qgutech.fs.utils;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.domain.SignLevelEnum;
import org.springframework.util.Assert;

import java.security.MessageDigest;

public class Signer {

    private static char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String sign(String serverHost, String serverCode, String secret, long timestamp) {
        return md5(md5(secret + FsConstants.VERTICAL_LINE + serverHost)
                + FsConstants.VERTICAL_LINE
                + md5(secret + FsConstants.VERTICAL_LINE + serverCode)
                + FsConstants.VERTICAL_LINE
                + md5(secret + FsConstants.VERTICAL_LINE + timestamp)
                + FsConstants.VERTICAL_LINE + secret);
    }

    public static String sign(String fsFileId, String serverHost, String serverCode
            , String secret, long timestamp) {
        return md5(md5(secret + FsConstants.VERTICAL_LINE + fsFileId)
                + FsConstants.VERTICAL_LINE
                + md5(secret + FsConstants.VERTICAL_LINE + serverHost)
                + FsConstants.VERTICAL_LINE
                + md5(secret + FsConstants.VERTICAL_LINE + serverCode)
                + FsConstants.VERTICAL_LINE
                + md5(secret + FsConstants.VERTICAL_LINE + timestamp)
                + FsConstants.VERTICAL_LINE + secret);
    }

    public static String sign(FsServer fsServer, FsFile fsFile, String session) {
        Assert.notNull(fsServer, "FsServer is null!");
        Assert.notNull(fsFile, "FsFile is null!");

        SignLevelEnum signLevel = fsServer.getSignLevel();
        String signingText;
        long timestamp;
        switch (signLevel) {
            case nn:
                return signLevel.name();
            case st:
                signingText = fsServer.getSecret()
                        + FsConstants.VERTICAL_LINE + fsServer.getHost()
                        + FsConstants.VERTICAL_LINE + signLevel.name()
                        + FsConstants.VERTICAL_LINE + fsFile.getCorpCode()
                        + FsConstants.VERTICAL_LINE + fsFile.getAppCode()
                        + FsConstants.VERTICAL_LINE + fsFile.getId()
                        + FsConstants.VERTICAL_LINE + fsServer.getSecret();
                return signLevel.name() + FsConstants.PATH_SEPARATOR + md5(signingText);
            case stt:
                timestamp = System.currentTimeMillis();
                timestamp = timestamp / 1800000 * 1800000 + 280000;
                signingText = fsServer.getSecret()
                        + FsConstants.VERTICAL_LINE + timestamp
                        + FsConstants.VERTICAL_LINE + fsServer.getHost()
                        + FsConstants.VERTICAL_LINE + signLevel.name()
                        + FsConstants.VERTICAL_LINE + fsFile.getCorpCode()
                        + FsConstants.VERTICAL_LINE + fsFile.getAppCode()
                        + FsConstants.VERTICAL_LINE + fsFile.getId()
                        + FsConstants.VERTICAL_LINE + fsServer.getSecret();
                return signLevel.name() + FsConstants.PATH_SEPARATOR
                        + md5(signingText) + FsConstants.UNDERLINE + timestamp;
            case sn:
                Assert.hasText(session, "Session is empty!");
                return signLevel.name() + FsConstants.PATH_SEPARATOR + session;
            case sts:
                Assert.hasText(session, "Session is empty!");
                timestamp = System.currentTimeMillis();
                timestamp = timestamp / 1800000 * 1800000 + 280000;
                signingText = fsServer.getSecret()
                        + FsConstants.VERTICAL_LINE + timestamp
                        + FsConstants.VERTICAL_LINE + session
                        + FsConstants.VERTICAL_LINE + fsServer.getHost()
                        + FsConstants.VERTICAL_LINE + signLevel.name()
                        + FsConstants.VERTICAL_LINE + fsFile.getCorpCode()
                        + FsConstants.VERTICAL_LINE + fsFile.getAppCode()
                        + FsConstants.VERTICAL_LINE + fsFile.getId()
                        + FsConstants.VERTICAL_LINE + fsServer.getSecret();
                return signLevel.name() + FsConstants.PATH_SEPARATOR
                        + md5(signingText) + FsConstants.UNDERLINE + timestamp
                        + FsConstants.UNDERLINE + session;
            default:
                throw new RuntimeException("SignLevel[" + signLevel + "] is invalid!");

        }
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes("UTF-8"));
            byte[] byteDigest = md.digest();
            return toHexString(byteDigest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHexString(byte[] byteDigest) {
        char[] chars = new char[byteDigest.length * 2];
        for (int i = 0; i < byteDigest.length; i++) {
            // left is higher.
            chars[i * 2] = HEX_DIGITS[byteDigest[i] >> 4 & 0x0F];
            // right is lower.
            chars[i * 2 + 1] = HEX_DIGITS[byteDigest[i] & 0x0F];
        }

        return new String(chars);
    }
}

package com.qgutech.fs.utils;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Path Manipulation问题
 * 1. 攻击者能够指定某一 file system 操作中所使用的路径。
 * 2. 攻击者可以通过指定特定资源来获取某种权限，而这种权限在一般情况下是不可能获得的。
 *
 * @author TangFD@HF 2018/6/23
 */
public class PathManipulationUtils {
    private static String[] chars = new String[]{"9", "8", "7", "6", "5", "4", "3", "1", "2", "0", "a", "b", "c", "d",
            "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y",
            "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", ":", "/", "\\"};
    private static Map<String, String> charMap;

    static {
        charMap = new HashMap<String, String>(65);
        for (String aChar : chars) {
            charMap.put(aChar, aChar);
        }
    }

    public static String filterPath(String path) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            String str = charMap.get(path.charAt(i) + "");
            if (str != null) {
                temp.append(str);
            }
        }

        return filterSeparator(temp.toString());
    }

    public static String filterSeparator(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }

        return path.replace("/", File.separator).replace("\\", File.separator);
    }
}

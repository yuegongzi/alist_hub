package org.alist.hub.utils;

import org.alist.hub.bean.PathInfo;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils extends org.springframework.util.StringUtils {

    public static boolean equal(String strA, String strB) {
        if (strA != null && strB != null) {
            return !strA.equals(strB);
        }
        return true;
    }

    /**
     * 比较两个版本号的大小
     *
     * @param version1 版本号1
     * @param version2 版本号2
     * @return 如果version1小于version2，返回-1；如果version1大于version2，返回1；如果版本号相等，返回0
     */
    public static int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");

        int maxLength = Math.max(v1Parts.length, v2Parts.length);

        for (int i = 0; i < maxLength; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;

            if (v1Part < v2Part) {
                return -1; // version1小于version2
            } else if (v1Part > v2Part) {
                return 1; // version1大于version2
            }
        }

        return 0; // 版本号相等
    }

    /**
     * 根据指定分隔符分割字符串，满足特定条件：
     * - 字符串为空则返回空数组
     * - 字符串不包含传入的分隔符，则将整个字符串作为数组的一项返回
     *
     * @param str       要分割的字符串
     * @param delimiter 分隔符
     * @return 分割后的字符串数组
     */
    public static String[] split(String str, String delimiter) {
        return splitIgnoreEmpty(str, delimiter, true);
    }

    public static String[] splitIgnoreEmpty(String str, String delimiter, boolean ignoreEmpty) {
        if (str == null || str.isEmpty()) {
            return new String[0];
        }
        List<String> result = Arrays.stream(str.split(Pattern.quote(String.valueOf(delimiter))))
                .filter(s -> {
                    if (ignoreEmpty) {
                        return !s.isEmpty();
                    }
                    return true;
                }) // 去除因连续分隔符产生的空字符串项
                .toList();

        // 如果没有找到分隔符，则直接将整个字符串作为一个元素返回
        if (result.size() == 1 && !str.contains(delimiter)) {
            return new String[]{result.get(0)};
        } else {
            return result.toArray(new String[0]);
        }
    }

    /**
     * 将路径字符串分割成路径和文件名
     *
     * @param path 路径字符串
     * @return 包含路径和文件名的 PathInfo 对象
     */
    public static PathInfo splitPath(String path) {
        String[] parts = path.split("/");

        // 过滤掉空字符串
        parts = Arrays.stream(parts)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        // 如果路径以 "/" 结尾，则在最后一部分添加 "/"
        if (path.endsWith("/")) {
            parts[parts.length - 1] += "/";
        }

        // 获取倒数第二个元素作为 path，最后一个元素作为 name
        String pathStr = Arrays.stream(parts, 0, parts.length - 1)
                .reduce((s1, s2) -> s1 + "/" + s2)
                .orElse("");

        String name = parts[parts.length - 1];

        return new PathInfo(pathStr, name);
    }

    /**
     * 判断字符串是否为URL
     *
     * @param s 字符串
     * @return 如果字符串符合URL的格式则返回true，否则返回false
     */
    public static boolean isUrl(String s) {
        String urlRegex = "^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(\\S*)?$";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }


    /**
     * 判断字符串是否为十进制数
     *
     * @param s 字符串
     * @return 如果字符串能被解析为十进制数则返回true，否则返回false
     */
    public static boolean isDecimal(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为长整型
     *
     * @param s 字符串
     * @return 如果字符串能被解析为长整型则返回true，否则返回false
     */
    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断一个字符串是否为整数
     *
     * @param str 待判断的字符串
     * @return 如果是整数，返回true；否则返回false
     */
    public static boolean isInteger(String str) {
        try {
            // 尝试将字符串转换为整数
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            // 转换失败，不是整数
            return false;
        }
    }
}

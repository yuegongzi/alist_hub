package org.alist.hub.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtils extends org.springframework.util.StringUtils {

    public static boolean equal(String strA, String strB) {
        if (strA != null && strB != null) {
            return !strA.equals(strB);
        }
        return true;
    }

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
        if (str == null || str.isEmpty()) {
            return new String[0];
        }
        List<String> result = Arrays.stream(str.split(Pattern.quote(String.valueOf(delimiter))))
                .filter(s -> !s.isEmpty()) // 去除因连续分隔符产生的空字符串项
                .toList();

        // 如果没有找到分隔符，则直接将整个字符串作为一个元素返回
        if (result.size() == 1 && !str.contains(delimiter)) {
            return new String[]{result.get(0)};
        } else {
            return result.toArray(new String[0]);
        }
    }
}

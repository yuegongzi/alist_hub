package org.alist.hub.utils;

public class StringUtils extends org.springframework.util.StringUtils {

    public static boolean equal(String strA, String strB) {
        if (strA != null && strB != null) {
            return !strA.equals(strB);
        }
        return true;
    }
}

package org.alist.hub.util;

public class ByteUtils {
    public static String convertBytes(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int i = 0;
        double b = bytes;

        while (b >= 1024 && i < units.length - 1) {
            b /= 1024;
            i++;
        }

        return String.format("%.2f %s", b, units[i]);
    }
}

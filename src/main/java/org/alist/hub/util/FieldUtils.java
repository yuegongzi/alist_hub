package org.alist.hub.util;


public class FieldUtils {

    public static boolean hasField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true; // 如果没有抛出异常，说明存在该字段
        } catch (NoSuchFieldException e) {
            return false; // 捕获 NoSuchFieldException 表示字段不存在
        }
    }

}

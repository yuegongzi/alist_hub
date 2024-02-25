package org.alist.hub.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomUtils {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 生成指定长度的随机字符串
     *
     * @param length 字符串长度
     * @return 生成的随机字符串
     */
    public static String generateRandomString(int length) {
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            randomString.append(randomChar);
        }

        return randomString.toString();
    }

    /**
     * 生成一个随机的字符串作为唯一标识符
     *
     * @return 生成的随机字符串
     */
    public static String generateRandomId() {
        Random random = new Random();
        double randomId = random.nextDouble();
        return String.valueOf(randomId);
    }
}

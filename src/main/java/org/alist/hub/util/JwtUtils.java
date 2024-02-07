package org.alist.hub.util;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.UserClaims;
import org.alist.hub.configure.HubProperties;
import org.alist.hub.context.ApplicationContextProvider;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Slf4j
public class JwtUtils {
    private static String JWT_SECRET = null;


    /**
     * 获取密钥
     *
     * @return 密钥字符串
     * @throws IOException 如果发生I/O错误
     */
    public static String getSecretKey() throws IOException {
        if (JWT_SECRET == null) {
            HubProperties hubProperties = ApplicationContextProvider.getHubProperties();
            String config = Files.readString(Path.of(hubProperties.getPath() + "/config.json"));
            JsonNode jsonNode = JsonUtils.readTree(config);
            JWT_SECRET = jsonNode.findValue("jwt_secret").asText();
        }
        return JWT_SECRET;
    }

    /**
     * 计算HMAC
     *
     * @param data 要计算HMAC的数据
     * @param key  用于计算HMAC的密钥
     * @return 计算得到的HMAC结果的Base64编码字符串
     * @throws NoSuchAlgorithmException 当指定的密码算法不可用时抛出该异常
     * @throws InvalidKeyException      当密钥无法使用时抛出该异常
     */
    public static String calculateHmac(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hmacBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
//        return Base64.getEncoder().encodeBase64URLSafeString(hmacBytes); //加密结果和go不一致
    }


    /**
     * 对经过URL安全 Base64 编码的字符串进行解码
     *
     * @param base64UrlEncoded 经过URL安全 Base64 编码的字符串
     * @return 解码后的字符串
     */
    public static String base64UrlDecode(String base64UrlEncoded) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        byte[] decodedBytes = decoder.decode(base64UrlEncoded.getBytes(StandardCharsets.UTF_8));
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }


    /**
     * 解码JWT令牌
     *
     * @param token 要解码的JWT令牌
     * @return 返回一个包含解码后的用户声明的Optional对象，如果解码失败则返回空的Optional对象
     */
    public static Optional<UserClaims> decodeJwt(String token) {
        try {
            String[] chunks = token.split("\\.");
            String result = calculateHmac(chunks[0] + "." + chunks[1], getSecretKey());
            if (result.equals(chunks[2])) {  // 加密结果匹配
                return JsonUtils.readValue(base64UrlDecode(chunks[1]), UserClaims.class);  // 解析JSON并返回UserClaims对象
            }
        } catch (Exception e) {
            log.error("无法验证JWT令牌的完整性！", e);
        }
        return Optional.empty();  // 解码失败，返回空的Optional对象
    }


}

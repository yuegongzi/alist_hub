package org.alist.hub.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件替换工具类, 用于替换文件内容, 无通用性
 */
@Slf4j
public class ReplaceUtils {

    public static List<Path> findJsonFiles(Path directory) throws IOException {
        try (Stream<Path> walk = Files.walk(directory)) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
        }
    }

    public static void replaceString(Path jsonFile, String targetString, String replacementString) {
        try {
            List<String> lines = Files.readAllLines(jsonFile, StandardCharsets.UTF_8);
            List<String> replacedLines = lines.stream()
                    .map(line -> line.replace(targetString, replacementString))
                    .collect(Collectors.toList());
            Files.write(jsonFile, replacedLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}

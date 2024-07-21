package org.alist.hub.service.impl;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileSystem;
import org.alist.hub.bean.PathInfo;
import org.alist.hub.external.AListClient;
import org.alist.hub.model.Movie;
import org.alist.hub.model.SearchNode;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.SearchNodeRepository;
import org.alist.hub.service.MovieService;
import org.alist.hub.service.SearchNodeService;
import org.alist.hub.service.StorageService;
import org.alist.hub.util.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SearchNodeServiceImpl extends GenericServiceImpl<SearchNode, Long> implements SearchNodeService {
    private static final List<SearchNode> searchNodeList = new ArrayList<>();
    private static final List<Movie> movieList = new ArrayList<>();
    private final SearchNodeRepository repository;
    @Resource
    private AListClient aListClient;
    @Resource
    private MovieService movieService;
    @Resource
    private StorageService storageService;

    public SearchNodeServiceImpl(SearchNodeRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    @Transactional
    public void build() {
        List<Storage> list = storageService.findAllByIdGreaterThan(Constants.MY_ALI_ID - 1L);
        repository.deleteByType(2);
        repository.removeDuplicate();
        new Thread(() -> {
            list.forEach(storage -> {
                execute(storage.getMountPath());
            });
        }).start();

    }

    private void execute(String path) {
        log.info("构建路径:{}", path);
        try {
            List<FileSystem> list = aListClient.fs(path);
            List<SearchNode> searchNodes = new ArrayList<>();
            for (FileSystem fileSystem : list) {
                if (!fileSystem.isDir()) {//不是目录, 不增加索引节点
                    continue;
                }
                SearchNode searchNode = new SearchNode();
                searchNode.setName(fileSystem.getName());
                searchNode.setParent(path);
                searchNode.setDir(fileSystem.isDir());
                searchNode.setType(2);
                searchNode.setSize(fileSystem.getSize());
                searchNodes.add(searchNode);
                if (fileSystem.isDir()) {
                    if (path.endsWith("/")) {
                        execute(path + fileSystem.getName());
                    } else {
                        execute(path + "/" + fileSystem.getName());
                    }

                }
            }
            saveAllAndFlush(searchNodes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }

    @Override
    @Transactional
    public void update() {
        movieService.deleteAll();
        repository.deleteByType(1);
        List<Path> paths = listFiles("/index");
        long now = System.currentTimeMillis();
        // 遍历文件路径列表并逐行处理文件内容
        paths.forEach(file -> {
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (searchNodeList.size() >= 50) {
                        saveNodes();
                    }
                    if (movieList.size() >= 50) {
                        saveMovies();
                    }
                    if (line.contains("/")) {
                        handleLine(line);
                    }
                }
                if (!searchNodeList.isEmpty()) {
                    saveNodes();
                }
                if (!movieList.isEmpty()) {
                    saveMovies();
                }
            } catch (IOException e) {
                log.error("Error occurred while reading the file: " + e.getMessage());
            }
        });
        log.info("耗时：{}ms", System.currentTimeMillis() - now);
    }

    private void saveMovies() {
        movieService.saveAll(movieList);
        movieService.flush();
        entityManager.clear();
        movieList.clear();
    }

    private void saveNodes() {
        repository.saveAll(searchNodeList);
        repository.flush();
        entityManager.clear();
        searchNodeList.clear();
    }

    /**
     * 列出指定目录下的所有文件
     *
     * @param directoryPath 目录路径
     * @return 指定目录下的所有文件的路径列表
     */
    private List<Path> listFiles(String directoryPath) {
        File directory = new File(directoryPath);
        // 判断目录是否存在
        if (directory.exists() && directory.isDirectory()) {
            try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
                return stream.filter(Files::isRegularFile)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    private void saveNode(String content) {
        PathInfo pathInfo = StringUtils.splitPath(content);
        SearchNode searchNode = new SearchNode();
        searchNode.setDir(!content.contains("."));
        searchNode.setType(1);
        searchNode.setSize(0L);
        searchNode.setParent(pathInfo.path());
        searchNode.setName(pathInfo.name());
        searchNodeList.add(searchNode);
    }

    private void handleLine(String content) {
        String[] strings = StringUtils.splitIgnoreEmpty(content, "#", false);
        Movie movie = new Movie();
        int i = 0;
        for (String str : strings) {
            if (i == 0) {
                movie.setPath(str);
                i++;
                continue;
            }
            if (StringUtils.isUrl(str)) { // 判断是否为URL
                movie.setImageUrl(str);
            } else if (StringUtils.isLong(str) || StringUtils.isInteger(str)) {
                movie.setMovieId(Long.valueOf(str));
            } else if (StringUtils.isDecimal(str)) {
                movie.setScore(Double.valueOf(str));
            } else {
                movie.setName(str);
            }
        }
        if (StringUtils.hasText(movie.getPath())) {
            saveNode(movie.getPath());
        }
        if (StringUtils.hasText(movie.getName()) && movie.getMovieId() != null) {
            movieList.add(movie);
        }
    }


}

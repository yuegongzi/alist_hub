package org.alist.hub.bean;

import lombok.Getter;

@Getter
public class PathInfo {
    private final String path;
    private final String name;

    public PathInfo(String path, String name) {
        this.path = path;
        this.name = name;
    }
}

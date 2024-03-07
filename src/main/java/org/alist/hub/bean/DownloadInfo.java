package org.alist.hub.bean;

import lombok.Data;

import java.util.List;

@Data
public class DownloadInfo {
    private String bitfield;
    private long completedLength;
    private int connections;
    private String dir;
    private long downloadSpeed;
    private List<File> files;
    private String gid;
    private int numPieces;
    private long pieceLength;
    private String status;
    private long totalLength;
    private long uploadLength;
    private long uploadSpeed;

    @Data
    public static class File {
        private long completedLength;
        private String index;
        private long length;
        private String path;
        private boolean selected;
    }
}

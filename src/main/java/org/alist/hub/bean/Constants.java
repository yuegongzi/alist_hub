package org.alist.hub.bean;

public interface Constants {
    String ALIST_BASE_URL = "http://localhost:5244/api";
    String XIAOYA_BASE_URL = "http://docker.xiaoya.pro/";
    String DATA_DIR = "/opt/alist/data";
    String APP_INIT = "app_init";
    int APP_GROUP = 0;
    int ALIST_GROUP = 1;
    int WATCHER_GROUP = 2;
    String API_DOMAIN = "https://api.xhofe.top";
    String TV_BOX_TOKEN = "tv_box_token";
    String FILE_NAME = "alist_hub_temp";
    //定义一个很高的自定义ID 避免自己添加后的ID占用小雅的ID
    Long MY_ALI_ID = 50000L;
    String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36";
}

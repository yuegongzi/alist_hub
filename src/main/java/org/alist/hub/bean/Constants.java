package org.alist.hub.bean;

public interface Constants {
    String ALIST_BASE_URL = "http://localhost:5244/api";
    String XIAOYA_BASE_URL = "http://docker.xiaoya.pro/";
    String DATA_DIR = "/opt/alist/data";
    String APP_INIT = "app_init";
    int APP_GROUP = 0;
    int ALIST_GROUP = 1;
    String API_DOMAIN = "https://api.xhofe.top";
    String TV_BOX_TOKEN = "tv_box_token";
    String FILE_NAME = "alist_hub_temp";
    String CONTENT = "<FORM METHOD=GET ACTION=/search >             \n" +
            "<input type=\"text\" name=\"box\" placeholder=\" 输入搜索关键词\"  >         \n" +
            "<input type=\"hidden\" name=\"url\"> \n" +
            "</FORM>\n";
}

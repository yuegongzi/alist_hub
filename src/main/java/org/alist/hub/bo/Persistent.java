package org.alist.hub.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 实现持久数据
 */
public interface Persistent {
    /**
     * 获取存入数据库的ID
     *
     * @return String
     */
    @JsonIgnore
    String getId();

    /**
     * 获取存入数据库的值
     *
     * @return String
     */
    @JsonIgnore
    String getValue();
}

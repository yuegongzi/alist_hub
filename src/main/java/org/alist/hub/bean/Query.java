package org.alist.hub.bean;

import lombok.Data;
import org.alist.hub.utils.FieldUtil;
import org.alist.hub.utils.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;


@Data
public class Query {
    private int current = 1; // 默认页码为 1
    private int pageSize = 10; // 默认页面大小为 10
    private String ascs;
    private String descs;


    public Sort getSort(Class<?> clazz) {
        List<Sort.Order> orders = new ArrayList<>();
        String[] asc = StringUtils.split(this.ascs, ",");
        for (String string : asc) {
            if (FieldUtil.hasField(clazz, string)) {
                orders.add(new Sort.Order(Sort.Direction.ASC, string));
            }
        }
        String[] desc = StringUtils.split(this.descs, ",");
        for (String string : desc) {
            if (FieldUtil.hasField(clazz, string)) {
                orders.add(new Sort.Order(Sort.Direction.DESC, string));
            }
        }
        return Sort.by(orders);
    }

    public PageRequest of(Class<?> clazz) {
        if (this.pageSize > 100) {
            this.pageSize = 100;
        }
        if (this.pageSize <= 0) {
            this.pageSize = 10;
        }
        if (this.current < 0) {
            this.current = 1;
        }
        return PageRequest.of(this.current - 1, this.pageSize, this.getSort(clazz));
    }
}

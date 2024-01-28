package org.alist.hub.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "x_search_node")
public class SearchNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "parent")
    private String parent;  // 父节点

    @Column(name = "name")
    private String name;  // 节点名称

    @Column(name = "is_dir")
    private boolean isDir;  // 是否为目录（由于是numeric类型，这里假设它代表布尔值）

    @Column(name = "size")
    private Long size;  // 文件大小
}
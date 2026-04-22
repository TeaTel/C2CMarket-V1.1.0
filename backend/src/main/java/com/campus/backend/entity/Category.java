package com.campus.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Long parentId;
    private String iconUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
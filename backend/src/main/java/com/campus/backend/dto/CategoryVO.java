package com.campus.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Long parentId;
    private String iconUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<CategoryVO> children;
    
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "禁用";
            case 1: return "启用";
            default: return "未知";
        }
    }
}
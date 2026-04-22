package com.campus.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class CategoryUpdateDTO {
    @Size(min = 2, max = 50, message = "分类名称长度必须在2-50个字符之间")
    private String name;
    
    @Size(max = 200, message = "分类描述不能超过200个字符")
    private String description;
    
    private Integer sortOrder;
    private Integer status;
    private Long parentId;
    private String iconUrl;
}
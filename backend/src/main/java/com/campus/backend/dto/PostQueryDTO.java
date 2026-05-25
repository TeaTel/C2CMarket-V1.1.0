package com.campus.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostQueryDTO {

    private Long boardId;

    private String postType;

    private String keyword;

    private Long userId;

    private List<Long> userIds;

    private String status;

    private String tag;

    private String campusTag;

    private String sortBy = "time_desc";

    private Integer page = 1;

    private Integer size = 20;

    public int getOffset() {
        return (page - 1) * size;
    }
}

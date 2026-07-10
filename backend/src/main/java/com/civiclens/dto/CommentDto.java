package com.civiclens.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private String id;
    private String leaderId;
    private String userId;
    private String comment;
    private Integer rating;
    private String type;
    private String parentId;
    private String createdAt;
    private String firstName;
    private String lastName;
    private String email;
    private String photoUrl;
    private String username;
    private java.util.List<CommentDto> replies;
}

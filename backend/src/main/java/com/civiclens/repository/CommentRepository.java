package com.civiclens.repository;

import com.civiclens.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByLeader_IdAndParentIsNullAndHiddenFalse(String leaderId, Pageable pageable);
    List<Comment> findByParent_IdInAndHiddenFalse(List<String> parentIds);
    List<Comment> findByUser_Id(String userId);
}
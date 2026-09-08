package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.edu.springboot.domain.board.Comment;

@Mapper
public interface CommentMapper {

	int insert(Comment comment);

	Comment findById(@Param("id") Long id);

	List<Comment> findByBoardId(@Param("boardId") Long boardId);

	int update(Comment comment);

	int delete(@Param("id") Long id);

	int deleteByBoardId(@Param("boardId") Long boardId);
}

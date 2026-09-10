package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.edu.springboot.domain.board.Comment;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: CommentMapper
 * 책임: MyBatis XML 매퍼 인터페이스
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Mapper
public interface CommentMapper {

	int insert(Comment comment);

	Comment findById(@Param("id") Long id);

	List<Comment> findByBoardId(@Param("boardId") Long boardId);

	int update(Comment comment);

	int delete(@Param("id") Long id);

	int deleteByBoardId(@Param("boardId") Long boardId);
}

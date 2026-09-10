package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.edu.springboot.domain.board.QnaBoard;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: QnaBoardMapper
 * 책임: MyBatis XML 매퍼 인터페이스
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Mapper
public interface QnaBoardMapper {

	int insert(QnaBoard board);

	QnaBoard findById(@Param("id") Long id);

	int update(QnaBoard board);

	int delete(@Param("id") Long id);

	List<QnaBoard> findPage(@Param("solution") String solution, @Param("searchType") String searchType,
		@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

	long count(@Param("solution") String solution, @Param("searchType") String searchType,
		@Param("keyword") String keyword);

	int increaseVisitCount(@Param("id") Long id);

	int increaseLikeCount(@Param("id") Long id);
}

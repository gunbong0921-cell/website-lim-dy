package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.edu.springboot.domain.board.ArchiveBoard;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: ArchiveBoardMapper
 * 책임: MyBatis XML 매퍼 인터페이스
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Mapper
public interface ArchiveBoardMapper {

	int insert(ArchiveBoard board);

	ArchiveBoard findById(@Param("id") Long id);

	int update(ArchiveBoard board);

	int delete(@Param("id") Long id);

	List<ArchiveBoard> findPage(@Param("searchType") String searchType, @Param("keyword") String keyword,
		@Param("offset") int offset, @Param("size") int size);

	long count(@Param("searchType") String searchType, @Param("keyword") String keyword);

	int increaseVisitCount(@Param("id") Long id);

	int increaseLikeCount(@Param("id") Long id);
}

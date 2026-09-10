package com.edu.springboot.infrastructure.persistence.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: LikeMapper
 * 책임: MyBatis XML 매퍼 인터페이스
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Mapper
public interface LikeMapper {

	int exists(@Param("boardType") String boardType, @Param("boardId") Long boardId,
		@Param("memberLoginId") String memberLoginId);

	int insert(@Param("boardType") String boardType, @Param("boardId") Long boardId,
		@Param("memberLoginId") String memberLoginId);
}

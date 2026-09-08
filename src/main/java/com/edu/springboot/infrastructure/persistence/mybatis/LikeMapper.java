package com.edu.springboot.infrastructure.persistence.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {

	int exists(@Param("boardType") String boardType, @Param("boardId") Long boardId,
		@Param("memberLoginId") String memberLoginId);

	int insert(@Param("boardType") String boardType, @Param("boardId") Long boardId,
		@Param("memberLoginId") String memberLoginId);
}

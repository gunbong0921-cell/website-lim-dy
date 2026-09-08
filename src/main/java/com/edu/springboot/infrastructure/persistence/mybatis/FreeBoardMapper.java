package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.edu.springboot.domain.board.FreeBoard;

@Mapper
public interface FreeBoardMapper {

	int insert(FreeBoard board);

	FreeBoard findById(@Param("id") Long id);

	int update(FreeBoard board);

	int delete(@Param("id") Long id);

	List<FreeBoard> findPage(@Param("searchType") String searchType, @Param("keyword") String keyword,
		@Param("offset") int offset, @Param("size") int size);

	long count(@Param("searchType") String searchType, @Param("keyword") String keyword);

	int increaseVisitCount(@Param("id") Long id);

	int increaseLikeCount(@Param("id") Long id);
}

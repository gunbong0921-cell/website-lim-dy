package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.edu.springboot.domain.board.BoardFile;

@Mapper
public interface BoardFileMapper {

	int insert(BoardFile file);

	List<BoardFile> findByBoardId(@Param("boardId") Long boardId);

	BoardFile findById(@Param("id") Long id);

	int deleteByBoardId(@Param("boardId") Long boardId);
}

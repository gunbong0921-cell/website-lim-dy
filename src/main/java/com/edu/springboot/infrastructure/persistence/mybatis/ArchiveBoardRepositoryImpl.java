package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.board.ArchiveBoard;
import com.edu.springboot.domain.board.ArchiveBoardRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ArchiveBoardRepositoryImpl implements ArchiveBoardRepository {

	private final ArchiveBoardMapper mapper;

	@Override
	public ArchiveBoard save(ArchiveBoard board) {
		mapper.insert(board);
		return board;
	}

	@Override
	public Optional<ArchiveBoard> findById(Long id) {
		return Optional.ofNullable(mapper.findById(id));
	}

	@Override
	public void update(ArchiveBoard board) {
		mapper.update(board);
	}

	@Override
	public void delete(Long id) {
		mapper.delete(id);
	}

	@Override
	public List<ArchiveBoard> findPage(String searchType, String keyword, int offset, int size) {
		return mapper.findPage(searchType, keyword, offset, size);
	}

	@Override
	public long count(String searchType, String keyword) {
		return mapper.count(searchType, keyword);
	}

	@Override
	public void increaseVisitCount(Long id) {
		mapper.increaseVisitCount(id);
	}

	@Override
	public void increaseLikeCount(Long id) {
		mapper.increaseLikeCount(id);
	}
}

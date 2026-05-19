package com.ssafy.ssabre.board.service;

import com.ssafy.ssabre.board.entity.Board;
import com.ssafy.ssabre.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "boards", allEntries = true)
    public Board save(com.ssafy.ssabre.board.dto.BoardCreateRequest request) {
        Board board = Board.builder()
                .name(request.name())
                .category(request.category())
                .description(request.description())
                .build();
        return boardRepository.save(board);
    }

    @org.springframework.cache.annotation.Cacheable(value = "boards")
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public Optional<Board> findById(Long id) {
        return boardRepository.findById(id);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "boards", allEntries = true)
    public Board update(Long id, com.ssafy.ssabre.board.dto.BoardUpdateRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));
        board.update(request.name(), request.description());
        return board;
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "boards", allEntries = true)
    public void delete(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));
        board.delete();
    }
}

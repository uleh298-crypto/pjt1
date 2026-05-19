package com.ssafy.ssabre.board.repository;

import com.ssafy.ssabre.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByDeletedAtIsNull();
}

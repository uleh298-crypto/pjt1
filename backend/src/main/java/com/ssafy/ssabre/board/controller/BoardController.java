package com.ssafy.ssabre.board.controller;

import com.ssafy.ssabre.board.entity.Board;
import com.ssafy.ssabre.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Tag(name = "Boards", description = "게시판 관리 API")
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    @Operation(summary = "게시판 목록 조회", description = "모든 게시판 목록을 조회합니다.")
    public ResponseEntity<List<Board>> getBoards() {
        return ResponseEntity.ok(boardService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시판 상세 조회", description = "특정 게시판의 상세 정보를 조회합니다.")
    public ResponseEntity<Board> getBoard(@PathVariable Long id) {
        Board board = boardService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 존재하지 않습니다. id=" + id));
        return ResponseEntity.ok(board);
    }

    @PostMapping
    @Operation(summary = "게시판 생성", description = "새로운 게시판을 생성합니다.")
    public ResponseEntity<Board> createBoard(@RequestBody com.ssafy.ssabre.board.dto.BoardCreateRequest request) {
        return ResponseEntity.ok(boardService.save(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시판 수정", description = "게시판 정보를 수정합니다.")
    public ResponseEntity<Board> updateBoard(@PathVariable Long id,
            @RequestBody com.ssafy.ssabre.board.dto.BoardUpdateRequest request) {
        return ResponseEntity.ok(boardService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시판 삭제", description = "게시판을 삭제합니다.")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.ok().build();
    }
}

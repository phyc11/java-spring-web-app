package com.example.comment.controller;

import com.example.common.dto.ApiResponse;
import com.example.comment.dto.CommentDTO;
import com.example.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/task/{taskId}")
    public ApiResponse<List<CommentDTO>> getTaskComments(@PathVariable Long taskId) {
        List<CommentDTO> comments = commentService.getTaskCommentTree(taskId);
        return ApiResponse.ok("Comments retrieved successfully", comments);
    }

    @GetMapping("/task/{taskId}/count")
    public ApiResponse<Map<String, Long>> getTaskCommentCount(@PathVariable Long taskId) {
        long count = commentService.getTaskCommentCount(taskId);
        return ApiResponse.ok("Comment count retrieved", Map.of("count", count));
    }

    @PostMapping
    public ApiResponse<CommentDTO> addComment(@Valid @RequestBody CommentDTO dto, Principal principal) {
        String author = principal != null ? principal.getName() : "User";
        String avatarColor = "#6366f1";
        CommentDTO created = commentService.addComment(dto, author, avatarColor);
        return ApiResponse.ok("Comment posted successfully", created);
    }

    @PutMapping("/{id}")
    public ApiResponse<CommentDTO> updateComment(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        String author = principal != null ? principal.getName() : "User";
        String content = body.get("content");
        CommentDTO updated = commentService.updateComment(id, content, author);
        return ApiResponse.ok("Comment updated successfully", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id, Principal principal) {
        String author = principal != null ? principal.getName() : "User";
        commentService.deleteComment(id, author);
        return ApiResponse.ok("Comment deleted successfully", null);
    }
}

package com.Abhishek.authify.controller;

import com.Abhishek.authify.entity.Note;
import com.Abhishek.authify.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/notes")
    public List<Note> getNotes(@CurrentSecurityContext(expression = "authentication?.name") String userId) {
        return noteService.getNotesByUserId(userId);
    }

    @PostMapping("/notes")
    public Note createNote(
            @CurrentSecurityContext(expression = "authentication?.name") String userId,
            @RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");
        return noteService.createNote(userId, title, content);
    }

    @PutMapping("/notes/{noteId}")
    public Note updateNote(
            @PathVariable Long noteId,
            @CurrentSecurityContext(expression = "authentication?.name") String userId,
            @RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");
        return noteService.updateNote(noteId, userId, title, content);
    }

    @DeleteMapping("/notes/{noteId}")
    public void deleteNote(
            @PathVariable Long noteId,
            @CurrentSecurityContext(expression = "authentication?.name") String userId) {
        noteService.deleteNote(noteId, userId);
    }
}
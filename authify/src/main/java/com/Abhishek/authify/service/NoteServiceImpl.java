package com.Abhishek.authify.service;

import com.Abhishek.authify.entity.Note;
import com.Abhishek.authify.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    @Override
    public List<Note> getNotesByUserId(String userId) {
        return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Override
    public Note createNote(String userId, String title, String content) {
        Note note = Note.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .build();
        return noteRepository.save(note);
    }

    @Override
    public Note updateNote(Long noteId, String userId, String title, String content) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Ensure the note belongs to the user
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to note");
        }

        note.setTitle(title);
        note.setContent(content);
        return noteRepository.save(note);
    }

    @Override
    public void deleteNote(Long noteId, String userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Ensure the note belongs to the user
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to note");
        }

        noteRepository.delete(note);
    }
}
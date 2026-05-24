package com.Abhishek.authify.service;

import com.Abhishek.authify.entity.Note;
import java.util.List;

public interface NoteService {
    List<Note> getNotesByUserId(String userId);
    Note createNote(String userId, String title, String content);
    Note updateNote(Long noteId, String userId, String title, String content);
    void deleteNote(Long noteId, String userId);
}
package com.example.onlinetrivia.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Insert
    void insert(ChatMessage chatMessage);

    @Query("SELECT * FROM chat_messages WHERE chatName = :chatName ORDER BY timestamp DESC")
    List<ChatMessage> getMessagesForChat(String chatName);

    @Query("DELETE FROM chat_messages WHERE chatName = :chatName")
    void deleteMessagesForChat(String chatName);
}

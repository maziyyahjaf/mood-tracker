package com.example.maziyyah.mood_tracker.model;

public class Update {
    private Long chat_id;
    private Long from_id;
    private String message_text;


    



    public Update() {
    }

    
    public Update(Long chat_id, Long from_id, String message_text) {
        this.chat_id = chat_id;
        this.from_id = from_id;
        this.message_text = message_text;
    }


    public Long getChat_id() {
        return chat_id;
    }
    public void setChat_id(Long chat_id) {
        this.chat_id = chat_id;
    }
    public Long getFrom_id() {
        return from_id;
    }
    public void setFrom_id(Long from_id) {
        this.from_id = from_id;
    }
    public String getMessage_text() {
        return message_text;
    }
    public void setMessage_text(String message_text) {
        this.message_text = message_text;
    }

    
    
}

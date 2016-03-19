package com.epitech.wepleb.events;

public class NewMessageEvent {
    public final String id;
    public final String message;

    public NewMessageEvent(String id, String message) {
        this.id = id;
        this.message = message;
    }
}

package net.woodburry;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: gamma_000
 * Date: 5/10/13
 * Time: 11:55 PM
 */
public class ChatMessage implements Serializable {

    private String text;
    private String user;

    public ChatMessage() {
    }

    public ChatMessage(String user, String text) {
        this.user = user;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return user + " : " + text;
    }
}

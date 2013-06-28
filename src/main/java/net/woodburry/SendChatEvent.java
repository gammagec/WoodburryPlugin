package net.woodburry;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jivesoftware.smack.XMPPException;

/**
 * Created with IntelliJ IDEA.
 * User: gamma_000
 * Date: 6/14/13
 * Time: 11:57 PM
 */
public class SendChatEvent extends BukkitRunnable {

    String message;
    ChatManager chatManager;

    public SendChatEvent(ChatManager cm, String message) {
        this.message = message;
        chatManager = cm;
    }

    @Override
    public void run() {
        try {
            chatManager.connect();
        } catch (XMPPException e) {
            e.printStackTrace();
            chatManager.logger.severe(e.getMessage().toString());
            return;
        }
        try {
            chatManager.chatRoom.sendMessage(message);
        } catch (XMPPException e) {
            e.printStackTrace();
            chatManager.logger.severe(e.getMessage().toString());
        }
    }
}

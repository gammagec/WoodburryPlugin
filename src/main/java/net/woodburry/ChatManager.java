package net.woodburry;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gamma_000
 * Date: 6/9/13
 * Time: 11:47 PM
 */
public class ChatManager implements PacketListener {

    private List<ChatMessage> chatMessages =
            new ArrayList<ChatMessage>();

    private XMPPConnection connection;
    MultiUserChat chatRoom;

    public static final String CHAT_ROOM_NAME = "woodburry@conference.chrisgammage.com";

    private final Server server;
    private final Plugin plugin;
    final Logger logger;

    class ChatListener implements Listener {
        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            sendChatMessage(event.getPlayer().getDisplayName(),
                    event.getMessage());
        }
    };

    public class SendChatListener implements Listener {
        @EventHandler
        public void onSendChat(SendChatEvent event) {

        }
    }
    public ChatManager(Server server, Plugin plugin,
                       Logger logger) {
        this.server = server;
        this.plugin = plugin;
        this.logger = logger;
        server.getPluginManager().registerEvents(
                new ChatListener(), plugin);
        server.getPluginManager().registerEvents(new SendChatListener(), plugin);

        try {
            connect();
        } catch (XMPPException e) {
            e.printStackTrace();
            logger.severe(e.getMessage().toString());
        }
    }

    private void sendChatMessage(String userName, String message) {
        new SendChatEvent(this, userName + ": " + message).runTaskLater(plugin, 2);
    }

    void connect() throws XMPPException {
        if(connection != null && connection.isConnected()) {
            logger.info("already logged in");
            return;
        }
        logger.info("logging in");
        ConnectionConfiguration config = new ConnectionConfiguration("localhost", 5222);
        connection = new XMPPConnection(config);
        connection.connect();
        SASLAuthentication.supportSASLMechanism("PLAIN", 0);
        //connection.login("woodburry", "woodburry");
        connection.loginAnonymously();
        logger.info("logged in");

        chatRoom = new MultiUserChat(connection, CHAT_ROOM_NAME);
        logger.info("Joining " + CHAT_ROOM_NAME);
        chatRoom.join("server");
        chatRoom.addMessageListener(this);
    }

    @Override
    public void processPacket(Packet packet) {
        Message message = (Message)packet;
        if(message.getType() == Message.Type.error) {
            logger.severe(message.getError().toString());
        } else if(message.getType() == Message.Type.groupchat) {
            int i = packet.getFrom().indexOf('/');
            String user = packet.getFrom().substring(i + 1);
            if(!user.equals("server")) {
                logger.info("got group chat: " + user + ": " + message.getBody());
                for(Player player : server.getOnlinePlayers()) {
                    player.sendMessage("(web)" + user + " says : " + message.getBody());
                }
            }
        }
    }
}

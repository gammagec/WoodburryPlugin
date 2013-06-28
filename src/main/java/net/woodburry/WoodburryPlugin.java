package net.woodburry;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gamma_000
 * Date: 5/2/13
 * Time: 10:20 PM
 */
public class WoodburryPlugin extends JavaPlugin {

    private Database sql = null;
    private ChatManager chatManager;

    @Override
    public void onEnable(){
        getLogger().info("onEnable has been invoked!");
        sql = new MySQL(Logger.getLogger("Minecraft"),
                "[WoodburryPlugin] ",
                "localhost",
                3306,
                "drupal",
                "root",
                "admin");
        getLogger().info("database connect done!");
        UpdateWhitelistTask updateWhitelistTask = new UpdateWhitelistTask(this);
        updateWhitelistTask.runTaskTimer(this, 0, 2400);

//        try {
//            final File[] libs = new File[] {
//                    new File(getDataFolder(), "activation.jar"),
//                    new File(getDataFolder(), "mail.jar") };
//            for (final File lib : libs) {
//                if (!lib.exists()) {
//                    JarUtils.extractFromJar(lib.getName(),
//                            lib.getAbsolutePath());
//                }
//            }
//            for (final File lib : libs) {
//                if (!lib.exists()) {
//                    getLogger().warning(
//                            "There was a critical error loading My plugin! Could not find lib: "
//                                    + lib.getName());
//                    Bukkit.getServer().getPluginManager().disablePlugin(this);
//                    return;
//                }
//                addClassPath(JarUtils.getJarUrl(lib));
//            }
//        } catch (final Exception e) {
//            e.printStackTrace();
//            getLogger().severe(e.getMessage().toString());
//        }

        chatManager = new ChatManager(getServer(), this, getLogger());
    }

    private void addClassPath(final URL url) throws IOException {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader
                .getSystemClassLoader();
        final Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            final Method method = sysclass.getDeclaredMethod("addURL",
                    new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (final Throwable t) {
            t.printStackTrace();
            throw new IOException("Error adding " + url
                    + " to system classloader");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("onDisable has been invoked!");
        if(sql != null) {
            sql.close();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("woodburry")) {
            if(sender instanceof Player) {
                getLogger().info("Woodburry command invoked by " + ((Player) sender).getDisplayName());
            } else {
                getLogger().info("Woodburry command invoked from console!");
            }
            return true;
        } else if(cmd.getName().equalsIgnoreCase("webusers")) {
            return webUsersCommand(sender);
        } else if(cmd.getName().equalsIgnoreCase("ratings")) {
            return ratingsCommand(sender);
        } else if(cmd.getName().equalsIgnoreCase("updateWhitelist")) {
            return updateWhitelistCommand(sender);
        }
        return false;
    }

    private boolean ratingsCommand(CommandSender sender) {
        if (!sql.isOpen()) {
            sql.open();
        }
        boolean consoleUser = !(sender instanceof Player);
        Player player = null;
        if(!consoleUser) {
            player = (Player)sender;
        }

        Connection conn = sql.getConnection();
        Statement statement = null;
        String playerString = new String();
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select name,v1.value,v2.value from users " +
                    "  inner join profile on users.uid = profile.uid " +
                    "  inner join votingapi_cache as v1 on profile.pid = v1.entity_id " +
                    "  inner join votingapi_cache as v2 on profile.pid = v2.entity_id " +
                    "  where v1.function = 'count' and v1.entity_type = 'profile2' and v2.function = 'average' and v2.entity_type = 'profile2'");
            if(rs != null) {
                while(rs.next()) {
                    String name = rs.getString("name");
                    if(consoleUser) {
                        getLogger().info("got user " + name + " has " + rs.getString("v1.value") + " votes and " + (rs.getDouble("v2.value") / 10.0) + " average stars");
                    } else {
                        playerString += " " + name + " has " + rs.getString("v1.value") + " votes and " + (rs.getDouble("v2.value") / 10.0) + " average stars";
                    }
                }
            }
            if(!consoleUser) {
                player.sendMessage(playerString);
            }
        } catch (SQLException e) {
            getLogger().severe("database failed to create statement: " + e.toString());
            e.printStackTrace();
            if(statement != null) {
                try { statement.close(); } catch (SQLException e1) {}
            }
        }
        return true;
    }

    private boolean webUsersCommand(CommandSender sender) {
        if (!sql.isOpen()) {
            sql.open();
        }
        boolean consoleUser = !(sender instanceof Player);
        Player player = null;
        if(!consoleUser) {
            player = (Player)sender;
        }

        Connection conn = sql.getConnection();
        Statement statement = null;
        String playerString = new String();
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select name from users");
            if(rs != null) {
                while(rs.next()) {
                    String name = rs.getString("name");
                    if(consoleUser) {
                        getLogger().info("got user " + name);
                    } else {
                        playerString += " " + name;
                    }
                }
            }
            if(!consoleUser) {
                player.sendMessage(playerString);
            }
        } catch (SQLException e) {
            getLogger().severe("database failed to create statement: " + e.toString());
            e.printStackTrace();
            if(statement != null) {
                try { statement.close(); } catch (SQLException e1) {}
            }
        }
        return true;
    }

    public class UpdateWhitelistTask extends BukkitRunnable {
        private final WoodburryPlugin plugin;
        public UpdateWhitelistTask(WoodburryPlugin plugin) {
            this.plugin = plugin;
        }
        public void run() {
            if(plugin.doWhitelistUpdate()) {
                plugin.getServer().reloadWhitelist();
                plugin.getLogger().info("Whitelist updated");
            } else {
                plugin.getLogger().warning("Whitelist update failed");
            }
        }
    }

    public boolean doWhitelistUpdate() {
        if (!sql.isOpen()) {
            sql.open();
        }
        Connection conn = sql.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select name from users inner join users_roles on users.uid = users_roles.uid where users_roles.rid = 5");
            File whiteList = new File("white-list.txt");
            if(whiteList.exists()) {
                whiteList.delete();
            }
            whiteList.createNewFile();
            FileWriter fw = new FileWriter(whiteList);
            BufferedWriter bw = new BufferedWriter(fw);

            if(rs != null) {
                while(rs.next()) {
                    String name = rs.getString("name");
                    bw.write(name);
                    bw.newLine();
                }
            }
            bw.close();
            fw.close();
        } catch (SQLException e) {
            getLogger().severe("database failed to create statement: " + e.toString());
            e.printStackTrace();
            if(statement != null) {
                try { statement.close(); } catch (SQLException e1) {}
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
        return true;
    }

    private boolean updateWhitelistCommand(CommandSender sender) {
        boolean consoleUser = !(sender instanceof Player);
        Player player = null;

        boolean done = doWhitelistUpdate();
        if(done) {
            getLogger().info("whitelist updated");
            if(!consoleUser) {
                player = (Player)sender;
                player.sendMessage("Whitelist Updated");
            }
        } else {
            getLogger().warning("failed to update whitelist");
        }
        return true;
    }
}

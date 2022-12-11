package net.thelipe.lobbycommand;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Random;

public final class LobbyCommandPlugin extends JavaPlugin {

    private static LobbyCommandPlugin instance;
    public static LobbyCommandPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getServer().getPluginCommand("lobby").setExecutor((sender, cmd, label, args) -> {
            FileConfiguration config = LobbyCommandPlugin.getInstance().getConfig();

            if (sender instanceof ConsoleCommandSender) {
                String message = config.getString("Config.Messages.OnlyPlayers");
                LobbyCommandPlugin.sendMessage(sender, message);
                return false;
            }

            Player player = (Player) sender;

            List<String> lobbies = config.getStringList("Config.Lobbies");
            if (lobbies.isEmpty()) {
                String message = config.getString("Config.Messages.NoLobbyFound");
                if (!message.equals("")) {
                    LobbyCommandPlugin.sendMessage(player, message);
                }
                String sound = config.getString("Config.Sounds.NoLobbyFound");
                if (!sound.equals("")) {
                    player.playSound(player.getLocation(), Sound.valueOf(sound), 1.0f, 1.0f);
                }
                return false;
            }

            Random random = new Random();
            String server = lobbies.get(random.nextInt(lobbies.size()));

            String message = config.getString("Config.Messages.Connecting").replace("%server%", server);
            if (!message.equals("")) {
                LobbyCommandPlugin.sendMessage(player, message);
            }
            String sound = config.getString("Config.Sounds.Connecting");
            if (!sound.equals("")) {
                player.playSound(player.getLocation(), Sound.valueOf(sound), 1.0f, 1.0f);
            }
            sendPlayerToServer(player, server);
            return false;
        });

        sendMessage(getServer().getConsoleSender(), "&aPlugin iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        sendMessage(getServer().getConsoleSender(),"&cPlugin desligado com sucesso!");
    }

    public static void sendPlayerToServer(Player player, String server) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (Exception e) {
            String message = LobbyCommandPlugin.getInstance().getConfig().getString("Config.Messages.ProblemOnConnection").replace("%server%", server);
            if (!message.equals("")) {
                LobbyCommandPlugin.sendMessage(player, message);
            }
            String sound = LobbyCommandPlugin.getInstance().getConfig().getString("Config.Sounds.ProblemOnConnection");
            if (!sound.equals("")) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
            return;
        }

        player.sendPluginMessage(LobbyCommandPlugin.getInstance(), "BungeeCord", b.toByteArray());
    }

    public static void sendMessage(CommandSender sender, String message) {
        message = message.replace("&", "§");
        sender.sendMessage(message);
    }

}

package fr.hyriode.pearlcontrol;

import fr.hyriode.hyrame.HyrameLoader;
import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.language.IHyriLanguageManager;
import fr.hyriode.pearlcontrol.config.HyriPCConfig;
import fr.hyriode.pearlcontrol.game.HyriPCGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:55
 */
public class HyriPearlControl extends JavaPlugin {

    public static final String NAME = "PearlControl";

    private static IHyriLanguageManager languageManager;

    private HyriPCConfig config;
    private HyriPCGame game;

    private IHyrame hyrame;

    @Override
    public void onEnable() {
        final ChatColor color = ChatColor.GREEN;
        final ConsoleCommandSender sender = Bukkit.getConsoleSender();

        sender.sendMessage(color + "  ___              _  ___         _           _ ");
        sender.sendMessage(color + " | _ \\___ __ _ _ _| |/ __|___ _ _| |_ _ _ ___| |");
        sender.sendMessage(color + " |  _/ -_) _` | '_| | (__/ _ \\ ' \\  _| '_/ _ \\ |");
        sender.sendMessage(color + " |_| \\___\\__,_|_| |_|\\___\\___/_||_\\__|_| \\___/_|");

        log("Starting " + NAME + "...");

        this.config = new HyriPCConfig(this);
        this.config.create();
        this.config.load();
        this.hyrame = HyrameLoader.load(new HyriPCProvider(this));

        languageManager = this.hyrame.getLanguageManager();

        this.game = new HyriPCGame(this.hyrame, this);
        this.hyrame.getGameManager().registerGame(this.game);
    }

    @Override
    public void onDisable() {
        log("Stopping " + NAME + "...");

        this.hyrame.getGameManager().unregisterGame(this.game);
    }

    public static void log(Level level, String message) {
        String prefix = ChatColor.GREEN + "[" + NAME + "] ";

        if (level == Level.SEVERE) {
            prefix += ChatColor.RED;
        } else if (level == Level.WARNING) {
            prefix += ChatColor.YELLOW;
        } else {
            prefix += ChatColor.RESET;
        }

        Bukkit.getConsoleSender().sendMessage(prefix + message);
    }

    public static void log(String msg) {
        log(Level.INFO, msg);
    }

    public static IHyriLanguageManager getLanguageManager() {
        return languageManager;
    }

    public IHyrame getHyrame() {
        return this.hyrame;
    }

    public HyriPCGame getGame() {
        return this.game;
    }

    public HyriPCConfig getConfiguration() {
        return this.config;
    }

}

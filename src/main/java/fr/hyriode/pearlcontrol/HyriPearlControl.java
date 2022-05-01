package fr.hyriode.pearlcontrol;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.server.IHyriServer;
import fr.hyriode.hyrame.HyrameLoader;
import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.language.IHyriLanguageManager;
import fr.hyriode.hyrame.utils.LocationWrapper;
import fr.hyriode.hystia.api.config.IConfig;
import fr.hyriode.pearlcontrol.config.PCConfig;
import fr.hyriode.pearlcontrol.game.PCGame;
import fr.hyriode.pearlcontrol.game.PCGameType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:55
 */
public class HyriPearlControl extends JavaPlugin {

    public static final String NAME = "PearlControl";

    private static IHyriLanguageManager languageManager;

    private PCConfig config;
    private PCGame game;

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

        final UUID world = IHyrame.WORLD.get().getUID();

        // Default config for all pearl control maps
        /*final PCConfig.GameArea spawnArea = new PCConfig.GameArea(new LocationWrapper(world, 30, 229, -26), new LocationWrapper(world, -19, 187, 20));
        final PCConfig.GameArea gameArea = new PCConfig.GameArea(new LocationWrapper(world, -23, 130, -23), new LocationWrapper(world, -23, 80, 23));
        final PCConfig.GameArea middleArea = new PCConfig.GameArea(new LocationWrapper(world, -2, 102, -2), new LocationWrapper(world, 2, 99, 2));
        final LocationWrapper spawn = new LocationWrapper(world, 0.5, 100, 0.5, -90, 0);
        final LocationWrapper worldSpawn = new LocationWrapper(world, 0.5, 200.5, 0.5, -90, 0);
        final IConfig config = new PCConfig(spawn, spawnArea, worldSpawn, gameArea, middleArea);*/

        this.config = HyriAPI.get().getServer().getConfig(PCConfig.class);
        this.hyrame = HyrameLoader.load(new PCPluginProvider(this));

        languageManager = this.hyrame.getLanguageManager();

        this.game = new PCGame(this.hyrame, this);
        this.hyrame.getGameManager().registerGame(() -> this.game);

        HyriAPI.get().getServer().setState(IHyriServer.State.READY);
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

    public PCGame getGame() {
        return this.game;
    }

    public PCConfig getConfiguration() {
        return this.config;
    }

}

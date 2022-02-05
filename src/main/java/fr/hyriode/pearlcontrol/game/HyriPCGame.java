package fr.hyriode.pearlcontrol.game;

import com.google.gson.Gson;
import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.team.HyriGameTeam;
import fr.hyriode.hyrame.game.util.HyriGameItems;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.config.HyriPCConfig;
import fr.hyriode.pearlcontrol.game.team.EHyriPCGameTeam;
import fr.hyriode.pearlcontrol.game.team.HyriPCGameTeam;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:56
 */
public class HyriPCGame extends HyriGame<HyriPCGamePlayer> {

    private final Location worldSpawn;
    private final Location spawn;

    private final HyriPCConfig config;

    private final HyriPearlControl plugin;

    public HyriPCGame(IHyrame hyrame, HyriPearlControl plugin) {
        super(hyrame, plugin, "pearlcontrol", HyriPearlControl.NAME, HyriPCGamePlayer.class, true);
        this.plugin = plugin;
        this.config = this.plugin.getConfiguration();
        this.worldSpawn = this.config.getWorldSpawn();
        this.spawn = this.config.getSpawn();
        this.minPlayers = 2;
        this.maxPlayers = 8;

        this.registerTeams();
    }

    private void registerTeams() {
        for (EHyriPCGameTeam team : EHyriPCGameTeam.values()) {
            this.registerTeam(new HyriPCGameTeam(team));
        }
    }

    @Override
    public void handleLogin(Player player) {
        super.handleLogin(player);
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setFoodLevel(20);
        player.setHealth(20);
        player.setLevel(0);
        player.setExp(0.0F);
        player.setCanPickupItems(false);

        final UUID uuid = player.getUniqueId();
        final HyriPCGamePlayer gamePlayer = this.getPlayer(uuid);

        gamePlayer.setPlugin(this.plugin);

        HyriGameItems.TEAM_CHOOSER.give(this.hyrame, player, 0);
        HyriGameItems.LEAVE_ITEM.give(this.hyrame, player, 8);

        player.teleport(this.worldSpawn);
    }

    @Override
    public void start() {
        super.start();

        for (HyriPCGamePlayer gamePlayer : this.players) {
            gamePlayer.startGame();
        }
    }

    @Override
    public void win(HyriGameTeam winner) {
        super.win(winner);
    }

    public HyriGameTeam getWinner() {
        final List<HyriPCGamePlayer> withLives = this.players.stream().filter(HyriPCGamePlayer::hasLife).collect(Collectors.toList());

        if (withLives.size() == 1) {
            return withLives.get(0).getTeam();
        }
        return null;
    }

}

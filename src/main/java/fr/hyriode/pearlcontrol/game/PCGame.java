package fr.hyriode.pearlcontrol.game;

import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.protocol.HyriDeathProtocol;
import fr.hyriode.hyrame.game.protocol.HyriLastHitterProtocol;
import fr.hyriode.hyrame.game.protocol.HyriWaitingProtocol;
import fr.hyriode.hyrame.game.team.HyriGameTeam;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.config.PCConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:56
 */
public class PCGame extends HyriGame<PCGamePlayer> {

    private final Location worldSpawn;

    private final PCConfig config;

    private final HyriPearlControl plugin;

    public PCGame(IHyrame hyrame, HyriPearlControl plugin) {
        super(hyrame, plugin, "pearlcontrol", HyriPearlControl.NAME, PCGamePlayer.class);
        this.plugin = plugin;
        this.config = this.plugin.getConfiguration();
        this.worldSpawn = this.config.getWorldSpawn();
        this.minPlayers = 2;
        this.maxPlayers = 8;

        this.registerTeams();
    }

    private void registerTeams() {
        for (PCGameTeam team : PCGameTeam.values()) {
            this.registerTeam(new HyriGameTeam(this, team.getName(), team.getDisplayName(), team.getColor(), 1));
        }
    }

    @Override
    public void handleLogin(Player player) {
        super.handleLogin(player);

        player.teleport(this.worldSpawn);

        final UUID uuid = player.getUniqueId();
        final PCGamePlayer gamePlayer = this.getPlayer(uuid);

        gamePlayer.setPlugin(this.plugin);
    }

    @Override
    public void postRegistration() {
        super.postRegistration();

        this.protocolManager.getProtocol(HyriWaitingProtocol.class).withTeamSelector(false);
    }

    @Override
    public void start() {
        super.start();

        final HyriDeathProtocol.Options.YOptions yOptions = new HyriDeathProtocol.Options.YOptions(this.config.getGameArea().asArea().getMin().getY());

        this.protocolManager.enableProtocol(new HyriLastHitterProtocol(this.hyrame, this.plugin, 8 * 20L));
        this.protocolManager.enableProtocol(new HyriDeathProtocol(this.hyrame, this.plugin, gamePlayer -> {
            final Player player = gamePlayer.getPlayer();

            player.teleport(this.worldSpawn);

            return this.getPlayer(player).kill();
        }, this.createDeathScreen(), HyriDeathProtocol.ScreenHandler.Default.class).withOptions(new HyriDeathProtocol.Options().withYOptions(yOptions).withDeathSound(true)));

        for (PCGamePlayer gamePlayer : this.players) {
            gamePlayer.startGame();
        }
    }

    private HyriDeathProtocol.Screen createDeathScreen() {
        return new HyriDeathProtocol.Screen(3, player -> {
            final PCGamePlayer gamePlayer = this.getPlayer(player.getUniqueId());

            gamePlayer.spawn(true);
            gamePlayer.setInvincibleTask(Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> gamePlayer.setInvincible(false), 3));
        });
    }

    @Override
    public void win(HyriGameTeam winner) {
        super.win(winner);
    }

    public HyriGameTeam getWinner() {
        final List<PCGamePlayer> withLives = this.players.stream().filter(PCGamePlayer::hasLife).collect(Collectors.toList());

        if (withLives.size() == 1) {
            return withLives.get(0).getTeam();
        }
        return null;
    }

}

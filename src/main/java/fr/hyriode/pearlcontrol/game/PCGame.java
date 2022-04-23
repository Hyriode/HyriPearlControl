package fr.hyriode.pearlcontrol.game;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.HyriGamePlayer;
import fr.hyriode.hyrame.game.HyriGameState;
import fr.hyriode.hyrame.game.HyriGameType;
import fr.hyriode.hyrame.game.protocol.HyriDeathProtocol;
import fr.hyriode.hyrame.game.protocol.HyriLastHitterProtocol;
import fr.hyriode.hyrame.game.protocol.HyriWaitingProtocol;
import fr.hyriode.hyrame.game.team.HyriGameTeam;
import fr.hyriode.hyrame.utils.Cuboid;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.api.PCStatistics;
import fr.hyriode.pearlcontrol.config.PCConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

    private boolean captureAllowed;

    private final Location worldSpawn;

    private final PCConfig config;

    private final HyriPearlControl plugin;

    public PCGame(IHyrame hyrame, HyriPearlControl plugin) {
        super(hyrame, plugin, HyriAPI.get().getGameManager().getGameInfo("pearlcontrol"), PCGamePlayer.class, HyriGameType.getFromData(PCGameType.values()));
        this.plugin = plugin;
        this.config = this.plugin.getConfiguration();
        this.worldSpawn = this.config.getWorldSpawn().asBukkit();

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

        final PCConfig.GameArea spawnArea = this.config.getSpawnArea();

        final Cuboid cuboid = new Cuboid(spawnArea.getAreaFirst().asBukkit(), spawnArea.getAreaSecond().asBukkit());

        for (Block block : cuboid.getBlocks()) {
            block.setType(Material.AIR);;
        }

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
    public void handleLogout(Player p) {
        final PCGamePlayer player = this.getPlayer(p.getUniqueId());
        final PCStatistics statistics = player.getStatistics();

        if (this.getState() == HyriGameState.PLAYING) {
            statistics.addDeaths(1);
            statistics.setPlayedTime(player.getStatistics().getPlayedTime() + player.getConnection());
        }

        statistics.update(p.getUniqueId());
        super.handleLogout(p);
    }

    @Override
    public void win(HyriGameTeam winner) {
        super.win(winner);

        for (HyriGamePlayer player : winner.getPlayers()) {
            this.getPlayer(player.getUUID()).getStatistics().addVictories(1);
        }
    }

    @Override
    public void end() {
        for (HyriGamePlayer player : this.players) {
            this.getPlayer(player.getUUID()).getStatistics().addGamesPlayed(1);
        }

        super.end();
    }

    public HyriGameTeam getWinner() {
        final List<PCGamePlayer> withLives = this.players.stream().filter(PCGamePlayer::hasLife).collect(Collectors.toList());

        if (withLives.size() == 1) {
            return withLives.get(0).getTeam();
        }
        return null;
    }

    public boolean isCaptureAllowed() {
        return this.captureAllowed;
    }

    public void setCaptureAllowed(boolean captureAllowed) {
        this.captureAllowed = captureAllowed;
    }

}

package fr.hyriode.pearlcontrol.game;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.language.HyriLanguageMessage;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.HyriGameState;
import fr.hyriode.hyrame.game.HyriGameType;
import fr.hyriode.hyrame.game.protocol.HyriDeathProtocol;
import fr.hyriode.hyrame.game.protocol.HyriLastHitterProtocol;
import fr.hyriode.hyrame.game.protocol.HyriWaitingProtocol;
import fr.hyriode.hyrame.game.team.HyriGameTeam;
import fr.hyriode.hyrame.game.util.HyriGameMessages;
import fr.hyriode.hyrame.game.util.HyriRewardAlgorithm;
import fr.hyriode.hyrame.utils.block.Cuboid;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.api.PCStatistics;
import fr.hyriode.pearlcontrol.config.PCConfig;
import fr.hyriode.pearlcontrol.game.scoreboard.PCScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:56
 */
public class PCGame extends HyriGame<PCGamePlayer> {

    private boolean initialCaptureAllowing;
    private boolean captureAllowed;

    private final PCConfig config;

    private final HyriPearlControl plugin;

    public PCGame(IHyrame hyrame, HyriPearlControl plugin) {
        super(hyrame, plugin, HyriAPI.get().getGameManager().getGameInfo("pearlcontrol"), PCGamePlayer.class, HyriGameType.getFromData(PCGameType.values()));
        this.plugin = plugin;
        this.config = this.plugin.getConfiguration();
        this.description = HyriLanguageMessage.get("message.game.description");
        this.reconnectionTime = 30;
        this.waitingRoom = new PCWaitingRoom(this);

        this.registerTeams();
    }

    private void registerTeams() {
        for (PCGameTeam team : PCGameTeam.values()) {
            this.registerTeam(new HyriGameTeam(this, team.getName(), team.getDisplayName(), team.getColor(), 1));
        }
    }

    @Override
    public void postRegistration() {
        super.postRegistration();

        this.protocolManager.getProtocol(HyriWaitingProtocol.class).withTeamSelector(false);
    }

    @Override
    public void handleLogin(Player player) {
        super.handleLogin(player);

        final UUID uuid = player.getUniqueId();
        final PCGamePlayer gamePlayer = this.getPlayer(uuid);

        gamePlayer.setPlugin(this.plugin);
    }

    @Override
    public void handleReconnection(Player player) {
        super.handleReconnection(player);

        final PCGamePlayer gamePlayer = this.getPlayer(player.getUniqueId());

        if (!gamePlayer.isSpectator()) {
            gamePlayer.removeLife();
            gamePlayer.spawn();
            gamePlayer.showScoreboard();
        }
    }

    @Override
    public void handleLogout(Player p) {
        final PCGamePlayer player = this.getPlayer(p.getUniqueId());
        final PCStatistics statistics = player.getStatistics();
        final PCStatistics.Data statisticsData = player.getStatisticsData();

        player.onLeave();

        if (!this.getState().isAccessible()) {
            statisticsData.setPlayedTime(statisticsData.getPlayedTime() + player.getPlayedTime());
        }

        this.hyrame.getScoreboardManager().getScoreboards(PCScoreboard.class).forEach(PCScoreboard::update);

        if (!HyriAPI.get().getServer().isHost()) {
            statistics.update(p.getUniqueId());
        }

        super.handleLogout(p);

        if (this.getState() == HyriGameState.PLAYING) {
            this.win(this.getWinner());
        }
    }

    @Override
    public void start() {
        final PCConfig.GameArea spawnArea = this.config.getSpawnArea();
        final Cuboid cuboid = new Cuboid(spawnArea.getAreaFirst().asBukkit(), spawnArea.getAreaSecond().asBukkit());

        for (Block block : cuboid.getBlocks()) {
            block.setType(Material.AIR);
        }

        final HyriDeathProtocol.Options.YOptions yOptions = new HyriDeathProtocol.Options.YOptions(this.config.getGameArea().asArea().getMin().getY());

        this.protocolManager.enableProtocol(new HyriLastHitterProtocol(this.hyrame, this.plugin, 8 * 20L));
        this.protocolManager.enableProtocol(new HyriDeathProtocol(this.hyrame, this.plugin, gamePlayer -> {
            final Player player = gamePlayer.getPlayer();

            player.teleport(this.config.getWorldSpawn().asBukkit());

            return this.getPlayer(player).kill();
        }, this.createDeathScreen(), HyriDeathProtocol.ScreenHandler.Default.class).withOptions(new HyriDeathProtocol.Options().withYOptions(yOptions).withDeathSound(true)));

        super.start();

        this.initialCaptureAllowing = PCValues.ALLOWING_CAPTURE.get();

        for (PCGamePlayer gamePlayer : this.players) {
            gamePlayer.startGame();
        }
    }

    private HyriDeathProtocol.Screen createDeathScreen() {
        return new HyriDeathProtocol.Screen(Math.toIntExact(PCValues.RESPAWN_TIME.get()), player -> {
            final PCGamePlayer gamePlayer = this.getPlayer(player.getUniqueId());

            if (gamePlayer == null) {
                return;
            }

            gamePlayer.spawn();
            gamePlayer.setInvincibleTask(Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> gamePlayer.setInvincible(false), 4));
        });
    }

    @Override
    public void win(HyriGameTeam winner) {
        super.win(winner);

        if (winner == null || this.getState() != HyriGameState.ENDED) {
            return;
        }

        for (PCGamePlayer gamePlayer : this.players) {
            if (winner.contains(gamePlayer)) {
                gamePlayer.getStatisticsData().addVictories(1);
            }

            final Player player = gamePlayer.getPlayer();
            final List<String> killsLines = new ArrayList<>();
            final List<PCGamePlayer> topKillers = new ArrayList<>(this.players);

            topKillers.sort((o1, o2) -> (int) (o2.getStatisticsData().getKills() - o1.getStatisticsData().getKills()));

            for (int i = 0; i <= 2; i++) {
                final PCGamePlayer endPlayer = topKillers.size() > i ? topKillers.get(i) : null;
                final String line = HyriLanguageMessage.get("message.game.end.kills").getValue(player).replace("%position%", HyriLanguageMessage.get("message.game.end." + (i + 1)).getValue(player));

                if (endPlayer == null) {
                    killsLines.add(line.replace("%player%", HyriLanguageMessage.get("message.game.end.nobody").getValue(player)).replace("%kills%", "0"));
                    continue;
                }

                final IHyriPlayer account = HyriAPI.get().getPlayerManager().getPlayer(endPlayer.getUniqueId());

                killsLines.add(line.replace("%player%", account.getNameWithRank(true)).replace("%kills%", String.valueOf(endPlayer.getKills())));
            }

            final IHyriPlayer account = gamePlayer.asHyriPlayer();
            final int kills = gamePlayer.getKills();
            final boolean isWinner = winner.contains(gamePlayer);
            final long earnedHyris = HyriRewardAlgorithm.getHyris(kills, gamePlayer.getPlayedTime(), isWinner);
            final long earnedXP = HyriRewardAlgorithm.getXP(kills, gamePlayer.getPlayedTime(), isWinner);
            final List<String> rewards = new ArrayList<>();

            rewards.add(ChatColor.LIGHT_PURPLE + String.valueOf(account.getHyris().add(earnedHyris).withMessage(false).exec()) + " Hyris");
            rewards.add(ChatColor.GREEN + String.valueOf(account.getNetworkLeveling().addExperience(earnedXP)) + " XP");

            account.update();

            player.spigot().sendMessage(HyriGameMessages.createWinMessage(this, player, winner, killsLines, rewards));
        }
    }

    @Override
    public void end() {
        for (PCGamePlayer player : this.players) {
            player.getStatisticsData().addGamesPlayed(1);
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
        return this.initialCaptureAllowing && this.captureAllowed;
    }

    public void setCaptureAllowed(boolean captureAllowed) {
        this.captureAllowed = captureAllowed;
    }

}

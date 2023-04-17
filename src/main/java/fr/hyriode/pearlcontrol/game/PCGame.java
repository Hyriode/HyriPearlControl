package fr.hyriode.pearlcontrol.game;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.HyriEventHandler;
import fr.hyriode.api.language.HyriLanguageMessage;
import fr.hyriode.api.leaderboard.IHyriLeaderboardProvider;
import fr.hyriode.api.leveling.NetworkLeveling;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.HyriGameState;
import fr.hyriode.hyrame.game.HyriGameType;
import fr.hyriode.hyrame.game.event.player.HyriGameReconnectedEvent;
import fr.hyriode.hyrame.game.protocol.HyriAntiSpawnKillProtocol;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
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
        this.waitingRoom = new PCWaitingRoom(this);

        this.registerTeams();
    }

    private void registerTeams() {
        for (PCGameTeam team : PCGameTeam.values()) {
            this.registerTeam(new HyriGameTeam(team.getName(), team.getDisplayName(), team.getColor(), 1));
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
    public void handleLogout(Player p) {
        final PCGamePlayer player = this.getPlayer(p.getUniqueId());
        final PCStatistics statistics = player.getStatistics();
        final PCStatistics.Data statisticsData = player.getStatisticsData();

        player.onLeave();

        if (!this.getState().isAccessible() && !HyriAPI.get().getServer().getAccessibility().equals(HyggServer.Accessibility.HOST)) {
            statisticsData.addGamesPlayed(1);

            statistics.update(p.getUniqueId());
        }

        super.handleLogout(p);

        this.hyrame.getScoreboardManager().getScoreboards(PCScoreboard.class).forEach(PCScoreboard::update);

        if (this.getState() == HyriGameState.PLAYING) {
            this.win(this.getWinner());
        }
    }

    @Override
    public void start() {
        final HyriDeathProtocol.Options.YOptions yOptions = new HyriDeathProtocol.Options.YOptions(this.config.getGameArea().asArea().getMin().getY());

        this.protocolManager.enableProtocol(new HyriAntiSpawnKillProtocol(this.hyrame, new HyriAntiSpawnKillProtocol.Options(3 * 20)));
        this.protocolManager.enableProtocol(new HyriDeathProtocol(this.hyrame, this.plugin, gamePlayer -> {
            final Player player = gamePlayer.getPlayer();

            return this.getPlayer(player).kill();
        }, this.createDeathScreen(), HyriDeathProtocol.ScreenHandler.Default.class).withOptions(new HyriDeathProtocol.Options().withYOptions(yOptions).withDeathSound(true)));
        this.protocolManager.enableProtocol(new HyriLastHitterProtocol(this.hyrame, this.plugin, 8 * 20L));

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
        if (winner == null || this.getState() != HyriGameState.PLAYING) {
            return;
        }

        super.win(winner);

        this.sendWinMessage(winner);
    }

    private void sendWinMessage(HyriGameTeam winner) {
        final List<HyriLanguageMessage> positions = Arrays.asList(
                HyriLanguageMessage.get("message.game.end.1"),
                HyriLanguageMessage.get("message.game.end.2"),
                HyriLanguageMessage.get("message.game.end.3")
        );

        final List<PCGamePlayer> topKillers = new ArrayList<>(this.players);

        topKillers.sort((o1, o2) -> o2.getKills() - o1.getKills());

        final Function<Player, List<String>> killersLineProvider = player -> {
            final List<String> killersLine = new ArrayList<>();

            for (int i = 0; i <= 2; i++) {
                final String killerLine = HyriLanguageMessage.get("message.game.end.kills").getValue(player).replace("%position%", positions.get(i).getValue(player));

                if (topKillers.size() > i){
                    final PCGamePlayer topKiller = topKillers.get(i);

                    killersLine.add(killerLine.replace("%player%", topKiller.formatNameWithTeam()).replace("%kills%", String.valueOf(topKiller.getKills())));
                    continue;
                }

                killersLine.add(killerLine.replace("%player%", HyriLanguageMessage.get("message.game.end.nobody").getValue(player)).replace("%kills%", "0"));
            }

            return killersLine;
        };

        // Send message to not-playing players
        for (Player player : Bukkit.getOnlinePlayers()) {
            final PCGamePlayer gamePlayer = this.getPlayer(player);

            if (gamePlayer == null) {
                player.spigot().sendMessage(HyriGameMessages.createWinMessage(this, player, winner, killersLineProvider.apply(player), null));
            }
        }

        for (PCGamePlayer gamePlayer : this.players) {
            final IHyriPlayer account = gamePlayer.asHyriPlayer();
            final UUID playerId = gamePlayer.getUniqueId();
            final int kills = gamePlayer.getKills();
            final boolean isWinner = winner.contains(gamePlayer);
            final long hyris = account.getHyris().add(HyriRewardAlgorithm.getHyris(kills, gamePlayer.getPlayTime(), isWinner)).withMessage(false).exec();
            final double xp = account.getNetworkLeveling().addExperience(HyriRewardAlgorithm.getXP(kills, gamePlayer.getPlayTime(), isWinner));

            // Update leaderboards
            final IHyriLeaderboardProvider provider = HyriAPI.get().getLeaderboardProvider();

            provider.getLeaderboard(NetworkLeveling.LEADERBOARD_TYPE, "pearlcontrol-experience").incrementScore(playerId, xp);
            provider.getLeaderboard("pearlcontrol", "kills").incrementScore(playerId, kills);

            if (gamePlayer.hasCapturedArea()) {
                provider.getLeaderboard("pearlcontrol", "captured-areas").incrementScore(playerId, 1);
            }

            if (isWinner) {
                provider.getLeaderboard("pearlcontrol", "victories").incrementScore(playerId, 1);
            }

            account.update();

            // Send message
            final String rewardsLine = ChatColor.LIGHT_PURPLE + "+" + hyris + " Hyris " + ChatColor.GREEN + "+" + xp + " XP";

            if (gamePlayer.isOnline()) {
                final Player player = gamePlayer.getPlayer();

                player.spigot().sendMessage(HyriGameMessages.createWinMessage(this, gamePlayer.getPlayer(), winner, killersLineProvider.apply(player), rewardsLine));
            } else if (HyriAPI.get().getPlayerManager().isOnline(playerId)) {
                HyriAPI.get().getPlayerManager().sendMessage(playerId, HyriGameMessages.createOfflineWinMessage(this, account, rewardsLine));
            }
        }
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

package fr.hyriode.pearlcontrol.game;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hyrame.actionbar.ActionBar;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.HyriGamePlayer;
import fr.hyriode.hyrame.game.protocol.HyriLastHitterProtocol;
import fr.hyriode.hyrame.item.ItemBuilder;
import fr.hyriode.hyrame.language.HyriLanguageMessage;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.api.PCStatistics;
import fr.hyriode.pearlcontrol.game.scoreboard.PCScoreboard;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:57
 */
public class PCGamePlayer extends HyriGamePlayer {

    private double knockbackPercentage = 1.0D;

    private final PCStatistics statistics;

    private PCScoreboard scoreboard;

    private BukkitTask captureTask;
    private int captureIndex;

    private BukkitTask invincibleTask;
    private boolean invincible = false;

    private long connection;

    private int lives = 3;

    private HyriPearlControl plugin;

    private PCGame game;

    public PCGamePlayer(HyriGame<?> game, Player player) {
        super(game, player);
        this.game = (PCGame) game;
        this.statistics = PCStatistics.get(player.getUniqueId());
    }

    public IHyriPlayer asHyriode() {
        return HyriAPI.get().getPlayerManager().getPlayer(this.player.getUniqueId());
    }

    void startGame() {
        this.connection = System.currentTimeMillis();

        this.spawn();

        this.scoreboard = new PCScoreboard(this.plugin, this.plugin.getGame(), this.player);
        this.scoreboard.show();
    }

    public void spawn() {
        final Location spawn = this.plugin.getConfiguration().getSpawn().asBukkit().clone();

        this.player.setGameMode(GameMode.ADVENTURE);
        this.player.teleport(spawn);
        this.player.getInventory().addItem(this.createEnderPearl(16));
    }

    private ItemStack createEnderPearl(int amount) {
        return new ItemBuilder(Material.ENDER_PEARL, amount)
                .withName(ChatColor.GREEN + "Ender Pearl")
                .build();
    }

    public boolean kill() {
        final PCGame game = this.plugin.getGame();
        final HyriLastHitterProtocol.LastHitter lastHitter = this.getLastHitter();

        this.setInvincible(true);
        this.lives--;

        if (lastHitter != null) {
            lastHitter.asPlayer().getInventory().addItem(this.createEnderPearl(4));

            final PCGamePlayer killer = (PCGamePlayer) lastHitter.asGamePlayer();

            if (this.hasLife()) {
                killer.getStatistics().addKills(1);
            } else {
                killer.getStatistics().addFinalKills(1);
            }
        }

        if (this.lives == 1) {
            this.game.sendMessageToAll(HyriLanguageMessage.get("message.life-remaining"));
        } else if (this.lives != 0){
            this.game.sendMessageToAll(target -> HyriLanguageMessage.get("message.lives-remaining").getForPlayer(target).replace("%lives%", String.valueOf(this.lives)));
        }

        this.statistics.addDeaths(1);

        this.knockbackPercentage = 1.0D;

        for (PCGamePlayer gamePlayer : this.game.getPlayers()) {
            gamePlayer.getScoreboard().update();
        }

        if (this.hasLife()) {
            return true;
        }

        game.win(game.getWinner());
        return false;
    }

    public HyriLastHitterProtocol.LastHitter getLastHitter() {
        final List<HyriLastHitterProtocol.LastHitter> lastHitters = this.game.getProtocolManager().getProtocol(HyriLastHitterProtocol.class).getLastHitters(this.player);

        if (lastHitters != null) {
            return lastHitters.get(0);
        }
        return null;
    }

    void setPlugin(HyriPearlControl plugin) {
        this.plugin = plugin;
    }

    public int getLives() {
        return this.lives;
    }

    public boolean hasLife() {
        return this.lives > 0;
    }

    public boolean isInvincible() {
        return this.invincible;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;

        if (invincible && this.invincibleTask != null) {
            this.invincibleTask.cancel();
        }
    }

    void setInvincibleTask(BukkitTask invincibleTask) {
        this.invincibleTask = invincibleTask;
    }

    public double getKnockbackPercentage() {
        return this.knockbackPercentage;
    }

    public void addKnockbackPercentage() {
        if (this.knockbackPercentage < 800) {
            this.knockbackPercentage += this.game.getType() == PCGameType.CHAOS ? 9.5D : 6.5D;

            this.scoreboard.update();
        }
    }

    public void onEnterCapture() {
        if (this.captureTask == null) {
            for (PCGamePlayer gamePlayer : this.game.getPlayers()) {
                if (gamePlayer.isInMiddleArea() && gamePlayer != this) {
                    this.game.setCaptureAllowed(false);
                }
            }

            this.captureTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
                new ActionBar(ChatColor.GREEN + "" + (this.captureIndex * 10) + "%").send(this.player);

                if (this.game.isCaptureAllowed()) {
                    this.captureIndex++;

                    if (this.captureIndex == 10) {
                        // Fin de la game, le joueur est resté 10 secondes
                        this.game.sendMessageToAll(target -> HyriLanguageMessage.get("message.zone-captured").getForPlayer(target).replace("%player%", this.formatNameWithTeam()));
                        this.statistics.addCapturedAreas(1);

                        this.captureTask.cancel();

                        this.game.win(this.team);
                    }
                }
            }, 20, 20);
        }
    }

    public void onLeaveCapture() {
        if (this.captureTask != null) {
            this.captureTask.cancel();
            this.captureTask = null;
            this.captureIndex = 0;
        }

        this.game.setCaptureAllowed(true);

        int amount = 0;

        for (PCGamePlayer gamePlayer : this.game.getPlayers()) {
            if (gamePlayer.isInMiddleArea()) {
                amount++;
            }
        }

        this.game.setCaptureAllowed(amount <= 1);
    }

    public boolean isInMiddleArea() {
        return this.captureTask != null;
    }

    public PCScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public PCStatistics getStatistics() {
        return this.statistics;
    }

    public long getConnection() {
        return this.connection;
    }

}

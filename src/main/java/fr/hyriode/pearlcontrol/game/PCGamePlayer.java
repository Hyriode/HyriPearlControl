package fr.hyriode.pearlcontrol.game;

import fr.hyriode.hyrame.actionbar.ActionBar;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.HyriGamePlayer;
import fr.hyriode.hyrame.game.protocol.HyriLastHitterProtocol;
import fr.hyriode.hyrame.item.ItemBuilder;
import fr.hyriode.api.language.HyriLanguageMessage;
import fr.hyriode.hyrame.utils.ThreadUtil;
import fr.hyriode.hyrame.utils.VoidPlayer;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.api.PCStatistics;
import fr.hyriode.pearlcontrol.game.scoreboard.PCScoreboard;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:57
 */
public class PCGamePlayer extends HyriGamePlayer {

    private double knockbackPercentage = 1.0D;

    private final PCStatistics statistics;
    private final PCStatistics.Data statisticsData;

    private PCScoreboard scoreboard;

    private BukkitTask captureTask;
    private int captureIndex;

    private BukkitTask invincibleTask;
    private boolean invincible = false;

    private final List<EnderPearl> pearls;

    private int lives;
    private int kills;

    private HyriPearlControl plugin;

    private final PCGame game;

    public PCGamePlayer(HyriGame<?> game, Player player) {
        super(game, player);
        this.game = (PCGame) game;
        this.statistics = PCStatistics.get(player.getUniqueId());
        this.statisticsData = this.statistics.getData((PCGameType) this.game.getType());
        this.pearls = new ArrayList<>();
    }

    void startGame() {
        this.lives = PCValues.LIVES.get();
        this.spawn();
        this.showScoreboard();
    }

    public void showScoreboard() {
        this.scoreboard = new PCScoreboard(this.plugin, this.plugin.getGame(), this.player);
        this.scoreboard.show();
    }

    public void spawn() {
        if (this.player instanceof VoidPlayer) {
            return;
        }

        final Location spawn = this.plugin.getConfiguration().getSpawn().asBukkit().clone();

        this.player.setGameMode(GameMode.ADVENTURE);
        this.player.teleport(spawn);
        this.player.getInventory().addItem(this.createEnderPearl(PCValues.SPAWN_ENDER_PEARLS.get()));
    }

    private ItemStack createEnderPearl(int amount) {
        return new ItemBuilder(Material.ENDER_PEARL, amount)
                .withName(ChatColor.GREEN + "Ender Pearl")
                .build();
    }

    public boolean kill() {
        final PCGame game = this.plugin.getGame();
        final HyriLastHitterProtocol.LastHitter lastHitter = this.getLastHitter();

        for (EnderPearl pearl : this.pearls) {
            if (pearl != null) {
                pearl.remove();
            }
        }

        this.setInvincible(true);
        this.lives--;

        if (lastHitter != null) {
            final PCGamePlayer killer = (PCGamePlayer) lastHitter.asGamePlayer();

            if (!killer.isSpectator() && !killer.isDead()) {
                lastHitter.asPlayer().getInventory().addItem(this.createEnderPearl(PCValues.KILL_ENDER_PEARLS.get()));
            }

            if (this.hasLife()) {
                killer.kills++;
                killer.getStatisticsData().addKills(1);
            } else {
                killer.getStatisticsData().addFinalKills(1);
            }
        }

        if (this.lives == 1) {
            this.game.sendMessageToAll(HyriLanguageMessage.get("message.life-remaining"));
        } else if (this.lives != 0){
            this.game.sendMessageToAll(target -> HyriLanguageMessage.get("message.lives-remaining").getValue(target).replace("%lives%", String.valueOf(this.lives)));
        }

        this.statisticsData.addDeaths(1);

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

    public void onLeave() {
        if (this.captureTask != null) {
            this.captureTask.cancel();
            this.captureTask = null;
            this.captureIndex = 0;
        }
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
        return this.lives > 0 && this.isOnline();
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
            this.knockbackPercentage += (this.game.getType() == PCGameType.CHAOS ? 9.5D : 6.5D) * PCValues.KNOCKBACK_MULTIPLIER.get();

            if (this.knockbackPercentage > 800) {
                this.knockbackPercentage = 800;
            }

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
                if (this.game.isCaptureAllowed()) {
                    new ActionBar(HyriLanguageMessage.get("action-bar.capture.display")
                            .getValue(this.player).replace("%percentage%", String.valueOf((int) ((double) this.captureIndex / PCValues.CAPTURE_TIME.get() * 100))))
                            .send(this.player);

                    if  (this.captureIndex == 1) {
                        this.game.sendMessageToAll(target -> HyriLanguageMessage.get("message.zone-enter").getValue(target).replace("%player%", this.formatNameWithTeam()));
                    }

                    if (this.captureIndex >= PCValues.CAPTURE_TIME.get()) {
                        this.game.sendMessageToAll(target -> HyriLanguageMessage.get("message.zone-captured").getValue(target).replace("%player%", this.formatNameWithTeam()));
                        this.statisticsData.addCapturedAreas(1);
                        this.captureTask.cancel();

                        ThreadUtil.backOnMainThread(this.plugin, () -> this.game.win(this.team));
                    }

                    this.captureIndex++;
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

    public void removeLife() {
        this.lives--;
    }

    public int getKills() {
        return this.kills;
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

    public PCStatistics.Data getStatisticsData() {
        return this.statisticsData;
    }

    public void addPearl(EnderPearl pearl) {
        this.pearls.add(pearl);
    }

    public List<EnderPearl> getPearls() {
        return this.pearls;
    }

}

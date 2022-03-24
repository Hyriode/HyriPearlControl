package fr.hyriode.pearlcontrol.game;

import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.HyriGamePlayer;
import fr.hyriode.hyrame.game.protocol.HyriLastHitterProtocol;
import fr.hyriode.hyrame.item.ItemBuilder;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.scoreboard.PCScoreboard;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Function;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:57
 */
public class PCGamePlayer extends HyriGamePlayer {

    private double knockbackPercentage = 1.0D;

    private PCScoreboard scoreboard;

    private BukkitTask invincibleTask;
    private boolean invincible = false;

    private int lives = 3;

    private HyriPearlControl plugin;

    public PCGamePlayer(HyriGame<?> game, Player player) {
        super(game, player);
    }

    void startGame() {
        this.spawn(true);

        this.scoreboard = new PCScoreboard(this.plugin, this.plugin.getGame(), this.player);
        this.scoreboard.show();
    }

    public void spawn() {
        this.spawn(false);
    }

    public void spawn(boolean inAir) {
        final Location spawn = this.plugin.getConfiguration().getSpawn();

        this.player.setGameMode(GameMode.ADVENTURE);
        this.player.teleport(inAir ? spawn.clone().add(0.0D, 5.0D, 0.0D) : spawn);
        this.player.getInventory().addItem(this.createEnderPearl(16));
    }

    private ItemStack createEnderPearl(int amount) {
        return new ItemBuilder(Material.ENDER_PEARL, amount)
                .withName(ChatColor.GREEN + "Ender Pearl")
                .build();
    }

    public boolean kill() {
        final PCGame game = this.plugin.getGame();
        final Player lastHitter = this.getLastHitter();
        final Function<Player, String> messageKillEnd = target -> {
            String result;
            if (this.lives == 1) {
                result = HyriPearlControl.getLanguageManager().getValue(target, "message.life-remaining");
            } else if (this.lives == 0) {
                result = HyriPearlControl.getLanguageManager().getValue(target, "message.eliminated");
            } else {
                result = HyriPearlControl.getLanguageManager().getValue(target, "message.lives-remaining").replace("%lives%", String.valueOf(this.lives));
            }
            return result;
        };

        this.setInvincible(true);
        this.lives--;

        if (lastHitter != null) {
            final HyriGamePlayer lastHitterGamePlayer = this.game.getPlayer(lastHitter.getUniqueId());

            this.game.sendMessageToAll(target -> {
                final String killedByPlayer = HyriPearlControl.getLanguageManager().getValue(target, "message.killed-by-player")
                        .replace("%player%", this.team.formatName(this.player));

                return killedByPlayer.replace("%killer%", lastHitterGamePlayer.getTeam().formatName(lastHitter)) + " " + messageKillEnd.apply(target);
            });

            lastHitter.getInventory().addItem(this.createEnderPearl(4));

            this.game.getProtocolManager().getProtocol(HyriLastHitterProtocol.class).removeLastHitters(this.player);
        } else {
            this.game.sendMessageToAll(target -> {
                final String killed = HyriPearlControl.getLanguageManager().getValue(target, "message.kill")
                        .replace("%player%", this.team.formatName(this.player));

                return killed + " " + messageKillEnd.apply(target);
            });
        }

        this.knockbackPercentage = 1.0D;

        if (this.hasLife()) {
            return true;
        }

        game.win(game.getWinner());
        return false;
    }

    public Player getLastHitter() {
        final List<HyriLastHitterProtocol.LastHitter> lastHitters = this.game.getProtocolManager().getProtocol(HyriLastHitterProtocol.class).getLastHitters(this.player);

        if (lastHitters != null) {
            return lastHitters.get(0).getPlayer();
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
            this.knockbackPercentage += 6.5D;

            this.scoreboard.update();
        }
    }

    public PCScoreboard getScoreboard() {
        return this.scoreboard;
    }

}

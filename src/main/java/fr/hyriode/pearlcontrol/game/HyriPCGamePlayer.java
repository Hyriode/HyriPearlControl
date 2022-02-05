package fr.hyriode.pearlcontrol.game;

import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.HyriGamePlayer;
import fr.hyriode.hyrame.game.util.HyriDeadScreen;
import fr.hyriode.hyrame.item.ItemBuilder;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.scoreboard.HyriPCScoreboard;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.function.Function;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 20:57
 */
public class HyriPCGamePlayer extends HyriGamePlayer {

    private double knockbackPercentage = 1.0D;

    private HyriPCScoreboard scoreboard;

    private Player lastHitter;

    private boolean invincible = false;

    private int lives = 3;

    private HyriPearlControl plugin;

    public HyriPCGamePlayer(HyriGame<?> game, Player player) {
        super(game, player);
    }

    void startGame() {
        this.spawn(true);

        this.scoreboard = new HyriPCScoreboard(this.plugin, this.player);
        this.scoreboard.show();
    }

    public void spawn() {
        this.spawn(false);
    }

    public void spawn(boolean inAir) {
        final Location spawn = this.plugin.getConfiguration().getSpawn();


        this.player.setGameMode(GameMode.SURVIVAL);
        this.player.teleport(inAir ? spawn.clone().add(0.0D, 5.0D, 0.0D) : spawn);
        this.player.getInventory().addItem(this.createEnderPearl(16));
    }

    private ItemStack createEnderPearl(int amount) {
        return new ItemBuilder(Material.ENDER_PEARL, amount)
                .withName(ChatColor.GREEN + "Ender Pearl")
                .build();
    }

    public void kill() {
        final HyriPCGame game = this.plugin.getGame();
        final PlayerInventory playerInventory = this.player.getInventory();
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

        this.hide();

        playerInventory.setArmorContents(null);
        playerInventory.clear();

        this.player.setHealth(20.0F);
        this.player.setGameMode(GameMode.SPECTATOR);
        this.player.setVelocity(new Vector(0,0,0));
        this.player.teleport(this.plugin.getConfiguration().getWorldSpawn());

        this.invincible = true;
        this.lives--;

        if (this.lastHitter != null) {
            final HyriGamePlayer lastHitterGamePlayer = this.game.getPlayer(this.lastHitter.getUniqueId());

            this.game.sendMessageToAll(target -> {
                final String killedByPlayer = HyriPearlControl.getLanguageManager().getValue(target, "message.killed-by-player")
                        .replace("%player%", this.team.formatName(this.player));

                return killedByPlayer.replace("%killer%", lastHitterGamePlayer.getTeam().formatName(this.lastHitter)) + " " + messageKillEnd.apply(target);
            });

            this.lastHitter.getInventory().addItem(this.createEnderPearl(1));
        } else {
            this.game.sendMessageToAll(target -> {
                final String killed = HyriPearlControl.getLanguageManager().getValue(target, "message.kill")
                        .replace("%player%", this.team.formatName(this.player));

                return killed + " " + messageKillEnd.apply(target);
            });
        }

        this.knockbackPercentage = 1.0D;

        if (this.hasLife()) {
            this.dead = true;

            HyriDeadScreen.create(this.plugin, this.player, 3, () -> {
                this.dead = false;

                this.show();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        invincible = false;
                    }
                }.runTaskLaterAsynchronously(this.plugin, 3 * 20L);

                this.spawn(true);
            });
        } else {
            this.eliminated = true;
            this.spectator = true;

            this.player.setGameMode(GameMode.ADVENTURE);
            this.player.setAllowFlight(true);
            this.player.setFlying(true);

            // TODO Give spectating objects

            game.win(game.getWinner());
        }

        this.scoreboard.update();
        this.lastHitter = null;
    }

    void setPlugin(HyriPearlControl plugin) {
        this.plugin = plugin;
    }

    public void setLastHitter(Player player) {
        this.lastHitter = player;

        new BukkitRunnable() {
            @Override
            public void run() {
                lastHitter = null;
            }
        }.runTaskLaterAsynchronously(this.plugin, 10 * 20L);
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
    }

    public double getKnockbackPercentage() {
        return this.knockbackPercentage;
    }

    public void addKnockbackPercentage() {
        if (this.knockbackPercentage < 800) {
            this.knockbackPercentage += 6.5D * (1 + (this.knockbackPercentage / 100));

            this.scoreboard.update();
        }
    }

}

package fr.hyriode.pearlcontrol.listener;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.HyriEventHandler;
import fr.hyriode.api.language.HyriLanguageMessage;
import fr.hyriode.hyrame.game.HyriGamePlayer;
import fr.hyriode.hyrame.game.HyriGameSpectator;
import fr.hyriode.hyrame.game.HyriGameState;
import fr.hyriode.hyrame.game.event.player.HyriGameDeathEvent;
import fr.hyriode.hyrame.game.event.player.HyriGameSpectatorEvent;
import fr.hyriode.hyrame.listener.HyriListener;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.PCGame;
import fr.hyriode.pearlcontrol.game.PCGamePlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 05/02/2022 at 09:54
 */
public class PlayerListener extends HyriListener<HyriPearlControl> {

    public PlayerListener(HyriPearlControl plugin) {
        super(plugin);

        HyriAPI.get().getEventBus().register(this);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();

        if (block != null) {
            if (block.getState() instanceof InventoryHolder) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        final Projectile entity = event.getEntity();

        if (entity instanceof EnderPearl) {
            final Entity shooter = (Entity) entity.getShooter();

            if (shooter instanceof Player) {
                final Player player = (Player) shooter;
                final PCGamePlayer gamePlayer = this.plugin.getGame().getPlayer(player);

                if (gamePlayer == null) {
                    return;
                }

                gamePlayer.addPearl((EnderPearl) entity);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }

        if (event.getEntity() instanceof Player) {
            event.setDamage(0.0D);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (this.plugin.getGame().getState() != HyriGameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            final PCGame game = this.plugin.getGame();
            final Player damageDealer = (Player) event.getDamager();
            final Player target = (Player) event.getEntity();
            final PCGamePlayer damageDealerGamePlayer = game.getPlayer(damageDealer.getUniqueId());
            final PCGamePlayer targetGamePlayer = game.getPlayer(target.getUniqueId());

            if (damageDealerGamePlayer == null || targetGamePlayer == null) {
                return;
            }

            if (!damageDealerGamePlayer.isSpectator()) {
                event.setDamage(0.0D);

                if (targetGamePlayer.isInvincible()) {
                    event.setCancelled(true);
                    return;
                } else if (damageDealerGamePlayer.isInvincible()) {
                    damageDealerGamePlayer.setInvincible(false);
                }

                targetGamePlayer.addKnockbackPercentage();

                target.setVelocity(damageDealer.getLocation().getDirection().normalize().multiply(targetGamePlayer.getKnockbackPercentage() / 100));
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        final PCGame game = this.plugin.getGame();
        final Player player = event.getPlayer();

        if (game.getState() == HyriGameState.PLAYING) {
            final PCGamePlayer gamePlayer = game.getPlayer(player.getUniqueId());

            if (gamePlayer == null) {
                return;
            }

            if (gamePlayer.isDead() || gamePlayer.isSpectator()) {
                return;
            }

            if (this.plugin.getConfiguration().getMiddleArea().asArea().isInArea(event.getTo())) {
                gamePlayer.onEnterCapture();
            } else {
                gamePlayer.onLeaveCapture();
            }
        }
    }

    @HyriEventHandler
    public void onSpectator(HyriGameSpectatorEvent event) {
        final PCGame game = event.getGame().cast();
        final HyriGameSpectator spectator = event.getSpectator();

        if (!(spectator instanceof HyriGamePlayer)) {
            event.getSpectator().getPlayer().teleport(this.plugin.getGame().getWaitingRoom().getConfig().getSpawn().asBukkit());
        } else {
            game.win(game.getWinner());
        }
    }

    @HyriEventHandler
    public void onDeath(HyriGameDeathEvent event) {
        final PCGamePlayer gamePlayer = (PCGamePlayer) event.getGamePlayer();

        if (gamePlayer.getLives() - 1 <= 0) {
            event.getMessagesToAdd().add(HyriLanguageMessage.get("message.eliminated"));
        }
    }

}

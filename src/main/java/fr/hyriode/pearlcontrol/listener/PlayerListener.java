package fr.hyriode.pearlcontrol.listener;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.HyriEventHandler;
import fr.hyriode.hyrame.game.HyriGameState;
import fr.hyriode.hyrame.game.event.player.HyriGameSpectatorEvent;
import fr.hyriode.hyrame.listener.HyriListener;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.PCGame;
import fr.hyriode.pearlcontrol.game.PCGamePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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
        final Location location = event.getTo();

        if (this.plugin.getConfiguration().getGameArea().asArea().getMin().getY() >= location.getY()) {
            if (game.getState() != HyriGameState.PLAYING) {
                player.teleport(this.plugin.getConfiguration().getWorldSpawn());
            }
        }
    }

    @HyriEventHandler
    public void onSpectator(HyriGameSpectatorEvent event) {
        event.getGamePlayer().getPlayer().teleport(this.plugin.getConfiguration().getWorldSpawn());
    }

}

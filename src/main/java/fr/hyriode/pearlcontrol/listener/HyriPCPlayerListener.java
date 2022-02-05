package fr.hyriode.pearlcontrol.listener;

import fr.hyriode.hyrame.game.HyriGameState;
import fr.hyriode.hyrame.listener.HyriListener;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.HyriPCGame;
import fr.hyriode.pearlcontrol.game.HyriPCGamePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 05/02/2022 at 09:54
 */
public class HyriPCPlayerListener extends HyriListener<HyriPearlControl> {

    public HyriPCPlayerListener(HyriPearlControl plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (this.plugin.getGame().getState() != HyriGameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            final HyriPCGame game = this.plugin.getGame();
            final Player damageDealer = (Player) event.getDamager();
            final Player target = (Player) event.getEntity();
            final HyriPCGamePlayer damageDealerGamePlayer = game.getPlayer(damageDealer.getUniqueId());
            final HyriPCGamePlayer targetGamePlayer = game.getPlayer(target.getUniqueId());

            if (targetGamePlayer.isInvincible()) {
                event.setCancelled(true);
                return;
            } else if (damageDealerGamePlayer.isInvincible()) {
                damageDealerGamePlayer.setInvincible(false);
            }

            targetGamePlayer.addKnockbackPercentage();
            targetGamePlayer.setLastHitter(damageDealer);

            target.setVelocity(damageDealer.getLocation().getDirection().normalize().multiply(targetGamePlayer.getKnockbackPercentage() / 100));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (this.plugin.getGame().getState() != HyriGameState.PLAYING) {
            event.setCancelled(true);
        }
        event.setDamage(0.0D);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        final HyriPCGame game = this.plugin.getGame();
        final Player player = event.getPlayer();
        final Location location = event.getTo();

        if (this.plugin.getConfiguration().getGameArea().asArea().getMin().getY() >= location.getY()) {
            if (game.getState() == HyriGameState.PLAYING) {
                this.plugin.getGame().getPlayer(player.getUniqueId()).kill();
            } else {
                player.teleport(this.plugin.getConfiguration().getWorldSpawn());
            }
        }
    }

}

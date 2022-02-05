package fr.hyriode.pearlcontrol.listener;

import fr.hyriode.hyrame.listener.HyriListener;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 05/02/2022 at 09:54
 */
public class HyriPCWorldListener extends HyriListener<HyriPearlControl> {

    public HyriPCWorldListener(HyriPearlControl plugin) {
        super(plugin);
    }

    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

}

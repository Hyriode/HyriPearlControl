package fr.hyriode.pearlcontrol.game.host.gui;

import fr.hyriode.hyrame.host.HostCategory;
import fr.hyriode.hyrame.host.gui.HostGUI;
import org.bukkit.entity.Player;

/**
 * Created by AstFaster
 * on 17/08/2022 at 10:36
 */
public class PCMainHostGUI extends HostGUI {

    public PCMainHostGUI(Player owner, HostCategory category) {
        super(owner, name(owner, "gui.host.pc-main.name"), category);
    }

}

package fr.hyriode.pearlcontrol.game.host.category;

import fr.hyriode.hyrame.host.HostCategory;
import fr.hyriode.hyrame.host.option.BooleanOption;
import fr.hyriode.hyrame.host.option.IntegerOption;
import fr.hyriode.hyrame.host.option.TimeOption;
import fr.hyriode.hyrame.item.ItemBuilder;
import fr.hyriode.pearlcontrol.game.host.gui.PCMainHostGUI;
import fr.hyriode.pearlcontrol.util.PCDisplay;
import fr.hyriode.pearlcontrol.util.PCHead;
import org.bukkit.Material;

/**
 * Created by AstFaster
 * on 15/08/2022 at 23:17
 */
public class PCHostMainCategory extends HostCategory {

    public PCHostMainCategory() {
        super(PCDisplay.categoryDisplay("main", Material.ENDER_PEARL));
        this.guiProvider = player -> new PCMainHostGUI(player, this);

        this.addOption(20, new IntegerOption(PCDisplay.optionDisplay("spawn-ender-pearls", Material.ENDER_PEARL), 16, 0, 64 * 36));
        this.addOption(21, new IntegerOption(PCDisplay.optionDisplay("kill-ender-pearls", Material.IRON_SWORD), 4, 0, 64 * 36));
        this.addOption(22, new BooleanOption(PCDisplay.optionDisplay("allowing-capture", Material.BEACON), true));
        this.addOption(23, new IntegerOption(PCDisplay.optionDisplay("capture-time", Material.WATCH), 10, 1, Integer.MAX_VALUE));
        this.addOption(24, new IntegerOption(PCDisplay.optionDisplay("knockback-multiplier", Material.FEATHER), 1, 0, 10));
        this.addOption(30, new IntegerOption(PCDisplay.optionDisplay("lives", Material.GOLDEN_APPLE), 3, 1, Integer.MAX_VALUE));
        this.addOption(32, new TimeOption(PCDisplay.optionDisplay("respawn-time", ItemBuilder.asHead(PCHead.DEATH).build()), 3L, 0L, 60L, new long[] {1, 3}));
    }

}

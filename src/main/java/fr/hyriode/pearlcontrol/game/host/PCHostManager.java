package fr.hyriode.pearlcontrol.game.host;

import fr.hyriode.hyrame.host.IHostController;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.host.category.PCHostMainCategory;

/**
 * Created by AstFaster
 * on 15/08/2022 at 23:16
 */
public class PCHostManager {

    private final IHostController controller;

    public PCHostManager() {
        this.controller = HyriPearlControl.get().getHyrame().getHostController();
    }

    public void attach() {
        this.controller.addCategory(25, new PCHostMainCategory());
    }

}

package fr.hyriode.pearlcontrol.config;

import fr.hyriode.api.config.IHyriConfig;
import fr.hyriode.hyrame.game.waitingroom.HyriWaitingRoom;
import fr.hyriode.hyrame.utils.AreaWrapper;
import fr.hyriode.hyrame.utils.LocationWrapper;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 22/04/2022 at 16:19
 */
public class PCConfig implements IHyriConfig {

    private final HyriWaitingRoom.Config waitingRoom;

    private final LocationWrapper spawn;
    private final AreaWrapper gameArea;

    /** The area where the player can win the game in the middle of the map */
    private final AreaWrapper middleArea;

    public PCConfig(HyriWaitingRoom.Config waitingRoom, LocationWrapper spawn, AreaWrapper gameArea, AreaWrapper middleArea) {
        this.waitingRoom = waitingRoom;
        this.spawn = spawn;
        this.gameArea = gameArea;
        this.middleArea = middleArea;
    }

    public HyriWaitingRoom.Config getWaitingRoom() {
        return this.waitingRoom;
    }

    public LocationWrapper getSpawn() {
        return this.spawn;
    }

    public AreaWrapper getGameArea() {
        return this.gameArea;
    }

    public AreaWrapper getMiddleArea() {
        return this.middleArea;
    }

}

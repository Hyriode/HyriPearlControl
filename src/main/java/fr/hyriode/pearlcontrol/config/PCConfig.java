package fr.hyriode.pearlcontrol.config;

import fr.hyriode.hyrame.utils.Area;
import fr.hyriode.hyrame.utils.LocationWrapper;
import fr.hyriode.hystia.api.config.IConfig;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 22/04/2022 at 16:19
 */
public class PCConfig implements IConfig {

    private final LocationWrapper spawn;
    private final GameArea spawnArea;
    private final LocationWrapper worldSpawn;
    private final GameArea gameArea;

    /** The area where the player can win the game in the middle of the map */
    private final GameArea middleArea;

    public PCConfig(LocationWrapper spawn, GameArea spawnArea, LocationWrapper worldSpawn, GameArea gameArea, GameArea middleArea) {
        this.spawn = spawn;
        this.spawnArea = spawnArea;
        this.worldSpawn = worldSpawn;
        this.gameArea = gameArea;
        this.middleArea = middleArea;
    }

    public LocationWrapper getSpawn() {
        return this.spawn;
    }

    public GameArea getSpawnArea() {
        return this.spawnArea;
    }

    public LocationWrapper getWorldSpawn() {
        return this.worldSpawn;
    }

    public GameArea getGameArea() {
        return this.gameArea;
    }

    public GameArea getMiddleArea() {
        return this.middleArea;
    }

    public static class GameArea {

        private final LocationWrapper areaFirst;
        private final LocationWrapper areaSecond;

        public GameArea(LocationWrapper areaFirst, LocationWrapper areaSecond) {
            this.areaFirst = areaFirst;
            this.areaSecond = areaSecond;
        }

        public LocationWrapper getAreaFirst() {
            return this.areaFirst;
        }

        public LocationWrapper getAreaSecond() {
            return this.areaSecond;
        }

        public Area asArea() {
            return new Area(this.areaFirst.asBukkit(), this.areaSecond.asBukkit());
        }

    }

}

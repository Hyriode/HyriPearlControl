package fr.hyriode.pearlcontrol.config;

import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.configuration.IHyriConfiguration;
import fr.hyriode.hyrame.utils.Area;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;

import static fr.hyriode.hyrame.configuration.HyriConfigurationEntry.LocationEntry;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 21:06
 */
public class HyriPCConfig implements IHyriConfiguration {

    private static final Supplier<Location> DEFAULT_LOCATION = () -> new Location(IHyrame.WORLD.get(), 0, 0, 0, 0, 0);

    private Location spawn;
    private final LocationEntry spawnEntry;

    private Location worldSpawn;
    private final LocationEntry worldSpawnEntry;

    private final GameArea gameArea;

    private final FileConfiguration config;
    private final JavaPlugin plugin;

    public HyriPCConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        this.spawn = DEFAULT_LOCATION.get();
        this.spawnEntry = new LocationEntry("spawn", this.config);
        this.worldSpawn = DEFAULT_LOCATION.get();
        this.worldSpawnEntry = new LocationEntry("world-spawn", config);
        this.gameArea = new GameArea();
    }

    @Override
    public void create() {
        this.spawnEntry.setDefault(this.spawn);
        this.worldSpawnEntry.setDefault(this.worldSpawn);

        this.gameArea.create();

        this.plugin.saveConfig();
    }

    @Override
    public void load() {
        HyriPearlControl.log("Loading configuration...");

        this.spawn = this.spawnEntry.get();
        this.worldSpawn = this.worldSpawnEntry.get();

        this.gameArea.load();
    }

    @Override
    public void save() {
        HyriPearlControl.log("Saving configuration...");

        this.spawnEntry.set(this.spawn);
        this.worldSpawnEntry.set(this.worldSpawn);

        this.gameArea.save();

        this.plugin.saveConfig();
    }

    @Override
    public FileConfiguration getConfig() {
        return this.config;
    }

    public Location getSpawn() {
        return this.spawn;
    }

    public Location getWorldSpawn() {
        return this.worldSpawn;
    }

    public GameArea getGameArea() {
        return this.gameArea;
    }

    public class GameArea implements IHyriConfiguration {

        private Location areaFirst;
        private final LocationEntry areaFirstEntry;
        private Location areaSecond;
        private final LocationEntry areaSecondEntry;

        public GameArea() {
            final String key = "area.";

            this.areaFirst = DEFAULT_LOCATION.get();
            this.areaFirstEntry = new LocationEntry(key + "first", config);
            this.areaSecond = DEFAULT_LOCATION.get();
            this.areaSecondEntry = new LocationEntry(key + "second", config);
        }

        @Override
        public void create() {
            this.areaFirstEntry.setDefault(this.areaFirst);
            this.areaSecondEntry.setDefault(this.areaSecond);
        }

        @Override
        public void load() {
            this.areaFirst = this.areaFirstEntry.get();
            this.areaSecond = this.areaSecondEntry.get();
        }

        @Override
        public void save() {
            this.areaFirstEntry.set(this.areaFirst);
            this.areaSecondEntry.set(this.areaSecond);
        }

        @Override
        public FileConfiguration getConfig() {
            return config;
        }

        public Location getAreaFirst() {
            return this.areaFirst;
        }

        public Location getAreaSecond() {
            return this.areaSecond;
        }

        public Area asArea() {
            return new Area(this.areaFirst, this.areaSecond);
        }

    }

}

package fr.hyriode.pearlcontrol.game;

import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.api.settings.HyriLanguage;
import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.waitingroom.HyriWaitingRoom;
import fr.hyriode.hyrame.language.HyriLanguageMessage;
import fr.hyriode.hyrame.utils.DurationFormatter;
import fr.hyriode.hyrame.utils.LocationWrapper;
import fr.hyriode.pearlcontrol.api.PCStatistics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Function;

/**
 * Created by AstFaster
 * on 27/05/2022 at 19:02
 */
public class PCWaitingRoom extends HyriWaitingRoom {

    private static final Function<String, HyriLanguageMessage> LANG_DATA = name -> HyriLanguageMessage.get("waiting-room.npc.data.prefix." + name);

    public PCWaitingRoom(HyriGame<?> game) {
        super(game, Material.ENDER_PEARL, createConfig());

        this.addStatistics(21, PCGameType.NORMAL);
        this.addStatistics(23, PCGameType.CHAOS);
    }

    private void addStatistics(int slot, PCGameType gameType) {
        final NPCCategory normal = new NPCCategory(new HyriLanguageMessage("").addValue(HyriLanguage.EN, gameType.getDisplayName()));

        normal.addData(new NPCData(LANG_DATA.apply("kills"), account -> String.valueOf(this.getStatistics(gameType, account).getKills())));
        normal.addData(new NPCData(LANG_DATA.apply("final-kills"), account -> String.valueOf(this.getStatistics(gameType, account).getFinalKills())));
        normal.addData(new NPCData(LANG_DATA.apply("deaths"), account -> String.valueOf(this.getStatistics(gameType, account).getDeaths())));
        normal.addData(new NPCData(LANG_DATA.apply("captured-areas"), account -> String.valueOf(this.getStatistics(gameType, account).getCapturedAreas())));
        normal.addData(NPCData.voidData());
        normal.addData(new NPCData(LANG_DATA.apply("victories"), account -> String.valueOf(this.getStatistics(gameType, account).getVictories())));
        normal.addData(new NPCData(LANG_DATA.apply("games-played"), account -> String.valueOf(this.getStatistics(gameType, account).getGamesPlayed())));
        normal.addData(new NPCData(LANG_DATA.apply("played-time"), account -> this.formatPlayedTime(account, this.getStatistics(gameType, account).getPlayedTime())));

        this.addNPCCategory(slot, normal);
    }

    private String formatPlayedTime(IHyriPlayer account, long playedTime) {
        return new DurationFormatter()
                .withSeconds(false)
                .format(account.getSettings().getLanguage(), playedTime);
    }

    private PCStatistics.Data getStatistics(PCGameType gameType, IHyriPlayer account) {
        return ((PCGamePlayer) this.game.getPlayer(account.getUniqueId())).getStatistics().getData(gameType);
    }

    private static Config createConfig() {
        final UUID world = IHyrame.WORLD.get().getUID();

        return new Config(new LocationWrapper(world, 0, 0, 0), new LocationWrapper(world, 30, 222, -18), new LocationWrapper(world, -22, 192, 22), new LocationWrapper(world, 5.5F, 200, -2.5F, 90, 0));
    }


}

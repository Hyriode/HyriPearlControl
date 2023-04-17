package fr.hyriode.pearlcontrol.game;

import fr.hyriode.api.language.HyriLanguage;
import fr.hyriode.api.language.HyriLanguageMessage;
import fr.hyriode.api.leaderboard.HyriLeaderboardScope;
import fr.hyriode.api.leveling.NetworkLeveling;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hyrame.IHyrame;
import fr.hyriode.hyrame.game.HyriGame;
import fr.hyriode.hyrame.game.waitingroom.HyriWaitingRoom;
import fr.hyriode.hyrame.utils.DurationFormatter;
import fr.hyriode.hyrame.utils.LocationWrapper;
import fr.hyriode.hyrame.utils.Symbols;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.api.PCStatistics;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.UUID;
import java.util.function.Function;

/**
 * Created by AstFaster
 * on 27/05/2022 at 19:02
 */
public class PCWaitingRoom extends HyriWaitingRoom {

    private static final Function<String, HyriLanguageMessage> LANG_DATA = name -> HyriLanguageMessage.get("waiting-room.npc." + name);

    public PCWaitingRoom(HyriGame<?> game) {
        super(game, Material.ENDER_PEARL, HyriPearlControl.get().getConfiguration().getWaitingRoom());

        this.addLeaderboard(new Leaderboard(NetworkLeveling.LEADERBOARD_TYPE, "pearlcontrol-experience",
                player -> HyriLanguageMessage.get("leaderboard.experience.display").getValue(player))
                .withScopes(HyriLeaderboardScope.DAILY, HyriLeaderboardScope.WEEKLY, HyriLeaderboardScope.MONTHLY));
        this.addLeaderboard(new Leaderboard("pearlcontrol", "kills", player -> HyriLanguageMessage.get("leaderboard.kills.display").getValue(player)));
        this.addLeaderboard(new Leaderboard("pearlcontrol", "victories", player -> HyriLanguageMessage.get("leaderboard.victories.display").getValue(player)));
        this.addLeaderboard(new Leaderboard("pearlcontrol", "captured-areas", player -> HyriLanguageMessage.get("leaderboard.captured-areas.display").getValue(player)));

        this.addStatistics(22, PCGameType.NORMAL);
//        this.addStatistics(23, PCGameType.CHAOS);
    }

    private void addStatistics(int slot, PCGameType gameType) {
        final NPCCategory normal = new NPCCategory(HyriLanguageMessage.from(gameType.getDisplayName()));

        normal.addData(new NPCData(LANG_DATA.apply("kills"), account -> String.valueOf(this.getStatistics(gameType, account).getKills())));
        normal.addData(new NPCData(LANG_DATA.apply("final-kills"), account -> String.valueOf(this.getStatistics(gameType, account).getFinalKills())));
        normal.addData(new NPCData(LANG_DATA.apply("deaths"), account -> String.valueOf(this.getStatistics(gameType, account).getDeaths())));
        normal.addData(new NPCData(LANG_DATA.apply("captured-areas"), account -> String.valueOf(this.getStatistics(gameType, account).getCapturedAreas())));
        normal.addData(NPCData.voidData());
        normal.addData(new NPCData(LANG_DATA.apply("victories"), account -> String.valueOf(this.getStatistics(gameType, account).getVictories())));
        normal.addData(new NPCData(LANG_DATA.apply("games-played"), account -> String.valueOf(this.getStatistics(gameType, account).getGamesPlayed())));
        normal.addData(new NPCData(LANG_DATA.apply("played-time"), account -> this.formatPlayedTime(account, account.getStatistics().getPlayTime("pearlcontrol#" + gameType.getName()))));

        this.addNPCCategory(slot, normal);
    }

    private String formatPlayedTime(IHyriPlayer account, long playedTime) {
        return playedTime < 1000 ? ChatColor.RED + Symbols.CROSS_STYLIZED_BOLD : new DurationFormatter()
                .withSeconds(false)
                .format(account.getSettings().getLanguage(), playedTime);
    }

    private PCStatistics.Data getStatistics(PCGameType gameType, IHyriPlayer account) {
        return ((PCGamePlayer) this.game.getPlayer(account.getUniqueId())).getStatistics().getData(gameType);
    }

}

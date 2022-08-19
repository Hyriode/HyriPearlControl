package fr.hyriode.pearlcontrol.api;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.HyriPlayerData;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.pearlcontrol.game.PCGameType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 22/04/2022 at 21:57
 */
public class PCStatistics extends HyriPlayerData {

    private final Map<PCGameType, Data> data;

    public PCStatistics() {
        this.data = new HashMap<>();
    }

    public Map<PCGameType, Data> getData() {
        return this.data;
    }

    public Data getData(PCGameType gameType) {
        Data data = this.data.get(gameType);

        if (data == null) {
            data = new Data();
            this.data.put(gameType, data);
        }

        return data;
    }

    public void update(IHyriPlayer account) {
        account.addStatistics("pearlcontrol", this);
        account.update();
    }

    public void update(UUID player) {
        this.update(HyriAPI.get().getPlayerManager().getPlayer(player));
    }

    public static PCStatistics get(IHyriPlayer account) {
        PCStatistics statistics = account.getStatistics("pearlcontrol", PCStatistics.class);

        if (statistics == null) {
            statistics = new PCStatistics();
            statistics.update(account);
        }

        return statistics;
    }

    public static PCStatistics get(UUID playerId) {
        return get(IHyriPlayer.get(playerId));
    }

    public static class Data {

        private long kills;
        private long finalKills;
        private long deaths;
        private long victories;
        private long gamesPlayed;
        private long playedTime;
        private long capturedAreas;

        public long getKills() {
            return this.kills;
        }

        public void setKills(long kills) {
            this.kills = kills;
        }

        public void addKills(long kills) {
            this.kills += kills;
        }

        public void removeKills(int kills) {
            this.kills -= kills;
        }

        public long getFinalKills() {
            return this.finalKills;
        }

        public void setFinalKills(long finalKills) {
            this.finalKills = finalKills;
        }

        public void addFinalKills(long finalKills) {
            this.finalKills += finalKills;
        }

        public void removeFinalKills(long finalKills) {
            this.finalKills -= finalKills;
        }

        public long getDeaths() {
            return this.deaths;
        }

        public void setDeaths(long deaths) {
            this.deaths = deaths;
        }

        public void addDeaths(long deaths) {
            this.deaths += deaths;
        }

        public void removeDeaths(long deaths) {
            this.deaths -= deaths;
        }

        public long getVictories() {
            return this.victories;
        }

        public void setVictories(long victories) {
            this.victories = victories;
        }

        public void addVictories(long victories) {
            this.victories += victories;
        }

        public void removeVictories(long victories) {
            this.victories -= victories;
        }

        public long getDefeats() {
            return this.gamesPlayed - this.victories;
        }

        public long getGamesPlayed() {
            return this.gamesPlayed;
        }

        public void setGamesPlayed(long gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
        }

        public void addGamesPlayed(int gamesPlayed) {
            this.gamesPlayed += gamesPlayed;
        }

        public void removeGamesPlayed(int gamesPlayed) {
            this.gamesPlayed -= gamesPlayed;
        }

        public long getPlayedTime() {
            return this.playedTime;
        }

        public void setPlayedTime(long playedTime) {
            this.playedTime = playedTime;
        }

        public long getCapturedAreas() {
            return this.capturedAreas;
        }

        public void setCapturedAreas(long capturedAreas) {
            this.capturedAreas = capturedAreas;
        }

        public void addCapturedAreas(int capturedAreas) {
            this.capturedAreas += capturedAreas;
        }

        public void removeCapturedAreas(int capturedAreas) {
            this.capturedAreas -= capturedAreas;
        }

    }

}

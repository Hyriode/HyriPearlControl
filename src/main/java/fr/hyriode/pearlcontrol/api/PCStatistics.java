package fr.hyriode.pearlcontrol.api;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.mongodb.MongoDocument;
import fr.hyriode.api.mongodb.MongoSerializable;
import fr.hyriode.api.mongodb.MongoSerializer;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.api.player.model.IHyriStatistics;
import fr.hyriode.pearlcontrol.game.PCGameType;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 22/04/2022 at 21:57
 */
public class PCStatistics implements IHyriStatistics {

    private final Map<PCGameType, Data> dataMap;

    public PCStatistics() {
        this.dataMap = new HashMap<>();
    }

    public Map<PCGameType, Data> getData() {
        return this.dataMap;
    }

    @Override
    public void save(MongoDocument document) {
        for (Map.Entry<PCGameType, Data> entry : this.dataMap.entrySet()) {
            document.append(entry.getKey().name(), MongoSerializer.serialize(entry.getValue()));
        }
    }

    @Override
    public void load(MongoDocument document) {
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            final MongoDocument dataDocument = MongoDocument.of((Document) entry.getValue());
            final Data data = new Data();

            data.load(dataDocument);

            this.dataMap.put(PCGameType.valueOf(entry.getKey()), data);
        }
    }

    public Data getData(PCGameType gameType) {
        Data data = this.dataMap.get(gameType);

        if (data == null) {
            data = new Data();
            this.dataMap.put(gameType, data);
        }

        return data;
    }

    public void update(IHyriPlayer account) {
        account.getStatistics().add("pearlcontrol", this);
        account.update();
    }

    public void update(UUID player) {
        this.update(HyriAPI.get().getPlayerManager().getPlayer(player));
    }

    public static PCStatistics get(IHyriPlayer account) {
        PCStatistics statistics = account.getStatistics().read("pearlcontrol", new PCStatistics());

        if (statistics == null) {
            statistics = new PCStatistics();
            statistics.update(account);
        }
        return statistics;
    }

    public static PCStatistics get(UUID playerId) {
        return get(IHyriPlayer.get(playerId));
    }

    public static class Data implements MongoSerializable {

        private long kills;
        private long finalKills;
        private long deaths;
        private long victories;
        private long gamesPlayed;
        private long capturedAreas;

        public long getKills() {
            return this.kills;
        }

        public void addKills(long kills) {
            this.kills += kills;
        }

        public long getFinalKills() {
            return this.finalKills;
        }

        public void addFinalKills(long finalKills) {
            this.finalKills += finalKills;
        }

        public long getDeaths() {
            return this.deaths;
        }

        public void addDeaths(long deaths) {
            this.deaths += deaths;
        }

        public long getVictories() {
            return this.victories;
        }

        public void addVictories(long victories) {
            this.victories += victories;
        }

        public long getDefeats() {
            return this.gamesPlayed - this.victories;
        }

        public long getGamesPlayed() {
            return this.gamesPlayed;
        }

        public void addGamesPlayed(int gamesPlayed) {
            this.gamesPlayed += gamesPlayed;
        }

        public long getCapturedAreas() {
            return this.capturedAreas;
        }

        public void addCapturedAreas(int capturedAreas) {
            this.capturedAreas += capturedAreas;
        }

        @Override
        public void save(MongoDocument document) {
            document.append("kills", this.kills);
            document.append("finalKills", this.finalKills);
            document.append("deaths", this.deaths);
            document.append("victories", this.victories);
            document.append("gamesPlayed", this.gamesPlayed);
            document.append("capturedAreas", this.capturedAreas);
        }

        @Override
        public void load(MongoDocument document) {
            this.kills = document.getLong("kills");
            this.finalKills = document.getLong("finalKills");
            this.deaths = document.getLong("deaths");
            this.victories = document.getLong("victories");
            this.gamesPlayed = document.getLong("gamesPlayed");
            this.capturedAreas = document.getLong("capturedAreas");
        }

    }

}

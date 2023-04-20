package fr.hyriode.pearlcontrol.game;

import fr.hyriode.hyrame.game.HyriGameType;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 22/04/2022 at 16:12
 */
public enum PCGameType implements HyriGameType {

    NORMAL("Normal", 3, 8),
    CHAOS("Chaos", 6, 12)
    ;

    private final String displayName;
    private final int minPlayers;
    private final int maxPlayers;

    PCGameType(String displayName, int minPlayers, int maxPlayers) {
        this.displayName = displayName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public int getMinPlayers() {
        return this.minPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

}

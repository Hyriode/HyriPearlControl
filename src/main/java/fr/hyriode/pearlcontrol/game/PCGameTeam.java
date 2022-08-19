package fr.hyriode.pearlcontrol.game;

import fr.hyriode.hyrame.game.team.HyriGameTeamColor;
import fr.hyriode.api.language.HyriLanguageMessage;
import fr.hyriode.pearlcontrol.HyriPearlControl;

import java.util.function.Supplier;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 21:13
 */
public enum PCGameTeam {

    BLUE("blue", HyriGameTeamColor.BLUE),
    RED("red", HyriGameTeamColor.RED),
    GREEN("green", HyriGameTeamColor.GREEN),
    YELLOW("yellow", HyriGameTeamColor.YELLOW),
    AQUA("aqua", HyriGameTeamColor.CYAN),
    PINK("pink", HyriGameTeamColor.PINK),
    WHITE("white", HyriGameTeamColor.WHITE),
    GRAY("gray", HyriGameTeamColor.GRAY),

    ;

    private final String teamName;
    private final HyriGameTeamColor teamColor;
    private final Supplier<HyriLanguageMessage> displayName;

    PCGameTeam(String teamName, HyriGameTeamColor teamColor) {
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.displayName = () -> HyriLanguageMessage.get("team." + this.teamName + ".display");
    }

    public String getName() {
        return this.teamName;
    }

    public HyriGameTeamColor getColor() {
        return this.teamColor;
    }

    public HyriLanguageMessage getDisplayName() {
        return this.displayName.get();
    }

}

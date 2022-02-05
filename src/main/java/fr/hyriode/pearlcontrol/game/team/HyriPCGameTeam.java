package fr.hyriode.pearlcontrol.game.team;

import fr.hyriode.hyrame.game.team.HyriGameTeam;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 04/02/2022 at 21:18
 */
public class HyriPCGameTeam extends HyriGameTeam {

    public HyriPCGameTeam(EHyriPCGameTeam gameTeam) {
        super(gameTeam.getName(), gameTeam.getDisplayName(), gameTeam.getColor(), 1);
    }

}

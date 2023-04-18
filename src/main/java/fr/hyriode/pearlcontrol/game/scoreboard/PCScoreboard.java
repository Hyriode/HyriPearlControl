package fr.hyriode.pearlcontrol.game.scoreboard;

import fr.hyriode.api.language.HyriLanguageMessage;
import fr.hyriode.hyrame.game.scoreboard.HyriGameScoreboard;
import fr.hyriode.hyrame.utils.Symbols;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.PCGame;
import fr.hyriode.pearlcontrol.game.PCGamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 05/02/2022 at 11:22
 */
public class PCScoreboard extends HyriGameScoreboard<PCGame> {

    private final PCGamePlayer gamePlayer;

    public PCScoreboard(Player player) {
        super(HyriPearlControl.get(), HyriPearlControl.get().getGame(), player, "pearlcontrol");
        this.gamePlayer = this.game.getPlayer(player);

        this.addLines();
        this.addCurrentDateLine(0);
        this.addBlankLine(1);

        if (this.gamePlayer != null) {
            this.addBlankLine(4);
            this.addBlankLine(6);
            this.addGameTimeLine(7, this.getLinePrefix("time"));
            this.addBlankLine(8);
        } else {
            this.addBlankLine(3);
            this.addGameTimeLine(4, this.getLinePrefix("time"));
            this.addBlankLine(5);
        }

        this.addHostnameLine();
    }

    private void addLines() {
        if (this.gamePlayer != null) {
            this.setLine(2, this.getLivesLines());
            this.setLine(3, this.getPercentageLine());
            this.addPlayersLine(5, this.getLinePrefix("players"), true);
        } else {
            this.addPlayersLine(2, this.getLinePrefix("players"), true);
        }
    }

    public void update() {
        this.addLines();
        this.updateLines();
    }

    private String getLivesLines() {
        final int lives = this.gamePlayer.getLives();

        return this.getLinePrefix("lives") + (lives == 0 ? ChatColor.RED + Symbols.CROSS_STYLIZED_BOLD : ChatColor.AQUA + String.valueOf(lives));
    }

    private String getPercentageLine() {
        return this.getLinePrefix("percentage") + ChatColor.AQUA + String.format(String.valueOf(this.gamePlayer.getKnockbackPercentage()), "%.2f") + "%";
    }

    private String getLinePrefix(String prefix) {
        return ChatColor.WHITE + HyriLanguageMessage.get("scoreboard." + prefix + ".display").getValue(this.player);
    }

}

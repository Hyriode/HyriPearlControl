package fr.hyriode.pearlcontrol.game.scoreboard;

import fr.hyriode.api.language.HyriLanguageMessage;
import fr.hyriode.hyrame.game.scoreboard.HyriGameScoreboard;
import fr.hyriode.hyrame.utils.Symbols;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.PCGame;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 05/02/2022 at 11:22
 */
public class PCScoreboard extends HyriGameScoreboard<PCGame> {

    public PCScoreboard(HyriPearlControl plugin, PCGame game, Player player) {
        super(plugin, game, player, "pearlcontrol");

        this.addLines();

        this.addCurrentDateLine(0);
        this.addBlankLine(1);
        this.addBlankLine(4);
        this.addBlankLine(6);
        this.addGameTimeLine(7, this.getLinePrefix("time"));
        this.addBlankLine(8);

        this.addHostnameLine();
    }

    private void addLines() {
        this.setLine(2, this.getLivesLines());
        this.setLine(3, this.getPercentageLine());
        this.addPlayersLine(5, this.getLinePrefix("players"), true);
    }

    public void update() {
        this.addLines();
        this.updateLines();
    }

    private String getLivesLines() {
        final int lives = this.game.getPlayer(this.player.getUniqueId()).getLives();

        return this.getLinePrefix("lives") + (lives == 0 ? ChatColor.RED + Symbols.CROSS_STYLIZED_BOLD : ChatColor.AQUA + String.valueOf(lives));
    }

    private String getPercentageLine() {
        return this.getLinePrefix("percentage") + ChatColor.AQUA + (int) this.game.getPlayer(this.player.getUniqueId()).getKnockbackPercentage() + "%";
    }

    private String getLinePrefix(String prefix) {
        return ChatColor.WHITE + HyriLanguageMessage.get("scoreboard." + prefix + ".display").getValue(this.player);
    }

}

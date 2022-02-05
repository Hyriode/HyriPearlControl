package fr.hyriode.pearlcontrol.game.scoreboard;

import fr.hyriode.hyrame.game.scoreboard.HyriScoreboardIpConsumer;
import fr.hyriode.hyrame.scoreboard.Scoreboard;
import fr.hyriode.hyrame.utils.Symbols;
import fr.hyriode.pearlcontrol.HyriPearlControl;
import fr.hyriode.pearlcontrol.game.HyriPCGame;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Project: HyriPearlControl
 * Created by AstFaster
 * on 05/02/2022 at 11:22
 */
public class HyriPCScoreboard extends Scoreboard {

    private final HyriPCGame game;
    private final HyriPearlControl plugin;

    public HyriPCScoreboard(HyriPearlControl plugin, Player player) {
        super(plugin, player, "pearlcontrol", ChatColor.DARK_AQUA + "     " + ChatColor.BOLD + plugin.getGame().getDisplayName() + "     ");
        this.plugin = plugin;
        this.game = this.plugin.getGame();

        this.addLines();

        this.setLine(0, this.getDateLine(), scoreboardLine -> scoreboardLine.setValue(this.getDateLine()), 20);
        this.setLine(1, "");
        this.setLine(4, "  ");
        this.setLine(6, "   ");
        this.setLine(7, this.getTimeLine(), scoreboardLine -> scoreboardLine.setValue(this.getTimeLine()), 20);
        this.setLine(8, "    ");
        this.setLine(9, ChatColor.DARK_AQUA + "hyriode.fr", new HyriScoreboardIpConsumer("hyriode.fr"), 2);
    }

    private void addLines() {
        this.setLine(2, this.getLivesLines());
        this.setLine(3, this.getPercentageLine());
        this.setLine(5, this.getPlayersLine());
    }

    public void update() {
        this.addLines();
        this.updateLines();
    }

    private String getDateLine() {
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        format.setTimeZone(TimeZone.getTimeZone("GMT+1"));

        return ChatColor.GRAY + format.format(new Date());
    }

    private String getLivesLines() {
        final int lives = this.game.getPlayer(this.player.getUniqueId()).getLives();

        return ChatColor.WHITE + this.getLinePrefix("lives") + (lives == 0 ? ChatColor.RED + Symbols.CROSS_STYLIZED_BOLD : ChatColor.AQUA + String.valueOf(lives));
    }

    private String getPercentageLine() {
        return ChatColor.WHITE + this.getLinePrefix("percentage") + ChatColor.AQUA + (int) this.game.getPlayer(this.player.getUniqueId()).getKnockbackPercentage() + "%";
    }

    private String getPlayersLine() {
        return ChatColor.WHITE + this.getLinePrefix("players") + ChatColor.AQUA + (this.game.getPlayers().size() - this.game.getDeadPlayers().size());
    }

    private String getTimeLine() {
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        final String line = format.format(this.plugin.getGame().getGameTime() * 1000);

        return this.getLinePrefix("time") + ChatColor.AQUA + (line.startsWith("00:") ? line.substring(3) : line);
    }

    private String getLinePrefix(String prefix) {
        return HyriPearlControl.getLanguageManager().getValue(this.player, "scoreboard." + prefix + ".display");
    }

}

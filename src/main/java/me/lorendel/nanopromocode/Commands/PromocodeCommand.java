package me.lorendel.nanopromocode.Commands;
import me.lorendel.nanopromocode.Models.PlayerPromocodeStats;
import me.lorendel.nanopromocode.Models.Promocodes;
import me.lorendel.nanopromocode.NanoPromocode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import java.sql.SQLException;
import java.util.*;

public class PromocodeCommand implements CommandExecutor, TabExecutor {

    private final NanoPromocode plugin;

    public PromocodeCommand(NanoPromocode plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        // Ensure the command sender is a Player
        if (!(commandSender instanceof Player p)) {
            commandSender.sendMessage((ChatColor.translateAlternateColorCodes('&', Objects
                    .requireNonNull(this.plugin.getCustomConfig().getString("only-player-usage-warning")))));
            return true;
        }

        // Check if no arguments are provided
        if (strings.length < 1) {
            sendChangedMessage(p, "incorrect-promo-code-warning");
            return true;
        }

        // Handle 'refresh' command
        if (strings[0].equalsIgnoreCase("refresh")) {
            if (!p.hasPermission("nano.refresh.stats.promo")) {
                sendChangedMessage(p, "refresh-promo-code-permission-warning");
                return true;
            }

            if (strings.length < 2) {
                sendChangedMessage(p, "refresh-promo-code-name-warning");
                return true;
            }

            String targetPlayerName = strings[1];
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

            try {
                this.plugin.getDatabase().deletePlayerPromocodeStats(targetPlayer);
                p.sendMessage((ChatColor.translateAlternateColorCodes('&', Objects
                        .requireNonNull(this.plugin.getCustomConfig().getString("promo-code-successfully-refreshed")))
                        + " " + targetPlayerName));
            } catch (SQLException exception) {
                p.sendMessage((ChatColor.translateAlternateColorCodes('&', Objects
                        .requireNonNull(this.plugin.getCustomConfig().getString("unable-to-refresh-promo-code")))
                        + " " + targetPlayerName));
                exception.printStackTrace();
            }
            return true;
        }

        // Handle 'add' command
        if (strings[0].equalsIgnoreCase("add")) {
            if (!p.hasPermission("nano.add.promo")) {
                sendChangedMessage(p, "add-promo-code-permission-warning");
                return true;
            }

            if (strings.length < 2) {
                sendChangedMessage(p, "add-promo-code-no-code-warning");
                return true;
            }

            String promoCodeName = strings[1];
            try {
                Promocodes promocodes = this.plugin.getDatabase().findPromocodeByName(promoCodeName);

                // If promo code doesn't exist, create it
                if (promocodes == null) {
                    promocodes = new Promocodes(promoCodeName, new Date(), 0);
                    this.plugin.getDatabase().createPromocode(promocodes);
                    String message = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this
                            .plugin.getCustomConfig().getString("add-promo-code-success")));
                    String messageToSend = message.replace("%promo_code%", promoCodeName);
                    p.sendMessage(messageToSend);
                } else {
                    sendChangedMessage(p, "add-promo-code-already-exist-warning");
                }

            } catch (SQLException exception) {
                sendChangedMessage(p, "add-promo-code-error");
                exception.printStackTrace();
            }
            return true;
        }

        // Handle promo code usage
        String promoCode = strings[0];
        try {
            Promocodes promocode = this.plugin.getDatabase().findPromocodeByName(promoCode);

            if (promocode == null) {
                sendChangedMessage(p, "promo-code-does-not-exist");
                return true;
            }

            // Check if the player has already used the promo code
            if (this.plugin.getDatabase().findPromocodePlayerStatsByUUID(p.getUniqueId().toString()) == null) {
                sendChangedMessage(p, "promo-code-activated");
                giveRewardToPlayer(p);
                addIsUsedToDatabase(p, promoCode);
                promocode.setCountUsed(promocode.getCountUsed() + 1);
                this.plugin.getDatabase().updatePromocode(promocode);
            } else {
                sendChangedMessage(p, "promo-code-already-used");
            }

        } catch (SQLException exception) {
            sendChangedMessage(p, "promo-code-activation-error");
            exception.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            return null;
        }

        List<String> completion = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("promo")) {
            if (strings.length == 1) {
                completion.add("<promo_code_name>");

                if (commandSender.hasPermission("nano.refresh.stats.promo")) {
                    completion.add("refresh");
                }
                if (commandSender.hasPermission("nano.add.promo")) {
                    completion.add("add");
                }
            } else if (strings.length == 2 && strings[0].equalsIgnoreCase("add")) {
                completion.add("<promo_code>");
            } else if (strings.length == 2 && strings[0].equalsIgnoreCase("refresh")) {

                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    completion.add(onlinePlayer.getName());
                }
            }
        }
        return completion;
    }

    private boolean addIsUsedToDatabase(Player p, String promoCode) throws SQLException {
        try {
            PlayerPromocodeStats stats = this.plugin.getDatabase().findPromocodePlayerStatsByUUID(p.getUniqueId().toString());

            if (stats == null) {
                stats = new PlayerPromocodeStats(p.getUniqueId().toString(), new Date(), promoCode);
                this.plugin.getDatabase().createPlayerPromocodeStats(stats);
                return true;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    private void giveRewardToPlayer(Player p) {

        List<String> promoRewards = this.plugin.getConfig().getStringList("promo-rewards");
        String playerName = p.getName();

        for (String command : promoRewards) {
            String commandToExecute = command.replace("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
        }
    }

    private void sendChangedMessage(Player p, String path){
        String message = plugin.getCustomConfig().getString(path);
        if (message != null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
package de.anglergames.cloudservermanager.commands;

import de.anglergames.cloudservermanager.main.CloudServerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CloudServerManagerCommand implements CommandExecutor {

    private CloudServerManager plugin;

    public CloudServerManagerCommand(CloudServerManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Command kann nur als Spieler ausgeführt werden!");
        }

        Player player = (Player) sender;

        if (!player.hasPermission("system.csm.gameserver") && !player.hasPermission("system.csm.lobbyserver") && !player.hasPermission("system.csm.proxy") && !player.hasPermission("system.csm")){
            return false;
        } else {
            plugin.getServerGroupOverviewInvHandler().openOverview(player, plugin.getInformationMySQL().getAllGroups().get(0));
        }






        return false;
    }
}

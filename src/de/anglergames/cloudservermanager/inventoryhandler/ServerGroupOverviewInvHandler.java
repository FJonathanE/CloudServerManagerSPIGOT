package de.anglergames.cloudservermanager.inventoryhandler;

import de.anglergames.api.utils.Item;
import de.anglergames.cloudservermanager.main.CloudServerManager;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.driver.service.ServiceLifeCycle;
import de.dytanic.cloudnet.ext.bridge.ServiceInfoSnapshotUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ServerGroupOverviewInvHandler implements Listener {

    private CloudNetDriver driver;
    private CloudServerManager plugin;

    private final String GUI_NAME = "§3§lC§b§lloud §3§lS§b§lerver §3§lM§b§lanager";

    public ServerGroupOverviewInvHandler(CloudServerManager plugin) {
        this.plugin = plugin;

        driver = CloudNetDriver.getInstance();
    }

    public void openOverview(Player player, String groupname) {
        Inventory menu = Bukkit.createInventory(player, 9 * 6, GUI_NAME);
        ItemStack emtyItem = new Item(Material.STAINED_GLASS_PANE, 1, (short) 5).setDisplayName("").build();

        player.openInventory(menu);

        Material groupItemIcon = plugin.getInformationMySQL().getGroupItemIcon(groupname);


        for (int i = 0; i < 9 * 6; i++) {

            if (i >= 0 && i <= 8) {
                menu.setItem(i, emtyItem);
            }
            if (i >= 36 && i <= 44) {
                menu.setItem(i, emtyItem);
            }
            if (i == 9 || i == 18 || i == 27 || i == 45 || i == 17 || i == 26 || i == 35 || i == 53) {
                menu.setItem(i, emtyItem);
            }
        }

        for (String name : plugin.getInformationMySQL().getAllServerFromGroup(groupname)) {


            Item item = new Item(groupItemIcon).setDisplayName("§c§l" + name);

            boolean online = false;


            for (ServiceInfoSnapshot cloudservice : CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices()) {
                if (cloudservice.getServiceId().getName().equals(name)) {

                    online = true;

                    if (cloudservice.getLifeCycle() == ServiceLifeCycle.RUNNING) {
                        item.setDisplayName("§a§l" + name);
                        item.addLoreLine("§7LifeCycleState: §a" + cloudservice.getLifeCycle().toString());
                    } else {
                        item.setDisplayName("§e§l" + name);
                        item.addLoreLine("§7LifeCycleState: §e" + cloudservice.getLifeCycle().toString());
                    }


                    item.addLoreLine("§7OnlinePlayers: §b" + ServiceInfoSnapshotUtil.getOnlineCount(cloudservice) + "§7/§b" + ServiceInfoSnapshotUtil.getMaxPlayers(cloudservice));
                    item.addLoreLine("§7   ");
                    item.addLoreLine("§7ServiceID: §b" + cloudservice.getServiceId().getTaskServiceId());
                    item.addLoreLine("§7Taskname: §b" + cloudservice.getServiceId().getTaskName());


                    break;
                }
            }

            if (!online) {
                item.addLoreLine("§7LifeCycleState: §cSTOPPED");
                item.addLoreLine("§7OnlinePlayers: §b/");
                item.addLoreLine("§7   ");
                item.addLoreLine("§7ServiceID: §b/");
                item.addLoreLine("§7Taskname: §b/");
            }


            menu.addItem(item.build());

        }

        for (int i = 0; i < 7; i++) {
            List<String> groups = plugin.getInformationMySQL().getAllGroups();

            if (i < plugin.getInformationMySQL().getAllGroups().size()) {
                Item item = new Item(plugin.getInformationMySQL().getGroupItemIcon(groups.get(i))).setDisplayName("§3" + groups.get(i))
                        .addLoreLine("§7Klicke auf dieses Item um").addLoreLine("§7diese Servergruppe zu bearbeiten.");

                if (groups.get(i).equals(groupname)) {
                    item.setGlow();
                }

                menu.setItem(i + 46, item.build());
            }
        }


    }


    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_NAME)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (!event.getCurrentItem().hasItemMeta()) {
            return;
        }

        String itemname = event.getCurrentItem().getItemMeta().getDisplayName();

        for (String groups : plugin.getInformationMySQL().getAllGroups()) {
            if (groups.equals(ChatColor.stripColor(itemname))) {
                openOverview(player, groups);
                break;
            }
        }

        for (String server : plugin.getInformationMySQL().getAllServer()) {
            if (server.equals(ChatColor.stripColor(itemname))) {

                String groupname = server.split("-")[0];

                if (!player.hasPermission("system.csm." + plugin.getInformationMySQL().getServerType(groupname))){
                    player.playSound(player.getLocation() ,Sound.NOTE_BASS, 1, 1);
                    player.sendMessage(plugin.PREFIX + "§cDu hast keine Rechte um diese Servergruppe zu bearbeiten!");
                    return;
                }else
                    plugin.getServerEditorInvHandler().openEditor(player, server);
                break;
            }
        }


    }
}

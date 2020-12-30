package de.anglergames.cloudservermanager.inventoryhandler;

import de.anglergames.api.utils.Item;
import de.anglergames.cloudservermanager.main.CloudServerManager;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.driver.service.ServiceLifeCycle;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import de.dytanic.cloudnet.driver.service.ServiceTemplate;
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

import java.util.ArrayList;
import java.util.Collection;

public class ServerEditorInvHandler implements Listener {
    
    private final CloudNetDriver DRIVER = CloudNetDriver.getInstance();

    private CloudServerManager plugin;

    private final String EDITOR_GUI_NAME1 = "§b§lServer §3§l";
    private final String EDITOR_GUI_NAME2 = " §b§leditieren";
    private final String SAFETYINV_GUI_NAME1 = "§b§lServergruppe §3§l";
    private final String SAFETYINV_GUI_NAME2 = "§b§lstoppen?";

    private final ItemStack START_SERVER = new Item(Material.STAINED_CLAY, 1, (short) 13).setDisplayName("§2§lServer starten")
            .addLoreLine("§7Klicke auf dieses Item um").addLoreLine("§7diesen Server zu starten.").build();
    private final ItemStack STOP_SERVER = new Item(Material.STAINED_CLAY, 1, (short) 1).setDisplayName("§c§lServer stoppen")
            .addLoreLine("§7Klicke auf dieses Item um").addLoreLine("§7diesen Server zu stoppen.").build();
    private final ItemStack STOP_ALL_SERVER = new Item(Material.STAINED_CLAY, 1, (short) 14).setDisplayName("§4§lAlle Server stoppen")
            .addLoreLine("§7Klicke auf dieses Item um").addLoreLine("§7alle Server dieser Gruppe zu stoppen.").build();

    private final ItemStack BACK_TO_OVERVIEW = new Item(Material.COMPASS, 1, (short) 0).setDisplayName("§3§lZurück zur Übersicht")
            .addLoreLine("§7Klicke auf dieses Item um").addLoreLine("§7zurück zur Übersicht zu gelangen.").build();



    public ServerEditorInvHandler(CloudServerManager plugin) {
        this.plugin = plugin;
    }

    public void openEditor(Player player, String servername){
        String groupname = servername.split("-")[0];

        Inventory menu = Bukkit.createInventory(player, 9*3, EDITOR_GUI_NAME1 + servername + EDITOR_GUI_NAME2);

        ItemStack emtyItem = new Item(Material.STAINED_GLASS_PANE, 1,(short) 5).setDisplayName("").build();
        Material groupItemIcon = plugin.getInformationMySQL().getGroupItemIcon(groupname);

        player.openInventory(menu);

        for (int i = 0; i < 9*3; i++){
            menu.setItem(i, emtyItem);
        }

        menu.setItem(11, START_SERVER);

        menu.setItem(15, STOP_SERVER);
        menu.setItem(16, STOP_ALL_SERVER);

        menu.setItem(18, BACK_TO_OVERVIEW);


        Item serverItem = new Item(groupItemIcon);

        if (serverIsOnline(servername)){

            ServiceInfoSnapshot cloudservice = getServiceInfoSnapshotFromName(servername);

            if (cloudservice.getLifeCycle() == ServiceLifeCycle.RUNNING){

                serverItem.setDisplayName("§a§l" + servername);
                serverItem.addLoreLine("§7LifeCycleState: §a" + cloudservice.getLifeCycle().toString());

            }else{

                serverItem.setDisplayName("§e§l" + servername);
                serverItem.addLoreLine("§7LifeCycleState: §e" + cloudservice.getLifeCycle().toString());

            }

            serverItem.addLoreLine("§7OnlinePlayers: §b" + ServiceInfoSnapshotUtil.getOnlineCount(cloudservice) + "§7/§b" + ServiceInfoSnapshotUtil.getMaxPlayers(cloudservice));
            serverItem.addLoreLine("§7   ");
            serverItem.addLoreLine("§7ServiceID: §b" + cloudservice.getServiceId().getTaskServiceId());
            serverItem.addLoreLine("§7Taskname: §b" + cloudservice.getServiceId().getTaskName());

        }else {
            serverItem.setDisplayName("§c§l" + servername);

            serverItem.addLoreLine("§7LifeCycleState: §cSTOPPED");
            serverItem.addLoreLine("§7OnlinePlayers: §b/");
            serverItem.addLoreLine("§7   ");
            serverItem.addLoreLine("§7ServiceID: §b/");
            serverItem.addLoreLine("§7Taskname: §b/");

        }

        menu.setItem(13, serverItem.build());

    }

    /*public void openSafetyInv(Player player, String groupname){
        Inventory menu = Bukkit.createInventory(player, 9*3, SAFETYINV_GUI_NAME1 + groupname + SAFETYINV_GUI_NAME2);

        ItemStack emtyItem = new Item(Material.STAINED_GLASS_PANE, 1,(short) 5).setDisplayName("").build();
        Material groupItemIcon = plugin.getInformationMySQL().getGroupItemIcon(groupname);

        player.openInventory(menu);

        for (int i = 0; i < 9*3; i++){
            menu.setItem(i, emtyItem);
        }
    }*/


    @EventHandler
    public void onClick(InventoryClickEvent event){

        if (!event.getView().getTitle().startsWith(EDITOR_GUI_NAME1) && !event.getView().getTitle().startsWith(SAFETYINV_GUI_NAME1)){
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null){
            return;
        }


        Player player = (Player) event.getWhoClicked();


        if (!event.getCurrentItem().hasItemMeta()){
            return;
        }


        String itemname = event.getCurrentItem().getItemMeta().getDisplayName();

        if (event.getView().getTitle().startsWith(EDITOR_GUI_NAME1)){

            String servername = ChatColor.stripColor(event.getView().getTitle()).split(" ")[1];
            String groupname = servername.split("-")[0];

            if (itemname.equals(START_SERVER.getItemMeta().getDisplayName())){
                if (serverIsOnline(servername)){
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                    player.sendMessage(plugin.PREFIX + "§cDer Server §e" + servername + "§c läuft bereits!");
                    player.closeInventory();
                    plugin.getServerGroupOverviewInvHandler().openOverview(player, groupname);
                }else {
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
                    player.sendMessage(plugin.PREFIX + "Der Server §e" + servername + " §a startet nun!");
                    player.closeInventory();
                    plugin.getServerGroupOverviewInvHandler().openOverview(player, groupname);
                    startCloudServer(groupname);
                }
            }
            if (itemname.equals(STOP_SERVER.getItemMeta().getDisplayName())){
                if (!serverIsOnline(servername)){
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                    player.sendMessage(plugin.PREFIX + "§cDer Server §e" + servername + "§c ist bereits gestoppt!");
                    player.closeInventory();
                    plugin.getServerGroupOverviewInvHandler().openOverview(player, groupname);
                }else {
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
                    player.sendMessage(plugin.PREFIX + "Der Server §e" + servername + " §a stoppt nun!");
                    DRIVER.getCloudServiceProvider(servername).stopAsync();
                    player.closeInventory();
                    plugin.getServerGroupOverviewInvHandler().openOverview(player, groupname);
                }
            }
            if (itemname.equals(STOP_ALL_SERVER.getItemMeta().getDisplayName())){
                player.closeInventory();
                plugin.getServerGroupOverviewInvHandler().openOverview(player, groupname);
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
                player.sendMessage(plugin.PREFIX + "Die Servergruppe §e" + groupname + " §a stoppt nun!");

                for (ServiceInfoSnapshot cloudserver : DRIVER.getCloudServiceProvider().getCloudServices()){
                    if (cloudserver.getServiceId().getTaskName().equals(groupname)){
                        DRIVER.getCloudServiceProvider(cloudserver.getServiceId().getUniqueId()).stopAsync();
                    }
                }


            }
            if (itemname.equals(BACK_TO_OVERVIEW.getItemMeta().getDisplayName())){
                plugin.getServerGroupOverviewInvHandler().openOverview(player, groupname);
            }



        }else if (event.getView().getTitle().startsWith(SAFETYINV_GUI_NAME1)){



        }

    }


    public boolean serverIsOnline(String servername){
        boolean online = false;
        for (ServiceInfoSnapshot cloudservice : DRIVER.getCloudServiceProvider().getCloudServices()) {
            if (cloudservice.getServiceId().getName().equals(servername)) {
                online = true;
                break;
            }
        }

        return online;
    }

    public ServiceInfoSnapshot getServiceInfoSnapshotFromName(String servername){
        for (ServiceInfoSnapshot cloudservice : DRIVER.getCloudServiceProvider().getCloudServices()) {
            if (cloudservice.getServiceId().getName().equals(servername)) {
                return cloudservice;
            }
        }
        return null;
    }

    public void startCloudServer(String groupname) {
        if (DRIVER.getServiceTaskProvider().isServiceTaskPresent(groupname)) {
            ServiceTask serviceTask = DRIVER.getServiceTaskProvider().getServiceTask(groupname);
            ServiceInfoSnapshot serviceInfoSnapshot = DRIVER.getCloudServiceFactory().createCloudService(serviceTask);

            if (serviceInfoSnapshot != null){
                DRIVER.getCloudServiceProvider(serviceInfoSnapshot.getServiceId().getUniqueId()).startAsync();
            }
        }
    }
}

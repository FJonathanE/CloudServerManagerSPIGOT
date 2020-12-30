package de.anglergames.cloudservermanager.main;

import de.anglergames.cloudservermanager.commands.CloudServerManagerCommand;
import de.anglergames.cloudservermanager.inventoryhandler.ServerEditorInvHandler;
import de.anglergames.cloudservermanager.inventoryhandler.ServerGroupOverviewInvHandler;
import de.anglergames.cloudservermanager.mysql.CSMInformationMySQL;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CloudServerManager extends JavaPlugin {

    private ServerGroupOverviewInvHandler serverGroupOverviewInvHandler;
    private CSMInformationMySQL informationMySQL;
    private ServerEditorInvHandler serverEditorInvHandler;

    public final String PREFIX = "§8[§3CSM§8] §a";

    @Override
    public void onEnable() {

        serverGroupOverviewInvHandler = new ServerGroupOverviewInvHandler(this);
        informationMySQL = new CSMInformationMySQL(this);
        serverEditorInvHandler = new ServerEditorInvHandler(this);




        registerListener();
        registerCommands();
    }

    private void registerCommands() {
        getCommand("cloudservermanager").setExecutor(new CloudServerManagerCommand(this));
    }

    private void registerListener() {

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(serverGroupOverviewInvHandler, this);
        pm.registerEvents(serverEditorInvHandler, this);

    }

    @Override
    public void onDisable() {

    }

    public ServerGroupOverviewInvHandler getServerGroupOverviewInvHandler() {
        return serverGroupOverviewInvHandler;
    }


    public CSMInformationMySQL getInformationMySQL() {
        return informationMySQL;
    }

    public ServerEditorInvHandler getServerEditorInvHandler() {
        return serverEditorInvHandler;
    }
}

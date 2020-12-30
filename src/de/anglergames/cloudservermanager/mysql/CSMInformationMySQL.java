package de.anglergames.cloudservermanager.mysql;

import de.anglergames.api.config.ManagedFile;
import de.anglergames.api.mysql.MySQLBase;
import de.anglergames.api.mysql.MySQLConfig;
import de.anglergames.api.mysql.MySQLData;
import de.anglergames.api.mysql.MySQLDataType;
import de.anglergames.cloudservermanager.main.CloudServerManager;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class CSMInformationMySQL {

    private CloudServerManager plugin;
    private MySQLData serverData, groupData;

    private List<String> groups, server;

    private HashMap<String, List<String>> serverFromGroups;
    private HashMap<String, Material> groupItemIcons;
    private HashMap<String, String> serverTypes;

    public CSMInformationMySQL(CloudServerManager plugin) {
        this.plugin = plugin;


        setup();
    }

    private void setup() {
        MySQLConfig config = new MySQLConfig(new ManagedFile("plugins/AnglerGames/", "mysql.yml"));
        config.create();
        MySQLBase myBase = new MySQLBase(config);

        LinkedHashMap<String, MySQLDataType> serverHM = new LinkedHashMap<>();
        serverHM.put("ServerName", MySQLDataType.STRING);
        serverHM.put("ServerGroup", MySQLDataType.STRING);

        LinkedHashMap<String, MySQLDataType> groupHM = new LinkedHashMap<>();
        groupHM.put("GroupName", MySQLDataType.STRING);
        groupHM.put("ServerType", MySQLDataType.STRING);
        groupHM.put("GroupItemIcon", MySQLDataType.STRING);


        serverData = new MySQLData(myBase, "CSMAvailableServer", serverHM);
        serverData.create();

        groupData = new MySQLData(myBase, "CSMServerGroups", groupHM);
        groupData.create();

        setupLists();

    }

    private void setupLists() {
        groups = new ArrayList<>();
        server = new ArrayList<>();
        serverFromGroups = new HashMap<>();
        groupItemIcons = new HashMap<>();
        serverTypes = new HashMap<>();

        groups.addAll(groupData.getAllValues("GroupName"));
        server.addAll(serverData.getAllValues("ServerName"));

        for (String groupname : groups){

            List<String> serverBelongingToGroup = new ArrayList<>();

            for (String servername : server){
                if (serverData.getString("ServerName", servername, "ServerGroup").equals(groupname)){
                    serverBelongingToGroup.add(servername);
                }
            }

            groupItemIcons.put(groupname, Material.valueOf(groupData.getString("GroupName", groupname, "GroupItemIcon")));
            serverFromGroups.put(groupname, serverBelongingToGroup);
            serverTypes.put(groupname, groupData.getString("GroupName", groupname, "ServerType"));
        }




    }

    public List<String> getAllGroups(){
        return groups;
    }

    public List<String> getAllServer(){
        return server;
    }

    public List<String> getAllServerFromGroup(String groupname){
        return serverFromGroups.get(groupname);
    }

    public Material getGroupItemIcon(String groupname){
        return groupItemIcons.get(groupname);
    }

    public String getServerType(String groupname){
        return serverTypes.get(groupname);
    }



}


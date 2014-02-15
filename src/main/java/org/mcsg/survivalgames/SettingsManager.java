package org.mcsg.survivalgames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.util.ChestRatioStorage;

public class SettingsManager {

	//makes the config easily accessible

	private static SettingsManager instance = new SettingsManager();
	private static Plugin p;
	private FileConfiguration spawns;
	private FileConfiguration system;
	private FileConfiguration kits;
	private FileConfiguration messages;


	private File f; //spawns
	private File f2; //system
	private File f4; //messages
	private File chestFile; //chest
	
	private static final int MESSAGE_VERSION = 1;
	private static final int SPAWN_VERSION = 0;
	private static final int SYSTEM_VERSION = 0;
	
	private SettingsManager() {

	}

	public static SettingsManager getInstance() {
		return instance;
	}

	public void setup(Plugin p) {
		SettingsManager.p = p;
		if (p.getConfig().getInt("config-version") == SurvivalGames.config_version) {
			SurvivalGames.config_todate = true;
		}else{
			File config = new File(p.getDataFolder(), "config.yml");
			config.delete();
		}
		
		p.getConfig().options().copyDefaults(true);
		p.saveDefaultConfig();
		
		f = new File(p.getDataFolder(), "spawns.yml");
		f2 = new File(p.getDataFolder(), "system.yml");
		f4 = new File(p.getDataFolder(), "messages.yml");
		chestFile = new File(p.getDataFolder(), "items.json");

		try {
			if (!f.exists()) 	f.createNewFile();
			if (!f2.exists())	f2.createNewFile();
			if (!f4.exists()) 	loadFile("messages.yml");
			if (!chestFile.exists()) 	loadFile("items.json");

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		reloadSystem();
		saveSystemConfig();
		
		reloadSpawns();
		saveSpawns();
				
		reloadMessages();
		saveMessages();
	}

	public void set(String arg0, Object arg1) {
		p.getConfig().set(arg0, arg1);
	}

	public FileConfiguration getConfig() {
		return p.getConfig();
	}

	public FileConfiguration getSystemConfig() {
		return system;
	}

	public FileConfiguration getSpawns() {
		return spawns;
	}

	public FileConfiguration getKits() {
		return kits;
	}
	
	public File getChestFile() {
		return chestFile;
	}
	
	public FileConfiguration getMessageConfig() {
		//System.out.println("asdf"+messages.getString("prefix.main"));
		return messages;
	}


	public void saveConfig() {
		// p.saveConfig();
	}

	public static World getGameWorld(int game) {
		if (SettingsManager.getInstance().getSystemConfig().getString("sg-system.arenas." + game + ".world") == null) {
			//LobbyManager.getInstance().error(true);
			return null;

		}
		return p.getServer().getWorld(SettingsManager.getInstance().getSystemConfig().getString("sg-system.arenas." + game + ".world"));
	}

	public void reloadConfig(){
		p.reloadConfig();
	}
	
	public boolean moveFile(File ff){
		SurvivalGames.$("Moving outdated config file. "+f.getName());
		String name = ff.getName();
		File ff2 = new File(SurvivalGames.getPluginDataFolder(), getNextName(name, 0));
		return ff.renameTo(ff2);
	}
	
	public String getNextName(String name, int n){
		File ff = new File(SurvivalGames.getPluginDataFolder(), name+".old"+n);
		if(!ff.exists()){
			return ff.getName();
		}
		else{
			return getNextName(name, n+1);
		}
	}
	
        public void reloadChestItems() {
            chestFile = new File(p.getDataFolder(), "items.json");
            try {
                if (!chestFile.exists()) 	loadFile("items.json");
            } catch (Exception ex) {
                return;
            }
            
            ChestRatioStorage.getInstance().setup();
        }
	
	public void reloadSpawns() {
		spawns = YamlConfiguration.loadConfiguration(f);
		if(spawns.getInt("version", 0) != SPAWN_VERSION){
			moveFile(f);
			reloadSpawns();
		}
		spawns.set("version", SPAWN_VERSION);
		saveSpawns();
	}

	public void reloadSystem() {
		system = YamlConfiguration.loadConfiguration(f2);
		if(system.getInt("version", 0) != SYSTEM_VERSION){
			moveFile(f2);
			reloadSystem();
		}
		system.set("version", SYSTEM_VERSION);
		saveSystemConfig();
	}
	
	public void reloadMessages() {
		messages = YamlConfiguration.loadConfiguration(f4);
		if(messages.getInt("version", 0) != MESSAGE_VERSION){
			moveFile(f4);
			loadFile("messages.yml");
		}
		messages.set("version", MESSAGE_VERSION);
		saveMessages();
	}

	public void saveSystemConfig() {
		try {
			system.save(f2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveSpawns() {
		try {
			spawns.save(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveMessages() {
		try {
			messages.save(f4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveChest() {
		
	}

	public int getSpawnCount(int gameid) {
		return spawns.getInt("spawns." + gameid + ".count");
	}



	//TODO: Implement per-arena settings aka flags
	public HashMap < String, Object > getGameFlags(int a) {
		HashMap < String, Object > flags = new HashMap < String, Object > ();

		flags.put("AUTOSTART_PLAYERS", system.getInt("sg-system.arenas." + a + ".flags.autostart"));
		flags.put("AUTOSTART_VOTE", system.getInt("sg-system.arenas." + a + ".flags.vote"));
		flags.put("ENDGAME_ENABLED", system.getBoolean("sg-system.arenas." + a + ".flags.endgame-enabled"));
		flags.put("ENDGAME_PLAYERS", system.getInt("sg-system.arenas." + a + ".flags.endgame-players"));
		flags.put("ENDGAME_CHEST", system.getBoolean("sg-system.arenas." + a + ".flags.endgame-chest"));
		flags.put("ENDGAME_LIGHTNING", system.getBoolean("sg-system.arenas." + a + ".flags.endgame-lightning"));
		flags.put("DUEL_PLAYERS", system.getInt("sg-system.arenas." + a + ".flags.endgame-duel-players"));
		flags.put("DUEL_TIME", system.getInt("sg-system.arenas." + a + ".flags.endgame-duel-time"));
		flags.put("DUEL_ENABLED", system.getBoolean("sg-system.arenas." + a + ".flags.endgame-duel"));
		flags.put("ARENA_NAME", system.getString("sg-system.arenas." + a + ".flags.arena-name"));
		flags.put("ARENA_COST", system.getInt("sg-system.arenas." + a + ".flags.arena-cost"));
		flags.put("ARENA_REWARD", system.getInt("sg-system.arenas." + a + ".flags.arena-reward"));
		flags.put("ARENA_MAXTIME", system.getInt("sg-system.arenas." + a + ".flags.arena-maxtime"));
		flags.put("SPONSOR_ENABLED", system.getBoolean("sg-system.arenas." + a + ".flags.sponsor-enabled"));
		flags.put("SPONSOR_MODE", system.getInt("sg-system.arenas." + a + ".flags.sponsor-mode"));

		return flags;

	}
	public void saveGameFlags(HashMap < String, Object > flags, int a) {

		system.set("sg-system.arenas." + a + ".flags.autostart", flags.get("AUTOSTART_PLAYERS"));
		system.set("sg-system.arenas." + a + ".flags.vote", flags.get("AUTOSTART_VOTE"));
		system.set("sg-system.arenas." + a + ".flags.endgame-enabled", flags.get("ENDGAME_ENABLED"));
		system.set("sg-system.arenas." + a + ".flags.endgame-players", flags.get("ENDGAME_PLAYERS"));
		system.set("sg-system.arenas." + a + ".flags.endgame-chest", flags.get("ENDGAME_CHEST"));
		system.set("sg-system.arenas." + a + ".flags.endgame-lightning", flags.get("ENDGAME_LIGHTNING"));
		system.set("sg-system.arenas." + a + ".flags.endgame-duel-players", flags.get("DUEL_PLAYERS"));
		system.set("sg-system.arenas." + a + ".flags.endgame-duel-time", flags.get("DUEL_TIME"));
		system.set("sg-system.arenas." + a + ".flags.endgame-duel", flags.get("DUEL_ENABLED"));
		system.set("sg-system.arenas." + a + ".flags.arena-name", flags.get("ARENA_NAME"));
		system.set("sg-system.arenas." + a + ".flags.arena-cost", flags.get("ARENA_COST"));
		system.set("sg-system.arenas." + a + ".flags.arena-reward", flags.get("ARENA_REWARD"));
		system.set("sg-system.arenas." + a + ".flags.arena-maxtime", flags.get("ARENA_MAXTIME"));
		system.set("sg-system.arenas." + a + ".flags.sponsor-enabled", flags.get("SPONSOR_ENABLED"));
		system.set("sg-system.arenas." + a + ".flags.sponsor-mode", flags.get("SPONSOR_MODE"));

		saveSystemConfig();

	}

	public Location getLobbySpawn() {
		try{
			return new Location(Bukkit.getWorld(system.getString("sg-system.lobby.spawn.world")),
				system.getDouble("sg-system.lobby.spawn.x"),
				system.getDouble("sg-system.lobby.spawn.y"),
				system.getDouble("sg-system.lobby.spawn.z"),
				(float)system.getDouble("sg-system.lobby.spawn.yaw"),
				(float)system.getDouble("sg-system.lobby.spawn.pitch")
				);
		}catch(Exception e){
			return null;
		}
	}

	public Location getSpawnPoint(int gameid, int spawnid) {
		return new Location(getGameWorld(gameid),
				spawns.getDouble("spawns." + gameid + "." + spawnid + ".x"),
				spawns.getDouble("spawns." + gameid + "." + spawnid + ".y"),
				spawns.getDouble("spawns." + gameid + "." + spawnid + ".z"),
				(float)spawns.getDouble("spawns." + gameid + "." + spawnid + ".yaw"),
				(float)spawns.getDouble("spawns." + gameid + "." + spawnid + ".pitch")
				);
	}
	
	public void setLobbySpawn(Location l) {
		system.set("sg-system.lobby.spawn.world", l.getWorld().getName());
		system.set("sg-system.lobby.spawn.x", l.getX());
		system.set("sg-system.lobby.spawn.y", l.getY());
		system.set("sg-system.lobby.spawn.z", l.getZ());
		system.set("sg-system.lobby.spawn.yaw", l.getYaw());
		system.set("sg-system.lobby.spawn.pitch", l.getPitch());
	}


	public void setSpawn(int gameid, int spawnid, Location l) {
		spawns.set("spawns." + gameid + "." + spawnid + ".x", l.getX());
		spawns.set("spawns." + gameid + "." + spawnid + ".y", l.getY());
		spawns.set("spawns." + gameid + "." + spawnid + ".z", l.getZ());
		spawns.set("spawns." + gameid + "." + spawnid + ".yaw", l.getYaw());
		spawns.set("spawns." + gameid + "." + spawnid + ".pitch", l.getPitch());
		
		if (spawnid > spawns.getInt("spawns." + gameid + ".count")) {
			spawns.set("spawns." + gameid + ".count", spawnid);
		}
		try {
			spawns.save(f);
		} catch (IOException e) {

		}
		GameManager.getInstance().getGame(gameid).addSpawn();
		
		LobbyManager.getInstance().updateWall(gameid);

	}

	public static String getSqlPrefix() {

		return getInstance().getConfig().getString("sql.prefix");
	}


	public void loadFile(String file){
		File t = new File(p.getDataFolder(), file);
		System.out.println("Writing new file: "+ t.getAbsolutePath());
			
			try {
				t.createNewFile();
				FileWriter out = new FileWriter(t);
				System.out.println(file);
				InputStream is = getClass().getResourceAsStream("/"+file);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					out.write(line+"\n");
					System.out.println(line);
				}
				out.flush();
				is.close();
				isr.close();
				br.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
}
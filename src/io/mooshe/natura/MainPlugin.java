package io.mooshe.natura;

import java.io.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MainPlugin extends JavaPlugin {

	private File file;
	public FileConfiguration cfg, defaults;
	private List<SnowTask> tasks = new ArrayList<SnowTask>();
	
	@Override
	public void onEnable() {
		file = new File(getDataFolder(), "config.yml");
		try {
			if(!file.exists()) {
				getLogger().warning("Could not find configuration file; loading defaults");
				getConfig().getDefaults().options().copyDefaults(true);
				saveDefaultConfig();
			}
			cfg = YamlConfiguration.loadConfiguration(file);
			defaults = YamlConfiguration.loadConfiguration(
					new InputStreamReader(getClass().getResourceAsStream("/config.yml")));
			for(String keys : defaults.getKeys(true)) {
				if(!cfg.contains(keys)) {
					updateConfig();
					break;
				}
			}
		} catch(Exception e) {
			getLogger().severe("Could not load configuration file! Disabling");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		getLogger().info("Registering Events");
		Bukkit.getPluginManager().registerEvents(
				new EventListener(this), this);
		
		getLogger().info("Registering Worlds");
		handleWeather();
		double intensity = cfg.getDouble("snow-rate", 1d);
		if(intensity <= 0d)
			return;
		for(String w : cfg.getStringList("snow-layering")) {
			World world = Bukkit.getWorld(w);
			if(world == null)
				getLogger().warning("Could not register world: "+w);
			else {
				SnowTask task = new SnowTask(world, cfg.getInt("snow-height", 4));
				task.runTaskTimer(this, 0, 1);
				tasks.add(task);
			}
		}
	}
	
	public void save() {
		try {
			cfg.save(file);
		} catch(Exception e) {
			getLogger().severe("Could not save config.yml!");
		}
	}
	
	private void updateConfig() {
		try {
			file.delete();
			file.createNewFile();
			for(String key : defaults.getKeys(true))
				if(!cfg.contains(key)) {
					cfg.set(key, defaults.get(key));
				}
			cfg.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		getLogger().info("Updated configuration with new values.");
	}
	
	private void handleWeather() {
		double
			rain = cfg.getDouble("rain-rate", 1d),
			thunder = cfg.getDouble("thunder-rate", 1d);
		for(World world : Bukkit.getWorlds()) {
			if(world.getEnvironment().equals(Environment.NORMAL))
				continue;
			
			if(world.hasStorm())
				world.setStorm(rain <= 0d);
			else
				world.setStorm(rain > 1d);
			
			if(world.isThundering())
				world.setThundering(thunder <= 0d);
			else
				world.setThundering(thunder > 1d);
		}
	}
	
	@Override
	public void onDisable() {
		save();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl,
			String[] args) {
		if(!cmd.getName().equalsIgnoreCase("natura"))
			return true;
		if(!sender.hasPermission("natura.admin")) {
			sender.sendMessage("\u00A7cYou do not have permission to use this command.");
			return true;
		}
		if(args.length < 2) {
			sender.sendMessage("\u00A7cUsage: /"+lbl+" <setting> <value>");
			return true;
		}
		String key = args[0];
		if(!cfg.contains(key)) {
			sender.sendMessage("\u00A7cNo setting found for value '"+key+"'.");
			return true;
		}
		String v = "";
		for(int i = 1; i < args.length; i++) {
			v += args[i] + " ";
		}
		v = v.trim();
		Object val = v;
		try {
			if(cfg.isDouble(key))
				val = Double.valueOf(v);
			else if(cfg.isInt(key))
				val = Integer.parseInt(v);
			else if(v.indexOf("%") > -1) 
				val = Double.valueOf(v.substring(0, v.indexOf("%"))) / 100d;
			else if(v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false"))
				val = Boolean.valueOf(v);
		} catch(Exception e) {
			sender.sendMessage("\u00A7dCould not convert value." +
					" Setting to raw value.");
		}
		cfg.set(key, val);
		sender.sendMessage("\u00A76Setting \u00A7a"+key
				+"\u00A76 to \u00A7b"+val);
		handleWeather();
		save();
		return true;
	}
}

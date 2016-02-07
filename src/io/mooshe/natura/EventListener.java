package io.mooshe.natura;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.weather.*;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

	private final MainPlugin plugin;
	private Random random = new Random();
	
	public EventListener(MainPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.LOWEST)
	public void onExplosion(EntityExplodeEvent event) {
		float y = (float) plugin.cfg.getDouble("explosion-yield-modifier", 1);
		event.setYield(event.getYield() * y);
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.LOWEST)
	public void onLeafDecay(LeavesDecayEvent event) {
		float decay = (float) plugin.cfg.getDouble("leaf-decay-modifier", 1);
		if(decay < random.nextFloat())
			event.setCancelled(true);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled=true, priority=EventPriority.LOWEST)
	public void onItemDespawn(ItemDespawnEvent event) {
		ItemStack item = event.getEntity().getItemStack();
		Block point = event.getLocation().getBlock();
		Block down = point.getRelative(BlockFace.DOWN);
		Material dt = down.getType(),
				pt = point.getType(),
				it = item.getType();
		if(it != Material.SAPLING ||
				(pt != Material.AIR && pt != Material.SNOW) ||
				(dt != Material.DIRT && dt != Material.GRASS))
			return;
		
		float r = (float) plugin.cfg.getDouble("sapling-plant-modifier", 0d);
		if(random.nextFloat() > r)
			return;
		
		BlockState s = point.getState();
		s.setType(item.getType());
		s.setRawData(item.getData().getData());
		BlockGrowEvent e = new BlockGrowEvent(point, s);
		Bukkit.getServer().getPluginManager().callEvent(e);
		if(e.isCancelled())
			return;
		s.update();
	}
	
	@EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
	public void onSnowLayerEvent(BlockFormEvent event) {
		if(event.getNewState().getType().equals(Material.SNOW)) {
			if(plugin.cfg.getDouble("snow-rate", 1d) < random.nextDouble())
				event.setCancelled(true);
			return;
		}
		
		if(event.getNewState().getType().equals(Material.ICE)) {
			if(plugin.cfg.getDouble("ice-form-modifier", 1d)
					< random.nextDouble())
				event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
	public void onWeatherChangeEvent(WeatherChangeEvent event) {
		double d = plugin.cfg.getDouble("rain-rate", 1d);
		boolean rain = event.toWeatherState();
		if(!rain) {
			if(d > 1d)
				event.setCancelled(true);
			return;
		}
		if(d < random.nextDouble())
			event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
	public void onThunderChangeEvent(ThunderChangeEvent event) {
		double d = plugin.cfg.getDouble("thunder-rate", 1d);
		boolean thunder = event.toThunderState();
		if(!thunder) {
			if(d > 1d)
				event.setCancelled(true);
			return;
		}
		if(d < random.nextDouble())
			event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
	public void onLightningEvent(LightningStrikeEvent event) {
		double d = plugin.cfg.getDouble("lightning-rate", 1d);
		if(d < random.nextDouble())
			event.setCancelled(true);
	}
}

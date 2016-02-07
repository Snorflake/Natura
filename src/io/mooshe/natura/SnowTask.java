package io.mooshe.natura;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowTask extends BukkitRunnable {

	private final Random random = new Random();
	
	public static final BlockFace[] FACES = new BlockFace[] {
		BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST
	};
	private final World world;
	private final byte max;
	
	public SnowTask(World world, int max) {
		this.world = world;
		this.max = (byte) Math.min(7, Math.max(0, max - 1));
	}
	
	@Override
	public void run() {
		if(!world.hasStorm())
			return;
		Chunk[] chunks = world.getLoadedChunks();
		for(int i = 0; i < chunks.length; i++) {
			Chunk c = chunks[i];
			int x = random.nextInt(16);
			int z = random.nextInt(16);
			Block b = world.getHighestBlockAt(
					(c.getX() * 16) + x,
					(c.getZ() * 16) + z);
			execute(b);
		}
	}

	@SuppressWarnings("deprecation")
	public void execute(Block orig) {
		double height = orig.getY();
		if(height < 95d || !orig.getType().name().equals("SNOW")) {
			return;
		}
		byte desiredH, startH = orig.getData();
		desiredH = startH;
		for(BlockFace face : FACES) {
			Block block = orig.getRelative(face);
			Material type = block.getType();
			if(!type.name().equals("SNOW") && type.isSolid()) {
				desiredH = max;
				break;
			} else {
				byte data = block.getData();
				if(desiredH < data)
					desiredH = data > max ? max : data;
			}
		}
		if(desiredH - 1 <= startH) {
			return;
		}
		double modifier = ((height - 48d)/240d) - ((startH/7) * 0.2d);
		if(modifier > 0.75)
			modifier = 0.75;
		if(random.nextDouble() < modifier) {
			BlockState state = orig.getState();
			byte up = (byte) (startH + 1);
			state.setRawData(up);
			BlockFormEvent event = new BlockFormEvent(orig, state);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(!event.isCancelled())
				state.update();
			return;
		}
	}
}

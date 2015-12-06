package org.maxgamer.QuickShop.Command;

import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.PluginHelper.utils.StringUtil;

public class CommandFind extends BaseCommand {
	QuickShop plugin;

	public CommandFind(final QuickShop plugin) {
		super("f");
		this.plugin = plugin;
		setMinimumArguments(2);
		setOnlyPlayerExecutable();
		setPermission("quickshop.find");
		setDescription(MsgUtil.p("command.description.find"));
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		String lookFor = StringUtil.consolidateStrings(args, 0);
		lookFor = lookFor.toLowerCase();
		final Player p = (Player) sender;
		final Location loc = p.getEyeLocation().clone();
		final double minDistance = plugin.getConfig().getInt("shop.find-distance");
		double minDistanceSquared = minDistance * minDistance;
		final int chunkRadius = (int) minDistance / 16 + 1;
		Shop closest = null;
		final Chunk c = loc.getChunk();
		for (int x = -chunkRadius + c.getX(); x < chunkRadius + c.getX(); x++) {
			for (int z = -chunkRadius + c.getZ(); z < chunkRadius + c.getZ(); z++) {
				final Chunk d = c.getWorld().getChunkAt(x, z);
				final HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(d);
				if (inChunk == null) {
					continue;
				}
				for (final Shop shop : inChunk.values()) {
					if (shop.getDataName().toLowerCase().contains(lookFor) && shop.getLocation().distanceSquared(loc) < minDistanceSquared) {
						closest = shop;
						minDistanceSquared = shop.getLocation().distanceSquared(loc);
					}
				}
			}
		}
		if (closest == null) {
			sender.sendMessage(MsgUtil.p("no-nearby-shop", args[0]));
			return;
		}
		final Location lookat = closest.getLocation().clone().add(0.5, 0.5, 0.5);
		// Hack fix to make /qs find not used by /back
		p.teleport(this.lookAt(loc, lookat).add(0, -1.62, 0), TeleportCause.PLUGIN);
		p.sendMessage(MsgUtil.p("nearby-shop-this-way", "" + (int) Math.floor(Math.sqrt(minDistanceSquared))));
		return;
	}

	/**
	 * Returns loc with modified pitch/yaw angles so it faces lookat
	 *
	 * @param loc
	 *            The location a players head is
	 * @param lookat
	 *            The location they should be looking
	 * @return The location the player should be facing to have their crosshairs
	 *         on the location lookAt Kudos to bergerkiller for most of this
	 *         function
	 */
	public Location lookAt(Location loc, final Location lookat) {
		// Clone the loc to prevent applied changes to the input loc
		loc = loc.clone();
		// Values of change in distance (make it relative)
		final double dx = lookat.getX() - loc.getX();
		final double dy = lookat.getY() - loc.getY();
		final double dz = lookat.getZ() - loc.getZ();
		// Set yaw
		if (dx != 0) {
			// Set yaw start value based on dx
			if (dx < 0) {
				loc.setYaw((float) (1.5 * Math.PI));
			} else {
				loc.setYaw((float) (0.5 * Math.PI));
			}
			loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
		} else if (dz < 0) {
			loc.setYaw((float) Math.PI);
		}
		// Get the distance from dx/dz
		final double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
		final float pitch = (float) -Math.atan(dy / dxz);
		// Set values, convert to degrees
		// Minecraft yaw (vertical) angles are inverted (negative)
		loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI + 360);
		// But pitch angles are normal
		loc.setPitch(pitch * 180f / (float) Math.PI);
		return loc;
	}
}

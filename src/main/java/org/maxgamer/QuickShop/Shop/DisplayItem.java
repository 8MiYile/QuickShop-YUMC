package org.maxgamer.QuickShop.Shop;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.maxgamer.QuickShop.Util.NMS;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 *         cannot be interacted with.
 */
public class DisplayItem {
	private final Shop shop;
	private final ItemStack iStack;
	private Item item;

	// private Location displayLoc;
	/**
	 * Creates a new display item.
	 *
	 * @param shop
	 *            The shop (See Shop)
	 * @param iStack
	 *            The item stack to clone properties of the display item from.
	 */
	public DisplayItem(final Shop shop, final ItemStack iStack) {
		this.shop = shop;
		this.iStack = iStack.clone();
		// this.displayLoc = shop.getLocation().clone().add(0.5, 1.2, 0.5);
	}

	/**
	 * @return Returns the exact location of the display item. (1 above shop
	 *         block, in the center)
	 */
	public Location getDisplayLocation() {
		return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
	}

	/**
	 * Returns the reference to this shops item. Do not modify.
	 */
	public Item getItem() {
		return this.item;
	}

	/**
	 * Removes the display item.
	 */
	public void remove() {
		if (this.item == null) {
			return;
		}
		this.item.remove();
	}

	/**
	 * Removes all items floating ontop of the chest that aren't the display
	 * item.
	 */
	public boolean removeDupe() {
		if (shop.getLocation().getWorld() == null) {
			return false;
		}
		// QuickShop qs = (QuickShop)
		// Bukkit.getPluginManager().getPlugin("QuickShop");
		final Location displayLoc = shop.getLocation().getBlock().getRelative(0, 1, 0).getLocation();
		boolean removed = false;
		final Chunk c = displayLoc.getChunk();
		for (final Entity e : c.getEntities()) {
			if (!(e instanceof Item)) {
				continue;
			}
			if (this.item != null && e.getEntityId() == this.item.getEntityId()) {
				continue;
			}
			final Location eLoc = e.getLocation().getBlock().getLocation();
			if (eLoc.equals(displayLoc) || eLoc.equals(shop.getLocation())) {
				final ItemStack near = ((Item) e).getItemStack();
				// if its the same its a dupe
				if (this.shop.matches(near)) {
					e.remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	/**
	 * Spawns the new display item. Does not remove duplicate items.
	 */
	public void respawn() {
		remove();
		spawn();
	}

	/**
	 * Spawns the dummy item on top of the shop.
	 */
	public void spawn() {
		if (shop.getLocation().getWorld() == null) {
			return;
		}
		final Location dispLoc = this.getDisplayLocation();
		try {
			this.item = shop.getLocation().getWorld().dropItem(dispLoc, this.iStack);
			this.item.setVelocity(new Vector(0, 0.1, 0));
			NMS.safeGuard(this.item);
		} catch (final Exception e) {
		}
	}
}
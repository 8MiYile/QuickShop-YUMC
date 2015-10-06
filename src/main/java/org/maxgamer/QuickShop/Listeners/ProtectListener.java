package org.maxgamer.QuickShop.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.Util.MarkUtil;

public class ProtectListener {

	@EventHandler
	public void onInvMove(final InventoryMoveItemEvent e) {
		final ItemStack ci = e.getItem();
		if (MarkUtil.hasMark(ci)) {
			e.setCancelled(true);
		}
	}

	// @EventHandler
	// public void onInvPickup(final InventoryPickupItemEvent e) {
	// final ItemStack ci = e.getItem().getItemStack();
	// if (MarkUtil.hasMark(ci)) {
	// e.setCancelled(true);
	// }
	// }

	@EventHandler
	public void onPlayerPickup(final PlayerPickupItemEvent e) {
		final ItemStack ci = e.getItem().getItemStack();
		if (MarkUtil.hasMark(ci)) {
			e.setCancelled(true);
		}
	}

}

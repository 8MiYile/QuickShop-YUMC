package org.maxgamer.QuickShop.Listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Info;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopAction;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class BlockListener implements Listener {
	private final QuickShop plugin;

	public BlockListener(final QuickShop plugin) {
		this.plugin = plugin;
	}

	/**
	 * Removes chests when they're destroyed.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreak(final BlockBreakEvent e) {
		final Block b = e.getBlock();
		final Player p = e.getPlayer();
		// If the shop was a chest
		if (b.getState() instanceof InventoryHolder) {
			final Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop == null) {
				return;
			}
			// If they're either survival or the owner, they can break it
			final ItemStack pinh = p.getItemInHand();
			if (p.getName().equals(shop.getOwner()) || p.getGameMode() == GameMode.SURVIVAL || pinh == null || pinh.getType() == plugin.getConfigManager().getSuperItem()) {
				// Cancel their current menu... Doesnt cancel other's menu's.
				final Info action = plugin.getShopManager().getActions().get(p.getName());
				if (action != null) {
					action.setAction(ShopAction.CANCELLED);
				}
				shop.delete();
				p.sendMessage(MsgUtil.p("success-removed-shop"));
			} else {
				e.setCancelled(true);
				p.sendMessage(MsgUtil.p("no-creative-break"));
				return;
			}
		} else if (b.getType() == Material.WALL_SIGN) {
			final Shop shop = getShopNextTo(b.getLocation());
			if (shop == null) {
				return;
			}
			e.setCancelled(true);
		}
	}

	/**
	 * Handles shops breaking through explosions
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onExplode(final EntityExplodeEvent e) {
		if (e.isCancelled()) {
			return;
		}
		for (int i = 0; i < e.blockList().size(); i++) {
			final Block b = e.blockList().get(i);
			final Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop != null) {
				shop.delete();
			}
		}
	}

	/**
	 * Listens for chest placement, so a doublechest shop can't be created.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlace(final BlockPlaceEvent e) {
		if (e.isCancelled()) {
			return;
		}
		final BlockState bs = e.getBlock().getState();
		if (bs instanceof DoubleChest == false) {
			return;
		}
		final Block b = e.getBlock();
		final Player p = e.getPlayer();
		final Block chest = Util.getSecondHalf(b);
		if (chest != null && plugin.getShopManager().getShop(chest.getLocation()) != null && !p.hasPermission("quickshop.create.double")) {
			e.setCancelled(true);
			p.sendMessage(MsgUtil.p("no-double-chests"));
		}
	}

	/**
	 * Gets the shop a sign is attached to
	 *
	 * @param loc
	 *            The location of the sign
	 * @return The shop
	 */
	private Shop getShopNextTo(final Location loc) {
		final Block b = Util.getAttached(loc.getBlock());
		// Util.getAttached(b)
		if (b == null) {
			return null;
		}
		return plugin.getShopManager().getShop(b.getLocation());
	}
}
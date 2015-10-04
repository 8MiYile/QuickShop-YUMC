package org.maxgamer.QuickShop.Listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Info;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopAction;
import org.maxgamer.QuickShop.Util.MarkUtil;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class PlayerListener implements Listener {
	private final QuickShop plugin;

	public PlayerListener(final QuickShop plugin) {
		this.plugin = plugin;
	}

	/*
	 * Could be useful one day private LinkedList<String> getParents(Class<?>
	 * clazz){ LinkedList<String> classes = new LinkedList<String>();
	 *
	 * while(clazz != null){ classes.add("Extends " + ChatColor.GREEN +
	 * clazz.getCanonicalName()); for(Class<?> iface : clazz.getInterfaces()){
	 * classes.add("Implements " + ChatColor.RED + iface.getCanonicalName());
	 * classes.addAll(getParents(iface)); }
	 *
	 * clazz = clazz.getSuperclass(); } return classes; }
	 */
	@SuppressWarnings("deprecation")
	/**
	 * Handles players left clicking a chest. Left click a NORMAL chest with
	 * item : Send creation menu Left click a SHOP chest : Send purchase menu
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onClick(final PlayerInteractEvent e) {
		if (e.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		final Block b = e.getClickedBlock();
		if (!Util.canBeShop(b) && b.getType() != Material.WALL_SIGN) {
			return;
		}
		final Player p = e.getPlayer();
		if (plugin.sneak && !p.isSneaking()) {
			// Sneak only
			return;
		}
		final Location loc = b.getLocation();
		final ItemStack item = e.getItem();
		// Get the shop
		Shop shop = plugin.getShopManager().getShop(loc);
		// If that wasn't a shop, search nearby shops
		if (shop == null && b.getType() == Material.WALL_SIGN) {
			final Block attached = Util.getAttached(b);
			if (attached != null) {
				shop = plugin.getShopManager().getShop(attached.getLocation());
			}
		}
		// Purchase handling
		if (shop != null && p.hasPermission("quickshop.use") && (plugin.sneakTrade == false || p.isSneaking())) {
			shop.onClick();
			// Text menu
			MsgUtil.sendShopInfo(p, shop);
			if (shop.isSelling()) {
				p.sendMessage(MsgUtil.p("how-many-buy"));
			} else {
				final int items = Util.countItems(p.getInventory(), shop.getItem());
				p.sendMessage(MsgUtil.p("how-many-sell", "" + items));
			}
			// Add the new action
			final HashMap<UUID, Info> actions = plugin.getShopManager().getActions();
			final Info info = new Info(shop.getLocation(), ShopAction.BUY, null, null, shop);
			actions.put(p.getUniqueId(), info);
			return;
		}
		// Handles creating shops
		else if (!e.isCancelled() && shop == null && item != null && item.getType() != Material.AIR && p.hasPermission("quickshop.create.sell") && Util.canBeShop(b)
				&& p.getGameMode() != GameMode.CREATIVE && (plugin.sneakCreate == false || p.isSneaking())) {
			if (!plugin.getShopManager().canBuildShop(p, b, e.getBlockFace())) {
				// As of the new checking system, most plugins will tell the
				// player why they can't create a shop there.
				// So telling them a message would cause spam etc.
				return;
			}
			if (Util.getSecondHalf(b) != null && !p.hasPermission("quickshop.create.double")) {
				p.sendMessage(MsgUtil.p("no-double-chests"));
				return;
			}
			if (Util.isBlacklisted(item.getType()) && !p.hasPermission("quickshop.bypass." + item.getTypeId())) {
				p.sendMessage(MsgUtil.p("blacklisted-item"));
				return;
			}
			// Finds out where the sign should be placed for the shop
			Block last = null;
			final Location from = p.getLocation().clone();
			from.setY(b.getY());
			from.setPitch(0);
			final BlockIterator bIt = new BlockIterator(from, 0, 7);
			while (bIt.hasNext()) {
				final Block n = bIt.next();
				if (n.equals(b)) {
					break;
				}
				last = n;
			}
			// Send creation menu.
			final Info info = new Info(b.getLocation(), ShopAction.CREATE, e.getItem(), last);
			plugin.getShopManager().getActions().put(p.getUniqueId(), info);
			p.sendMessage(MsgUtil.p("how-much-to-trade-for", Util.getName(info.getItem())));
		}
	}

	@EventHandler
	public void onItemClick(final InventoryClickEvent e) {
		final ItemStack ci = e.getCurrentItem();
		if (MarkUtil.hasMark(ci)) {
			ci.setType(Material.AIR);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		// Notify the player any messages they were sent
		Bukkit.getScheduler().runTaskLater(QuickShop.instance, new Runnable() {
			@Override
			public void run() {
				MsgUtil.flush(e.getPlayer());
			}
		}, 60);
	}

	@EventHandler(priority = EventPriority.HIGH)
	/**
	 * Waits for a player to move too far from a shop, then cancels the menu.
	 */
	public void onMove(final PlayerMoveEvent e) {
		if (e.isCancelled()) {
			return;
		}
		final Info info = plugin.getShopManager().getActions().get(e.getPlayer().getUniqueId());
		if (info != null) {
			final Player p = e.getPlayer();
			final Location loc1 = info.getLocation();
			final Location loc2 = p.getLocation();
			if (loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > 25) {
				if (info.getAction() == ShopAction.CREATE) {
					p.sendMessage(MsgUtil.p("shop-creation-cancelled"));
				} else if (info.getAction() == ShopAction.BUY) {
					p.sendMessage(MsgUtil.p("shop-purchase-cancelled"));
				}
				plugin.getShopManager().getActions().remove(p.getUniqueId());
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerPickup(final PlayerPickupItemEvent e) {
		final ItemStack ci = e.getItem().getItemStack();
		e.setCancelled(!MarkUtil.hasMark(ci));
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent e) {
		// Remove them from the menu
		plugin.getShopManager().getActions().remove(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onTeleport(final PlayerTeleportEvent e) {
		final PlayerMoveEvent me = new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo());
		onMove(me);
	}
}
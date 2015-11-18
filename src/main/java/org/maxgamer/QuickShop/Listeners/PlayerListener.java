package org.maxgamer.QuickShop.Listeners;

import java.util.HashMap;

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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Info;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopAction;
import org.maxgamer.QuickShop.Shop.ShopType;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class PlayerListener implements Listener {
	private final QuickShop plugin;

	public PlayerListener(final QuickShop plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	/**
	 * Handles players left clicking a chest. Left click a NORMAL chest with
	 * item : Send creation menu Left click a SHOP chest : Send purchase menu
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onClick(final PlayerInteractEvent e) {
		final Block b = e.getClickedBlock();
		final Player p = e.getPlayer();
		if (e.getAction() != Action.LEFT_CLICK_BLOCK || (e.getMaterial() == plugin.getConfigManager().getSuperItem() && b.getType() == Material.WALL_SIGN)) {
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
		if (shop != null && p.hasPermission("quickshop.use") && (plugin.getConfigManager().isSneakTrade() == p.isSneaking())) {
			shop.onClick();
			// Text menu
			MsgUtil.sendShopInfo(p, shop);
			if (!plugin.getConfigManager().isEnableMagicLib() && b.getType() == Material.WALL_SIGN) {
				final Inventory in = Bukkit.createInventory(null, 9, plugin.getConfigManager().getGuiTitle());
				in.setItem(4, shop.getItem());
				p.openInventory(in);
			}
			// Add the new action
			final HashMap<String, Info> actions = plugin.getShopManager().getActions();
			final Info info = new Info(shop.getLocation(), ShopAction.BUY, null, null, shop);
			actions.put(p.getName(), info);
			return;
		}
		// Handles creating shops
		else if (shop == null && item != null && item.getType() != Material.AIR && p.hasPermission("quickshop.create.sell") && Util.canBeShop(b) && p.getGameMode() != GameMode.CREATIVE
				&& (plugin.getConfigManager().isSneakCreate() == p.isSneaking())) {
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
			plugin.getShopManager().getActions().put(p.getName(), info);
			p.sendMessage(MsgUtil.p("how-much-to-trade-for", Util.getName(info.getItem())));
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

	/**
	 * Waits for a player to move too far from a shop, then cancels the menu.
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onMove(final PlayerMoveEvent e) {
		final Info info = plugin.getShopManager().getActions().get(e.getPlayer().getName());
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
				plugin.getShopManager().getActions().remove(p.getName());
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent e) {
		// Remove them from the menu
		plugin.getShopManager().getActions().remove(e.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSuperItemClick(final PlayerInteractEvent e) {
		final Player p = e.getPlayer();
		if (p.getGameMode() != GameMode.SURVIVAL || e.getMaterial() != plugin.getConfigManager().getSuperItem()) {
			return;
		}
		final Block b = e.getClickedBlock();
		if (b == null || b.getType() == null) {
			return;
		}
		// If that wasn't a shop, search nearby shops
		if (b.getType() == Material.WALL_SIGN) {
			final Block attached = Util.getAttached(b);
			final Shop shop = attached == null ? null : plugin.getShopManager().getShop(attached.getLocation());
			if (shop != null) {
				final Location loc = shop.getLocation();
				String shopmode = "";
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (p.hasPermission("quickshop.unlimited") && (shop.getOwner().equalsIgnoreCase(p.getName()) || p.isOp())) {
						shop.setUnlimited(!shop.isUnlimited());
						shopmode = shop.isUnlimited() ? "§e无限模式" : "§c有限模式";
						p.sendMessage(MsgUtil.p("command.toggle-unlimited", shopmode));
						return;
					}
				} else {
					if (shop.getShopType() == ShopType.BUYING && p.hasPermission("quickshop.create.sell") && (shop.getOwner().equalsIgnoreCase(p.getName()) || p.isOp())) {
						shop.setShopType(ShopType.SELLING);
						p.sendMessage(MsgUtil.p("command.now-selling", shop.getDataName()));
						shopmode = "出售模式";
						return;
					} else if (shop.getShopType() == ShopType.SELLING && p.hasPermission("quickshop.create.buy") && (shop.getOwner().equalsIgnoreCase(p.getName()) || p.isOp())) {
						shop.setShopType(ShopType.BUYING);
						p.sendMessage(MsgUtil.p("command.now-buying", shop.getDataName()));
						shopmode = "收购模式";
						return;
					}
				}
				if (!shopmode.isEmpty()) {
					plugin.log(String.format("玩家: %s 将 %s(%s,%s,%s) 的商店切换为 %s !", p.getName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), shopmode));
				}
				shop.setSignText();
				shop.update();
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onTeleport(final PlayerTeleportEvent e) {
		final PlayerMoveEvent me = new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo());
		onMove(me);
	}
}
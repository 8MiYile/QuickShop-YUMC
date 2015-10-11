package org.maxgamer.QuickShop.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Util.MarkUtil;

public class ProtectListener implements Listener {

	private final QuickShop plugin;

	public ProtectListener(final QuickShop plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInvMove(final InventoryMoveItemEvent e) {
		final ItemStack ci = e.getItem();
		if (MarkUtil.hasMark(ci)) {
			e.setCancelled(true);
			final ItemStack[] items = e.getSource().getContents();
			for (final ItemStack itemStack : items) {
				if (MarkUtil.hasMark(itemStack)) {
					itemStack.setType(Material.AIR);
				}
			}
			e.getSource().setContents(items);
		}
	}

	@EventHandler
	public void onInvPickup(final InventoryPickupItemEvent e) {
		if (!plugin.getConfigManager().isPreventHopper()) {
			return;
		}
		final ItemStack ci = e.getItem().getItemStack();
		if (MarkUtil.hasMark(ci)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemClick(final InventoryClickEvent e) {
		final Player p = (Player) e.getWhoClicked();
		final ItemStack ci = e.getCurrentItem();
		final Inventory inv = e.getInventory();
		final int solt = e.getSlot();
		try {
			if (MarkUtil.hasMark(ci)) {
				inv.setItem(solt, new ItemStack(Material.AIR));
				Bukkit.broadcastMessage("§6[§b快捷商店§6] §4警告 " + p.getDisplayName() + " §c非法 §d§l获取 " + ci.getItemMeta().getDisplayName() + " §a已清理...");
			}
		} catch (final Exception ex) {
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDespawn(final ItemDespawnEvent e) {
		final ItemStack ci = e.getEntity().getItemStack();
		if (MarkUtil.hasMark(ci)) {
			ci.setType(Material.AIR);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerHandlerItem(final PlayerItemHeldEvent e) {
		final Player p = e.getPlayer();
		final ItemStack[] cis = p.getInventory().getArmorContents();
		for (final ItemStack itemStack : cis) {
			if (MarkUtil.hasMark(itemStack)) {
				Bukkit.broadcastMessage("§6[§b快捷商店§6] §4警告 " + p.getDisplayName() + " §c非法 §e§l穿戴 " + itemStack.getItemMeta().getDisplayName() + " §a已清理...");
				itemStack.setType(Material.AIR);
			}
		}
		p.getInventory().setArmorContents(cis);
	}

	@EventHandler
	public void onPlayerPickup(final PlayerPickupItemEvent e) {
		final ItemStack ci = e.getItem().getItemStack();
		if (MarkUtil.hasMark(ci)) {
			e.setCancelled(true);
		}
	}
}

package org.maxgamer.QuickShop.Listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.Util.MarkUtil;

import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.ItemPreCleanEvent;

public class WowSuchCleanerListener implements Listener {
	@EventHandler
	public void onWSCClear(final ItemPreCleanEvent e) {
		final List<Item> clearList = new ArrayList<Item>();
		final List<ItemStack> aucList = new ArrayList<ItemStack>();
		for (final Item item : e.getItemsToClean()) {
			if (MarkUtil.hasMark(item.getItemStack())) {
				clearList.add(item);
			}
		}
		for (final ItemStack itemStack : e.getItemsToAuction()) {
			if (MarkUtil.hasMark(itemStack)) {
				aucList.add(itemStack);
			}
		}
		e.getItemsToClean().removeAll(clearList);
		e.getItemsToAuction().removeAll(aucList);
	}
}

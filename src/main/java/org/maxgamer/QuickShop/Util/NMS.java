package org.maxgamer.QuickShop.Util;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class NMS {

	public static void safeGuard(final Item item) throws ClassNotFoundException {
		rename(item.getItemStack());
		item.setPickupDelay(2147483647);
	}

	private static void rename(final ItemStack iStack) {
		MarkUtil.addMark(iStack);
	}
}
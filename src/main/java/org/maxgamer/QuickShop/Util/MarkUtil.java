package org.maxgamer.QuickShop.Util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MarkUtil {
	static String mark = "§q§s§r";

	public static void addMark(final ItemStack ci) {
		final ItemMeta meta = ci.getItemMeta();
		meta.setDisplayName(mark + Util.getName(ci));
		ci.setItemMeta(meta);
	}

	public static boolean hasMark(final ItemStack ci) {
		try {
			return ci.getItemMeta().getDisplayName().startsWith(mark);
		} catch (final Exception e) {
			return false;
		}
	}
}

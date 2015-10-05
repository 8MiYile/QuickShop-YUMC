package org.maxgamer.QuickShop.Util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MarkUtil {
	static String mark = "§q§s§6[§b快捷商店§6] §c悬浮物品§r ";
	static int conut = 0;

	public static void addMark(final ItemStack ci) {
		final ItemMeta meta = ci.getItemMeta();
		meta.setDisplayName(mark + " " + Util.getName(ci) + " " + conut++);
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

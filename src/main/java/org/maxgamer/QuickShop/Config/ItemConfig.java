package org.maxgamer.QuickShop.Config;

import java.io.File;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.config.FileConfig;

public class ItemConfig {
	public static FileConfig config;
	public static File file;
	private static String CONFIG_NAME = "item.yml";

	public static String getItemName(final ItemStack i) {
		if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
			return i.getItemMeta().getDisplayName();
		}
		final String name = i.getType().name();
		final int dur = i.getDurability();
		final String dura = i.getMaxStackSize() != 1 ? dur != 0 ? "_" + dur : "" : "";
		final String iname = name + dura;
		return getItemName(iname);
	}

	public static String getItemName(final String iname) {
		String aname = config.getString(iname);
		if (aname == null) {
			aname = iname;
			config.set(iname, iname);
			config.save();
		}
		return aname;
	}

	public static void load(final Plugin p) {
		config = new FileConfig(p, CONFIG_NAME);
	}

	public static void reload() {
		config.reload();
	}

}

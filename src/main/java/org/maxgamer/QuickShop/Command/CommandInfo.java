package org.maxgamer.QuickShop.Command;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopChunk;
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandInfo extends BaseCommand {
	QuickShop plugin;

	public CommandInfo(final QuickShop plugin) {
		super("info");
		this.plugin = plugin;
		setPermission("quickshop.info");
		setDescription(MsgUtil.p("command.description.info"));
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		int buying, selling, doubles, chunks, worlds;
		buying = selling = doubles = chunks = worlds = 0;
		int nostock = 0;
		sender.sendMessage(ChatColor.RED + "检索商店信息中...");
		for (final HashMap<ShopChunk, HashMap<Location, Shop>> inWorld : plugin.getShopManager().getShops().values()) {
			worlds++;
			for (final HashMap<Location, Shop> inChunk : inWorld.values()) {
				chunks++;
				for (final Shop shop : inChunk.values()) {
					if (shop.isBuying()) {
						buying++;
					} else if (shop.isSelling()) {
						selling++;
					}
					if (shop instanceof ContainerShop && ((ContainerShop) shop).isDoubleShop()) {
						doubles++;
					} else if (shop.isSelling() && shop.getRemainingStock() == 0) {
						nostock++;
					}
				}
			}
		}
		sender.sendMessage(MsgUtil.p("info.title", chunks, buying + selling, worlds));
		sender.sendMessage(MsgUtil.p("info.selling", selling));
		sender.sendMessage(MsgUtil.p("info.buying", buying));
		sender.sendMessage(MsgUtil.p("info.double", doubles));
		sender.sendMessage(MsgUtil.p("info.canclean", nostock));
	}
}

package org.maxgamer.QuickShop.Command;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopType;
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandSell extends BaseCommand {
	QuickShop plugin;

	public CommandSell(final QuickShop plugin) {
		super("sell");
		this.plugin = plugin;
		setPermission("quickshop.create.sell");
		setOnlyPlayerExecutable();
		setDescription(MsgUtil.p("command.description.sell"));
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		final BlockIterator bIt = new BlockIterator((Player) sender, 10);
		while (bIt.hasNext()) {
			final Block b = bIt.next();
			final Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop != null && shop.getOwner().equals(((Player) sender).getUniqueId())) {
				shop.setShopType(ShopType.SELLING);
				shop.setSignText();
				shop.update();
				sender.sendMessage(MsgUtil.p("command.now-selling", shop.getDataName()));
				return;
			}
		}
		sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
		return;
	}
}

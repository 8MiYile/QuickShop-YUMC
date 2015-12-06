package org.maxgamer.QuickShop.Command;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandRefill extends BaseCommand {
	QuickShop plugin;

	public CommandRefill(final QuickShop plugin) {
		super("r");
		this.plugin = plugin;
		setMinimumArguments(1);
		setPossibleArguments("<数量>");
		setPermission("quickshop.refill");
		setDescription(MsgUtil.p("command.description.refill"));
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		int add;
		try {
			add = Integer.parseInt(args[0]);
		} catch (final NumberFormatException e) {
			sender.sendMessage(MsgUtil.p("thats-not-a-number"));
			return;
		}
		final BlockIterator bIt = new BlockIterator((Player) sender, 10);
		while (bIt.hasNext()) {
			final Block b = bIt.next();
			final Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop != null) {
				shop.add(shop.getItem(), add);
				sender.sendMessage(MsgUtil.p("refill-success"));
				return;
			}
		}
		sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
		return;
	}
}

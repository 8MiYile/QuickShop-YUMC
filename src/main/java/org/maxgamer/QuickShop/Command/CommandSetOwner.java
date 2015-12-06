package org.maxgamer.QuickShop.Command;

import org.bukkit.OfflinePlayer;
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

public class CommandSetOwner extends BaseCommand {
	QuickShop plugin;

	public CommandSetOwner(final QuickShop plugin) {
		super("so");
		this.plugin = plugin;
		setPermission("quickshop.setowner");
		setOnlyPlayerExecutable();
		setDescription(MsgUtil.p("command.description.setowner"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		if (args.length < 2) {
			sender.sendMessage(MsgUtil.p("command.no-owner-given"));
			return;
		}
		final BlockIterator bIt = new BlockIterator((Player) sender, 10);
		while (bIt.hasNext()) {
			final Block b = bIt.next();
			final Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop != null) {
				final OfflinePlayer p = this.plugin.getServer().getOfflinePlayer(args[1]);
				shop.setOwner(p.getName());
				shop.update();
				sender.sendMessage(MsgUtil.p("command.new-owner", this.plugin.getServer().getOfflinePlayer(shop.getOwner()).getName()));
				return;
			}
		}
		sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
		return;
	}

}

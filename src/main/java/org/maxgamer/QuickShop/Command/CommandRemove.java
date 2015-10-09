package org.maxgamer.QuickShop.Command;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandRemove extends BaseCommand {
	QuickShop plugin;

	public CommandRemove(final QuickShop plugin) {
		super("remove", "delete");
		this.plugin = plugin;
		setPermission("quickshop.delete");
		setOnlyPlayerExecutable();
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		final Player p = (Player) sender;
		final BlockIterator bIt = new BlockIterator(p, 10);
		while (bIt.hasNext()) {
			final Block b = bIt.next();
			final Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop != null) {
				if (shop.getOwner().equals(p.getName())) {
					shop.delete();
					sender.sendMessage(ChatColor.GREEN + "Success. Deleted shop.");
				} else {
					p.sendMessage(ChatColor.RED + "That's not your shop!");
				}
				return;
			}
		}
		p.sendMessage(ChatColor.RED + "No shop found!");
	}
}

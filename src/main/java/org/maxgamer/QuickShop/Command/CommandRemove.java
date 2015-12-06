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
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandRemove extends BaseCommand {
	QuickShop plugin;

	public CommandRemove(final QuickShop plugin) {
		super("r");
		this.plugin = plugin;
		setOnlyPlayerExecutable();
		setPermission("quickshop.delete");
		setDescription(MsgUtil.p("command.description.remove"));
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
					sender.sendMessage(ChatColor.GREEN + "商店已成功移除");
				} else {
					p.sendMessage(ChatColor.RED + "这个不是你的商店!");
				}
				return;
			}
		}
		p.sendMessage(ChatColor.RED + "未找到商店!");
	}
}

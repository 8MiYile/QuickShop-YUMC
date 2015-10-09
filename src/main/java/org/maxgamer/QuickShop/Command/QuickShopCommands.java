package org.maxgamer.QuickShop.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.QuickShop.QuickShop;

import cn.citycraft.PluginHelper.commands.DefaultCommand;
import cn.citycraft.PluginHelper.commands.HandlerSubCommand;

public class QuickShopCommands implements CommandExecutor, DefaultCommand {
	HandlerSubCommand hsc;
	QuickShop plugin;

	public QuickShopCommands(final QuickShop plugin) {
		this.plugin = plugin;
		hsc = new HandlerSubCommand(plugin);
		hsc.setDefaultCommand(this);
		hsc.registerCommand(new CommandClean(plugin));
		hsc.registerCommand(new CommandEmpty(plugin));
		hsc.registerCommand(new CommandExport(plugin));
		hsc.registerCommand(new CommandFind(plugin));
		hsc.registerCommand(new CommandInfo(plugin));
		hsc.registerCommand(new CommandPrice(plugin));
		hsc.registerCommand(new CommandRefill(plugin));
		hsc.registerCommand(new CommandReload(plugin));
		hsc.registerCommand(new CommandRemove(plugin));
		hsc.registerCommand(new CommandBuy(plugin));
		hsc.registerCommand(new CommandSetOwner(plugin));
		hsc.registerCommand(new CommandSell(plugin));
		hsc.registerCommand(new CommandUnlimited(plugin));
	}

	@Override
	public void defaultexecute(final CommandSender sender, final Command command, final String label) throws CommandException {
		hsc.sendHelp(sender, label);
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		return hsc.onCommand(sender, cmd, label, args);
	}

}
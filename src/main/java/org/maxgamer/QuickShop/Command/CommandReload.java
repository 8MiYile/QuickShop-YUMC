package org.maxgamer.QuickShop.Command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandReload extends BaseCommand {
	QuickShop plugin;

	public CommandReload(final QuickShop plugin) {
		super("reload");
		this.plugin = plugin;
		setPermission("quickshop.reload");
		setDescription(MsgUtil.p("command.description.reload"));
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		sender.sendMessage(MsgUtil.p("command.reloading"));
		Bukkit.getPluginManager().disablePlugin(plugin);
		Bukkit.getPluginManager().enablePlugin(plugin);
		plugin.reloadConfig();
		return;
	}
}

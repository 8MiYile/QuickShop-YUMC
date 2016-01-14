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

public class CommandSetOwner extends BaseCommand {
    QuickShop plugin;

    public CommandSetOwner(final QuickShop plugin) {
        super("so");
        this.plugin = plugin;
        setOnlyPlayerExecutable();
        setMinimumArguments(1);
        setPermission("quickshop.setowner");
        setDescription(MsgUtil.p("command.description.setowner"));
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        final BlockIterator bIt = new BlockIterator((Player) sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                shop.setOwner(args[0]);
                shop.update();
                sender.sendMessage(MsgUtil.p("command.new-owner", args[0]));
                return;
            }
        }
        sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
        return;
    }

}

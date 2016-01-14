package org.maxgamer.QuickShop.Command;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandEmpty extends BaseCommand {
    QuickShop plugin;

    public CommandEmpty(final QuickShop plugin) {
        super("e");
        this.plugin = plugin;
        setPermission("quickshop.empty");
        setDescription(MsgUtil.p("command.description.empty"));
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        final BlockIterator bIt = new BlockIterator((Player) sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (shop instanceof ContainerShop) {
                    final ContainerShop cs = (ContainerShop) shop;
                    cs.getInventory().clear();
                    sender.sendMessage(MsgUtil.p("empty-success"));
                } else {
                    sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
                }
                return;
            }
        }
        sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
        return;
    }
}

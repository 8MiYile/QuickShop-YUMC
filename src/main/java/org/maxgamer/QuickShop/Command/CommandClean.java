package org.maxgamer.QuickShop.Command;

import java.util.Iterator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandClean extends BaseCommand {
    QuickShop plugin;

    public CommandClean(final QuickShop plugin) {
        super("c");
        this.plugin = plugin;
        setPermission("quickshop.clean");
        setDescription(MsgUtil.p("command.description.clean"));
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        sender.sendMessage(MsgUtil.p("command.cleaning"));
        final Iterator<Shop> shIt = plugin.getShopManager().getShopIterator();
        int i = 0;
        while (shIt.hasNext()) {
            final Shop shop = shIt.next();
            try {
                if (shop.getLocation().getWorld() != null && shop.isSelling() && shop.getRemainingStock() == 0 && shop instanceof ContainerShop) {
                    final ContainerShop cs = (ContainerShop) shop;
                    if (cs.isDoubleShop()) {
                        continue;
                    }
                    shIt.remove(); // Is selling, but has no stock, and is a chest shop, but is not a double shop. Can be deleted safely.
                    i++;
                }
            } catch (final IllegalStateException e) {
                // shIt.remove(); // The shop is not there anymore, remove it
            }
        }
        MsgUtil.clean();
        sender.sendMessage(MsgUtil.p("command.cleaned", "" + i));
        return;
    }
}

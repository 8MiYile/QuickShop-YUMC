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

public class CommandUnlimited extends BaseCommand {
    QuickShop plugin;

    public CommandUnlimited(final QuickShop plugin) {
        super("u");
        this.plugin = plugin;
        setOnlyPlayerExecutable();
        setPermission("quickshop.unlimited");
        setDescription(MsgUtil.p("command.description.unlimited"));
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        final BlockIterator bIt = new BlockIterator((Player) sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                shop.setUnlimited(!shop.isUnlimited());
                shop.update();
                sender.sendMessage(MsgUtil.p("command.toggle-unlimited", (shop.isUnlimited() ? "无限模式" : "有限模式")));
                return;
            }
        }
        sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
        return;
    }

}

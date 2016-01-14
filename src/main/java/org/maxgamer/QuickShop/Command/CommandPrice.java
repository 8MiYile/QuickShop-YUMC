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

public class CommandPrice extends BaseCommand {
    QuickShop plugin;

    public CommandPrice(final QuickShop plugin) {
        super("p");
        this.plugin = plugin;
        setMinimumArguments(1);
        setOnlyPlayerExecutable();
        setPossibleArguments("<价格>");
        setPermission("quickshop.create.changeprice");
        setDescription(MsgUtil.p("command.description.price"));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        final Player p = (Player) sender;
        double price;
        try {
            price = Double.parseDouble(args[0]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MsgUtil.p("thats-not-a-number"));
            return;
        }
        if (price < 0.01) {
            sender.sendMessage(MsgUtil.p("price-too-cheap"));
            return;
        }
        double fee = 0;
        if (plugin.getConfigManager().isPriceChangeRequiresFee()) {
            fee = plugin.getConfigManager().getFeeForPriceChange();
            if (fee > 0 && plugin.getEcon().getBalance(p.getName()) < fee) {
                sender.sendMessage(MsgUtil.p("you-cant-afford-to-change-price", plugin.getEcon().format(fee)));
                return;
            }
        }
        final BlockIterator bIt = new BlockIterator(p, 10);
        // Loop through every block they're looking at upto 10 blocks away
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null && (shop.getOwner().equals(p.getName()) || sender.hasPermission("quickshop.other.price"))) {
                if (shop.getPrice() == price) {
                    // Stop here if there isn't a price change
                    sender.sendMessage(MsgUtil.p("no-price-change"));
                    return;
                }
                if (fee > 0) {
                    if (!plugin.getEcon().withdraw(p.getName(), fee)) {
                        sender.sendMessage(MsgUtil.p("you-cant-afford-to-change-price", plugin.getEcon().format(fee)));
                        return;
                    }
                    sender.sendMessage(MsgUtil.p("fee-charged-for-price-change", plugin.getEcon().format(fee)));
                    plugin.getEcon().deposit(plugin.getConfig().getString("tax-account"), fee);
                }
                // Update the shop
                shop.setPrice(price);
                shop.setSignText();
                shop.update();
                sender.sendMessage(MsgUtil.p("price-is-now", plugin.getEcon().format(shop.getPrice())));
                // Chest shops can be double shops.
                if (shop instanceof ContainerShop) {
                    final ContainerShop cs = (ContainerShop) shop;
                    if (cs.isDoubleShop()) {
                        final Shop nextTo = cs.getAttachedShop();
                        if (cs.isSelling()) {
                            if (cs.getPrice() < nextTo.getPrice()) {
                                sender.sendMessage(MsgUtil.p("buying-more-than-selling"));
                            }
                        } else {
                            // Buying
                            if (cs.getPrice() > nextTo.getPrice()) {
                                sender.sendMessage(MsgUtil.p("buying-more-than-selling"));
                            }
                        }
                    }
                }
                return;
            }
        }
        sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
        return;
    }
}

package org.maxgamer.QuickShop.Command;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Database.MySQLCore;
import org.maxgamer.QuickShop.Database.SQLiteCore;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopChunk;
import org.maxgamer.QuickShop.Shop.ShopType;
import org.maxgamer.QuickShop.Util.MsgUtil;

import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.commands.CommandSub;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.annotation.Sort;
import pw.yumc.YumCore.commands.interfaces.Executor;
import pw.yumc.YumCore.commands.interfaces.HelpParse;

public class QuickShopCommands implements Executor, HelpParse {
    QuickShop plugin = P.getPlugin();

    public QuickShopCommands() {
        new CommandSub("qs", this).setHelpParse(this);
    }

    @Sort(1)
    @Cmd(aliases = "b", permission = "quickshop.create.buy", executor = Cmd.Executor.PLAYER)
    @Help("command.description.buy")
    public void buy(Player player) {
        changeShopType(player, ShopType.BUYING);
    }

    @Sort(7)
    @Cmd(aliases = "c", permission = "quickshop.clean")
    @Help("command.description.clean")
    public void clean(CommandSender sender) {
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
            } catch (final IllegalStateException ex) {
                // shIt.remove(); // The shop is not there anymore, remove it
            }
        }
        MsgUtil.clean();
        sender.sendMessage(MsgUtil.p("command.cleaned", "" + i));
    }

    @Sort(5)
    @Cmd(aliases = "e", permission = "quickshop.empty", executor = Cmd.Executor.PLAYER)
    @Help("command.description.empty")
    public void empty(Player player) {
        final BlockIterator bIt = new BlockIterator(player, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (shop instanceof ContainerShop) {
                    final ContainerShop cs = (ContainerShop) shop;
                    cs.getInventory().clear();
                    player.sendMessage(MsgUtil.p("empty-success"));
                } else {
                    player.sendMessage(MsgUtil.p("not-looking-at-shop"));
                }
                return;
            }
        }
        player.sendMessage(MsgUtil.p("not-looking-at-shop"));
    }

    @Cmd(minimumArguments = 1, permission = "quickshop.export")
    @Help(value = "command.description.export", possibleArguments = "[mysql|sqlite]")
    public void export(CommandSender sender, String type) {
        if (type.startsWith("mysql")) {
            if (plugin.getDB().getCore() instanceof MySQLCore) {
                sender.sendMessage(ChatColor.RED + "数据已保存在 MySQL 无需转换!");
                return;
            }
            final ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("database");
            final String host = cfg.getString("host");
            final String port = cfg.getString("port");
            final String user = cfg.getString("user");
            final String pass = cfg.getString("password");
            final String name = cfg.getString("database");
            final MySQLCore core = new MySQLCore(host, user, pass, name, port);
            Database target;
            try {
                target = new Database(core);
                QuickShop.instance.getDB().copyTo(target);
                sender.sendMessage(ChatColor.GREEN + "导出成功 - 数据已保存至 MySQL " + user + "@" + host + "." + name);
            } catch (final Exception ex) {
                ex.printStackTrace();
                sender.sendMessage(ChatColor.RED + "导出数据到 MySQL 失败 " + user + "@" + host + "." + name + ChatColor.DARK_RED + " 由于: " + ex.getMessage());
            }
            return;
        }
        if (type.startsWith("sql") || type.contains("file")) {
            if (plugin.getDB().getCore() instanceof SQLiteCore) {
                sender.sendMessage(ChatColor.RED + "数据已保存在 SQLite 无需转换!");
                return;
            }
            final File file = new File(plugin.getDataFolder(), "shops.db");
            if (file.exists() && !file.delete()) {
                sender.sendMessage(ChatColor.RED + "警告: 删除旧的数据文件 shops.db 失败. 可能会导致部分信息错误.");
            }
            final SQLiteCore core = new SQLiteCore(file);
            try {
                final Database target = new Database(core);
                QuickShop.instance.getDB().copyTo(target);
                sender.sendMessage(ChatColor.GREEN + "导出成功 - 数据已保存至 SQLite: " + file.toString());
            } catch (final Exception ex) {
                ex.printStackTrace();
                sender.sendMessage(ChatColor.RED + "导出数据到 SQLite: " + file.toString() + " 失败 由于: " + ex.getMessage());
            }
        }
    }

    @Cmd(aliases = "f", minimumArguments = 1, permission = "quickshop.find", executor = Cmd.Executor.PLAYER)
    @Help("command.description.find")
    public void find(Player p, String lookFor) {
        lookFor = lookFor.toLowerCase();
        final Location loc = p.getEyeLocation().clone();
        final double minDistance = plugin.getConfig().getInt("shop.find-distance");
        double minDistanceSquared = minDistance * minDistance;
        final int chunkRadius = (int) minDistance / 16 + 1;
        Shop closest = null;
        final Chunk c = loc.getChunk();
        for (int x = -chunkRadius + c.getX(); x < chunkRadius + c.getX(); x++) {
            for (int z = -chunkRadius + c.getZ(); z < chunkRadius + c.getZ(); z++) {
                final Chunk d = c.getWorld().getChunkAt(x, z);
                final HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(d);
                if (inChunk == null) {
                    continue;
                }
                for (final Shop shop : inChunk.values()) {
                    if (shop.getDataName().toLowerCase().contains(lookFor) && shop.getLocation().distanceSquared(loc) < minDistanceSquared) {
                        closest = shop;
                        minDistanceSquared = shop.getLocation().distanceSquared(loc);
                    }
                }
            }
        }
        if (closest == null) {
            p.sendMessage(MsgUtil.p("no-nearby-shop", lookFor));
            return;
        }
        final Location lookat = closest.getLocation().clone().add(0.5, 0.5, 0.5);
        // Hack fix to make /qs find not used by /back
        p.teleport(this.lookAt(loc, lookat).add(0, -1.62, 0), TeleportCause.PLUGIN);
        p.sendMessage(MsgUtil.p("nearby-shop-this-way", "" + (int) Math.floor(Math.sqrt(minDistanceSquared))));
    }

    @Cmd(aliases = "i", permission = "quickshop.info")
    @Help("command.description.info")
    public void info(CommandSender sender) {
        int buying, selling, doubles, chunks, worlds, unlimited;
        buying = selling = doubles = chunks = worlds = unlimited = 0;
        int nostock = 0;
        sender.sendMessage(ChatColor.RED + "开始检索商店信息中...");
        for (final HashMap<ShopChunk, HashMap<Location, Shop>> inWorld : plugin.getShopManager().getShops().values()) {
            worlds++;
            for (final HashMap<Location, Shop> inChunk : inWorld.values()) {
                chunks++;
                for (final Shop shop : inChunk.values()) {
                    if (shop.isUnlimited()) {
                        unlimited++;
                    }
                    if (shop.isBuying()) {
                        buying++;
                    } else if (shop.isSelling()) {
                        selling++;
                    }
                    if (shop instanceof ContainerShop && ((ContainerShop) shop).isDoubleShop()) {
                        doubles++;
                    } else if (shop.isSelling() && shop.getRemainingStock() == 0) {
                        nostock++;
                    }
                }
            }
        }
        sender.sendMessage(MsgUtil.p("info.title", chunks, buying + selling, worlds));
        sender.sendMessage(MsgUtil.p("info.selling", selling));
        sender.sendMessage(MsgUtil.p("info.buying", buying));
        sender.sendMessage(MsgUtil.p("info.unlimited", unlimited));
        sender.sendMessage(MsgUtil.p("info.double", doubles));
        sender.sendMessage(MsgUtil.p("info.canclean", nostock));
    }

    @Override
    public String parse(final String str) {
        return MsgUtil.p(str);
    }

    @Sort(4)
    @Cmd(aliases = "p", minimumArguments = 1, permission = "quickshop.create.changeprice", executor = Cmd.Executor.PLAYER)
    @Help(value = "command.description.price", possibleArguments = "<价格>")
    public void price(Player sender, Double price) {
        if (price < 0.01) {
            sender.sendMessage(MsgUtil.p("price-too-cheap"));
            return;
        }
        double fee = 0;
        if (plugin.getConfigManager().isPriceChangeRequiresFee()) {
            fee = plugin.getConfigManager().getFeeForPriceChange();
            if (fee > 0 && plugin.getEcon().getBalance(sender.getName()) < fee) {
                sender.sendMessage(MsgUtil.p("you-cant-afford-to-change-price", plugin.getEcon().format(fee)));
                return;
            }
        }
        final BlockIterator bIt = new BlockIterator(sender, 10);
        // Loop through every block they're looking at upto 10 blocks away
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null && (shop.getOwner().equals(sender.getName()) || sender.hasPermission("quickshop.other.price"))) {
                if (shop.getPrice() == price) {
                    // Stop here if there isn't a price change
                    sender.sendMessage(MsgUtil.p("no-price-change"));
                    return;
                }
                if (fee > 0) {
                    if (!plugin.getEcon().withdraw(sender.getName(), fee)) {
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
    }

    @Sort(6)
    @Cmd(minimumArguments = 1, permission = "quickshop.refill", executor = Cmd.Executor.PLAYER)
    @Help(value = "command.description.refill", possibleArguments = "<数量>")
    public void refill(Player sender, Integer add) {
        final BlockIterator bIt = new BlockIterator(sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                shop.add(shop.getItem(), add);
                sender.sendMessage(MsgUtil.p("refill-success"));
                return;
            }
        }
        sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
    }

    @Cmd(permission = "quickshop.reload")
    @Help("command.description.reload")
    public void reload(CommandSender sender) {
        sender.sendMessage(MsgUtil.p("command.reloading"));
        plugin.reloadConfig();
        Bukkit.getPluginManager().disablePlugin(plugin);
        Bukkit.getPluginManager().enablePlugin(plugin);
    }

    @Sort(6)
    @Cmd(aliases = "r", permission = "quickshop.delete", executor = Cmd.Executor.PLAYER)
    @Help("command.description.remove")
    public void remove(Player p) {
        final BlockIterator bIt = new BlockIterator(p, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (shop.getOwner().equals(p.getName())) {
                    shop.delete();
                    p.sendMessage(ChatColor.GREEN + "商店已成功移除");
                } else {
                    p.sendMessage(ChatColor.RED + "这个不是你的商店!");
                }
                return;
            }
        }
        p.sendMessage(ChatColor.RED + "未找到商店!");
    }

    @Sort(2)
    @Cmd(aliases = "s", permission = "quickshop.create.sell", executor = Cmd.Executor.PLAYER)
    @Help("command.description.sell")
    public void sell(Player player) {
        changeShopType(player, ShopType.SELLING);
    }

    @Sort(3)
    @Cmd(aliases = "so", minimumArguments = 1, permission = "quickshop.setowner", executor = Cmd.Executor.PLAYER)
    @Help("command.description.setowner")
    public void setowner(CommandSender sender, String owner) {
        final BlockIterator bIt = new BlockIterator((Player) sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                shop.setOwner(owner);
                shop.update();
                sender.sendMessage(MsgUtil.p("command.new-owner", owner));
                return;
            }
        }
        sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
    }

    @Sort(0)
    @Cmd(permission = "quickshop.unlimited", executor = Cmd.Executor.PLAYER)
    @Help("command.description.unlimited")
    public void unlimited(Player sender) {
        final BlockIterator bIt = new BlockIterator(sender, 10);
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
    }

    private void changeShopType(final Player sender, final ShopType shopType) {
        final BlockIterator bIt = new BlockIterator(sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null && shop.getOwner().equals(sender.getName())) {
                shop.setShopType(shopType);
                shop.setSignText();
                shop.update();
                String msgtype = "";
                switch (shopType) {
                case BUYING:
                    msgtype = "command.now-buying";
                    break;
                case SELLING:
                    msgtype = "command.now-selling";
                    break;
                }
                sender.sendMessage(MsgUtil.p(msgtype, shop.getDataName()));
                return;
            }
        }
        sender.sendMessage(MsgUtil.p("not-looking-at-shop"));
    }

    /**
     * Returns loc with modified pitch/yaw angles so it faces lookat
     *
     * @param loc
     *            The location a players head is
     * @param lookat
     *            The location they should be looking
     * @return The location the player should be facing to have their crosshairs
     *         on the location lookAt Kudos to bergerkiller for most of this
     *         function
     */
    private Location lookAt(Location loc, final Location lookat) {
        // Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();
        // Values of change in distance (make it relative)
        final double dx = lookat.getX() - loc.getX();
        final double dy = lookat.getY() - loc.getY();
        final double dz = lookat.getZ() - loc.getZ();
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        // Get the distance from dx/dz
        final double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        final float pitch = (float) -Math.atan(dy / dxz);
        // Set values, convert to degrees
        // Minecraft yaw (vertical) angles are inverted (negative)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI + 360);
        // But pitch angles are normal
        loc.setPitch(pitch * 180f / (float) Math.PI);
        return loc;
    }

}
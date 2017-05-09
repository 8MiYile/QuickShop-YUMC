package org.maxgamer.QuickShop.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.config.FileConfig;
import pw.yumc.YumCore.tellraw.Tellraw;

public class MsgUtil {
    private static FileConfig messages;
    private static HashMap<String, LinkedList<String>> player_messages = new HashMap<>();
    private static QuickShop plugin;

    /**
     * Deletes any messages that are older than a week in the database, to save
     * on space.
     */
    public static void clean() {
        plugin.getLogger().info("清理超过 一周 的 商店交易记录...");
        // 604800,000 msec = 1 week.
        final long weekAgo = System.currentTimeMillis() - 604800000;
        plugin.getDB().execute("DELETE FROM messages WHERE time < ?", weekAgo);
    }

    /**
     * Empties the queue of messages a player has and sends them to the player.
     *
     * @param p
     *            The player to message
     * @return true if success, false if the player is offline or null
     */
    public static void flush(final OfflinePlayer p) {
        if (p != null && p.isOnline()) {
            final String pName = p.getName();
            final LinkedList<String> msgs = player_messages.get(pName);
            if (msgs != null) {
                for (final String msg : msgs) {
                    p.getPlayer().sendMessage(msg);
                }
                plugin.getDB().execute("DELETE FROM messages WHERE owner = ?", pName);
                msgs.clear();
            }
        }
    }

    public static void init(final QuickShop plugin) {
        MsgUtil.plugin = plugin;
        // Load messages.yml
        messages = new FileConfig("messages.yml");
        // Parse colour codes
        Util.parseColours(messages);
    }

    /**
     * loads all player purchase messages from the database.
     */
    public static void loadTransactionMessages() {
        player_messages.clear(); // Delete old messages
        try {
            final ResultSet rs = plugin.getDB().getConnection().prepareStatement("SELECT * FROM messages").executeQuery();
            while (rs.next()) {
                final String owner = rs.getString("owner");
                final String message = rs.getString("message");
                LinkedList<String> msgs = player_messages.computeIfAbsent(owner, k -> new LinkedList<>());
                msgs.add(message);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            Log.d("无法从数据库获得玩家的交易记录 跳过...");
        }
    }

    public static String p(final String loc, final Object... args) {
        String raw = messages.getString(loc);
        if (raw == null || raw.isEmpty()) { return ChatColor.RED + "语言文件词条丢失: " + loc; }
        if (args == null) { return raw; }
        for (int i = 0; i < args.length; i++) {
            raw = raw.replace("{" + i + "}", args[i] == null ? "null" : args[i].toString());
        }
        return raw;
    }

    public static void reload() {
        messages.reload();
        Util.parseColours(messages);
    }

    /**
     * @param player
     *            The name of the player to message
     * @param message
     *            The message to send them Sends the given player a message if
     *            they're online. Else, if they're not online, queues it for
     *            them in the database.
     */
    public static void send(final String player, final String message) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final OfflinePlayer p = Bukkit.getOfflinePlayer(player);
            if (p == null || !p.isOnline()) {
                LinkedList<String> msgs = player_messages.computeIfAbsent(player, k -> new LinkedList<>());
                msgs.add(message);
                final String q = "INSERT INTO messages (owner, message, time) VALUES (?, ?, ?)";
                plugin.getDB().execute(q, player, message, System.currentTimeMillis());
            } else {
                p.getPlayer().sendMessage(message);
            }
        });
    }

    public static void sendItemMessage(final Player p, final ItemStack is, final String msg) {
        try {
            final Tellraw fm = Tellraw.create();
            fm.text(msg).item(is).send(p);
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError e) {
            plugin.getConfigManager().setEnableMagicLib(false);
            p.sendMessage(msg);
        }
    }

    public static void sendPurchaseSuccess(final Player p, final Shop shop, final int amount) {
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.successful-purchase"));
        final String msg = ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.item-name-and-price", "" + amount, shop.getDataName(), Util.format((amount * shop.getPrice())));
        sendItemMessage(p, shop.getItem(), msg);
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
    }

    public static void sendSellSuccess(final Player p, final Shop shop, final int amount) {
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.successfully-sold"));
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.item-name-and-price", "" + amount, shop.getDataName(), Util.format((amount * shop.getPrice()))));
        if (plugin.getConfig().getBoolean("show-tax")) {
            final double tax = plugin.getConfig().getDouble("tax");
            final double total = amount * shop.getPrice();
            if (tax != 0) {
                if (!p.getName().equals(shop.getOwner())) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.sell-tax", "" + Util.format((tax * total))));
                } else {
                    p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.sell-tax-self"));
                }
            }
        }
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
    }

    public static void sendShopInfo(final Player p, final Shop shop) {
        sendShopInfo(p, shop, shop.getRemainingStock());
    }

    public static void sendShopInfo(final Player p, final Shop shop, final int stock) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Potentially faster with an array?
            final ItemStack item = shop.getItem();
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.shop-information"));
            p.sendMessage(ChatColor.DARK_PURPLE + "| "
                    + MsgUtil.p("menu.owner", Bukkit.getOfflinePlayer(shop.getOwner()).getName() == null ? (shop.isUnlimited() ? "系统商店" : "未知") : Bukkit.getOfflinePlayer(shop.getOwner()).getName()));
            final String msg = ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.item", shop.getDataName());
            sendItemMessage(p, shop.getItem(), msg);
            if (Util.isTool(item.getType())) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.damage-percent-remaining", Util.getToolPercentage(item)));
            }
            if (shop.isSelling()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.stock", "" + (stock == 10000 ? "无限" : stock)));
            } else {
                final int space = shop.getRemainingSpace();
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.space", "" + (space == 10000 ? "无限" : space)));
            }
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.price-per", shop.getDataName(), Util.format(shop.getPrice())));
            if (shop.isBuying()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.this-shop-is-buying"));
            } else {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.p("menu.this-shop-is-selling"));
            }
            p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
            if (shop.isSelling()) {
                p.sendMessage(MsgUtil.p("how-many-buy"));
            } else {
                final int items = Util.countItems(p.getInventory(), shop.getItem());
                p.sendMessage(MsgUtil.p("how-many-sell", items));
            }
        });
    }
}
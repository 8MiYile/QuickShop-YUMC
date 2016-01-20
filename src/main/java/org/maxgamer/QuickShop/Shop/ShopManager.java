package org.maxgamer.QuickShop.Shop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class ShopManager {
    private final HashMap<String, Info> actions = new HashMap<String, Info>();

    private final QuickShop plugin;
    private final HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>> shops = new HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>>();

    public ShopManager(final QuickShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks other plugins to make sure they can use the chest they're making a
     * shop.
     *
     * @param p
     *            The player to check
     * @param b
     *            The block to check
     * @return True if they're allowed to place a shop there.
     */
    public boolean canBuildShop(final Player p, final Block b, final BlockFace bf) {
        if (plugin.getConfigManager().isLimit()) {
            int owned = 0;
            final Iterator<Shop> it = getShopIterator();
            while (it.hasNext()) {
                if (it.next().getOwner().equals(p.getName())) {
                    owned++;
                }
            }
            final int max = plugin.getShopLimit(p);
            if (owned + 1 > max) {
                p.sendMessage(MsgUtil.p("you-cant-create-more-shop", owned));
                return false;
            }
        }
        /* 修复其他插件调用产生的报错... */
        try {
            final PlayerInteractEvent pie = new PlayerInteractEvent(p, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), b, bf); // PIE = PlayerInteractEvent - What else?
            Bukkit.getPluginManager().callEvent(pie);
            pie.getPlayer().closeInventory(); // If the player has chat open, this will close their chat.
            if (pie.isCancelled()) {
                return false;
            }
        } catch (final Exception e) {
        }
        final ShopPreCreateEvent spce = new ShopPreCreateEvent(p, b.getLocation());
        Bukkit.getPluginManager().callEvent(spce);
        if (spce.isCancelled()) {
            return false;
        }
        return true;
    }

    /**
     * Removes all shops from memory and the world. Does not delete them from
     * the database. Call this on plugin disable ONLY.
     */
    public void clear() {
        if (plugin.getConfigManager().isDisplay()) {
            for (final World world : Bukkit.getWorlds()) {
                for (final Chunk chunk : world.getLoadedChunks()) {
                    final HashMap<Location, Shop> inChunk = this.getShops(chunk);
                    if (inChunk == null) {
                        continue;
                    }
                    for (final Shop shop : inChunk.values()) {
                        shop.onUnload();
                    }
                }
            }
        }
        this.actions.clear();
        this.shops.clear();
    }

    public void createShop(final Shop shop) {
        final Location loc = shop.getLocation();
        final ItemStack item = shop.getItem();
        try {
            // Write it to the database
            final String q = "INSERT INTO shops (owner, price, itemConfig, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            plugin.getDB().execute(q,
                    shop.getOwner().toString(),
                    shop.getPrice(),
                    Util.serialize(item),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    loc.getWorld().getName(),
                    (shop.isUnlimited() ? 1 : 0),
                    shop.getShopType().toID());
            // Add it to the world
            addShop(loc.getWorld().getName(), shop);
        } catch (final Exception e) {
            plugin.getLogger().warning("无法保存商店到数据库! 下次重启商店将会消失!");
            plugin.getLogger().warning("错误信息: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String format(final double d) {
        return plugin.getEcon().format(d);
    }

    /**
     * @return Returns the HashMap<Player name, shopInfo>. Info contains what
     *         their last question etc was.
     */
    public HashMap<String, Info> getActions() {
        return this.actions;
    }

    public Database getDatabase() {
        return plugin.getDB();
    }

    /**
     * Gets a shop in a specific location
     *
     * @param loc
     *            The location to get the shop from
     * @return The shop at that location
     */
    public Shop getShop(final Location loc) {
        final HashMap<Location, Shop> inChunk = getShops(loc.getChunk());
        if (inChunk == null) {
            return null;
        }
        // We can do this because WorldListener updates the world reference so
        // the world in loc is the same as world in inChunk.get(loc)
        return inChunk.get(loc);
    }

    /**
     * Returns a new shop iterator object, allowing iteration over shops easily,
     * instead of sorting through a 3D hashmap.
     *
     * @return a new shop iterator object.
     */
    public Iterator<Shop> getShopIterator() {
        return new ShopIterator();
    }

    /**
     * Returns a hashmap of World -> Chunk -> Shop
     *
     * @return a hashmap of World -> Chunk -> Shop
     */
    public HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>> getShops() {
        return this.shops;
    }

    /**
     * Returns a hashmap of Shops
     *
     * @param c
     *            The chunk to search. Referencing doesn't matter, only
     *            coordinates and world are used.
     * @return
     */
    public HashMap<Location, Shop> getShops(final Chunk c) {
        // long start = System.nanoTime();
        final HashMap<Location, Shop> shops = getShops(c.getWorld().getName(), c.getX(), c.getZ());
        // long end = System.nanoTime();
        // System.out.println("Chunk lookup in " + ((end - start)/1000000.0) +
        // "ms.");
        return shops;
    }

    /**
     * Returns a hashmap of Chunk -> Shop
     *
     * @param world
     *            The name of the world (case sensitive) to get the list of
     *            shops from
     * @return a hashmap of Chunk -> Shop
     */
    public HashMap<ShopChunk, HashMap<Location, Shop>> getShops(final String world) {
        return this.shops.get(world);
    }

    public HashMap<Location, Shop> getShops(final String world, final int chunkX, final int chunkZ) {
        final HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops(world);
        if (inWorld == null) {
            return null;
        }
        final ShopChunk shopChunk = new ShopChunk(world, chunkX, chunkZ);
        return inWorld.get(shopChunk);
    }

    public void handleChat(final Player p, final String msg) {
        final String message = ChatColor.stripColor(msg);
        // Use from the main thread, because Bukkit hates life
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                final HashMap<String, Info> actions = getActions();
                // They wanted to do something.
                final Info info = actions.remove(p.getName());
                if (info == null) {
                    return; // multithreaded means this can happen
                }
                if (info.getLocation().getWorld() != p.getLocation().getWorld()) {
                    p.sendMessage(MsgUtil.p("shop-creation-cancelled"));
                    return;
                }
                if (info.getLocation().distanceSquared(p.getLocation()) > 25) {
                    p.sendMessage(MsgUtil.p("shop-creation-cancelled"));
                    return;
                }
                /* Creation handling */
                if (info.getAction() == ShopAction.CREATE) {
                    try {
                        // Checking the shop can be created
                        if (plugin.getShopManager().getShop(info.getLocation()) != null) {
                            p.sendMessage(MsgUtil.p("shop-already-owned"));
                            return;
                        }
                        if (Util.getSecondHalf(info.getLocation().getBlock()) != null && !p.hasPermission("quickshop.create.double")) {
                            p.sendMessage(MsgUtil.p("no-double-chests"));
                            return;
                        }
                        if (Util.canBeShop(info.getLocation().getBlock()) == false) {
                            p.sendMessage(MsgUtil.p("chest-was-removed"));
                            return;
                        }
                        // Price per item
                        double price;
                        if (plugin.getConfig().getBoolean("whole-number-prices-only")) {
                            price = Integer.parseInt(message);
                        } else {
                            price = Double.parseDouble(message);
                        }
                        if (price < 0.01) {
                            p.sendMessage(MsgUtil.p("price-too-cheap"));
                            return;
                        }
                        final double tax = plugin.getConfig().getDouble("shop.cost");
                        // Tax refers to the cost to create a shop. Not actual
                        // tax, that would be silly
                        if (tax != 0 && plugin.getEcon().getBalance(p.getName()) < tax) {
                            p.sendMessage(MsgUtil.p("you-cant-afford-a-new-shop", format(tax)));
                            return;
                        }
                        // Create the sample shop.
                        final Shop shop = new ContainerShop(info.getLocation(), price, info.getItem(), p.getName());
                        // This must be called after the event has been called.
                        // Else, if the event is cancelled, they won't get their
                        // money back.
                        if (tax != 0) {
                            if (!plugin.getEcon().withdraw(p.getName(), tax)) {
                                p.sendMessage(MsgUtil.p("you-cant-afford-a-new-shop", format(tax)));
                                return;
                            }
                            plugin.getEcon().deposit(plugin.getConfig().getString("tax-account"), tax);
                        }
                        final ShopCreateEvent e = new ShopCreateEvent(shop, p);
                        Bukkit.getPluginManager().callEvent(e);
                        if (e.isCancelled()) {
                            return;
                        }
                        shop.onLoad();
                        /* The shop has hereforth been successfully created */
                        createShop(shop);
                        p.sendMessage(MsgUtil.p("success-created-shop"));
                        final Location loc = shop.getLocation();
                        plugin.log(String.format("玩家: %s 创建了一个 %s 商店 在 (%s - %s, %s, %s)", p.getName(), shop.getDataName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
                        if (!plugin.getConfig().getBoolean("shop.lock")) {
                            // Warn them if they haven't been warned since
                            // reboot
                            final HashSet<String> warings = plugin.getConfigManager().getWarnings();
                            if (!warings.contains(p.getName())) {
                                p.sendMessage(MsgUtil.p("shops-arent-locked"));
                                warings.add(p.getName());
                            }
                        }
                        // Figures out which way we should put the sign on and
                        // sets its text.
                        if (info.getSignBlock() != null && info.getSignBlock().getType() == Material.AIR && plugin.getConfig().getBoolean("shop.auto-sign")) {
                            final BlockState bs = info.getSignBlock().getState();
                            final BlockFace bf = info.getLocation().getBlock().getFace(info.getSignBlock());
                            bs.setType(Material.WALL_SIGN);
                            final Sign sign = (Sign) bs.getData();
                            sign.setFacingDirection(bf);
                            bs.update(true);
                            shop.setSignText();
                        }
                        if (shop instanceof ContainerShop) {
                            final ContainerShop cs = (ContainerShop) shop;
                            if (cs.isDoubleShop()) {
                                final Shop nextTo = cs.getAttachedShop();
                                if (nextTo.getPrice() > shop.getPrice()) {
                                    // The one next to it must always be a
                                    // buying shop.
                                    p.sendMessage(MsgUtil.p("buying-more-than-selling"));
                                }
                            }
                        }
                    }
                    /* They didn't enter a number. */
                    catch (final NumberFormatException ex) {
                        p.sendMessage(MsgUtil.p("shop-creation-cancelled"));
                        return;
                    }
                }
                /* Purchase Handling */
                else if (info.getAction() == ShopAction.BUY) {
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(message);
                    } catch (final NumberFormatException e) {
                        p.sendMessage(MsgUtil.p("shop-purchase-cancelled"));
                        return;
                    }
                    // Get the shop they interacted with
                    final Shop shop = plugin.getShopManager().getShop(info.getLocation());
                    // It's not valid anymore
                    if (shop == null || Util.canBeShop(info.getLocation().getBlock()) == false) {
                        p.sendMessage(MsgUtil.p("chest-was-removed"));
                        return;
                    }
                    if (info.hasChanged(shop)) {
                        p.sendMessage(MsgUtil.p("shop-has-changed"));
                        return;
                    }
                    if (shop.isSelling()) {
                        final int stock = shop.getRemainingStock();
                        if (stock < amount) {
                            p.sendMessage(MsgUtil.p("shop-stock-too-low", "" + shop.getRemainingStock(), shop.getDataName()));
                            return;
                        }
                        if (amount == 0) {
                            // Dumb.
                            MsgUtil.sendPurchaseSuccess(p, shop, amount);
                            return;
                        } else if (amount < 0) {
                            // & Dumber
                            p.sendMessage(MsgUtil.p("negative-amount"));
                            return;
                        }
                        final int pSpace = Util.countSpace(p.getInventory(), shop.getItem());
                        if (amount > pSpace) {
                            p.sendMessage(MsgUtil.p("not-enough-space", "" + pSpace));
                            return;
                        }
                        final ShopPurchaseEvent e = new ShopPurchaseEvent(shop, p, amount);
                        Bukkit.getPluginManager().callEvent(e);
                        if (e.isCancelled()) {
                            return; // Cancelled
                        }
                        // Money handling
                        if (!p.getName().equals(shop.getOwner())) {
                            // Check their balance. Works with *most* economy
                            // plugins*
                            if (plugin.getEcon().getBalance(p.getName()) < amount * shop.getPrice()) {
                                p.sendMessage(MsgUtil.p("you-cant-afford-to-buy", format(amount * shop.getPrice()), format(plugin.getEcon().getBalance(p.getName()))));
                                return;
                            }
                            // Don't tax them if they're purchasing from
                            // themselves.
                            // Do charge an amount of tax though.
                            final double tax = plugin.getConfigManager().getTax();
                            final double total = amount * shop.getPrice();
                            if (!plugin.getEcon().withdraw(p.getName(), total)) {
                                p.sendMessage(MsgUtil.p("you-cant-afford-to-buy", format(amount * shop.getPrice()), format(plugin.getEcon().getBalance(p.getName()))));
                                return;
                            }
                            if (!shop.isUnlimited() || plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
                                plugin.getEcon().deposit(shop.getOwner(), total * (1 - tax));
                                if (tax != 0) {
                                    plugin.getEcon().deposit(plugin.getConfigManager().getTaxAccount(), total * tax);
                                }
                            }
                            // Notify the shop owner
                            if (plugin.getConfigManager().isShowTax()) {
                                String msg = MsgUtil.p("player-bought-from-your-store-tax", p.getName(), "" + amount, shop.getDataName(), Util.format((tax * total)));
                                if (stock == amount) {
                                    msg += "\n" + MsgUtil.p("shop-out-of-stock",
                                            "" + shop.getLocation().getBlockX(),
                                            "" + shop.getLocation().getBlockY(),
                                            "" + shop.getLocation().getBlockZ(),
                                            shop.getDataName());
                                }
                                MsgUtil.send(shop.getOwner(), msg);
                            } else {
                                String msg = MsgUtil.p("player-bought-from-your-store", p.getName(), "" + amount, shop.getDataName());
                                if (stock == amount) {
                                    msg += "\n" + MsgUtil.p("shop-out-of-stock",
                                            "" + shop.getLocation().getBlockX(),
                                            "" + shop.getLocation().getBlockY(),
                                            "" + shop.getLocation().getBlockZ(),
                                            shop.getDataName());
                                }
                                MsgUtil.send(shop.getOwner(), msg);
                            }
                        }
                        // Transfers the item from A to B
                        shop.sell(p, amount);
                        MsgUtil.sendPurchaseSuccess(p, shop, amount);
                        plugin.log(String.format("玩家: %s 从 %s 购买了 %s 件商品 花费 %s", p.getName(), shop.toString(), amount, shop.getPrice() * amount));
                    } else if (shop.isBuying()) {
                        final int space = shop.getRemainingSpace();
                        if (space < amount) {
                            p.sendMessage(MsgUtil.p("shop-has-no-space", "" + space, shop.getDataName()));
                            return;
                        }
                        final int count = Util.countItems(p.getInventory(), shop.getItem());
                        // Not enough items
                        if (amount > count) {
                            p.sendMessage(MsgUtil.p("you-dont-have-that-many-items", "" + count, shop.getDataName()));
                            return;
                        }
                        if (amount == 0) {
                            // Dumb.
                            MsgUtil.sendPurchaseSuccess(p, shop, amount);
                            return;
                        } else if (amount < 0) {
                            // & Dumber
                            p.sendMessage(MsgUtil.p("negative-amount"));
                            return;
                        }
                        // Money handling
                        if (!p.getName().equals(shop.getOwner())) {
                            // Don't tax them if they're purchasing from
                            // themselves.
                            // Do charge an amount of tax though.
                            final double tax = plugin.getConfigManager().getTax();
                            final double total = amount * shop.getPrice();
                            if (!shop.isUnlimited() || plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
                                // Tries to check their balance nicely to see if
                                // they can afford it.
                                if (plugin.getEcon().getBalance(shop.getOwner()) < amount * shop.getPrice()) {
                                    p.sendMessage(MsgUtil.p("the-owner-cant-afford-to-buy-from-you", format(amount * shop.getPrice()), format(plugin.getEcon().getBalance(shop.getOwner()))));
                                    return;
                                }
                                // Check for plugins faking econ.has(amount)
                                if (!plugin.getEcon().withdraw(shop.getOwner(), total)) {
                                    p.sendMessage(MsgUtil.p("the-owner-cant-afford-to-buy-from-you", format(amount * shop.getPrice()), format(plugin.getEcon().getBalance(shop.getOwner()))));
                                    return;
                                }
                                if (tax != 0) {
                                    plugin.getEcon().deposit(plugin.getConfigManager().getTaxAccount(), total * tax);
                                }
                            }
                            // Give them the money after we know we succeeded
                            plugin.getEcon().deposit(p.getName(), total * (1 - tax));
                            // Notify the owner of the purchase.
                            String msg = MsgUtil.p("player-sold-to-your-store", p.getName(), "" + amount, shop.getDataName());
                            if (space == amount) {
                                msg += "\n" + MsgUtil.p("shop-out-of-space", "" + shop.getLocation().getBlockX(), "" + shop.getLocation().getBlockY(), "" + shop.getLocation().getBlockZ());
                            }
                            MsgUtil.send(shop.getOwner(), msg);
                        }
                        shop.buy(p, amount);
                        MsgUtil.sendSellSuccess(p, shop, amount);
                        plugin.log(String.format("玩家: %s 出售了 %s 件商品 到 %s 获得 %s", p.getName(), amount, shop.toString(), shop.getPrice() * amount));
                    }
                    shop.setSignText(); // Update the signs count
                }
                /* If it was already cancelled (from destroyed) */
                else {
                    return; // It was cancelled, go away.
                }
            }
        });
    }

    /**
     * Loads the given shop into storage. This method is used for loading data
     * from the database. Do not use this method to create a shop.
     *
     * @param world
     *            The world the shop is in
     * @param shop
     *            The shop to load
     */
    public void loadShop(final String world, final Shop shop) {
        this.addShop(world, shop);
    }

    /**
     * Removes a shop from the world. Does NOT remove it from the database. *
     * REQUIRES * the world to be loaded
     *
     * @param shop
     *            The shop to remove
     */
    public void removeShop(final Shop shop) {
        final Location loc = shop.getLocation();
        final String world = loc.getWorld().getName();
        final HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops().get(world);
        final int x = (int) Math.floor((shop.getLocation().getBlockX()) / 16.0);
        final int z = (int) Math.floor((shop.getLocation().getBlockZ()) / 16.0);
        final ShopChunk shopChunk = new ShopChunk(world, x, z);
        final HashMap<Location, Shop> inChunk = inWorld.get(shopChunk);
        inChunk.remove(loc);
    }

    /**
     * Adds a shop to the world. Does NOT require the chunk or world to be
     * loaded
     *
     * @param world
     *            The name of the world
     * @param shop
     *            The shop to add
     */
    private void addShop(final String world, final Shop shop) {
        HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops().get(world);
        // There's no world storage yet. We need to create that hashmap.
        if (inWorld == null) {
            inWorld = new HashMap<ShopChunk, HashMap<Location, Shop>>(3);
            // Put it in the data universe
            this.getShops().put(world, inWorld);
        }
        // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
        // location rounded to the nearest 16.
        final int x = (int) Math.floor((shop.getLocation().getBlockX()) / 16.0);
        final int z = (int) Math.floor((shop.getLocation().getBlockZ()) / 16.0);
        // Get the chunk set from the world info
        final ShopChunk shopChunk = new ShopChunk(world, x, z);
        HashMap<Location, Shop> inChunk = inWorld.get(shopChunk);
        // That chunk data hasn't been created yet - Create it!
        if (inChunk == null) {
            inChunk = new HashMap<Location, Shop>(1);
            // Put it in the world
            inWorld.put(shopChunk, inChunk);
        }
        // Put the shop in its location in the chunk list.
        inChunk.put(shop.getLocation(), shop);
    }

    public class ShopIterator implements Iterator<Shop> {
        private Iterator<HashMap<Location, Shop>> chunks;
        private Shop current;
        private Iterator<Shop> shops;
        private final Iterator<HashMap<ShopChunk, HashMap<Location, Shop>>> worlds;

        public ShopIterator() {
            worlds = getShops().values().iterator();
        }

        /**
         * Returns true if there is still more shops to iterate over.
         */
        @Override
        public boolean hasNext() {
            if (shops == null || !shops.hasNext()) {
                if (chunks == null || !chunks.hasNext()) {
                    if (!worlds.hasNext()) {
                        return false;
                    }
                    chunks = worlds.next().values().iterator();
                    return hasNext();
                }
                shops = chunks.next().values().iterator();
                return hasNext();
            }
            return true;
        }

        /**
         * Fetches the next shop. Throws NoSuchElementException if there are no
         * more shops.
         */
        @Override
        public Shop next() {
            if (shops == null || !shops.hasNext()) {
                if (chunks == null || !chunks.hasNext()) {
                    if (!worlds.hasNext()) {
                        throw new NoSuchElementException("No more shops to iterate over!");
                    }
                    chunks = worlds.next().values().iterator();
                }
                shops = chunks.next().values().iterator();
            }
            if (!shops.hasNext()) {
                return this.next(); // Skip to the next one (Empty iterator?)
            }
            current = shops.next();
            return current;
        }

        /**
         * Removes the current shop. This method will delete the shop from
         * memory and the database.
         */
        @Override
        public void remove() {
            current.delete(false);
            shops.remove();
        }
    }
}
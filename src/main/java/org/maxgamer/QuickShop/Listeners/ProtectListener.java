package org.maxgamer.QuickShop.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Util.MarkUtil;

public class ProtectListener implements Listener {

    private final QuickShop plugin;

    public ProtectListener(final QuickShop plugin) {
        this.plugin = plugin;
    }

    public Shop getShop(final Inventory inv) {
        if (inv == null) {
            return null;
        }
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof DoubleChest) {
            holder = ((DoubleChest) holder).getLeftSide();
        }

        if (holder instanceof BlockState) {
            final Block block = ((BlockState) holder).getBlock();
            final Shop sp = plugin.getShopManager().getShop(block.getLocation());
            if (sp != null) {
                return sp;
            }
        }
        return null;
    }

    @EventHandler
    public void onInvMove(final InventoryMoveItemEvent e) {
        final ItemStack ci = e.getItem();
        if (MarkUtil.hasMark(ci)) {
            e.setCancelled(true);
        }
        final Inventory src = e.getSource();
        final Inventory me = e.getInitiator();
        final Inventory des = e.getDestination();
        final Shop srcshop = getShop(src);
        final Shop meshop = getShop(me);
        final Shop desshop = getShop(des);
        if ((srcshop != null && meshop == null) || (meshop != null && desshop == null) || (srcshop != null && desshop != null && srcshop.getOwner().equalsIgnoreCase(desshop.getOwner()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInvPickup(final InventoryPickupItemEvent e) {
        if (!plugin.getConfigManager().isPreventHopper()) {
            return;
        }
        final ItemStack ci = e.getItem().getItemStack();
        if (MarkUtil.hasMark(ci)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemClick(final InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        final ItemStack ci = e.getCurrentItem();
        final Inventory inv = e.getInventory();
        final int solt = e.getSlot();
        if (inv.getType() != InventoryType.PLAYER && inv.getType() != InventoryType.HOPPER) {
            if (inv.getTitle() != null && inv.getTitle().equalsIgnoreCase(plugin.getConfigManager().getGuiTitle())) {
                e.setCancelled(true);
                p.closeInventory();
            }
            return;
        }
        try {
            if (MarkUtil.hasMark(ci)) {
                inv.setItem(solt, new ItemStack(Material.AIR));
                sendWarning(p, ci, "§d§l获取");
                p.closeInventory();
            }
        } catch (final Exception ex) {
        }
    }

    @EventHandler
    public void onPlayerHandlerItem(final PlayerItemHeldEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                final Player p = e.getPlayer();
                final PlayerInventory inv = p.getInventory();
                final ItemStack[] cis = inv.getArmorContents();
                for (int i = 0; i < cis.length; i++) {
                    final ItemStack itemStack = cis[i];
                    if (MarkUtil.hasMark(itemStack)) {
                        cis[i] = new ItemStack(Material.AIR);
                        sendWarning(p, itemStack, "§e§l穿戴");
                    }
                }
                inv.setArmorContents(cis);
                final int newslot = e.getNewSlot();
                final ItemStack newItem = inv.getItem(newslot);
                if (MarkUtil.hasMark(newItem)) {
                    inv.setItem(newslot, new ItemStack(Material.AIR));
                    sendWarning(p, newItem, "§3§l使用");
                }
            }
        });
    }

    @EventHandler
    public void onPlayerPickup(final PlayerPickupItemEvent e) {
        final ItemStack ci = e.getItem().getItemStack();
        if (MarkUtil.hasMark(ci)) {
            e.setCancelled(true);
        }
    }

    private void clearIllegalItem(final Player player) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                final Inventory inv = player.getInventory();
                int clearnum = 0;
                for (final ItemStack itemStack : inv) {
                    if (MarkUtil.hasMark(itemStack)) {
                        inv.remove(itemStack);
                        clearnum++;
                    }
                }
                if (clearnum != 0) {
                    Bukkit.broadcastMessage(plugin.getConfigManager().getGuiTitle() + " §4提示 §d扫描完毕 §d已清理 §a" + player.getName() + " §c非法获取的物品 §4" + clearnum + " §c个物品...");
                }
            }
        });
    }

    private void sendWarning(final Player p, final ItemStack ci, final String action) {
        Bukkit.broadcastMessage(plugin.getConfigManager().getGuiTitle() + " §4警告 " + p.getDisplayName() + " §c非法 " + action + " " + ci.getItemMeta().getDisplayName());
        Bukkit.broadcastMessage(plugin.getConfigManager().getGuiTitle() + " §4提示 §d系统 §d已清理 §a" + p.getName() + " §c非法获取的物品 §a并扫描玩家背包...");
        clearIllegalItem(p);
    }
}

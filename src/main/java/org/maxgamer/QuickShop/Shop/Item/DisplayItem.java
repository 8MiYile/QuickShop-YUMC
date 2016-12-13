package org.maxgamer.QuickShop.Shop.Item;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.ContainerShop;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 *         cannot be interacted with.
 */
public abstract class DisplayItem {
    public static QuickShop plugin = P.getPlugin();

    public static DisplayItem create(final ContainerShop shop) {
        if (plugin.getConfigManager().isDisplay()) {
            if (plugin.getConfigManager().isFakeItem()) {
                try {
                    return new FakeItem_19_110(shop, shop.getItem());
                } catch (final Throwable e) {
                    Log.d(e);
                    try {
                        return new FakeItem_17_18(shop, shop.getItem());
                    } catch (final Throwable e2) {
                        Log.d(e2);
                    }
                }
            }
            return new NormalItem(shop, shop.getItem());
        }
        return null;
    }

    /**
     * 获得悬浮物地点
     *
     * @return 获得悬浮地点
     */
    public abstract Location getDisplayLocation();

    /**
     * @return {@link Item}
     */
    public abstract Item getItem();

    /**
     * 移除悬浮物
     */
    public abstract void remove();

    /**
     * 移除多余物品
     *
     * @return
     */
    public abstract boolean removeDupe();

    /**
     * 更新悬浮物
     */
    public abstract void respawn();

    /**
     * 刷出悬浮物
     */
    public abstract void spawn();
}
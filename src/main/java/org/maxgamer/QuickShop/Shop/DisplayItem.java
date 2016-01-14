package org.maxgamer.QuickShop.Shop;

import org.bukkit.Location;
import org.bukkit.entity.Item;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 *         cannot be interacted with.
 */
public interface DisplayItem {
    /**
     * 获得悬浮物地点
     *
     * @return 获得悬浮地点
     */
    public Location getDisplayLocation();

    /**
     * @return {@link Item}
     */
    public Item getItem();

    /**
     * 移除悬浮物
     */
    public void remove();

    /**
     * 移除多余物品
     *
     * @return
     */
    public boolean removeDupe();

    /**
     * 更新悬浮物
     */
    public void respawn();

    /**
     * 刷出悬浮物
     */
    public void spawn();
}
package org.maxgamer.QuickShop.Shop.Item;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Minecraft 虚拟悬浮物品工具类
 * 需要depend ProtocolLib
 *
 * @author 橙子(chengzi)
 * @version 1.0.1
 */
public class FakeItem_18 extends FakeItem_17 {

    public FakeItem_18(Location loc, final ItemStack item) {
        super(loc, item);
    }

    private static int getNormalizedDistance(final double value) {
        return (int) Math.floor(value * 32.0D);
    }
}

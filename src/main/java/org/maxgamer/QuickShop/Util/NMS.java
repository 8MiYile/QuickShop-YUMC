package org.maxgamer.QuickShop.Util;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class NMS {

    public static void safeGuard(final Item item) throws ClassNotFoundException {
        rename(item.getItemStack());
        item.setPickupDelay(Integer.MAX_VALUE);
    }

    private static void rename(final ItemStack iStack) {
        MarkUtil.addMark(iStack);
    }
}
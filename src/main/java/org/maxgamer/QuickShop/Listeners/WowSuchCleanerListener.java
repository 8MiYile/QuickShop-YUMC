package org.maxgamer.QuickShop.Listeners;

import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.ItemPreCleanEvent;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.Util.MarkUtil;

import java.util.ArrayList;
import java.util.List;

public class WowSuchCleanerListener implements Listener {
    @EventHandler
    public void onWSCClear(final ItemPreCleanEvent e) {
        final List<Item> clearList = new ArrayList<>();
        final List<ItemStack> aucList = new ArrayList<>();
        final List<Item> cleanList = e.getItemsToClean();
        final List<ItemStack> acList = e.getItemsToAuction();
        if (cleanList != null) {
            for (final Item item : cleanList) {
                if (MarkUtil.hasMark(item.getItemStack())) {
                    clearList.add(item);
                }
            }
            e.getItemsToClean().removeAll(clearList);
        }
        if (acList != null) {
            for (final ItemStack itemStack : acList) {
                if (MarkUtil.hasMark(itemStack)) {
                    aucList.add(itemStack);
                }
            }
            e.getItemsToAuction().removeAll(aucList);
        }
    }
}

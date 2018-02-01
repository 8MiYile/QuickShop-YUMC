package org.maxgamer.QuickShop.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.maxgamer.QuickShop.QuickShop;

/**
 * @author Netherfoam
 */
public class ChatListener implements Listener {
    private QuickShop plugin;

    public ChatListener(final QuickShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(final AsyncPlayerChatEvent e) {
        if (!plugin.getShopManager().getActions().containsKey(e.getPlayer().getName())) {
            return;
        }
        e.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getShopManager().handleChat(e.getPlayer(), e.getMessage()));
    }
}
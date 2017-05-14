package org.maxgamer.QuickShop.Shop.Item;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Listeners.ChunkListener;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Watcher.ItemWatcher;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.bukkit.compatible.C;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 *         cannot be interacted with.
 */
public abstract class DisplayItem {
    private static QuickShop plugin = P.getPlugin();

    private static Class<? extends DisplayItem> displayItemClass = NormalItem.class;

    private static Constructor<? extends DisplayItem> constructor;

    public static void init(boolean fakeItem) {
        if (fakeItem) {
            Log.i("启用虚拟悬浮物 尝试启动中...");
            String nms = C.getNMSVersion();
            Class<? extends DisplayItem> c = null;
            switch (nms) {
            case "v1_7_R4":
                c = FakeItem_17.class;
                break;
            case "v1_8_R3":
                c = FakeItem_18.class;
                break;
            case "v1_9_R1":
            case "v1_9_R2":
            case "v1_10_R1":
            case "v1_11_R1":
                c = FakeItem_19_111.class;
                break;
            }
            if (c != null) {
                FakeItem.register(plugin);
                try {
                    c.getConstructor(Location.class, ItemStack.class).newInstance(new Location(Bukkit.getWorlds().get(0), 0, 0, 0), new ItemStack(Material.STONE)).spawn();
                    displayItemClass = c;
                    Log.i("虚拟悬浮物功能测试正常(%s)...", c.getSimpleName());
                } catch (Throwable e) {
                    Log.d(e);
                    Log.w("+=========================================");
                    Log.w("| 警告: 启动虚拟物品失败 使用原版悬浮物品...");
                    Log.w("+=========================================");
                }
            } else {
                Log.i("没有可用的虚拟物品类 使用原版悬浮物品...");
            }
        }
        try {
            constructor = displayItemClass.getConstructor(Location.class, ItemStack.class);
            if (displayItemClass == NormalItem.class) {
                // Display item handler thread
                Log.i("开启商店检查以及悬浮物刷新线程...");
                final ItemWatcher itemWatcher = new ItemWatcher(plugin);
                plugin.itemWatcherTask = Bukkit.getScheduler().runTaskTimer(plugin, itemWatcher, 20, 1800);
            }
        } catch (NoSuchMethodException ignored) {
        }
    }

    public static DisplayItem create(final ContainerShop shop) {
        if (plugin.getConfigManager().isDisplay()) try {
            return constructor.newInstance(shop.getLocation(), shop.getItem());
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ignored) {
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
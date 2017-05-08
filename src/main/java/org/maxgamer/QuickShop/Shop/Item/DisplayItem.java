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
import org.maxgamer.QuickShop.Shop.ContainerShop;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 *         cannot be interacted with.
 */
public abstract class DisplayItem {
    private static QuickShop plugin = P.getPlugin();

    private static Class<? extends DisplayItem> displayItemClass;

    private static Constructor<? extends DisplayItem> constructor;

    public static void init() {
        List<Class<? extends DisplayItem>> fakeItems = Arrays.asList(FakeItem_19_111.class, FakeItem_18.class, FakeItem_17.class);
        Log.i("启用虚拟悬浮物 尝试启动中...");
        FakeItem.register(plugin);
        fakeItems.forEach(c -> {
            try {
                c.getConstructor(Location.class, ItemStack.class).newInstance(new Location(Bukkit.getWorlds().get(0), 0, 0, 0), new ItemStack(Material.STONE)).spawn();
                displayItemClass = c;
                Log.i("虚拟悬浮物功能测试正常(%s)...", c.getSimpleName());
            } catch (Throwable e) {
                Log.d(e);
            }
        });
        if (displayItemClass == null) {
            displayItemClass = NormalItem.class;
            Log.w("+=========================================");
            Log.w("| 警告: 启动虚拟物品失败 使用原版悬浮物品...");
            Log.w("+=========================================");
        }
        try {
            constructor = displayItemClass.getConstructor(Location.class, ItemStack.class);
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
package org.maxgamer.QuickShop.Shop.Item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

/**
 * Minecraft 虚拟悬浮物品工具类
 * 需要depend ProtocolLib
 *
 * @author 橙子(chengzi)
 * @version 1.0.1
 */
public class FakeItem_18 extends FakeItem {

    public FakeItem_18(Location loc, final ItemStack item) {
        super(loc, item);
    }

    private static int getNormalizedDistance(final double value) {
        return (int) Math.floor(value * 32.0D);
    }

    @Override
    protected PacketContainer setMetadataPacket(PacketContainer fakePacket) {
        fakePacket.getIntegers().write(0, eid);
        final WrappedWatchableObject itemMeta = new WrappedWatchableObject(10, itemStack);
        final List<WrappedWatchableObject> entityMetaList = new ArrayList<>(1);
        entityMetaList.add(itemMeta);
        fakePacket.getWatchableCollectionModifier().write(0, entityMetaList);
        return fakePacket;
    }

    @Override
    protected PacketContainer setSpawnPacket(PacketContainer fakePacket) {
        StructureModifier<Integer> is = fakePacket.getIntegers();
        is.write(0, eid);
        is.write(1, getNormalizedDistance(location.getX()));
        is.write(2, getNormalizedDistance(location.getY()));
        is.write(3, getNormalizedDistance(location.getZ()));
        return fakePacket;
    }
}

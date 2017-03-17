package org.maxgamer.QuickShop.Shop.Item;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.google.common.base.Optional;

/**
 * Minecraft 虚拟悬浮物品工具类
 * 需要depend ProtocolLib 4.x
 *
 * @author 橙子(chengzi)
 * @version 1.1.0
 */
public class FakeItem_19_111 extends FakeItem {

    public FakeItem_19_111(Location loc, final ItemStack item) {
        super(loc, item);
    }

    @Override
    protected PacketContainer setMetadataPacket(PacketContainer fakePacket) {
        fakePacket.getIntegers().write(0, eid);
        final WrappedDataWatcher wr = new WrappedDataWatcher();
        final Serializer serializer = WrappedDataWatcher.Registry.getItemStackSerializer(true);
        final WrappedDataWatcherObject object = new WrappedDataWatcher.WrappedDataWatcherObject(6, serializer);
        wr.setObject(object, Optional.of(itemStack));
        fakePacket.getWatchableCollectionModifier().write(0, wr.getWatchableObjects());
        return fakePacket;
    }

    @Override
    protected PacketContainer setSpawnPacket(PacketContainer fakePacket) {
        StructureModifier<Object> mdf = fakePacket.getModifier();
        mdf.write(0, eid);
        mdf.write(1, uuid);
        mdf.write(2, location.getX());
        mdf.write(3, location.getY());
        mdf.write(4, location.getZ());
        return fakePacket;
    }
}

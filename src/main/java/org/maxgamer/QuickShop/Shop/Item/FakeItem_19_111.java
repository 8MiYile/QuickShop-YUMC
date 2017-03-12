package org.maxgamer.QuickShop.Shop.Item;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
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
    protected PacketContainer getMetadataPacket() {
        final PacketContainer fakePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        fakePacket.getIntegers().write(0, eid);
        final WrappedDataWatcher wr = new WrappedDataWatcher();
        final Serializer serializer = WrappedDataWatcher.Registry.getItemStackSerializer(true);
        final WrappedDataWatcherObject object = new WrappedDataWatcher.WrappedDataWatcherObject(6, serializer);
        wr.setObject(object, Optional.of(itemStack));
        fakePacket.getWatchableCollectionModifier().write(0, wr.getWatchableObjects());
        return fakePacket;
    }

    @Override
    protected PacketContainer getSpawnPacket() {
        final PacketContainer fakePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        fakePacket.getIntegers().write(0, eid);
        fakePacket.getModifier().write(1, uuid);
        fakePacket.getDoubles().write(0, location.getX());
        fakePacket.getDoubles().write(1, location.getY());
        fakePacket.getDoubles().write(2, location.getZ());
        fakePacket.getIntegers().write(6, 2);
        return fakePacket;
    }
}

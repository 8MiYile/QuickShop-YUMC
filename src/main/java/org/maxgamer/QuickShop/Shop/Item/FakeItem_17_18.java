package org.maxgamer.QuickShop.Shop.Item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.Shop.ContainerShop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

/**
 * Minecraft 虚拟悬浮物品工具类
 * 需要depend ProtocolLib
 *
 * @author 橙子(chengzi)
 * @version 1.0.1
 */
public class FakeItem_17_18 extends FakeItem {

    public FakeItem_17_18(final ContainerShop containerShop, final ItemStack item) {
        super(containerShop, item);
    }

    private static int getNormalizedDistance(final double value) {
        return (int) Math.floor(value * 32.0D);
    }

    @Override
    protected PacketContainer getMetadataPacket() {
        final PacketContainer fakePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        fakePacket.getIntegers().write(0, eid);
        final WrappedWatchableObject itemMeta = new WrappedWatchableObject(10, itemStack);
        final List<WrappedWatchableObject> entityMetaList = new ArrayList<>(1);
        entityMetaList.add(itemMeta);
        fakePacket.getWatchableCollectionModifier().write(0, entityMetaList);
        return fakePacket;
    }

    @Override
    protected PacketContainer getSpawnPacket() {
        final PacketContainer fakePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        fakePacket.getIntegers().write(0, eid);
        fakePacket.getIntegers().write(1, getNormalizedDistance(location.getX()));
        fakePacket.getIntegers().write(2, getNormalizedDistance(location.getY()));
        fakePacket.getIntegers().write(3, getNormalizedDistance(location.getZ()));
        fakePacket.getIntegers().write(9, 2);
        return fakePacket;
    }

}

package org.maxgamer.QuickShop.Shop.Item;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

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

    @Override
    protected PacketContainer setSpawnPacket(PacketContainer fakePacket) {
        StructureModifier<Integer> is = fakePacket.getIntegers();
        is.write(0, eid);
        is.write(1, (int) location.getX() * 32);
        is.write(2, (int) location.getY() * 32);
        is.write(3, (int) location.getZ() * 32);
        return fakePacket;
    }
}

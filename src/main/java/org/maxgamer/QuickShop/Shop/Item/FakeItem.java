package org.maxgamer.QuickShop.Shop.Item;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

/**
 * Minecraft 虚拟悬浮物品工具类 需要depend ProtocolLib 4.x
 *
 * @author 橙子(chengzi)
 * @version 1.1.1
 */
public abstract class FakeItem extends DisplayItem {

    private static Map<String, List<FakeItem>> fakes = new HashMap<>();
    private static boolean registered = false;
    private static int lastId = Integer.MAX_VALUE;

    protected final ItemStack itemStack;
    protected final Location location;
    protected final int eid;
    protected final UUID uuid;
    protected boolean created = false;

    public FakeItem(Location loc, final ItemStack item) {
        this.itemStack = item;
        this.location = loc.clone().add(0.5, 1, 0.5);
        this.eid = getFakeEntityId();
        this.uuid = UUID.randomUUID();
    }
    public static boolean isRegistered() {
        return registered;
    }

    public static void register(final Plugin plugin) {
        if (registered) {
            return;
        }
        final PluginManager pm = Bukkit.getPluginManager();
        final Plugin p = pm.getPlugin("ProtocolLib");
        if (p != null) {
            if (!p.isEnabled()) {
                pm.enablePlugin(p);
            }
            if (!p.isEnabled()) {
                throw new IllegalStateException("前置插件ProtocolLib启动失败 请检查版本.");
            }
        } else {
            throw new IllegalStateException("服务器未找到前置插件ProtocolLib.");
        }
        final PacketAdapter chunkPacketListener = new PacketAdapter(plugin, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                try {
                    final PacketContainer packet = event.getPacket();
                    final Player p = event.getPlayer();
                    final int chunkX = packet.getIntegers().read(0);
                    final int chunkZ = packet.getIntegers().read(1);
                    final List<FakeItem> fakesInChunk = fakes.get(getChunkIdentifyString(p.getWorld().getChunkAt(chunkX, chunkZ)));
                    sendChunkPacket(p, fakesInChunk);
                } catch (final Exception ignored) {
                }
            }
        };
        final PacketAdapter chunkBulkPacketListener = new PacketAdapter(plugin, PacketType.Play.Server.MAP_CHUNK_BULK) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                try {
                    final PacketContainer packet = event.getPacket();
                    final Player p = event.getPlayer();
                    final int[] chunksX = packet.getIntegerArrays().read(0);
                    final int[] chunksZ = packet.getIntegerArrays().read(1);
                    for (int i = 0; i < chunksX.length; i++) {
                        final List<FakeItem> fakesInChunk = fakes.get(getChunkIdentifyString(p.getWorld().getChunkAt(chunksX[i], chunksZ[i])));
                        sendChunkPacket(p, fakesInChunk);
                    }
                } catch (final Exception ignored) {
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(chunkPacketListener);
        ProtocolLibrary.getProtocolManager().addPacketListener(chunkBulkPacketListener);
        registered = true;
    }

    private static void sendChunkPacket(Player p, List<FakeItem> fakesInChunk) throws InvocationTargetException {
        if (fakesInChunk != null) {
            for (final FakeItem fake : fakesInChunk) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, fake.getSpawnPacket());
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, fake.getVelocityPacket());
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, fake.getMetadataPacket());
            }

        }
    }

    private static String getChunkIdentifyString(final Chunk chunk) {
        return chunk.getWorld().getName() + "@@" + chunk.getX() + "@@" + chunk.getZ();
    }

    private static int getFakeEntityId() {
        return lastId--;
    }

    @Override
    public Location getDisplayLocation() {
        return location;
    }

    @Override
    public Item getItem() {
        return null;
    }

    @Override
    public void remove() {
        destory();
    }

    @Override
    public boolean removeDupe() {
        return true;
    }

    @Override
    public void respawn() {
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getDestoryPacket());
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getSpawnPacket());
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getVelocityPacket());
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getMetadataPacket());
    }

    @Override
    public void spawn() {
        create();
    }

    private void create() {
        if (!registered) {
            throw new IllegalStateException("You have to call the register method first.");
        }
        if (created) {
            return;
        }
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getSpawnPacket());
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getVelocityPacket());
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getMetadataPacket());

        final String chunkId = getChunkIdentifyString(location.getChunk());
        List<FakeItem> fakesInChunk = fakes.get(chunkId);
        if (fakesInChunk == null) {
            fakesInChunk = new ArrayList<>();
        }
        fakesInChunk.add(this);
        fakes.put(chunkId, fakesInChunk);
        created = true;
    }

    private void destory() {
        if (!created) {
            return;
        }
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getDestoryPacket());

        final String chunkId = getChunkIdentifyString(location.getChunk());
        final List<FakeItem> fakesInChunk = fakes.get(chunkId);
        if (fakesInChunk == null) {
            // NOTE: This is what should not happens if everything is correct.
            created = false;
            return;
        }
        fakesInChunk.remove(this);
        fakes.put(chunkId, fakesInChunk);
        created = false;
    }

    private PacketContainer getDestoryPacket() {
        final PacketContainer fakePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY, true);
        fakePacket.getIntegerArrays().write(0, new int[] { eid });
        return fakePacket;
    }

    private PacketContainer getVelocityPacket() {
        final PacketContainer fakePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        fakePacket.getIntegers().write(0, eid);
        return fakePacket;
    }

    protected abstract PacketContainer getMetadataPacket();

    protected abstract PacketContainer getSpawnPacket();

}

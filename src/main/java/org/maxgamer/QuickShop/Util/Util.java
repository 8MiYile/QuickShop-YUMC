package org.maxgamer.QuickShop.Util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sign;
import org.maxgamer.QuickShop.QuickShop;
import pw.yumc.YumCore.global.L10N;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
public class Util {
    private static HashSet<Material> blacklist = new HashSet<>();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static QuickShop plugin;
    private static HashSet<Material> shoppables = new HashSet<>();
    private static HashSet<Material> tools = new HashSet<>();
    private static HashSet<Material> transparent = new HashSet<>();

    public static void addTransparentBlock(final Material m) {
        if (!transparent.add(m)) {
            System.out.println("已添加透明方块: " + m.toString());
        }
        if (!m.isBlock()) {
            System.out.println(m + " 不是一个方块!");
        }
    }

    /**
     * Returns true if the given block could be used to make a shop out of.
     *
     * @param b
     *            The block to check. Possibly a chest, dispenser, etc.
     * @return True if it can be made into a shop, otherwise false.
     */
    public static boolean canBeShop(final Block b) {
        try {
            final BlockState bs = b.getState();
            return bs instanceof InventoryHolder && shoppables.contains(bs.getType());
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Counts the number of items in the given inventory where
     * Util.matches(inventory item, item) is true.
     *
     * @param inv
     *            The inventory to search
     * @param item
     *            The ItemStack to search for
     * @return The number of items that match in this inventory.
     */
    public static int countItems(final Inventory inv, final ItemStack item) {
        int items = 0;
        for (final ItemStack iStack : inv.getContents()) {
            if (iStack == null) {
                continue;
            }
            if (Util.matches(item, iStack)) {
                items += iStack.getAmount();
            }
        }
        return items;
    }

    /**
     * Returns the number of items that can be given to the inventory safely.
     *
     * @param inv
     *            The inventory to count
     * @param item
     *            The item prototype. Material, durabiltiy and enchants must
     *            match for 'stackability' to occur.
     * @return The number of items that can be given to the inventory safely.
     */
    public static int countSpace(final Inventory inv, final ItemStack item) {
        int space = 0;
        for (final ItemStack iStack : inv.getContents()) {
            if (iStack == null || iStack.getType() == Material.AIR) {
                space += item.getMaxStackSize();
            } else if (matches(item, iStack)) {
                space += item.getMaxStackSize() - iStack.getAmount();
            }
        }
        return space;
    }

    public static ItemStack deserialize(final String config) throws InvalidConfigurationException {
        final YamlConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(config);
        return cfg.getItemStack("item");
    }

    public static String firstUppercase(final String string) {
        if (string.length() > 1) { return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase(); }
        return string.toUpperCase();
    }

    /**
     * Formats the given number according to how vault would like it. E.g. $50
     * or 5 dollars.
     *
     * @return The formatted string.
     */
    public static String format(final double n) {
        try {
            return DECIMAL_FORMAT.format(n) + plugin.getEcon().currencyNamePlural();
        } catch (final NumberFormatException e) {
            return n + "元";
        }
    }

    /**
     * Fetches the block which the given sign is attached to
     *
     * @param b
     *            The sign which is attached
     * @return The block the sign is attached to
     */
    public static Block getAttached(final Block b) {
        try {
            final Sign sign = (Sign) b.getState().getData(); // Throws a NPE
            // sometimes??
            final BlockFace attached = sign.getAttachedFace();
            return attached == null ? null : b.getRelative(attached);
        } catch (final NullPointerException e) {
            return null; // /Not sure what causes this.
        }
    }

    /**
     * Fetches an ItemStack's name - For example, converting INK_SAC:11 to
     * Dandellion Yellow, or WOOL:14 to Red Wool
     *
     * @param i
     *            The itemstack to fetch the name of
     * @return The human readable item name.
     */
    public static String getName(final ItemStack i) {
        return L10N.getItemName(i);
    }

    // Let's make very long names shorter for our sign
    public static String getNameForSign(final ItemStack itemStack) {
        String name = getName(itemStack);
        if (name.length() > 16) {
            name = name.substring(0, 16);
        }
        return name;
    }

    /**
     * Returns the chest attached to the given chest. The given block must be a
     * chest.
     *
     * @param b
     *            The chest to check.
     * @return the block which is also a chest and connected to b.
     */
    public static Block getSecondHalf(final Block b) {
        if (!b.getType().toString().contains("CHEST")) { return null; }
        final Block[] blocks = new Block[4];
        blocks[0] = b.getRelative(1, 0, 0);
        blocks[1] = b.getRelative(-1, 0, 0);
        blocks[2] = b.getRelative(0, 0, 1);
        blocks[3] = b.getRelative(0, 0, -1);
        for (final Block c : blocks) {
            if (c.getType() == b.getType()) { return c; }
        }
        return null;
    }

    /**
     * Gets the percentage (Without trailing %) damage on a tool.
     *
     * @param item
     *            The ItemStack of tools to check
     * @return The percentage 'health' the tool has. (Opposite of total damage)
     */
    public static String getToolPercentage(final ItemStack item) {
        final double dura = item.getDurability();
        final double max = item.getType().getMaxDurability();
        return String.format("%.2f%%(剩余耐久%s/总耐久%s)", (1 - dura / max) * 100.0, max - dura, max);
    }

    public static void initialize() {
        tools.clear();
        blacklist.clear();
        shoppables.clear();
        transparent.clear();
        plugin = QuickShop.instance;
        for (final String s : plugin.getConfig().getStringList("shop-blocks")) {
            Material mat = Material.getMaterial(s.toUpperCase());
            if (mat == null) {
                try {
                    mat = Material.getMaterial(Integer.parseInt(s));
                } catch (final NumberFormatException ignored) {
                }
            }
            if (mat == null) {
                plugin.getLogger().info("Invalid shop-block: " + s);
            } else {
                shoppables.add(mat);
            }
        }
        tools.add(Material.BOW);
        tools.add(Material.SHEARS);
        tools.add(Material.FISHING_ROD);
        tools.add(Material.FLINT_AND_STEEL);
        tools.add(Material.CHAINMAIL_BOOTS);
        tools.add(Material.CHAINMAIL_CHESTPLATE);
        tools.add(Material.CHAINMAIL_HELMET);
        tools.add(Material.CHAINMAIL_LEGGINGS);
        tools.add(Material.WOOD_AXE);
        tools.add(Material.WOOD_HOE);
        tools.add(Material.WOOD_PICKAXE);
        tools.add(Material.WOOD_SPADE);
        tools.add(Material.WOOD_SWORD);
        tools.add(Material.LEATHER_BOOTS);
        tools.add(Material.LEATHER_CHESTPLATE);
        tools.add(Material.LEATHER_HELMET);
        tools.add(Material.LEATHER_LEGGINGS);
        tools.add(Material.DIAMOND_AXE);
        tools.add(Material.DIAMOND_HOE);
        tools.add(Material.DIAMOND_PICKAXE);
        tools.add(Material.DIAMOND_SPADE);
        tools.add(Material.DIAMOND_SWORD);
        tools.add(Material.DIAMOND_BOOTS);
        tools.add(Material.DIAMOND_CHESTPLATE);
        tools.add(Material.DIAMOND_HELMET);
        tools.add(Material.DIAMOND_LEGGINGS);
        tools.add(Material.STONE_AXE);
        tools.add(Material.STONE_HOE);
        tools.add(Material.STONE_PICKAXE);
        tools.add(Material.STONE_SPADE);
        tools.add(Material.STONE_SWORD);
        tools.add(Material.GOLD_AXE);
        tools.add(Material.GOLD_HOE);
        tools.add(Material.GOLD_PICKAXE);
        tools.add(Material.GOLD_SPADE);
        tools.add(Material.GOLD_SWORD);
        tools.add(Material.GOLD_BOOTS);
        tools.add(Material.GOLD_CHESTPLATE);
        tools.add(Material.GOLD_HELMET);
        tools.add(Material.GOLD_LEGGINGS);
        tools.add(Material.IRON_AXE);
        tools.add(Material.IRON_HOE);
        tools.add(Material.IRON_PICKAXE);
        tools.add(Material.IRON_SPADE);
        tools.add(Material.IRON_SWORD);
        tools.add(Material.IRON_BOOTS);
        tools.add(Material.IRON_CHESTPLATE);
        tools.add(Material.IRON_HELMET);
        tools.add(Material.IRON_LEGGINGS);
        final List<String> configBlacklist = plugin.getConfig().getStringList("blacklist");
        for (final String s : configBlacklist) {
            Material mat = Material.getMaterial(s.toUpperCase());
            if (mat == null) {
                mat = Material.getMaterial(Integer.parseInt(s));
                if (mat == null) {
                    plugin.getLogger().info(s + " is not a valid material.  Check your spelling or ID");
                    continue;
                }
            }
            blacklist.add(mat);
        }

        // ToDo: add extras to config file
        addTransparentBlock(Material.AIR);
        /* Misc */
        addTransparentBlock(Material.CAKE_BLOCK);
        /* Redstone Material */
        addTransparentBlock(Material.REDSTONE_WIRE);
        /* Redstone Torches */
        addTransparentBlock(Material.REDSTONE_TORCH_OFF);
        addTransparentBlock(Material.REDSTONE_TORCH_ON);
        /* Diodes (Repeaters) */
        addTransparentBlock(Material.DIODE_BLOCK_OFF);
        addTransparentBlock(Material.DIODE_BLOCK_ON);
        /* Power Sources */
        addTransparentBlock(Material.DETECTOR_RAIL);
        addTransparentBlock(Material.LEVER);
        addTransparentBlock(Material.STONE_BUTTON);
        addTransparentBlock(Material.WOOD_BUTTON);
        addTransparentBlock(Material.STONE_PLATE);
        addTransparentBlock(Material.WOOD_PLATE);
        /* Nature Material */
        addTransparentBlock(Material.RED_MUSHROOM);
        addTransparentBlock(Material.BROWN_MUSHROOM);
        addTransparentBlock(Material.RED_ROSE);
        addTransparentBlock(Material.YELLOW_FLOWER);
        addTransparentBlock(Material.FLOWER_POT);
        /* Greens */
        addTransparentBlock(Material.LONG_GRASS);
        addTransparentBlock(Material.VINE);
        addTransparentBlock(Material.WATER_LILY);
        /* Seedy things */
        addTransparentBlock(Material.MELON_STEM);
        addTransparentBlock(Material.PUMPKIN_STEM);
        addTransparentBlock(Material.CROPS);
        addTransparentBlock(Material.NETHER_WARTS);
        /* Semi-nature */
        addTransparentBlock(Material.SNOW);
        addTransparentBlock(Material.FIRE);
        addTransparentBlock(Material.WEB);
        addTransparentBlock(Material.TRIPWIRE);
        addTransparentBlock(Material.TRIPWIRE_HOOK);
        /* Stairs */
        addTransparentBlock(Material.COBBLESTONE_STAIRS);
        addTransparentBlock(Material.BRICK_STAIRS);
        addTransparentBlock(Material.SANDSTONE_STAIRS);
        addTransparentBlock(Material.NETHER_BRICK_STAIRS);
        addTransparentBlock(Material.SMOOTH_STAIRS);
        /* Wood Stairs */
        addTransparentBlock(Material.BIRCH_WOOD_STAIRS);
        addTransparentBlock(Material.WOOD_STAIRS);
        addTransparentBlock(Material.JUNGLE_WOOD_STAIRS);
        addTransparentBlock(Material.SPRUCE_WOOD_STAIRS);
        /* Lava & Water */
        addTransparentBlock(Material.LAVA);
        addTransparentBlock(Material.STATIONARY_LAVA);
        addTransparentBlock(Material.WATER);
        addTransparentBlock(Material.STATIONARY_WATER);
        /* Saplings and bushes */
        addTransparentBlock(Material.SAPLING);
        addTransparentBlock(Material.DEAD_BUSH);
        /* Construction Material */
        /* Fences */
        addTransparentBlock(Material.FENCE);
        addTransparentBlock(Material.FENCE_GATE);
        addTransparentBlock(Material.IRON_FENCE);
        addTransparentBlock(Material.NETHER_FENCE);
        /* Ladders, Signs */
        addTransparentBlock(Material.LADDER);
        addTransparentBlock(Material.SIGN_POST);
        addTransparentBlock(Material.WALL_SIGN);
        /* Bed */
        addTransparentBlock(Material.BED_BLOCK);
        /* Pistons */
        addTransparentBlock(Material.PISTON_EXTENSION);
        addTransparentBlock(Material.PISTON_MOVING_PIECE);
        addTransparentBlock(Material.RAILS);
        /* Torch & Trapdoor */
        addTransparentBlock(Material.TORCH);
        addTransparentBlock(Material.TRAP_DOOR);
        /* New */
        addTransparentBlock(Material.BREWING_STAND);
        addTransparentBlock(Material.WOODEN_DOOR);
        addTransparentBlock(Material.WOOD_STEP);
    }

    /**
     * @param m
     *            The material to check if it is blacklisted
     * @return true if the material is black listed. False if not.
     */
    public static boolean isBlacklisted(final Material m) {
        return blacklist.contains(m);
    }

    /**
     * Returns true if the given location is loaded or not.
     *
     * @param loc
     *            The location
     * @return true if the given location is loaded or not.
     */
    public static boolean isLoaded(final Location loc) {
        // System.out.println("Checking isLoaded(Location loc)");
        if (loc.getWorld() == null) {
            // System.out.println("Is not loaded. (No world)");
            return false;
        }
        // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
        // location rounded to the nearest 16.
        final int x = (int) Math.floor((loc.getBlockX()) / 16.0);
        final int z = (int) Math.floor((loc.getBlockZ()) / 16.0);
        if (loc.getWorld().isChunkLoaded(x, z)) {
            // System.out.println("Chunk is loaded " + x + ", " + z);
            return true;
        }
        // System.out.println("Chunk is NOT loaded " + x + ", " + z);
        return false;
    }

    /**
     * @param mat
     *            The material to check
     * @return Returns true if the item is a tool (Has durability) or false if
     *         it doesn't.
     */
    public static boolean isTool(final Material mat) {
        return tools.contains(mat);
    }

    public static boolean isTransparent(final Material m) {
        return transparent.contains(m);
    }

    /**
     * Converts a string into an item from the database.
     *
     * @param itemString
     *            The database string. Is the result of makeString(ItemStack
     *            item).
     * @return A new itemstack, with the properties given in the string
     */
    public static ItemStack makeItem(final String itemString) {
        final String[] itemInfo = itemString.split(":");
        final ItemStack item = new ItemStack(Material.getMaterial(itemInfo[0]));
        final MaterialData data = new MaterialData(Integer.parseInt(itemInfo[1]));
        item.setData(data);
        item.setDurability(Short.parseShort(itemInfo[2]));
        item.setAmount(Integer.parseInt(itemInfo[3]));
        for (int i = 4; i < itemInfo.length; i = i + 2) {
            int level = Integer.parseInt(itemInfo[i + 1]);
            final Enchantment ench = Enchantment.getByName(itemInfo[i]);
            if (ench == null) {
                continue; // Invalid
            }
            if (ench.canEnchantItem(item)) {
                if (level <= 0) {
                    continue;
                }
                level = Math.min(ench.getMaxLevel(), level);
                item.addEnchantment(ench, level);
            }
        }
        return item;
    }

    /**
     * Compares two items to each other. Returns true if they match.
     *
     * @param stack1
     *            The first item stack
     * @param stack2
     *            The second item stack
     * @return true if the itemstacks match. (Material, durability, enchants, name)
     */
    public static boolean matches(final ItemStack stack1, final ItemStack stack2) {
        if (stack1 == stack2) { return true; // Referring to the same thing, or both are null.
        }
        if (stack1 == null || stack2 == null) { return false; // One of them is null (Can't be both, see above)
        }
        if (stack1.getType() != stack2.getType()) { return false; // Not the same material
        }
        if (stack1.getDurability() != stack2.getDurability()) { return false; // Not the same durability
        }
        if (!stack1.getEnchantments().equals(stack2.getEnchantments())) { return false; // They have the same enchants
        }
        if (stack1.getItemMeta().hasDisplayName() || stack2.getItemMeta().hasDisplayName()) {
            if (stack1.getItemMeta().hasDisplayName() && stack2.getItemMeta().hasDisplayName()) {
                if (!stack1.getItemMeta().getDisplayName().equals(stack2.getItemMeta().getDisplayName())) { return false; // items have different display name
                }
            } else {
                return false; // one of the item stacks have a display name
            }
        }
        try {
            Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
            final boolean book1 = stack1.getItemMeta() instanceof EnchantmentStorageMeta;
            final boolean book2 = stack2.getItemMeta() instanceof EnchantmentStorageMeta;
            if (book1 != book2) { return false;// One has enchantment meta, the other does not.
            }
            if (book1) { // They are the same here (both true or both
                             // false). So if one is true, the other is
                                 // true.
                final Map<Enchantment, Integer> ench1 = ((EnchantmentStorageMeta) stack1.getItemMeta()).getStoredEnchants();
                final Map<Enchantment, Integer> ench2 = ((EnchantmentStorageMeta) stack2.getItemMeta()).getStoredEnchants();
                if (!ench1.equals(ench2)) { return false; // Enchants aren't the same.
                }
            }
        } catch (final ClassNotFoundException e) {
            // Nothing. They dont have a build high enough to support this.
        }
        return true;
    }

    public static void parseColours(final YamlConfiguration config) {
        final Set<String> keys = config.getKeys(true);
        for (final String key : keys) {
            String filtered = config.getString(key);
            if (filtered.startsWith("MemorySection")) {
                continue;
            }
            filtered = ChatColor.translateAlternateColorCodes('&', filtered);
            config.set(key, filtered);
        }
    }

    public static String serialize(final ItemStack iStack) {
        final YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("item", iStack);
        return cfg.saveToString();
    }
}

package org.maxgamer.QuickShop.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.maxgamer.QuickShop.QuickShop;

public class NMS {
	// private static ArrayList<NMSDependent> dependents = new ArrayList<NMSDependent>();
	private static int nextId = 0;
	// private static NMSDependent nms;

	// static {
	// NMSDependent dep;
	// dep = new NMSDependent("v1_6_R3") {
	// @Override
	// public void safeGuard(Item item) {
	// org.bukkit.inventory.ItemStack iStack = item.getItemStack();
	// net.minecraft.server.v1_6_R3.ItemStack nmsI = org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack.asNMSCopy(iStack);
	// nmsI.count = 0;
	// iStack = org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack.asBukkitCopy(nmsI);
	// item.setItemStack(iStack);
	// }
	//
	// @Override
	// public byte[] getNBTBytes(org.bukkit.inventory.ItemStack iStack) {
	// net.minecraft.server.v1_6_R3.ItemStack is = org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack.asNMSCopy(iStack);
	// net.minecraft.server.v1_6_R3.NBTTagCompound itemCompound = new net.minecraft.server.v1_6_R3.NBTTagCompound();
	// itemCompound = is.save(itemCompound);
	// return net.minecraft.server.v1_6_R3.NBTCompressedStreamTools.a(itemCompound);
	// }
	//
	// @Override
	// public org.bukkit.inventory.ItemStack getItemStack(byte[] bytes) {
	// net.minecraft.server.v1_6_R3.NBTTagCompound c = net.minecraft.server.v1_6_R3.NBTCompressedStreamTools.a(bytes);
	// net.minecraft.server.v1_6_R3.ItemStack is = net.minecraft.server.v1_6_R3.ItemStack.createStack(c);
	// return org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack.asBukkitCopy(is);
	// }
	// };
	// dependents.add(dep);
	// dep = new NMSDependent("v1_7_R1") {
	// @Override
	// public void safeGuard(Item item) {
	// if(QuickShop.debug)System.out.println("safeGuard");
	// org.bukkit.inventory.ItemStack iStack = item.getItemStack();
	// net.minecraft.server.v1_7_R1.ItemStack nmsI = org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack.asNMSCopy(iStack);
	// nmsI.count = 0;
	// iStack = org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack.asBukkitCopy(nmsI);
	// item.setItemStack(iStack);
	// }
	//
	// @Override
	// public byte[] getNBTBytes(org.bukkit.inventory.ItemStack iStack) {
	// if(QuickShop.debug)System.out.println("getNBTBytes");
	// net.minecraft.server.v1_7_R1.ItemStack is = org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack.asNMSCopy(iStack);
	// net.minecraft.server.v1_7_R1.NBTTagCompound itemCompound = new net.minecraft.server.v1_7_R1.NBTTagCompound();
	// itemCompound = is.save(itemCompound);
	// return net.minecraft.server.v1_7_R1.NBTCompressedStreamTools.a(itemCompound);
	// }
	//
	// @Override
	// public org.bukkit.inventory.ItemStack getItemStack(byte[] bytes) {
	// if(QuickShop.debug)System.out.println("getItemStack");
	// net.minecraft.server.v1_7_R1.NBTTagCompound c = net.minecraft.server.v1_7_R1.NBTCompressedStreamTools.a(bytes);
	// net.minecraft.server.v1_7_R1.ItemStack is = net.minecraft.server.v1_7_R1.ItemStack.createStack(c);
	// return org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack.asBukkitCopy(is);
	// }
	// };
	// dependents.add(dep);
	// dep = new NMSDependent("v1_7_R3") {
	// @Override
	// public void safeGuard(Item item) {
	// if(QuickShop.debug)System.out.println("safeGuard");
	// org.bukkit.inventory.ItemStack iStack = item.getItemStack();
	// net.minecraft.server.v1_7_R3.ItemStack nmsI = org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack.asNMSCopy(iStack);
	// nmsI.count = 0;
	// iStack = org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack.asBukkitCopy(nmsI);
	// item.setItemStack(iStack);
	// }
	//
	// @Override
	// public byte[] getNBTBytes(org.bukkit.inventory.ItemStack iStack) {
	// if(QuickShop.debug)System.out.println("getNBTBytes");
	// net.minecraft.server.v1_7_R3.ItemStack is = org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack.asNMSCopy(iStack);
	// net.minecraft.server.v1_7_R3.NBTTagCompound itemCompound = new net.minecraft.server.v1_7_R3.NBTTagCompound();
	// itemCompound = is.save(itemCompound);
	// return net.minecraft.server.v1_7_R3.NBTCompressedStreamTools.a(itemCompound);
	// }
	//
	// @Override
	// public org.bukkit.inventory.ItemStack getItemStack(byte[] bytes) {
	// if(QuickShop.debug)System.out.println("getItemStack");
	// net.minecraft.server.v1_7_R3.NBTTagCompound c = net.minecraft.server.v1_7_R3.NBTCompressedStreamTools.a(bytes, null);
	// net.minecraft.server.v1_7_R3.ItemStack is = net.minecraft.server.v1_7_R3.ItemStack.createStack(c);
	// return org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack.asBukkitCopy(is);
	// }
	// };
	// dependents.add(dep);
	// dep = new NMSDependent("v1_8") {
	// @Override
	// public void safeGuard(Item item) {
	// if(QuickShop.debug)System.out.println("safeGuard");
	// org.bukkit.inventory.ItemStack iStack = item.getItemStack();
	// net.minecraft.server.v1_8_R1.ItemStack nmsI = org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack.asNMSCopy(iStack);
	// nmsI.count = 0;
	// iStack = org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack.asBukkitCopy(nmsI);
	// item.setItemStack(iStack);
	// }
	//
	// @Override
	// public byte[] getNBTBytes(org.bukkit.inventory.ItemStack iStack) {
	// try{
	// if(QuickShop.debug)System.out.println("getNBTBytes");
	// net.minecraft.server.v1_8_R1.ItemStack is = org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack.asNMSCopy(iStack);
	// net.minecraft.server.v1_8_R1.NBTTagCompound itemCompound = new net.minecraft.server.v1_8_R1.NBTTagCompound();
	// itemCompound = is.save(itemCompound);
	// ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
	// DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));
	// try {
	// net.minecraft.server.v1_8_R1.NBTCompressedStreamTools.a(itemCompound, (DataOutput) dataoutputstream);
	// } finally {
	// dataoutputstream.close();
	// }
	// return bytearrayoutputstream.toByteArray();
	// }catch(Exception e){
	// return new byte[0];
	// }
	// //return net.minecraft.server.v1_8_R1.NBTCompressedStreamTools.a(itemCompound);
	// }
	//
	// @Override
	// public org.bukkit.inventory.ItemStack getItemStack(byte[] bytes) {
	// try{
	// if(QuickShop.debug)System.out.println("getItemStack");
	// DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes))));
	// net.minecraft.server.v1_8_R1.NBTTagCompound nbttagcompound;
	// try {
	// nbttagcompound = net.minecraft.server.v1_8_R1.NBTCompressedStreamTools.a((DataInput) datainputstream, null);
	// } finally {
	// datainputstream.close();
	// }
	// //net.minecraft.server.v1_8_R1.NBTTagCompound c = net.minecraft.server.v1_8_R1.NBTCompressedStreamTools.a(bytes, null);
	// net.minecraft.server.v1_8_R1.ItemStack is = net.minecraft.server.v1_8_R1.ItemStack.createStack(nbttagcompound);
	// return org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack.asBukkitCopy(is);
	// }catch(Exception e){
	// return new ItemStack(Material.AIR);
	// }
	// }
	// };
	// dependents.add(dep);
	// }

	public static void safeGuard(final Item item) throws ClassNotFoundException {
		if (QuickShop.debug) {
			System.out.println("Renaming");
		}
		rename(item.getItemStack());
		if (QuickShop.debug) {
			System.out.println("Protecting");
		}
		protect(item);
		if (QuickShop.debug) {
			System.out.println("Seting pickup delay");
		}
		item.setPickupDelay(2147483647);
	}

	private static void protect(final Item item) {
		try {
			final Field itemField = item.getClass().getDeclaredField("item");
			itemField.setAccessible(true);
			final Object nmsEntityItem = itemField.get(item);
			Method getItemStack;
			try {
				getItemStack = nmsEntityItem.getClass().getMethod("getItemStack", new Class[0]);
			} catch (final NoSuchMethodException e) {
				try {
					getItemStack = nmsEntityItem.getClass().getMethod("d", new Class[0]);
				} catch (final NoSuchMethodException e2) {
					return;
				}
			}
			final Object itemStack = getItemStack.invoke(nmsEntityItem, new Object[0]);
			Field countField;
			try {
				countField = itemStack.getClass().getDeclaredField("count");
			} catch (final NoSuchFieldException e) {
				countField = itemStack.getClass().getDeclaredField("a");
			}
			countField.setAccessible(true);
			countField.set(itemStack, Integer.valueOf(1));
		} catch (final NoSuchFieldException e) {
			e.printStackTrace();
			System.out.println("[QuickShop] Could not protect item from pickup properly! Dupes are now possible.");
		} catch (final Exception e) {
			System.out.println("Other error");
			e.printStackTrace();
		}
	}

	// public static byte[] getNBTBytes(org.bukkit.inventory.ItemStack iStack) throws ClassNotFoundException {
	// validate();
	// return nms.getNBTBytes(iStack);
	// }
	//
	// public static ItemStack getItemStack(byte[] bytes) throws ClassNotFoundException {
	// validate();
	// return nms.getItemStack(bytes);
	// }

	private static void rename(final ItemStack iStack) {
		final ItemMeta meta = iStack.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "QuickShop " + Util.getName(iStack) + " " + nextId++);
		iStack.setItemMeta(meta);
	}

	// private static void validate() throws ClassNotFoundException {
	// if (nms != null) {
	// return;
	// }
	// String packageName = Bukkit.getServer().getClass().getPackage().getName();
	// packageName = packageName.substring(packageName.lastIndexOf(".") + 1);
	// // System.out.println("Package: " + packageName);
	// for (NMSDependent dep : dependents) {
	// if ((packageName.startsWith(dep.getVersion())) || ((dep.getVersion().isEmpty()) && ((packageName.equals("bukkit")) || (packageName.equals("craftbukkit"))))) {
	// nms = dep;
	// return;
	// }
	// }
	// throw new ClassNotFoundException("This version of QuickShop is incompatible.");
	// }

	// private static abstract class NMSDependent {
	// private String version;
	//
	// public String getVersion() {
	// return this.version;
	// }
	//
	// public NMSDependent(String version) {
	// this.version = version;
	// }
	//
	// public abstract void safeGuard(Item paramItem);
	//
	// public abstract byte[] getNBTBytes(org.bukkit.inventory.ItemStack paramItemStack);
	//
	// public abstract org.bukkit.inventory.ItemStack getItemStack(byte[] paramArrayOfByte);
	// }
}
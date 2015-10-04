package org.maxgamer.QuickShop.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;

public class NMS {

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

	private static void rename(final ItemStack iStack) {
		MarkUtil.addMark(iStack);
	}
}
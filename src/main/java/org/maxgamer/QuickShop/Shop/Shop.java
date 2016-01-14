package org.maxgamer.QuickShop.Shop;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract interface Shop {
    public abstract void add(ItemStack paramItemStack, int paramInt);

    public abstract void buy(Player paramPlayer, int paramInt);

    public abstract Shop clone();

    public abstract void delete();

    public abstract void delete(boolean paramBoolean);

    public abstract String getDataName();

    public abstract short getDurability();

    public abstract ItemStack getItem();

    public abstract Location getLocation();

    public abstract String getOwner();

    public abstract double getPrice();

    public abstract int getRemainingSpace();

    public abstract int getRemainingStock();

    public abstract ShopType getShopType();

    public abstract List<Sign> getSigns();

    public abstract boolean isAttached(Block paramBlock);

    public abstract boolean isBuying();

    public abstract boolean isSelling();

    public abstract boolean isUnlimited();

    public abstract boolean isValid();

    public abstract boolean matches(ItemStack paramItemStack);

    public abstract void onClick();

    public abstract void onLoad();

    public abstract void onUnload();

    public abstract void remove(ItemStack paramItemStack, int paramInt);

    public abstract void sell(Player paramPlayer, int paramInt);

    public abstract void setOwner(String paramString);

    public abstract void setPrice(double paramDouble);

    public abstract void setShopType(ShopType paramShopType);

    public abstract void setSignText();

    public abstract void setSignText(String[] paramArrayOfString);

    public abstract void setUnlimited(boolean paramBoolean);

    public abstract void update();
}
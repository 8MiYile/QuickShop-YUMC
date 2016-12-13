package org.maxgamer.QuickShop.Shop;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Shop {
    void add(ItemStack paramItemStack, int paramInt);

    void buy(Player paramPlayer, int paramInt);

    Shop clone();

    void delete();

    void delete(boolean paramBoolean);

    String getDataName();

    short getDurability();

    ItemStack getItem();

    Location getLocation();

    String getOwner();

    double getPrice();

    int getRemainingSpace();

    int getRemainingStock();

    ShopType getShopType();

    List<Sign> getSigns();

    boolean isAttached(Block paramBlock);

    boolean isBuying();

    boolean isSelling();

    boolean isUnlimited();

    boolean isValid();

    boolean matches(ItemStack paramItemStack);

    void onClick();

    void onLoad();

    void onUnload();

    void remove(ItemStack paramItemStack, int paramInt);

    void sell(Player paramPlayer, int paramInt);

    void setOwner(String paramString);

    void setPrice(double paramDouble);

    void setShopType(ShopType paramShopType);

    void setSignText();

    void setSignText(String[] paramArrayOfString);

    void setUnlimited(boolean paramBoolean);

    void update();
}
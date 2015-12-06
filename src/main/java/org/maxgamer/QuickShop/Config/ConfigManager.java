package org.maxgamer.QuickShop.Config;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;

import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.tellraw.FancyMessage;

public class ConfigManager {
	private boolean enableMagicLib = false;
	/** Whether debug info should be shown in the console */
	private final boolean debug = false;

	/** Whether we should use display items or not */
	private boolean display = true;
	private double feeForPriceChange = 0.0;
	private int findDistance = 30;
	private String guiTitle = "§6[§b快捷商店§6]§r";
	/** Whether or not to limit players shop amounts */
	private boolean limit = false;

	private int limitdefault = 0;
	private final HashMap<String, Integer> limits = new HashMap<>();
	private boolean logAction = true;
	private boolean preventhopper = false;
	/**
	 * Whether we players are charged a fee to change the price on their shop
	 * (To help deter endless undercutting
	 */
	private boolean priceChangeRequiresFee = false;
	private boolean shopLock = true;
	private boolean showTax = false;
	/** Whether players are required to sneak to create/buy from a shop */
	private boolean sneak = false;
	/** Whether players are required to sneak to create a shop */
	private boolean sneakCreate = false;
	/** Whether players are required to sneak to trade with a shop */
	private boolean sneakTrade = false;
	private Material superItem = Material.GOLD_AXE;
	private double tax = 0;
	private final String taxAccount;
	/**
	 * A set of players who have been warned
	 * ("Your shop isn't automatically locked")
	 */
	private final HashSet<String> warnings = new HashSet<>();

	public ConfigManager(final QuickShop plugin) {
		final FileConfig config = (FileConfig) plugin.getConfig();
		ConfigurationSection limitCfg = config.getConfigurationSection("limits");
		if (limitCfg != null) {
			this.limit = limitCfg.getBoolean("use", false);
			this.limitdefault = config.getInt("limits.default");
			limitCfg = limitCfg.getConfigurationSection("ranks");
			for (final String key : limitCfg.getKeys(true)) {
				limits.put(key, limitCfg.getInt(key));
			}
		}
		try {
			this.superItem = Material.valueOf(config.getString("superitem"));
		} catch (final Exception e) {
		}
		this.tax = config.getDouble("tax");
		this.showTax = config.getBoolean("show-tax");
		this.taxAccount = config.getString("tax-account");
		this.logAction = config.getBoolean("log-actions");
		this.shopLock = config.getBoolean("shop.lock");
		this.display = config.getBoolean("shop.display-items");
		this.sneak = config.getBoolean("shop.sneak-only");
		this.sneakCreate = config.getBoolean("shop.sneak-to-create");
		this.sneakTrade = config.getBoolean("shop.sneak-to-trade");
		this.priceChangeRequiresFee = config.getBoolean("shop.price-change-requires-fee");
		this.findDistance = config.getInt("shop.find-distance");
		this.feeForPriceChange = config.getDouble("shop.fee-for-price-change");
		this.preventhopper = config.getBoolean("preventhopper");
		this.guiTitle = config.getMessage("guititle", guiTitle);
		if (config.getBoolean("usemagiclib", true)) {
			try {
				plugin.getLogger().info("启用魔改库 尝试启动中...");
				final FancyMessage fm = FancyMessage.newFM("test");
				fm.then("item").itemTooltip(new ItemStack(Material.DIAMOND_SWORD));
				fm.then("link").link("ci.citycraft.cn");
				fm.then("suggest").suggest("qs help");
				fm.toJSONString();
				plugin.getLogger().info("魔改库功能测试正常...");
				this.enableMagicLib = true;
			} catch (final Error | Exception e) {
				plugin.getLogger().warning("+=========================================");
				plugin.getLogger().warning("| 警告: 启动魔改库失败 将使用GUI商店界面...");
				plugin.getLogger().warning("+=========================================");
			}
		}
	}

	public double getFeeForPriceChange() {
		return feeForPriceChange;
	}

	public int getFindDistance() {
		return findDistance;
	}

	public String getGuiTitle() {
		return guiTitle;
	}

	public int getLimitdefault() {
		return limitdefault;
	}

	public HashMap<String, Integer> getLimits() {
		return limits;
	}

	public Material getSuperItem() {
		return superItem;
	}

	public double getTax() {
		return tax;
	}

	public String getTaxAccount() {
		return taxAccount;
	}

	public HashSet<String> getWarnings() {
		return warnings;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isDisplay() {
		return display;
	}

	public boolean isEnableMagicLib() {
		return enableMagicLib;
	}

	public boolean isLimit() {
		return limit;
	}

	public boolean isLogAction() {
		return logAction;
	}

	public boolean isPreventHopper() {
		return preventhopper;
	}

	public boolean isPriceChangeRequiresFee() {
		return priceChangeRequiresFee;
	}

	public boolean isShopLock() {
		return shopLock;
	}

	public boolean isShowTax() {
		return showTax;
	}

	public boolean isSneak() {
		return sneak;
	}

	public boolean isSneakCreate() {
		return sneakCreate;
	}

	public boolean isSneakTrade() {
		return sneakTrade;
	}

	public void setEnableMagicLib(final boolean enableMagicLib) {
		this.enableMagicLib = enableMagicLib;
	}

}

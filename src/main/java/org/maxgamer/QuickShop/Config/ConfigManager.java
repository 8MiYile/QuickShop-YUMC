package org.maxgamer.QuickShop.Config;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.maxgamer.QuickShop.QuickShop;

import cn.citycraft.PluginHelper.config.FileConfig;

public class ConfigManager {
	/** Whether debug info should be shown in the console */
	protected boolean debug = false;
	/** Whether we should use display items or not */
	protected boolean display = true;
	protected double feeForPriceChange = 0.0;
	protected int findDistance = 30;
	protected String guiTitle;
	/** Whether or not to limit players shop amounts */
	protected boolean limit = false;

	protected int limitdefault = 0;
	protected final HashMap<String, Integer> limits = new HashMap<String, Integer>();
	protected boolean logAction = true;
	protected boolean preventhopper = false;
	/**
	 * Whether we players are charged a fee to change the price on their shop
	 * (To help deter endless undercutting
	 */
	protected boolean priceChangeRequiresFee = false;
	protected boolean shopLock = true;
	protected boolean showTax;
	/** Whether players are required to sneak to create/buy from a shop */
	protected boolean sneak;
	/** Whether players are required to sneak to create a shop */
	protected boolean sneakCreate;
	/** Whether players are required to sneak to trade with a shop */
	protected boolean sneakTrade;
	protected Material superItem = Material.GOLD_AXE;
	protected double tax = 0;
	protected String taxAccount;
	/** Use SpoutPlugin to get item / block names */
	protected boolean useSpout = false;
	/**
	 * A set of players who have been warned
	 * ("Your shop isn't automatically locked")
	 */
	protected HashSet<String> warnings = new HashSet<String>();

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
			this.superItem = Enum.valueOf(Material.class, config.getString("superitem"));
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
		this.guiTitle = config.getMessage("guititle");
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

	public boolean isUseSpout() {
		return useSpout;
	}

}

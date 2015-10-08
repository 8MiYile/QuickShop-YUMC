package org.maxgamer.QuickShop.Config;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.maxgamer.QuickShop.QuickShop;

public class ConfigManager {
	/** Whether debug info should be shown in the console */
	protected boolean debug = false;
	/** Whether we should use display items or not */
	protected boolean display = true;
	protected double feeForPriceChange = 0.0;
	protected int findDistance = 30;
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

		ConfigurationSection limitCfg = plugin.getConfig().getConfigurationSection("limits");
		if (limitCfg != null) {
			this.limit = limitCfg.getBoolean("use", false);
			this.limitdefault = plugin.getConfig().getInt("limits.default");
			limitCfg = limitCfg.getConfigurationSection("ranks");
			for (final String key : limitCfg.getKeys(true)) {
				limits.put(key, limitCfg.getInt(key));
			}
		}
		try {
			this.superItem = Enum.valueOf(Material.class, plugin.getConfig().getString("superitem"));
		} catch (final Exception e) {
		}
		this.tax = plugin.getConfig().getDouble("tax");
		this.showTax = plugin.getConfig().getBoolean("show-tax");
		this.taxAccount = plugin.getConfig().getString("tax-account");
		this.logAction = plugin.getConfig().getBoolean("log-actions");
		this.shopLock = plugin.getConfig().getBoolean("shop.lock");
		this.display = plugin.getConfig().getBoolean("shop.display-items");
		this.sneak = plugin.getConfig().getBoolean("shop.sneak-only");
		this.sneakCreate = plugin.getConfig().getBoolean("shop.sneak-to-create");
		this.sneakTrade = plugin.getConfig().getBoolean("shop.sneak-to-trade");
		this.priceChangeRequiresFee = plugin.getConfig().getBoolean("shop.price-change-requires-fee");
		this.findDistance = plugin.getConfig().getInt("shop.find-distance");
		this.feeForPriceChange = plugin.getConfig().getDouble("shop.fee-for-price-change");
		this.preventhopper = plugin.getConfig().getBoolean("preventhopper");
	}

	public double getFeeForPriceChange() {
		return feeForPriceChange;
	}

	public int getFindDistance() {
		return findDistance;
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

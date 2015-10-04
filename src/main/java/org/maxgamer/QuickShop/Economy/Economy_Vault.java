package org.maxgamer.QuickShop.Economy;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class Economy_Vault implements EconomyCore {
	private Economy vault;

	public Economy_Vault() {
		setupEconomy();
	}

	@Override
	@Deprecated
	public boolean deposit(final String name, final double amount) {
		return this.vault.depositPlayer(name, amount).transactionSuccess();
	}

	@Override
	public boolean deposit(final UUID name, final double amount) {
		final OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		return this.vault.depositPlayer(p, amount).transactionSuccess();
	}

	@Override
	public String format(final double balance) {
		try {
			return this.vault.format(balance);
		} catch (final NumberFormatException e) {
		}
		return "" + balance;
	}

	@Override
	@Deprecated
	public double getBalance(final String name) {
		return this.vault.getBalance(name);
	}

	@Override
	public double getBalance(final UUID name) {
		final OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		return this.vault.getBalance(p);
	}

	@Override
	public boolean isValid() {
		return this.vault != null;
	}

	@Override
	@Deprecated
	public boolean transfer(final String from, final String to, final double amount) {
		if (this.vault.getBalance(from) >= amount) {
			if (this.vault.withdrawPlayer(from, amount).transactionSuccess()) {
				if (!this.vault.depositPlayer(to, amount).transactionSuccess()) {
					this.vault.depositPlayer(from, amount);
					return false;
				}
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public boolean transfer(final UUID from, final UUID to, final double amount) {
		final OfflinePlayer pFrom = Bukkit.getOfflinePlayer(from);
		final OfflinePlayer pTo = Bukkit.getOfflinePlayer(to);
		if (this.vault.getBalance(pFrom) >= amount) {
			if (this.vault.withdrawPlayer(pFrom, amount).transactionSuccess()) {
				if (!this.vault.depositPlayer(pTo, amount).transactionSuccess()) {
					this.vault.depositPlayer(pFrom, amount);
					return false;
				}
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	@Deprecated
	public boolean withdraw(final String name, final double amount) {
		return this.vault.withdrawPlayer(name, amount).transactionSuccess();
	}

	@Override
	public boolean withdraw(final UUID name, final double amount) {
		final OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		return this.vault.withdrawPlayer(p, amount).transactionSuccess();
	}

	private boolean setupEconomy() {
		final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			this.vault = (economyProvider.getProvider());
		}
		return this.vault != null;
	}
}
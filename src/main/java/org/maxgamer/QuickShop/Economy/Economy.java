package org.maxgamer.QuickShop.Economy;

import java.util.UUID;

public class Economy implements EconomyCore {
    private final EconomyCore core;

    public Economy(final EconomyCore core) {
        this.core = core;
    }

    @Override
    public String currencyNamePlural() {
        return this.core.currencyNamePlural();
    }

    /**
     * Deposits a given amount of money from thin air to the given username.
     *
     * @param name
     *            The exact (case insensitive) username to give money to
     * @param amount
     *            The amount to give them
     * @return True if success (Should be almost always)
     */
    @Override
    @Deprecated
    public boolean deposit(final String name, final double amount) {
        return this.core.deposit(name, amount);
    }

    @Override
    public boolean deposit(final UUID name, final double amount) {
        return this.core.deposit(name, amount);
    }

    /**
     * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50
     * Dollars 5 Cents
     *
     * @param balance
     *            The given number
     * @return The balance in human readable text.
     */
    @Override
    public String format(final double balance) {
        return this.core.format(balance);
    }

    /**
     * Fetches the balance of the given account name
     *
     * @param name
     *            The name of the account
     * @return Their current balance.
     */
    @Override
    @Deprecated
    public double getBalance(final String name) {
        return this.core.getBalance(name);
    }

    @Override
    public double getBalance(final UUID name) {
        return this.core.getBalance(name);
    }

    @Deprecated
    public boolean has(final String name, final double amount) {
        return this.core.getBalance(name) >= amount;
    }

    /**
     * Checks that this economy is valid. Returns false if it is not valid.
     *
     * @return True if this economy will work, false if it will not.
     */
    @Override
    public boolean isValid() {
        return this.core.isValid();
    }

    @Override
    public String toString() {
        return this.core.getClass().getName().split("_")[1];
    }

    /**
     * Transfers the given amount of money from Player1 to Player2
     *
     * @param from
     *            The player who is paying money
     * @param to
     *            The player who is receiving money
     * @param amount
     *            The amount to transfer
     * @return true if success (Payer had enough cash, receiver was able to
     *         receive the funds)
     */
    @Override
    @Deprecated
    public boolean transfer(final String from, final String to, final double amount) {
        return this.core.transfer(from, to, amount);
    }

    @Override
    public boolean transfer(final UUID from, final UUID to, final double amount) {
        return this.core.transfer(from, to, amount);
    }

    /**
     * Withdraws a given amount of money from the given username and turns it to
     * thin air.
     *
     * @param name
     *            The exact (case insensitive) username to take money from
     * @param amount
     *            The amount to take from them
     * @return True if success, false if they didn't have enough cash
     */
    @Override
    @Deprecated
    public boolean withdraw(final String name, final double amount) {
        return this.core.withdraw(name, amount);
    }

    @Override
    public boolean withdraw(final UUID name, final double amount) {
        return this.core.withdraw(name, amount);
    }
}
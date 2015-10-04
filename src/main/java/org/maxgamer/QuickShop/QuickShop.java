package org.maxgamer.QuickShop;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.QuickShop.Command.QS;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Database.Database.ConnectionException;
import org.maxgamer.QuickShop.Database.DatabaseCore;
import org.maxgamer.QuickShop.Database.DatabaseHelper;
import org.maxgamer.QuickShop.Database.MySQLCore;
import org.maxgamer.QuickShop.Database.SQLiteCore;
import org.maxgamer.QuickShop.Economy.Economy;
import org.maxgamer.QuickShop.Economy.EconomyCore;
import org.maxgamer.QuickShop.Economy.Economy_Vault;
import org.maxgamer.QuickShop.Listeners.BlockListener;
import org.maxgamer.QuickShop.Listeners.ChatListener;
import org.maxgamer.QuickShop.Listeners.ChunkListener;
import org.maxgamer.QuickShop.Listeners.LockListener;
import org.maxgamer.QuickShop.Listeners.PlayerListener;
import org.maxgamer.QuickShop.Listeners.WorldListener;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopManager;
import org.maxgamer.QuickShop.Shop.ShopType;
import org.maxgamer.QuickShop.Util.Converter;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;
import org.maxgamer.QuickShop.Watcher.ItemWatcher;
import org.maxgamer.QuickShop.Watcher.LogWatcher;
import org.mcstats.Metrics;

import cn.citycraft.PluginHelper.utils.VersionChecker;

public class QuickShop extends JavaPlugin {
	/** The active instance of QuickShop */
	public static QuickShop instance;
	// private Metrics metrics;
	/** Whether debug info should be shown in the console */
	public static boolean debug = true;
	/** The economy we hook into for transactions */
	private Economy economy;
	/** The Shop Manager used to store shops */
	private ShopManager shopManager;
	/**
	 * A set of players who have been warned
	 * ("Your shop isn't automatically locked")
	 */
	public HashSet<String> warnings = new HashSet<String>();
	/** The database for storing all our data for persistence */
	private Database database;
	// Listeners - We decide which one to use at runtime
	private ChatListener chatListener;
	// private HeroChatListener heroChatListener;
	// Listeners (These don't)
	private final BlockListener blockListener = new BlockListener(this);
	private final PlayerListener playerListener = new PlayerListener(this);
	private final ChunkListener chunkListener = new ChunkListener(this);
	private final WorldListener worldListener = new WorldListener(this);
	private BukkitTask itemWatcherTask;
	private LogWatcher logWatcher;
	/** Whether players are required to sneak to create/buy from a shop */
	public boolean sneak;
	/** Whether players are required to sneak to create a shop */
	public boolean sneakCreate;
	/** Whether players are required to sneak to trade with a shop */
	public boolean sneakTrade;
	/** Whether we should use display items or not */
	public boolean display = true;
	/**
	 * Whether we players are charged a fee to change the price on their shop
	 * (To help deter endless undercutting
	 */
	public boolean priceChangeRequiresFee = false;
	/** Whether or not to limit players shop amounts */
	public boolean limit = false;
	private final HashMap<String, Integer> limits = new HashMap<String, Integer>();
	/** Use SpoutPlugin to get item / block names */
	public boolean useSpout = false;

	/**
	 * Prints debug information if QuickShop is configured to do so.
	 *
	 * @param s
	 *            The string to print.
	 */
	public void debug(final String s) {
		if (!debug) {
			return;
		}
		this.getLogger().info(ChatColor.YELLOW + "[Debug] " + s);
	}

	/**
	 * @return Returns the database handler for queries etc.
	 */
	public Database getDB() {
		return this.database;
	}

	/**
	 * Returns the economy for moving currency around
	 *
	 * @return The economy for moving currency around
	 */
	public EconomyCore getEcon() {
		return economy;
	}

	/** The plugin metrics from Hidendra */
	// public Metrics getMetrics(){ return metrics; }
	public int getShopLimit(final Player p) {
		int max = getConfig().getInt("limits.default");
		for (final Entry<String, Integer> entry : limits.entrySet()) {
			if (entry.getValue() > max && p.hasPermission(entry.getKey())) {
				max = entry.getValue();
			}
		}
		return max;
	}

	/**
	 * Returns the ShopManager. This is used for fetching, adding and removing
	 * shops.
	 *
	 * @return The ShopManager.
	 */
	public ShopManager getShopManager() {
		return this.shopManager;
	}

	/**
	 * Tries to load the economy and its core. If this fails, it will try to use
	 * vault. If that fails, it will return false.
	 *
	 * @return true if successful, false if the core is invalid or is not found,
	 *         and vault cannot be used.
	 */
	public boolean loadEcon() {
		final EconomyCore core = new Economy_Vault();
		if (!core.isValid()) {
			// getLogger().severe("Economy is not valid!");
			getLogger().severe("QuickShop could not hook an economy!");
			getLogger().severe("QuickShop CANNOT start!");
			this.getPluginLoader().disablePlugin(this);
			// if(econ.equals("Vault"))
			// getLogger().severe("(Does Vault have an Economy to hook into?!)");
			return false;
		} else {
			this.economy = new Economy(core);
			return true;
		}
	}

	/**
	 * Logs the given string to qs.log, if QuickShop is configured to do so.
	 *
	 * @param s
	 *            The string to log. It will be prefixed with the date and time.
	 */
	public void log(final String s) {
		if (this.logWatcher == null) {
			return;
		}
		final Date date = Calendar.getInstance().getTime();
		final Timestamp time = new Timestamp(date.getTime());
		this.logWatcher.add("[" + time.toString() + "] " + s);
	}

	@Override
	public void onDisable() {
		if (itemWatcherTask != null) {
			itemWatcherTask.cancel();
		}
		if (logWatcher != null) {
			logWatcher.task.cancel();
			logWatcher.close(); // Closes the file
		}
		/* Remove all display items, and any dupes we can find */
		shopManager.clear();
		/* Empty the buffer */
		database.close();
		try {
			this.database.getConnection().close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		this.warnings.clear();
		this.reloadConfig();
	}

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig(); // Creates the config folder and copies config.yml
								// (If one doesn't exist) as required.
		reloadConfig(); // Reloads messages.yml too, aswell as config.yml and
						// others.
		getConfig().options().copyDefaults(true); // Load defaults.
		if (getConfig().contains("debug")) {
			debug = true;
		}
		if (loadEcon() == false) {
			return;
		}

		// Initialize Util
		Util.initialize();

		// Create the shop manager.
		this.shopManager = new ShopManager(this);
		if (this.display) {
			// Display item handler thread
			getLogger().info("Starting item scheduler");
			final ItemWatcher itemWatcher = new ItemWatcher(this);
			itemWatcherTask = Bukkit.getScheduler().runTaskTimer(this, itemWatcher, 600, 600);
		}
		if (this.getConfig().getBoolean("log-actions")) {
			// Logger Handler
			this.logWatcher = new LogWatcher(this, new File(this.getDataFolder(), "qs.log"));
			logWatcher.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this.logWatcher, 150, 150);
		}
		if (getConfig().getBoolean("shop.lock")) {
			final LockListener ll = new LockListener(this);
			getServer().getPluginManager().registerEvents(ll, this);
		}
		ConfigurationSection limitCfg = this.getConfig().getConfigurationSection("limits");
		if (limitCfg != null) {
			getLogger().info("Limit cfg found...");
			this.limit = limitCfg.getBoolean("use", false);
			getLogger().info("Limits.use: " + limit);
			limitCfg = limitCfg.getConfigurationSection("ranks");
			for (final String key : limitCfg.getKeys(true)) {
				limits.put(key, limitCfg.getInt(key));
			}
			getLogger().info(limits.toString());
		}
		try {
			final ConfigurationSection dbCfg = getConfig().getConfigurationSection("database");
			DatabaseCore dbCore;
			if (dbCfg.getBoolean("mysql")) {
				// MySQL database - Required database be created first.
				final String user = dbCfg.getString("user");
				final String pass = dbCfg.getString("password");
				final String host = dbCfg.getString("host");
				final String port = dbCfg.getString("port");
				final String database = dbCfg.getString("database");
				dbCore = new MySQLCore(host, user, pass, database, port);
			} else {
				// SQLite database - Doing this handles file creation
				dbCore = new SQLiteCore(new File(this.getDataFolder(), "shops.db"));
			}
			this.database = new Database(dbCore);
			// Make the database up to date
			DatabaseHelper.setup(getDB());
		} catch (final ConnectionException e) {
			e.printStackTrace();
			getLogger().severe("Error connecting to database. Aborting plugin load.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} catch (final SQLException e) {
			e.printStackTrace();
			getLogger().severe("Error setting up database. Aborting plugin load.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		/* Load shops from database to memory */
		int count = 0; // Shops count
		Connection con;
		try {
			getLogger().info("Loading shops from database...");
			final int res = Converter.convert();
			if (res < 0) {
				System.out.println("Could not convert shops. Exitting.");
				return;
			}
			if (res > 0) {
				System.out.println("Conversion success. Continuing...");
			}
			con = database.getConnection();
			final PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
			final ResultSet rs = ps.executeQuery();
			int errors = 0;
			while (rs.next()) {
				int x = 0;
				int y = 0;
				int z = 0;
				String worldName = null;
				try {
					x = rs.getInt("x");
					y = rs.getInt("y");
					z = rs.getInt("z");
					worldName = rs.getString("world");
					final World world = Bukkit.getWorld(worldName);
					final ItemStack item = Util.deserialize(rs.getString("itemConfig"));
					final String owner = rs.getString("owner");
					final double price = rs.getDouble("price");
					final Location loc = new Location(world, x, y, z);
					/* Skip invalid shops, if we know of any */
					if (world != null && (loc.getBlock().getState() instanceof InventoryHolder) == false) {
						getLogger().info("Shop is not an InventoryHolder in " + rs.getString("world") + " at: " + x + ", " + y + ", " + z + ".  Deleting.");
						final PreparedStatement delps = getDB().getConnection().prepareStatement("DELETE FROM shops WHERE x = ? AND y = ? and z = ? and world = ?");
						delps.setInt(1, x);
						delps.setInt(2, y);
						delps.setInt(3, z);
						delps.setString(4, worldName);
						delps.execute();
						continue;
					}
					final int type = rs.getInt("type");
					final Shop shop = new ContainerShop(loc, price, item, UUID.fromString(owner));
					shop.setUnlimited(rs.getBoolean("unlimited"));
					shop.setShopType(ShopType.fromID(type));
					shopManager.loadShop(rs.getString("world"), shop);
					if (loc.getWorld() != null && loc.getChunk().isLoaded()) {
						shop.onLoad();
					}
					count++;
				} catch (final Exception e) {
					errors++;
					e.printStackTrace();
					getLogger().severe("Error loading a shop! Coords: " + worldName + " (" + x + ", " + y + ", " + z + ")...");
					if (errors < 3) {
						getLogger().info("Deleting the shop...");
						final PreparedStatement delps = getDB().getConnection().prepareStatement("DELETE FROM shops WHERE x = ? AND y = ? and z = ? and world = ?");
						delps.setInt(1, x);
						delps.setInt(2, y);
						delps.setInt(3, z);
						delps.setString(4, worldName);
						delps.execute();
					} else {
						getLogger().severe("Multiple errors in shops - Something seems to be wrong with your shops database! Please check it out immediately!");
						e.printStackTrace();
					}
				}
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			getLogger().severe("Could not load shops.");
		}

		getLogger().info("Loaded " + count + " shops.");
		MsgUtil.loadTransactionMessages();
		MsgUtil.clean();
		// Register events
		getLogger().info("Registering Listeners");
		Bukkit.getServer().getPluginManager().registerEvents(blockListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(playerListener, this);
		if (this.display) {
			Bukkit.getServer().getPluginManager().registerEvents(chunkListener, this);
		}
		Bukkit.getServer().getPluginManager().registerEvents(worldListener, this);
		if (this.getConfig().getBoolean("force-bukkit-chat-handler", false) && Bukkit.getPluginManager().getPlugin("Herochat") != null) {
			this.getLogger().info("Found Herochat... Hooking!");
			// this.heroChatListener = new HeroChatListener(this);
			// Bukkit.getServer().getPluginManager().registerEvents(heroChatListener, this);
		} else {
			this.chatListener = new ChatListener(this);
			Bukkit.getServer().getPluginManager().registerEvents(chatListener, this);
		}
		// Command handlers
		final QS commandExecutor = new QS(this);
		getCommand("qs").setExecutor(commandExecutor);
		if (getConfig().getInt("shop.find-distance") > 100) {
			getLogger().severe("Shop.find-distance is too high! Pick a number under 100!");
		}
		new VersionChecker(this);
		try {
			final Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (final IOException e) {
		}
	}

	/** Reloads QuickShops config */
	@Override
	public void reloadConfig() {
		super.reloadConfig();
		// Load quick variables
		this.display = this.getConfig().getBoolean("shop.display-items");
		this.sneak = this.getConfig().getBoolean("shop.sneak-only");
		this.sneakCreate = this.getConfig().getBoolean("shop.sneak-to-create");
		this.sneakTrade = this.getConfig().getBoolean("shop.sneak-to-trade");
		this.priceChangeRequiresFee = this.getConfig().getBoolean("shop.price-change-requires-fee");
		MsgUtil.loadCfgMessages();
	}
}
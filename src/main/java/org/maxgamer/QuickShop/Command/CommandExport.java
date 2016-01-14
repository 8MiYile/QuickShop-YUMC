package org.maxgamer.QuickShop.Command;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Database.MySQLCore;
import org.maxgamer.QuickShop.Database.SQLiteCore;
import org.maxgamer.QuickShop.Util.MsgUtil;

import cn.citycraft.PluginHelper.commands.BaseCommand;

public class CommandExport extends BaseCommand {
    QuickShop plugin;

    public CommandExport(final QuickShop plugin) {
        super("export");
        this.plugin = plugin;
        setPermission("quickshop.export");
        setMinimumArguments(1);
        setPossibleArguments("[mysql|sqlite]");
        setDescription(MsgUtil.p("command.description.export"));
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        final String type = args[0].toLowerCase();
        if (type.startsWith("mysql")) {
            if (plugin.getDB().getCore() instanceof MySQLCore) {
                sender.sendMessage(ChatColor.RED + "数据已保存在 MySQL 无需转换!");
                return;
            }
            final ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("database");
            final String host = cfg.getString("host");
            final String port = cfg.getString("port");
            final String user = cfg.getString("user");
            final String pass = cfg.getString("password");
            final String name = cfg.getString("database");
            final MySQLCore core = new MySQLCore(host, user, pass, name, port);
            Database target;
            try {
                target = new Database(core);
                QuickShop.instance.getDB().copyTo(target);
                sender.sendMessage(ChatColor.GREEN + "导出成功 - 数据已保存至 MySQL " + user + "@" + host + "." + name);
            } catch (final Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "导出数据到 MySQL 失败 " + user + "@" + host + "." + name + ChatColor.DARK_RED + " 由于: " + e.getMessage());
            }
            return;
        }
        if (type.startsWith("sql") || type.contains("file")) {
            if (plugin.getDB().getCore() instanceof SQLiteCore) {
                sender.sendMessage(ChatColor.RED + "数据已保存在 SQLite 无需转换!");
                return;
            }
            final File file = new File(plugin.getDataFolder(), "shops.db");
            if (file.exists()) {
                if (file.delete() == false) {
                    sender.sendMessage(ChatColor.RED + "警告: 删除旧的数据文件 shops.db 失败. 可能会导致部分信息错误.");
                }
            }
            final SQLiteCore core = new SQLiteCore(file);
            try {
                final Database target = new Database(core);
                QuickShop.instance.getDB().copyTo(target);
                sender.sendMessage(ChatColor.GREEN + "导出成功 - 数据已保存至 SQLite: " + file.toString());
            } catch (final Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "导出数据到 SQLite: " + file.toString() + " 失败 由于: " + e.getMessage());
            }
            return;
        }
    }
}

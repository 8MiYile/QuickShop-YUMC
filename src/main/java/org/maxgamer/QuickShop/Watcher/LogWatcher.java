package org.maxgamer.QuickShop.Watcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.QuickShop.QuickShop;

public class LogWatcher implements Runnable {
    private PrintStream ps;
    private final ArrayList<String> logs = new ArrayList<String>(5);
    public BukkitTask task;

    public LogWatcher(final QuickShop plugin, final File log) {
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            final FileOutputStream fos = new FileOutputStream(log, true);
            this.ps = new PrintStream(fos, true, "UTF-8");
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            plugin.getLogger().severe("日志文件未找到!");
        } catch (final IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("无法创建日志文件!");
        }
    }

    public void add(final String s) {
        synchronized (logs) {
            logs.add(s);
        }
    }

    public void close() {
        this.ps.close();
    }

    @Override
    public void run() {
        synchronized (logs) {
            for (final String s : logs) {
                ps.println(s);
            }
            logs.clear();
        }
    }
}
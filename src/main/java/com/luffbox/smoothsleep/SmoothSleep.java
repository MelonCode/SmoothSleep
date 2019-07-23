package com.luffbox.smoothsleep;

import com.luffbox.smoothsleep.commands.*;
import com.luffbox.smoothsleep.lib.ConfigHelper;
import com.luffbox.smoothsleep.lib.LoggablePlugin;
import com.luffbox.smoothsleep.listeners.NightListeners;
import com.luffbox.smoothsleep.listeners.PlayerListeners;
import com.luffbox.smoothsleep.tasks.EveryTickTask;
import com.luffbox.smoothsleep.tasks.TransmitDataTask;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

// TODO Morning buff options?

@SuppressWarnings("ConstantConditions")
public final class SmoothSleep extends LoggablePlugin {

	public static final String PERM_IGNORE = "smoothsleep.ignore";
	public static final String PERM_NOTIFY = "smoothsleep.notify";

	public static final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23460L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	public static final long TICKS_PER_DAY = 1728000L,
			TICKS_PER_HOUR = 72000L,
			TICKS_PER_MIN = 1200L;

	public static String nmsver;
	public static boolean hasUpdate = false;

	public DataStore data;
	public static Metrics metrics;

	private BukkitTask everyTickTask;

	@Override
	public void onEnable() {
		resourceId = "32043";
		nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
		hasUpdate = checkUpdate();

		metrics = new Metrics(this);
		data = new DataStore(this); // init() after assign so data variable isn't null
		data.init();
		logDebug("DataStore initialized");

		getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
		getServer().getPluginManager().registerEvents(new NightListeners(this), this);

		getServer().getPluginCommand("smoothsleepreload").setExecutor(new Reload(this));
		getServer().getPluginCommand("smoothsleeptoggle").setExecutor(new ToggleEnabled(this));
		getServer().getPluginCommand("smoothsleepmetrics").setExecutor(new ToggleMetrics(this));
		getServer().getPluginCommand("smoothsleepaddworld").setExecutor(new AddWorld(this));
		getServer().getPluginCommand("smoothsleepconfigureworld").setExecutor(new ConfigureWorld(this));

		everyTickTask = new EveryTickTask(this).runTaskTimer(this, 0L, 0L);
		if (data.config.getBoolean(ConfigHelper.GlobalSettingKey.ENABLE_DATA)) {
			new TransmitDataTask(this).runTaskLater(this, 1L);
		}
	}

	@Override
	public void onDisable() {
		everyTickTask.cancel();
		data.purgeData();
	}
}

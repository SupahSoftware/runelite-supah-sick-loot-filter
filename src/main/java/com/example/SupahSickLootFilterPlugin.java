package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Supah Sick Loot Filter"
)
public class SupahSickLootFilterPlugin extends Plugin
{
	@Inject
	private SupahSickLootFilterConfig config;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SupahSickLootFilterOverlay overlay;

	private Plugin groundItemsPlugin;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Supah Sick Loot Filter started!");
		overlayManager.add(overlay);
		if (config.enabled())
		{
			disableGroundItems();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Supah Sick Loot Filter stopped!");
		overlayManager.remove(overlay);
		enableGroundItems();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("supahsicklootfilter"))
		{
			return;
		}

		if (config.enabled())
		{
			disableGroundItems();
		}
		else
		{
			enableGroundItems();
		}
	}

	private Plugin findGroundItemsPlugin()
	{
		if (groundItemsPlugin == null)
		{
			for (Plugin plugin : pluginManager.getPlugins())
			{
				if (plugin.getClass().getAnnotation(PluginDescriptor.class).name().equals("Ground Items"))
				{
					groundItemsPlugin = plugin;
					break;
				}
			}
		}
		return groundItemsPlugin;
	}

	private void disableGroundItems()
	{
		Plugin plugin = findGroundItemsPlugin();
		if (plugin != null && pluginManager.isPluginEnabled(plugin))
		{
			log.debug("Disabling Ground Items plugin");
			pluginManager.setPluginEnabled(plugin, false);
			try
			{
				pluginManager.stopPlugin(plugin);
			}
			catch (Exception e)
			{
				log.warn("Failed to stop Ground Items plugin", e);
			}
		}
	}

	private void enableGroundItems()
	{
		Plugin plugin = findGroundItemsPlugin();
		if (plugin != null && !pluginManager.isPluginEnabled(plugin))
		{
			log.debug("Re-enabling Ground Items plugin");
			pluginManager.setPluginEnabled(plugin, true);
			try
			{
				pluginManager.startPlugin(plugin);
			}
			catch (Exception e)
			{
				log.warn("Failed to start Ground Items plugin", e);
			}
		}
	}

	@Provides
	SupahSickLootFilterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SupahSickLootFilterConfig.class);
	}
}

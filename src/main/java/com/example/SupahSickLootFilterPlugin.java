package com.example;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
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
	private ConfigManager configManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SupahSickLootFilterOverlay overlay;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Gson gson;

	private NavigationButton navButton;
	private SupahSickLootFilterPanel panel;
	private Plugin groundItemsPlugin;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Supah Sick Loot Filter started!");

		panel = new SupahSickLootFilterPanel(configManager, gson);

		final BufferedImage icon = createIcon();
		navButton = NavigationButton.builder()
			.tooltip("Supah Sick Loot Filter")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
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
		clientToolbar.removeNavigation(navButton);
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

		if (event.getKey().equals("enabled"))
		{
			if (config.enabled())
			{
				disableGroundItems();
			}
			else
			{
				enableGroundItems();
			}
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

	private BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setColor(new Color(255, 215, 0));
		g.fillRect(2, 4, 12, 10);
		g.setColor(new Color(200, 160, 0));
		g.drawRect(2, 4, 12, 10);
		g.setColor(Color.WHITE);
		g.fillRect(5, 1, 6, 4);
		g.dispose();
		return image;
	}

	@Provides
	SupahSickLootFilterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SupahSickLootFilterConfig.class);
	}
}

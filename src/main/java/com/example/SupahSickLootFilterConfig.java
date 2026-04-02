package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("supahsicklootfilter")
public interface SupahSickLootFilterConfig extends Config
{
	@ConfigItem(
		keyName = "enabled",
		name = "Enable Filter",
		description = "Enable the Supah Sick Loot Filter"
	)
	default boolean enabled()
	{
		return true;
	}
}

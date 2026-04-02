package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SupahSickLootFilterConfig.CONFIG_GROUP)
public interface SupahSickLootFilterConfig extends Config
{
	String CONFIG_GROUP = "supahsicklootfilter";
	String RULES_KEY = "filterRules";

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

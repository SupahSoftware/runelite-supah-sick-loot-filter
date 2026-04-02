package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class FilterRuleService
{
	private static final Type RULES_TYPE = new TypeToken<List<ItemFilterRule>>(){}.getType();

	private final ConfigManager configManager;
	private final Gson gson;

	private List<ItemFilterRule> cachedRules = new ArrayList<>();
	private String lastRulesJson = "";

	@Inject
	public FilterRuleService(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	public List<ItemFilterRule> getRules()
	{
		String json = configManager.getConfiguration(SupahSickLootFilterConfig.CONFIG_GROUP, SupahSickLootFilterConfig.RULES_KEY);
		if (json == null)
		{
			json = "";
		}

		if (!json.equals(lastRulesJson))
		{
			lastRulesJson = json;
			if (!json.isEmpty())
			{
				json = json.replace("\"HIGHLIGHT\"", "\"TINT\"");
				List<ItemFilterRule> loaded = gson.fromJson(json, RULES_TYPE);
				cachedRules = loaded != null ? loaded : new ArrayList<>();
			}
			else
			{
				cachedRules = new ArrayList<>();
			}
		}

		return cachedRules;
	}

	public ItemFilterRule findMatchingRule(String itemName, int quantity)
	{
		for (ItemFilterRule rule : getRules())
		{
			if (rule.getItemName().isEmpty())
			{
				continue;
			}

			if (!itemName.toLowerCase().contains(rule.getItemName().toLowerCase()))
			{
				continue;
			}

			boolean quantityMatch;
			if (rule.getComparator() == ItemFilterRule.Comparator.GREATER_THAN)
			{
				quantityMatch = quantity >= rule.getQuantity();
			}
			else
			{
				quantityMatch = quantity <= rule.getQuantity();
			}

			if (quantityMatch)
			{
				return rule;
			}
		}
		return null;
	}
}

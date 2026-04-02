package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class SupahSickLootFilterOverlay extends Overlay
{
	private static final Type RULES_TYPE = new TypeToken<List<ItemFilterRule>>(){}.getType();
	private static final int SCENE_SIZE = 104;

	private final Client client;
	private final SupahSickLootFilterConfig config;
	private final ConfigManager configManager;
	private final ItemManager itemManager;
	private final Gson gson;

	private List<ItemFilterRule> cachedRules = new ArrayList<>();
	private String lastRulesJson = "";

	@Inject
	public SupahSickLootFilterOverlay(Client client, SupahSickLootFilterConfig config,
		ConfigManager configManager, ItemManager itemManager, Gson gson)
	{
		this.client = client;
		this.config = config;
		this.configManager = configManager;
		this.itemManager = itemManager;
		this.gson = gson;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enabled())
		{
			return null;
		}

		List<ItemFilterRule> rules = loadRules();

		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();
		int plane = client.getPlane();

		for (int x = 0; x < SCENE_SIZE; x++)
		{
			for (int y = 0; y < SCENE_SIZE; y++)
			{
				Tile tile = tiles[plane][x][y];
				if (tile == null)
				{
					continue;
				}

				List<TileItem> items = tile.getGroundItems();
				if (items == null || items.isEmpty())
				{
					continue;
				}

				int offset = 0;
				for (TileItem item : items)
				{
					String name = itemManager.getItemComposition(item.getId()).getName();
					if (name == null || name.equals("null"))
					{
						continue;
					}

					int quantity = item.getQuantity();
					ItemFilterRule matchedRule = findMatchingRule(rules, name, quantity);

					if (matchedRule != null && matchedRule.getAction() == ItemFilterRule.Action.HIDE)
					{
						continue;
					}

					Color color = Color.WHITE;
					if (matchedRule != null && matchedRule.getAction() == ItemFilterRule.Action.TINT)
					{
						color = matchedRule.getColor();
					}

					LocalPoint localPoint = tile.getLocalLocation();
					int height = tile.getItemLayer() != null ? tile.getItemLayer().getHeight() : 0;
					Point textPoint = Perspective.getCanvasTextLocation(client, graphics, localPoint, name, height + 20 + offset);
					if (textPoint == null)
					{
						continue;
					}

					String displayText = quantity > 1 ? name + " (" + quantity + ")" : name;
					renderText(graphics, textPoint, displayText, color);
					offset += 15;
				}
			}
		}

		return null;
	}

	private ItemFilterRule findMatchingRule(List<ItemFilterRule> rules, String itemName, int quantity)
	{
		for (ItemFilterRule rule : rules)
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

	private List<ItemFilterRule> loadRules()
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

	private void renderText(Graphics2D graphics, Point point, String text, Color color)
	{
		int x = point.getX();
		int y = point.getY();

		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);

		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}
}

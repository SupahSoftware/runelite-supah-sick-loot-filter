package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class SupahSickLootFilterOverlay extends Overlay
{
	private final Client client;
	private final SupahSickLootFilterConfig config;
	private final ItemManager itemManager;

	@Inject
	public SupahSickLootFilterOverlay(Client client, SupahSickLootFilterConfig config, ItemManager itemManager)
	{
		this.client = client;
		this.config = config;
		this.itemManager = itemManager;
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

		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();
		int plane = client.getPlane();

		for (int x = 0; x < Constants.SCENE_SIZE; x++)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; y++)
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

					LocalPoint localPoint = tile.getLocalLocation();
					int height = tile.getItemLayer() != null ? tile.getItemLayer().getHeight() : 0;
					Point textPoint = Perspective.getCanvasTextLocation(client, graphics, localPoint, name, height + 20 + offset);
					if (textPoint == null)
					{
						continue;
					}

					renderText(graphics, textPoint, name, Color.WHITE);
					offset += 15;
				}
			}
		}

		return null;
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

	private static class Constants
	{
		static final int SCENE_SIZE = 104;
	}
}

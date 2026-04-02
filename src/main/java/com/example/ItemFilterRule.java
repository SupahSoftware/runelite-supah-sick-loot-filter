package com.example;

import java.awt.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemFilterRule
{
	private String itemName = "";
	private Comparator comparator = Comparator.GREATER_THAN;
	private int quantity = 1;
	private Action action = Action.TINT;
	private int colorR = 255;
	private int colorG = 255;
	private int colorB = 0;

	public Color getColor()
	{
		return new Color(colorR, colorG, colorB);
	}

	public void setColor(Color color)
	{
		this.colorR = color.getRed();
		this.colorG = color.getGreen();
		this.colorB = color.getBlue();
	}

	public enum Comparator
	{
		GREATER_THAN(">="),
		LESS_THAN("<=");

		private final String symbol;

		Comparator(String symbol)
		{
			this.symbol = symbol;
		}

		@Override
		public String toString()
		{
			return symbol;
		}
	}

	public enum Action
	{
		HIDE,
		TINT
	}
}

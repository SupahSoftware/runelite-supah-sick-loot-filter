package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class SupahSickLootFilterPanel extends PluginPanel
{
	private static final Type RULES_TYPE = new TypeToken<List<ItemFilterRule>>(){}.getType();
	private static final Color INPUT_BG = new Color(60, 60, 60);

	private final ConfigManager configManager;
	private final Gson gson;
	private final List<ItemFilterRule> rules = new ArrayList<>();
	private final JPanel ruleListPanel;
	private JScrollPane scrollPane;

	public SupahSickLootFilterPanel(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 5, 10, 5));

		// Header with add button
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel titleLabel = new JLabel("Loot Filter Rules");
		titleLabel.setForeground(Color.WHITE);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		JButton addButton = new JButton("+");
		addButton.setToolTipText("Add new filter rule");
		addButton.addActionListener(e -> addRule());
		headerPanel.add(addButton, BorderLayout.EAST);

		add(headerPanel, BorderLayout.NORTH);

		// Scrollable rule list
		ruleListPanel = new JPanel();
		ruleListPanel.setLayout(new BoxLayout(ruleListPanel, BoxLayout.Y_AXIS));
		ruleListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		scrollPane = new JScrollPane(ruleListPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		add(scrollPane, BorderLayout.CENTER);

		loadRules();
		rebuildPanel();
	}

	private void addRule()
	{
		rules.add(new ItemFilterRule());
		saveRules();
		rebuildPanel();
	}

	private void removeRule(int index)
	{
		if (index >= 0 && index < rules.size())
		{
			rules.remove(index);
			saveRules();
			rebuildPanel();
		}
	}

	private void rebuildPanel()
	{
		ruleListPanel.removeAll();

		for (int i = 0; i < rules.size(); i++)
		{
			ruleListPanel.add(Box.createRigidArea(new Dimension(0, 3)));
			ruleListPanel.add(buildRuleRow(i));
		}

		ruleListPanel.add(Box.createVerticalGlue());
		revalidate();
		repaint();
	}

	private JPanel buildRuleRow(int index)
	{
		ItemFilterRule rule = rules.get(index);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		JPanel rowPanel = new JPanel();
		rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
		rowPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rowPanel.setBorder(new EmptyBorder(3, 4, 3, 4));

		// Item name
		JTextField nameField = new HintTextField("Item name");
		nameField.setText(rule.getItemName());
		styleInput(nameField);
		nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		nameField.addActionListener(e ->
		{
			rule.setItemName(nameField.getText());
			saveRules();
		});
		nameField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				rule.setItemName(nameField.getText());
				saveRules();
			}
		});
		rowPanel.add(nameField);
		rowPanel.add(Box.createRigidArea(new Dimension(2, 0)));

		// Comparator toggle button
		JButton comparatorBtn = createToggleButton(rule.getComparator().toString());
		comparatorBtn.setPreferredSize(new Dimension(24, 22));
		comparatorBtn.setMaximumSize(new Dimension(24, 22));
		comparatorBtn.setMinimumSize(new Dimension(24, 22));
		comparatorBtn.addActionListener(e ->
		{
			if (rule.getComparator() == ItemFilterRule.Comparator.GREATER_THAN)
			{
				rule.setComparator(ItemFilterRule.Comparator.LESS_THAN);
			}
			else
			{
				rule.setComparator(ItemFilterRule.Comparator.GREATER_THAN);
			}
			comparatorBtn.setText(rule.getComparator().toString());
			saveRules();
		});
		rowPanel.add(comparatorBtn);
		rowPanel.add(Box.createRigidArea(new Dimension(2, 0)));

		// Quantity text field
		JTextField qtyField = new HintTextField("1");
		qtyField.setText(rule.getQuantity() > 0 ? String.valueOf(rule.getQuantity()) : "");
		styleInput(qtyField);
		qtyField.setPreferredSize(new Dimension(32, 22));
		qtyField.setMaximumSize(new Dimension(32, 22));
		qtyField.setMinimumSize(new Dimension(32, 22));
		FocusAdapter qtyListener = new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				parseQuantity(rule, qtyField);
			}
		};
		qtyField.addFocusListener(qtyListener);
		qtyField.addActionListener(e -> parseQuantity(rule, qtyField));
		rowPanel.add(qtyField);
		rowPanel.add(Box.createRigidArea(new Dimension(2, 0)));

		// Action toggle button
		JButton actionBtn = createToggleButton(rule.getAction().name());
		actionBtn.setPreferredSize(new Dimension(38, 22));
		actionBtn.setMaximumSize(new Dimension(38, 22));
		actionBtn.setMinimumSize(new Dimension(38, 22));

		// Color swatch
		JButton colorButton = new JButton();
		colorButton.setBackground(rule.getColor());
		colorButton.setPreferredSize(new Dimension(18, 18));
		colorButton.setMaximumSize(new Dimension(18, 18));
		colorButton.setMinimumSize(new Dimension(18, 18));
		colorButton.setBorder(BorderFactory.createEmptyBorder());
		colorButton.setFocusPainted(false);
		colorButton.setVisible(rule.getAction() == ItemFilterRule.Action.TINT);
		colorButton.addActionListener(e ->
		{
			Color chosen = JColorChooser.showDialog(rowPanel, "Tint Color", rule.getColor());
			if (chosen != null)
			{
				rule.setColor(chosen);
				colorButton.setBackground(chosen);
				saveRules();
			}
		});

		actionBtn.addActionListener(e ->
		{
			if (rule.getAction() == ItemFilterRule.Action.HIDE)
			{
				rule.setAction(ItemFilterRule.Action.TINT);
			}
			else
			{
				rule.setAction(ItemFilterRule.Action.HIDE);
			}
			actionBtn.setText(rule.getAction().name());
			colorButton.setVisible(rule.getAction() == ItemFilterRule.Action.TINT);
			saveRules();
		});
		rowPanel.add(actionBtn);
		rowPanel.add(Box.createRigidArea(new Dimension(2, 0)));

		rowPanel.add(colorButton);
		rowPanel.add(Box.createRigidArea(new Dimension(2, 0)));

		// Delete button
		JButton deleteButton = new JButton("X");
		deleteButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
		deleteButton.setForeground(new Color(200, 80, 80));
		deleteButton.setContentAreaFilled(false);
		deleteButton.setBorder(BorderFactory.createEmptyBorder());
		deleteButton.setFocusPainted(false);
		deleteButton.setPreferredSize(new Dimension(14, 18));
		deleteButton.setMaximumSize(new Dimension(14, 18));
		deleteButton.addActionListener(e -> removeRule(index));
		rowPanel.add(deleteButton);

		wrapper.add(rowPanel, BorderLayout.CENTER);
		return wrapper;
	}

	private void parseQuantity(ItemFilterRule rule, JTextField field)
	{
		try
		{
			rule.setQuantity(Integer.parseInt(field.getText()));
		}
		catch (NumberFormatException ex)
		{
			rule.setQuantity(1);
			field.setText("1");
		}
		saveRules();
	}

	private JButton createToggleButton(String text)
	{
		JButton btn = new JButton(text);
		btn.setBackground(INPUT_BG);
		btn.setForeground(Color.WHITE);
		btn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		btn.setFocusPainted(false);
		btn.setFont(btn.getFont().deriveFont(16f));
		return btn;
	}

	private void styleInput(JTextField field)
	{
		field.setBackground(INPUT_BG);
		field.setForeground(Color.WHITE);
		field.setCaretColor(Color.WHITE);
		field.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
	}

	private void loadRules()
	{
		String json = configManager.getConfiguration(SupahSickLootFilterConfig.CONFIG_GROUP, SupahSickLootFilterConfig.RULES_KEY);
		if (json != null && !json.isEmpty())
		{
			json = json.replace("\"HIGHLIGHT\"", "\"TINT\"");
			List<ItemFilterRule> loaded = gson.fromJson(json, RULES_TYPE);
			if (loaded != null)
			{
				rules.addAll(loaded);
			}
		}
	}

	private void saveRules()
	{
		String json = gson.toJson(rules, RULES_TYPE);
		configManager.setConfiguration(SupahSickLootFilterConfig.CONFIG_GROUP, SupahSickLootFilterConfig.RULES_KEY, json);
	}

	private static class HintTextField extends JTextField
	{
		private final String hint;

		HintTextField(String hint)
		{
			this.hint = hint;
		}

		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (getText().isEmpty() && !hasFocus())
			{
				g.setColor(new Color(130, 130, 130));
				g.drawString(hint, getInsets().left + 2, g.getFontMetrics().getAscent() + getInsets().top);
			}
		}
	}
}

package com.narohid.quest.utils;

import com.narohid.quest.database.SQLiteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InventoryUtils {

    public static Inventory createQuestGui(List<FileConfiguration> missionConfigs, Player player, SQLiteManager sqliteManager) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Quests");

        try (Connection connection = sqliteManager.getConnection()) {
            for (int i = 0; i < missionConfigs.size() && i < 27; i++) {
                FileConfiguration config = missionConfigs.get(i);
                String iconName = config.getString("icon", "DIAMOND_SWORD");
                Material iconMaterial;

                try {
                    iconMaterial = Material.valueOf(iconName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    iconMaterial = Material.DIAMOND_SWORD; // Default icon if invalid
                }

                ItemStack item = new ItemStack(iconMaterial);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("name", "Quest")));
                    List<String> lore = config.getStringList("lore");
                    for (int j = 0; j < lore.size(); j++) {
                        lore.set(j, ChatColor.translateAlternateColorCodes('&', lore.get(j)));
                    }

                    boolean repeatable = config.getBoolean("repeatable", false);
                    String missionName = config.getString("name");

                    PreparedStatement statement = connection.prepareStatement("SELECT completed FROM player_missions WHERE player_uuid = ? AND mission_name = ?");
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setString(2, missionName);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        boolean completed = resultSet.getBoolean("completed");
                        if (completed && !repeatable) {
                            lore.add("");
                            lore.add(ChatColor.RED + "Mission already completed");
                            meta.setLore(lore);
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            item.setItemMeta(meta);
                            gui.setItem(i, item);
                            continue;
                        }
                    }

                    lore.add("");
                    lore.add(ChatColor.YELLOW + "[Left-Click] to accept");
                    lore.add(ChatColor.YELLOW + "[Right-Click] to cancel");
                    meta.setLore(lore);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    item.setItemMeta(meta);
                }

                gui.setItem(i, item);
            }

            // Añadir decoraciones (ej. paneles de cristal tintado)
            ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);

            for (int i = 0; i < gui.getSize(); i++) {
                if (gui.getItem(i) == null) {
                    gui.setItem(i, filler);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return gui;
    }

    public static Inventory createConfirmationGui() {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Confirm Quest");

        ItemStack yesItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta yesMeta = yesItem.getItemMeta();
        yesMeta.setDisplayName(ChatColor.GREEN + "Yes");
        yesItem.setItemMeta(yesMeta);

        ItemStack noItem = new ItemStack(Material.RED_WOOL);
        ItemMeta noMeta = noItem.getItemMeta();
        noMeta.setDisplayName(ChatColor.RED + "No");
        noItem.setItemMeta(noMeta);

        gui.setItem(11, yesItem);
        gui.setItem(15, noItem);

        // Añadir decoraciones (ej. paneles de cristal tintado)
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        return gui;
    }
}

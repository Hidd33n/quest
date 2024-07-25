package com.narohid.quest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;

public class QuestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Inventory questGui = Bukkit.createInventory(null, 27, "Misiones");

            // Leer la configuración del archivo default.yml
            FileConfiguration config = getDefaultMissionConfig();
            if (config != null) {
                String materialName = config.getString("icon.material", "STONE");
                String itemName = config.getString("icon.name", "Misión");
                List<String> itemLore = config.getStringList("icon.lore");

                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material == null) {
                    material = Material.STONE; // Default to STONE if the material is not found
                }

                ItemStack questItem = new ItemStack(material);
                ItemMeta meta = questItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(itemName);
                    meta.setLore(itemLore);
                    questItem.setItemMeta(meta);
                }

                questGui.setItem(0, questItem);
            }

            player.openInventory(questGui);
            return true;
        }
        sender.sendMessage("¡Este comando solo puede ser usado por jugadores!");
        return false;
    }

    private FileConfiguration getDefaultMissionConfig() {
        File defaultMissionFile = new File(Bukkit.getPluginManager().getPlugin("Quest").getDataFolder(), "missions/default.yml");
        if (!defaultMissionFile.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(defaultMissionFile);
    }
}

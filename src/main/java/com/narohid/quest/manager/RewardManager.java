package com.narohid.quest.manager;

import com.narohid.quest.lang.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardManager {

    private final LanguageManager languageManager;

    public RewardManager(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    public void giveReward(Player player, String missionDisplayName) {
        File missionsFolder = new File(Bukkit.getPluginManager().getPlugin("Quest").getDataFolder(), "missions");
        File[] missionFiles = missionsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (missionFiles == null) {
            return;
        }

        File missionFile = null;
        for (File file : missionFiles) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (missionDisplayName.equals(config.getString("name"))) {
                missionFile = file;
                break;
            }
        }

        if (missionFile == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("mission_name", missionDisplayName);
            player.sendMessage(languageManager.getMessage("no_config", placeholders));
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(missionFile);

        // Manejar recompensas de ítems
        if (config.contains("reward.items")) {
            List<Map<?, ?>> rewardItems = (List<Map<?, ?>>) config.getMapList("reward.items");
            if (rewardItems != null && !rewardItems.isEmpty()) {
                for (Map<?, ?> rewardItem : rewardItems) {
                    String materialName = (String) rewardItem.get("item");
                    int amount = (int) rewardItem.get("amount");

                    Material material = Material.getMaterial(materialName.toUpperCase());
                    if (material == null) {
                        material = Material.STONE; // Default to STONE if the material is not found
                    }

                    ItemStack reward = new ItemStack(material, amount);
                    player.getInventory().addItem(reward);
                }
            }
        }

        // Manejar recompensa de experiencia
        if (config.contains("reward.experience")) {
            int experience = config.getInt("reward.experience", 0);
            if (experience > 0) {
                player.giveExp(experience);
            }
        }

        player.sendMessage(languageManager.getMessage("reward", new HashMap<>()));
    }
}

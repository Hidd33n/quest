package com.narohid.quest.utils;

import com.narohid.quest.rewards.Reward;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class RewardLoader {

    public static Reward loadReward(FileConfiguration config) {
        String itemName = config.getString("reward.item", "DIAMOND");
        int itemAmount = config.getInt("reward.amount", 1);
        int experience = config.getInt("reward.experience", 0);

        Material material = Material.getMaterial(itemName.toUpperCase());
        if (material == null) {
            material = Material.DIAMOND; // Default to DIAMOND if the material is not found
        }

        ItemStack itemReward = new ItemStack(material, itemAmount);

        return new Reward(itemReward, experience);
    }
}

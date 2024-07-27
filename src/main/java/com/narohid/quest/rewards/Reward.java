package com.narohid.quest.rewards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Reward {
    private ItemStack itemReward;
    private int experienceReward;

    public Reward(ItemStack itemReward, int experienceReward) {
        this.itemReward = itemReward;
        this.experienceReward = experienceReward;
    }

    public void giveReward(Player player) {
        if (itemReward != null) {
            player.getInventory().addItem(itemReward);
        }
        if (experienceReward > 0) {
            player.giveExp(experienceReward);
        }
        player.sendMessage("You have received your reward!");
    }
}

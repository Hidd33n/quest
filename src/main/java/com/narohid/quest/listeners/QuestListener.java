package com.narohid.quest.listeners;

import com.narohid.quest.manager.QuestManager;
import com.narohid.quest.utils.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class QuestListener implements Listener {

    private final QuestManager questManager;
    private final Map<Player, FileConfiguration> confirmationMap = new HashMap<>();

    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        ItemStack currentItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (title.equals(ChatColor.BLUE + "Quests")) {
            event.setCancelled(true);

            if (currentItem == null || currentItem.getType() == Material.AIR) {
                return;
            }

            int slot = event.getSlot();
            if (slot >= 0 && slot < questManager.getMissionConfigs().size()) {
                FileConfiguration config = questManager.getMissionConfigs().get(slot);

                if (event.isRightClick()) {
                    questManager.cancelMission(player, config.getString("name"));
                    player.closeInventory();
                } else if (event.isLeftClick()) {
                    boolean repeatable = config.getBoolean("repeatable", true);
                    if (!repeatable && questManager.isMissionCompleted(player, config.getString("name"))) {
                        player.sendMessage(questManager.getLanguageManager().getMessage("mission_already_completed", new HashMap<>()));
                    } else if (questManager.isMissionActive(player, config.getString("name"))) {
                        player.sendMessage(questManager.getLanguageManager().getMessage("mission_already_active", new HashMap<>()));
                    } else {
                        confirmationMap.put(player, config);
                        player.openInventory(InventoryUtils.createConfirmationGui());
                    }
                }
            }
        } else if (title.equals(ChatColor.BLUE + "Confirm Quest")) {
            event.setCancelled(true);

            if (currentItem == null || currentItem.getType() == Material.AIR) {
                return;
            }

            FileConfiguration config = confirmationMap.get(player);

            if (event.getSlot() == 11) { // "Yes"
                questManager.activateMission(player, config);
                player.closeInventory();
                confirmationMap.remove(player);
            } else if (event.getSlot() == 15) { // "No"
                player.closeInventory();
                confirmationMap.remove(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.BLUE + "Confirm Quest")) {
            Player player = (Player) event.getPlayer();
            confirmationMap.remove(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.getOpenInventory().getTitle().equals(ChatColor.BLUE + "Quests")) {
                return;
            }
        }
    }
}

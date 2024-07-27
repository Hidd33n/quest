package com.narohid.quest.commands;

import com.narohid.quest.lang.LanguageManager;
import com.narohid.quest.manager.QuestManager;
import com.narohid.quest.utils.InventoryUtils;
import com.narohid.quest.utils.MissionLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class QuestCommand implements CommandExecutor {

    private List<FileConfiguration> missionConfigs;
    private final LanguageManager languageManager;
    private final QuestManager questManager;

    public QuestCommand(LanguageManager languageManager, QuestManager questManager) {
        this.languageManager = languageManager;
        this.questManager = questManager;
        missionConfigs = MissionLoader.loadMissions();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                showHelp(player);
                return true;
            }

            if (args.length == 0) {
                player.openInventory(InventoryUtils.createQuestGui(missionConfigs, player, questManager.getSQLiteManager()));
                return true;
            }

            player.sendMessage(languageManager.getMessage("invalid_command", new HashMap<>()));
            return true;
        }
        sender.sendMessage("This command can only be used by players!");
        return false;
    }

    private void showHelp(Player player) {
        player.sendMessage(languageManager.getMessage("help_header", new HashMap<>()));
        player.sendMessage(languageManager.getMessage("help_quest", new HashMap<>()));
        player.sendMessage(languageManager.getMessage("help_quest_help", new HashMap<>()));

        if (player.hasPermission("quest.admin")) {
            player.sendMessage(languageManager.getMessage("help_questadmin_update", new HashMap<>()));
            player.sendMessage(languageManager.getMessage("help_questadmin_reload", new HashMap<>()));
        }
    }

    public void reloadMissions() {
        missionConfigs = MissionLoader.loadMissions();
    }
}

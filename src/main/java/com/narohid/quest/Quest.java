package com.narohid.quest;

import com.narohid.quest.commands.QuestAdminCommand;
import com.narohid.quest.commands.QuestCommand;
import com.narohid.quest.database.SQLiteManager;
import com.narohid.quest.hooks.PlaceholderHook;
import com.narohid.quest.lang.LanguageManager;
import com.narohid.quest.listeners.QuestListener;
import com.narohid.quest.listeners.QuestEventListener;
import com.narohid.quest.listeners.QuestProgressListener;
import com.narohid.quest.manager.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class Quest extends JavaPlugin {

    private LanguageManager languageManager;
    private SQLiteManager sqliteManager;
    private QuestManager questManager;

    @Override
    public void onEnable() {
        getLogger().info("Quest has been enabled!");

        // Create the "quest" and "missions" folders if they don't exist
        createPluginFolders();

        // Initialize LanguageManager
        languageManager = new LanguageManager(getDataFolder());

        // Initialize SQLiteManager
        sqliteManager = new SQLiteManager(languageManager.getConfig(), this);

        // Initialize QuestManager
        questManager = new QuestManager(languageManager, sqliteManager);

        // Register commands and listeners
        QuestCommand questCommand = new QuestCommand(languageManager, questManager);
        getCommand("quest").setExecutor(questCommand);
        getCommand("questadmin").setExecutor(new QuestAdminCommand(questCommand, languageManager));

        Bukkit.getPluginManager().registerEvents(new QuestListener(questManager), this);
        Bukkit.getPluginManager().registerEvents(new QuestEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new QuestProgressListener(languageManager, sqliteManager), this);

        // Create the default.yml file if it doesn't exist
        createDefaultMissionFile();

        // Register PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            List<String> authors = getDescription().getAuthors();
            String author = authors.isEmpty() ? "Unknown" : authors.get(0);
            new PlaceholderHook("quest", author, getDescription().getVersion(), sqliteManager).register();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Quest has been disabled!");
        if (sqliteManager != null) {
            sqliteManager.closeConnection();
        }
    }

    private void createPluginFolders() {
        File pluginFolder = getDataFolder();
        File missionsFolder = new File(pluginFolder, "missions");
        File playersFolder = new File(pluginFolder, "players");

        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }

        if (!missionsFolder.exists()) {
            missionsFolder.mkdir();
        }

        if (!playersFolder.exists()) {
            playersFolder.mkdir();
        }
    }

    private void createDefaultMissionFile() {
        File missionsFolder = new File(getDataFolder(), "missions");
        File defaultMissionFile = new File(missionsFolder, "default.yml");

        if (!defaultMissionFile.exists()) {
            try (InputStream in = getResource("default.yml")) {
                Files.copy(in, defaultMissionFile.toPath());
            } catch (IOException e) {
                getLogger().severe("Error creating default.yml: " + e.getMessage());
            }
        }
    }
}

package com.narohid.quest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Quest extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("¡Quest ha sido habilitado!");
        getCommand("quest").setExecutor(new QuestCommand());
        getCommand("questadmin").setExecutor(new QuestAdminCommand());
        Bukkit.getPluginManager().registerEvents(new QuestEventListener(), this);

        // Crear la carpeta "quest" y "missions" si no existen
        createPluginFolders();

        // Crear el archivo default.yml si no existe
        createDefaultMissionFile();
    }

    @Override
    public void onDisable() {
        getLogger().info("¡Quest ha sido deshabilitado!");
    }

    private void createPluginFolders() {
        File pluginFolder = getDataFolder();
        File missionsFolder = new File(pluginFolder, "missions");

        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }

        if (!missionsFolder.exists()) {
            missionsFolder.mkdir();
        }
    }

    private void createDefaultMissionFile() {
        File missionsFolder = new File(getDataFolder(), "missions");
        File defaultMissionFile = new File(missionsFolder, "default.yml");

        if (!defaultMissionFile.exists()) {
            try (InputStream in = getResource("default.yml")) {
                Files.copy(in, defaultMissionFile.toPath());
            } catch (IOException e) {
                getLogger().severe("Error al crear el archivo default.yml: " + e.getMessage());
            }
        }
    }
}

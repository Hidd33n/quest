package com.narohid.quest.lang;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private FileConfiguration config;
    private FileConfiguration languageConfig;
    private final Map<String, String> messages = new HashMap<>();
    private String prefix;
    private final File dataFolder;

    public LanguageManager(File dataFolder) {
        this.dataFolder = dataFolder;
        createConfig(dataFolder);
        loadConfig(dataFolder);
        loadLanguage(dataFolder);
    }

    private void createConfig(File dataFolder) {
        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);
                config.set("language", "en");
                config.set("database.file", "quests.db");  // Nombre del archivo de la base de datos SQLite
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    private void loadConfig(File dataFolder) {
        File configFile = new File(dataFolder, "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void loadLanguage(File dataFolder) {
        String language = config.getString("language", "en");
        File langFolder = new File(dataFolder, "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        createDefaultLanguageFile(langFolder, "en.yml");
        createDefaultLanguageFile(langFolder, "es.yml");

        File languageFile = new File(langFolder, language + ".yml");
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        prefix = languageConfig.getString("prefix", "&9[Quest]");
        for (String key : languageConfig.getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, languageConfig.getString("messages." + key));
        }
    }

    public void reloadLanguages() {
        loadConfig(dataFolder);
        loadLanguage(dataFolder);
    }

    private void createDefaultLanguageFile(File langFolder, String fileName) {
        File langFile = new File(langFolder, fileName);
        if (!langFile.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/" + fileName)) {
                if (in != null) {
                    Files.copy(in, langFile.toPath());
                } else {
                    langFile.createNewFile();
                    FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(langFile);
                    defaultConfig.set("prefix", "&9[Quest]");

                    if (fileName.equals("en.yml")) {
                        defaultConfig.set("messages.progress", "&7Mission progress: %current%/%total% %target% killed.");
                        defaultConfig.set("messages.complete", "&7You have completed the mission to kill %total% &9%target%&7!");
                        defaultConfig.set("messages.reward", "&7You have received your reward for completing the mission!");
                        defaultConfig.set("messages.no_mission", "&7You have no active mission.");
                        defaultConfig.set("messages.accept_mission", "&7You have accepted the mission: &9%mission_name%&7");
                        defaultConfig.set("messages.cancel_mission", "&7You have canceled your current mission.");
                        defaultConfig.set("messages.no_config", "&7The mission &9%mission_name%&7 does not have a valid configuration file.");
                        defaultConfig.set("messages.help_header", "&eQuest Plugin v%version%");
                        defaultConfig.set("messages.help_quest", "&a/quest &f- Open the quest GUI.");
                        defaultConfig.set("messages.help_quest_help", "&a/quest help &f- Show this help message.");
                        defaultConfig.set("messages.help_questadmin_update", "&a/questadmin update &f- Update the plugin to the latest version.");
                        defaultConfig.set("messages.help_questadmin_reload", "&a/questadmin reload &f- Reload the missions without restarting the server.");
                        defaultConfig.set("messages.invalid_command", "&cUsage: /quest or /quest help");
                        defaultConfig.set("messages.reload_complete", "&aReload complete!");
                        defaultConfig.set("messages.no_permission", "&cYou do not have permission to use this command.");
                        defaultConfig.set("messages.update_error", "&cError downloading the update: %error%");
                        defaultConfig.set("messages.update_complete", "&aThe Quest plugin has been updated to version %version%.");
                        defaultConfig.set("messages.up_to_date", "&aThe plugin is already up to date.");
                        defaultConfig.set("messages.delete_error", "&cError deleting the old plugin file.");
                        defaultConfig.set("messages.players_only", "&cThis command can only be used by players!");
                        defaultConfig.set("messages.active_mission_exists", "&cYou already have an active mission. Complete or cancel it before starting a new one.");
                        defaultConfig.set("messages.mission_already_completed", "&7You have already completed this mission and it cannot be repeated.");
                        defaultConfig.set("messages.quest_gui_title", "&9Quest");
                    } else if (fileName.equals("es.yml")) {
                        defaultConfig.set("messages.progress", "&7Progreso de la misión: %current%/%total% %target% asesinados.");
                        defaultConfig.set("messages.complete", "&7¡Has completado la misión de matar %total% &9%target%&7!");
                        defaultConfig.set("messages.reward", "&7¡Has recibido tu recompensa por completar la misión!");
                        defaultConfig.set("messages.no_mission", "&7No tienes ninguna misión activa.");
                        defaultConfig.set("messages.accept_mission", "&7Has aceptado la misión: &9%mission_name%&7");
                        defaultConfig.set("messages.cancel_mission", "&7Has cancelado tu misión actual.");
                        defaultConfig.set("messages.no_config", "&7La misión &9%mission_name%&7 no tiene un archivo de configuración válido.");
                        defaultConfig.set("messages.help_header", "&eQuest Plugin v%version%");
                        defaultConfig.set("messages.help_quest", "&a/quest &f- Abrir el GUI de misiones.");
                        defaultConfig.set("messages.help_quest_help", "&a/quest help &f- Mostrar este mensaje de ayuda.");
                        defaultConfig.set("messages.help_questadmin_update", "&a/questadmin update &f- Actualizar el plugin a la última versión.");
                        defaultConfig.set("messages.help_questadmin_reload", "&a/questadmin reload &f- Recargar las misiones sin reiniciar el servidor.");
                        defaultConfig.set("messages.invalid_command", "&cUso: /quest o /quest help");
                        defaultConfig.set("messages.reload_complete", "&a¡Recarga completa!");
                        defaultConfig.set("messages.no_permission", "&cNo tienes permiso para usar este comando.");
                        defaultConfig.set("messages.update_error", "&cError al descargar la actualización: %error%");
                        defaultConfig.set("messages.update_complete", "&aEl plugin Quest se ha actualizado a la versión %version%.");
                        defaultConfig.set("messages.up_to_date", "&aEl plugin ya está actualizado.");
                        defaultConfig.set("messages.delete_error", "&cError al eliminar el archivo antiguo del plugin.");
                        defaultConfig.set("messages.players_only", "&c¡Este comando solo puede ser utilizado por jugadores!");
                        defaultConfig.set("messages.active_mission_exists", "&cYa tienes una misión activa. Complétala o cancélala antes de comenzar una nueva.");
                        defaultConfig.set("messages.mission_already_completed", "&7Ya completaste esta misión y no puede ser repetida.");
                        defaultConfig.set("messages.quest_gui_title", "&9Quest");
                    }
                    
                    defaultConfig.save(langFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, key);
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            message = message.replace("%" + placeholder.getKey() + "%", placeholder.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + message);
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}

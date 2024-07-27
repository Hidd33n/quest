package com.narohid.quest.commands;

import com.narohid.quest.lang.LanguageManager;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

public class QuestAdminCommand implements CommandExecutor {

    private static final String PLUGIN_URL = "https://github.com/Hidd33n/quest/releases/latest/download/quest.jar"; // URL del plugin más reciente en GitHub
    private static final String VERSION_URL = "https://api.github.com/repos/Hidd33n/quest/releases/latest"; // URL para obtener la última versión

    private final QuestCommand questCommand;
    private final LanguageManager languageManager;

    public QuestAdminCommand(QuestCommand questCommand, LanguageManager languageManager) {
        this.questCommand = questCommand;
        this.languageManager = languageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Verificar que el jugador tenga el permiso adecuado
            if (!player.hasPermission("quest.admin")) {
                player.sendMessage(languageManager.getMessage("no_permission", new HashMap<>()));
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
                try {
                    // Obtener la versión más reciente desde GitHub
                    String latestVersion = getLatestVersion();
                    String currentVersion = Bukkit.getPluginManager().getPlugin("Quest").getDescription().getVersion();

                    // Comparar la versión actual con la versión más reciente
                    if (currentVersion.equalsIgnoreCase(latestVersion)) {
                        player.sendMessage(languageManager.getMessage("up_to_date", new HashMap<>()));
                        return true;
                    }

                    // Descargar la nueva versión del plugin a un archivo temporal
                    File tempFile = new File(Bukkit.getPluginManager().getPlugin("Quest").getDataFolder().getParentFile(), "quest_temp.jar");
                    FileUtils.copyURLToFile(new URL(PLUGIN_URL), tempFile);

                    // Desactivar el plugin
                    Plugin plugin = Bukkit.getPluginManager().getPlugin("Quest");
                    if (plugin != null) {
                        Bukkit.getPluginManager().disablePlugin(plugin);
                    }

                    // Reemplazar el archivo JAR del plugin
                    File pluginFile = new File(Bukkit.getPluginManager().getPlugin("Quest").getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
                    if (pluginFile.delete()) {
                        tempFile.renameTo(pluginFile);
                    } else {
                        player.sendMessage(languageManager.getMessage("delete_error", new HashMap<>()));
                        return true;
                    }

                    // Reiniciar el servidor para cargar el nuevo plugin
                    Bukkit.reload();

                    HashMap<String, String> placeholders = new HashMap<>();
                    placeholders.put("version", latestVersion);
                    player.sendMessage(languageManager.getMessage("update_complete", placeholders));
                } catch (IOException e) {
                    HashMap<String, String> placeholders = new HashMap<>();
                    placeholders.put("error", e.getMessage());
                    player.sendMessage(languageManager.getMessage("update_error", placeholders));
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                questCommand.reloadMissions();
                languageManager.reloadLanguages();
                player.sendMessage(languageManager.getMessage("reload_complete", new HashMap<>()));
                return true;
            }

            player.sendMessage(languageManager.getMessage("invalid_command", new HashMap<>()));
            return true;
        }
        sender.sendMessage(languageManager.getMessage("players_only", new HashMap<>()));
        return false;
    }

    private String getLatestVersion() throws IOException {
        StringBuilder json = new StringBuilder();
        try (Scanner scanner = new Scanner(new URL(VERSION_URL).openStream(), StandardCharsets.UTF_8)) {
            while (scanner.hasNext()) {
                json.append(scanner.nextLine());
            }
        }
        // Verifica si el JSON obtenido es válido
        String jsonString = json.toString();
        if (!jsonString.startsWith("{")) {
            throw new IOException("Invalid JSON response from the server.");
        }
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString("tag_name");
    }
}

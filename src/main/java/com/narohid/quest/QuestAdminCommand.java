package com.narohid.quest;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class QuestAdminCommand implements CommandExecutor {

    private static final String PLUGIN_URL = "URL_DE_TU_PLUGIN/quest-latest.jar"; // URL del plugin más reciente

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Verificar que el jugador tenga el permiso adecuado
            if (!player.hasPermission("quest.admin")) {
                player.sendMessage("No tienes permiso para usar este comando.");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
                try {
                    // Descargar la nueva versión del plugin
                    File pluginFile = new File(Bukkit.getPluginManager().getPlugin("Quest").getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
                    FileUtils.copyURLToFile(new URL(PLUGIN_URL), pluginFile);

                    // Reiniciar el plugin
                    Plugin plugin = Bukkit.getPluginManager().getPlugin("Quest");
                    if (plugin != null) {
                        Bukkit.getPluginManager().disablePlugin(plugin);
                        Bukkit.getPluginManager().enablePlugin(plugin);
                        player.sendMessage("El plugin Quest ha sido actualizado.");
                    } else {
                        player.sendMessage("No se pudo encontrar el plugin Quest.");
                    }
                } catch (IOException e) {
                    player.sendMessage("Error al descargar la actualización: " + e.getMessage());
                }
                return true;
            }

            player.sendMessage("Uso: /quest:admin update");
            return true;
        }
        sender.sendMessage("¡Este comando solo puede ser usado por jugadores!");
        return false;
    }
}

package com.narohid.quest;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class QuestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Crear una GUI tipo cofre de 27 espacios vacíos
            Inventory questGui = Bukkit.createInventory(null, 27, "Quest GUI");

            // Abrir la GUI para el jugador
            player.openInventory(questGui);

            return true;
        }
        sender.sendMessage("¡Este comando solo puede ser usado por jugadores!");
        return false;
    }
}

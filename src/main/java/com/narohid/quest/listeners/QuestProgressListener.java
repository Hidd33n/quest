package com.narohid.quest.listeners;

import com.narohid.quest.database.SQLiteManager;
import com.narohid.quest.lang.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestProgressListener implements Listener {

    private final LanguageManager languageManager;
    private final SQLiteManager sqliteManager;

    public QuestProgressListener(LanguageManager languageManager, SQLiteManager sqliteManager) {
        this.languageManager = languageManager;
        this.sqliteManager = sqliteManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();

            try (Connection connection = sqliteManager.getConnection()) {
                // Verificar si el jugador tiene una misión activa
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM player_missions WHERE player_uuid = ? AND completed = 0"
                );
                statement.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = statement.executeQuery();

                if (!resultSet.next()) {
                    return;
                }

                String missionType = resultSet.getString("mission_type");
                if (missionType == null || !missionType.equalsIgnoreCase("kill")) {
                    return;
                }

                String target = resultSet.getString("mission_target");
                int amount = resultSet.getInt("mission_amount");
                int progress = resultSet.getInt("mission_progress");

                if (event.getEntityType().toString().equalsIgnoreCase(target)) {
                    progress++;
                    PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE player_missions SET mission_progress = ? WHERE player_uuid = ? AND mission_name = ?"
                    );
                    updateStatement.setInt(1, progress);
                    updateStatement.setString(2, player.getUniqueId().toString());
                    updateStatement.setString(3, resultSet.getString("mission_name"));
                    updateStatement.executeUpdate();

                    // Informar al jugador sobre el progreso actual
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("current", String.valueOf(progress));
                    placeholders.put("total", String.valueOf(amount));
                    placeholders.put("target", target.toLowerCase() + "s");
                    player.sendMessage(languageManager.getMessage("progress", placeholders));

                    if (progress >= amount) {
                        // Completar la misión
                        player.sendMessage(languageManager.getMessage("complete", placeholders));
                        giveReward(player, resultSet.getString("mission_name"));
                        completeMission(player, resultSet.getString("mission_name"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        try (Connection connection = sqliteManager.getConnection()) {
            // Verificar si el jugador tiene una misión activa
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM player_missions WHERE player_uuid = ? AND completed = 0"
            );
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return;
            }

            String missionType = resultSet.getString("mission_type");
            if (missionType == null || !missionType.equalsIgnoreCase("gather")) {
                return;
            }

            String target = resultSet.getString("mission_target");
            int amount = resultSet.getInt("mission_amount");
            int progress = resultSet.getInt("mission_progress");

            if (event.getBlock().getType().toString().equalsIgnoreCase(target)) {
                progress++;
                PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE player_missions SET mission_progress = ? WHERE player_uuid = ? AND mission_name = ?"
                );
                updateStatement.setInt(1, progress);
                updateStatement.setString(2, player.getUniqueId().toString());
                updateStatement.setString(3, resultSet.getString("mission_name"));
                updateStatement.executeUpdate();

                // Informar al jugador sobre el progreso actual
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("current", String.valueOf(progress));
                placeholders.put("total", String.valueOf(amount));
                placeholders.put("target", target.toLowerCase() + "s");
                player.sendMessage(languageManager.getMessage("progress", placeholders));

                if (progress >= amount) {
                    // Completar la misión
                    player.sendMessage(languageManager.getMessage("complete", placeholders));
                    giveReward(player, resultSet.getString("mission_name"));
                    completeMission(player, resultSet.getString("mission_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void completeMission(Player player, String missionName) {
        try (Connection connection = sqliteManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "UPDATE player_missions SET completed = 1 WHERE player_uuid = ? AND mission_name = ?"
            );
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, missionName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void giveReward(Player player, String missionDisplayName) {
        // Buscar el archivo de misión en el directorio de misiones
        File missionsFolder = new File(Bukkit.getPluginManager().getPlugin("Quest").getDataFolder(), "missions");
        File[] missionFiles = missionsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (missionFiles == null) {
            return;
        }

        File missionFile = null;
        for (File file : missionFiles) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (missionDisplayName.equals(config.getString("name"))) {
                missionFile = file;
                break;
            }
        }

        if (missionFile == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("mission_name", missionDisplayName);
            player.sendMessage(languageManager.getMessage("no_config", placeholders));
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(missionFile);

        // Manejar recompensas de ítems
        if (config.contains("reward.items")) {
            List<Map<?, ?>> rewardItems = (List<Map<?, ?>>) config.getMapList("reward.items");
            if (rewardItems != null && !rewardItems.isEmpty()) {
                for (Map<?, ?> rewardItem : rewardItems) {
                    String materialName = (String) rewardItem.get("item");
                    int amount = (int) rewardItem.get("amount");

                    Material material = Material.getMaterial(materialName.toUpperCase());
                    if (material == null) {
                        material = Material.STONE; // Default to STONE if the material is not found
                    }

                    ItemStack reward = new ItemStack(material, amount);
                    player.getInventory().addItem(reward);
                }
            }
        }

        // Manejar recompensa de experiencia
        if (config.contains("reward.experience")) {
            int experience = config.getInt("reward.experience", 0);
            if (experience > 0) {
                player.giveExp(experience);
            }
        }

        player.sendMessage(languageManager.getMessage("reward", new HashMap<>()));
    }
}

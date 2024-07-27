package com.narohid.quest.manager;

import com.narohid.quest.database.SQLiteManager;
import com.narohid.quest.lang.LanguageManager;
import com.narohid.quest.utils.MissionLoader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestManager {

    private List<FileConfiguration> missionConfigs;
    private final LanguageManager languageManager;
    private final SQLiteManager sqliteManager;

    public QuestManager(LanguageManager languageManager, SQLiteManager sqliteManager) {
        this.languageManager = languageManager;
        this.sqliteManager = sqliteManager;
        missionConfigs = MissionLoader.loadMissions();
    }

    public List<FileConfiguration> getMissionConfigs() {
        return missionConfigs;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public SQLiteManager getSQLiteManager() {
        return sqliteManager;
    }

    public void activateMission(Player player, FileConfiguration config) {
        try (Connection connection = sqliteManager.getConnection()) {
            PreparedStatement checkStatement = connection.prepareStatement(
                "SELECT * FROM player_missions WHERE player_uuid = ? AND mission_name = ?"
            );
            checkStatement.setString(1, player.getUniqueId().toString());
            checkStatement.setString(2, config.getString("name"));
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE player_missions SET mission_type = ?, mission_target = ?, mission_amount = ?, mission_progress = ?, completed = 0, active = 1 WHERE player_uuid = ? AND mission_name = ?"
                );
                updateStatement.setString(1, config.getString("objective.type"));
                updateStatement.setString(2, config.getString("objective.target"));
                updateStatement.setInt(3, config.getInt("objective.amount"));
                updateStatement.setInt(4, 0);
                updateStatement.setString(5, player.getUniqueId().toString());
                updateStatement.setString(6, config.getString("name"));
                updateStatement.executeUpdate();
            } else {
                PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO player_missions (player_uuid, mission_name, mission_type, mission_target, mission_amount, mission_progress, completed, active) VALUES (?, ?, ?, ?, ?, ?, 0, 1)"
                );
                insertStatement.setString(1, player.getUniqueId().toString());
                insertStatement.setString(2, config.getString("name"));
                insertStatement.setString(3, config.getString("objective.type"));
                insertStatement.setString(4, config.getString("objective.target"));
                insertStatement.setInt(5, config.getInt("objective.amount"));
                insertStatement.setInt(6, 0);
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mission_name", config.getString("name"));
        player.sendMessage(languageManager.getMessage("accept_mission", placeholders));
    }

    public void cancelMission(Player player, String missionName) {
        try (Connection connection = sqliteManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM player_missions WHERE player_uuid = ? AND mission_name = ?"
            );
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, missionName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        player.sendMessage(languageManager.getMessage("cancel_mission", new HashMap<>()));
    }

    public boolean isMissionCompleted(Player player, String missionName) {
        try (Connection connection = sqliteManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT completed FROM player_missions WHERE player_uuid = ? AND mission_name = ?"
            );
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, missionName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("completed") == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isMissionActive(Player player, String missionName) {
        try (Connection connection = sqliteManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT active FROM player_missions WHERE player_uuid = ? AND mission_name = ?"
            );
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, missionName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("active") == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

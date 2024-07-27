package com.narohid.quest.hooks;

import com.narohid.quest.database.SQLiteManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaceholderHook extends PlaceholderExpansion {

    private final String identifier;
    private final String author;
    private final String version;
    private final SQLiteManager sqliteManager;

    public PlaceholderHook(String identifier, String author, String version, SQLiteManager sqliteManager) {
        this.identifier = identifier;
        this.author = author;
        this.version = version;
        this.sqliteManager = sqliteManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equals("current_mission")) {
            return getCurrentMission(player);
        }

        if (params.equals("mission_progress")) {
            return getMissionProgress(player);
        }

        return null;
    }

    private String getCurrentMission(Player player) {
        try (Connection connection = sqliteManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT mission_type, mission_target, mission_amount FROM player_missions WHERE player_uuid = ? AND completed = 0"
            );
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String missionType = resultSet.getString("mission_type");
                String missionTarget = resultSet.getString("mission_target");
                int missionAmount = resultSet.getInt("mission_amount");

                if (missionType != null && missionTarget != null) {
                    return "Kill " + missionAmount + " " + missionTarget.toLowerCase() + "s";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No active mission";
    }

    private String getMissionProgress(Player player) {
        try (Connection connection = sqliteManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT mission_amount, mission_progress FROM player_missions WHERE player_uuid = ? AND completed = 0"
            );
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int missionAmount = resultSet.getInt("mission_amount");
                int missionProgress = resultSet.getInt("mission_progress");

                return missionProgress + "/" + missionAmount;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0/0";
    }
}

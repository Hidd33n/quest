package com.narohid.quest.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteManager {
    private static final String DB_NAME = "quests.db";
    private Connection connection;
    private final String dbFilePath;

    public SQLiteManager(FileConfiguration config, JavaPlugin plugin) {
        this.dbFilePath = plugin.getDataFolder().getAbsolutePath() + "/" + DB_NAME;
        connect();
        this.initialize();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbFilePath);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        try (Statement statement = this.connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS player_missions (" +
                                      "player_uuid TEXT NOT NULL," +
                                      "mission_name TEXT NOT NULL," +
                                      "mission_type TEXT NOT NULL," +
                                      "mission_target TEXT NOT NULL," +
                                      "mission_amount INTEGER NOT NULL," +
                                      "mission_progress INTEGER NOT NULL DEFAULT 0," +
                                      "completed INTEGER NOT NULL DEFAULT 0," +
                                      "active INTEGER NOT NULL DEFAULT 0," +
                                      "PRIMARY KEY (player_uuid, mission_name))";
            statement.execute(createTableQuery);

            // Verificar si la columna "completed" ya existe
            ensureColumnExists(statement, "completed", "INTEGER NOT NULL DEFAULT 0");
            // Verificar si la columna "active" ya existe
            ensureColumnExists(statement, "active", "INTEGER NOT NULL DEFAULT 0");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureColumnExists(Statement statement, String columnName, String columnDefinition) throws SQLException {
        ResultSet resultSet = statement.executeQuery("PRAGMA table_info(player_missions)");
        boolean columnExists = false;
        while (resultSet.next()) {
            if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                columnExists = true;
                break;
            }
        }

        if (!columnExists) {
            String addColumnQuery = "ALTER TABLE player_missions ADD COLUMN " + columnName + " " + columnDefinition;
            statement.execute(addColumnQuery);
        }
    }

    public Connection getConnection() {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this.connection;
    }

    public void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

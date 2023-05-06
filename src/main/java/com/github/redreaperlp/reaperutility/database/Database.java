package com.github.redreaperlp.reaperutility.database;

import ch.qos.logback.classic.Logger;
import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.settings.JSettings;
import com.github.redreaperlp.reaperutility.util.Color;
import com.github.redreaperlp.reaperutility.util.ColorLoggerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    HikariDataSource ds;

    public Database() {
        Logger hikariLogger = (Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
        hikariLogger.getLoggerContext().reset();
        hikariLogger.addAppender(new ColorLoggerFactory());
        hikariLogger.getAppender("ColorLogger").start();

        JSettings.JDatabaseSettings settings = Main.settings.databaseSettings();
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setJdbcUrl(settings.getURL());
        config.setUsername(settings.getUsername());
        config.setPassword(settings.getPassword().equals("${token}") ? Main.settings.jdaSettings().token() : settings.getPassword());
        try {
            HikariDataSource ds = new HikariDataSource(config);
            this.ds = ds;
        } catch (HikariPool.PoolInitializationException e) {
            System.out.println();
            new Color.Print("*** Something went wrong ***", Color.ORANGE)
                    .appendLine("Failed to connect to database!")
                    .appendLine("Please check your database settings in config.json")
                    .appendLine("Error: ").append(e.getMessage(), Color.YELLOW).printError();
            if (e.getMessage().contains("Unknown database")) {
                System.out.println();
                new Color.Print("*** Hint ***", Color.ORANGE).appendLine("Please create the database and try again.")
                        .appendLine("Command: ").append("\"CREATE DATABASE IF NOT EXISTS " + settings.getDatabase() + ";\"", Color.YELLOW).printInfo();
            }
            Main.exit();
        }
        new Color.Print("Checking for tables...").printInfo();
        List<String> tables = new ArrayList<>();
        if (ds == null) {
            new Color.Print("*** Something went wrong ***")
                    .appendLine("Failed to connect to database!").printError();
            Main.exit();
        }
        try (Connection con = ds.getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, null, new String[]{"TABLE"});
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tables.add(tableName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (JSettings.JTable jTable : settings.getTables()) {
            if (!tables.contains(jTable.getName())) {
                new Color.Print("Table " + jTable.getName() + " not found!").printDebug();
                new Color.Print("Creating table " + jTable.getName() + "...").printDebug();
                try (Connection con = ds.getConnection()) {
                    PreparedStatement statement = con.prepareStatement( "CREATE TABLE IF NOT EXISTS " + jTable.getName() + " " + jTable.getTable().getStatement());
                    statement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}

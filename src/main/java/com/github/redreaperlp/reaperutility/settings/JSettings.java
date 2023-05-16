package com.github.redreaperlp.reaperutility.settings;

import com.github.redreaperlp.reaperutility.util.Color;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JSettings {
    private JJDASettings jdaSettings;
    private JDatabaseSettings databaseSettings;
    private JConsoleSettings consoleSettings;

    public JSettings() {
        this.jdaSettings = new JJDASettings();
        this.databaseSettings = new JDatabaseSettings();
        this.consoleSettings = new JConsoleSettings();
    }

    public JSettings(JJDASettings jdaSettings, JDatabaseSettings databaseSettings, JConsoleSettings consoleSettings) {
        this.jdaSettings = jdaSettings;
        this.databaseSettings = databaseSettings;
        this.consoleSettings = consoleSettings;
        if (jdaSettings == null || !jdaSettings.check()) {
            this.jdaSettings = new JJDASettings();
        }
        if (databaseSettings == null || !databaseSettings.check()) {
            this.databaseSettings = new JDatabaseSettings();
        }
        if (consoleSettings == null) {
            this.consoleSettings = new JConsoleSettings();
        }
    }

    public JJDASettings jdaSettings() {
        return jdaSettings;
    }

    public JDatabaseSettings databaseSettings() {
        return databaseSettings;
    }

    public JConsoleSettings consoleSettings() {
        return consoleSettings;
    }

    public void save() {
        new Color.Print("Saving Settings...").printInfo();
        File file = new File("settings.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Color.Print("Saved Settings").printInfo();
    }

    public static class JJDASettings {
        private final String token;
        private Activity.ActivityType activity;
        private final String activityText;
        private OnlineStatus status;

        public JJDASettings() {
            this("your_token", Activity.ActivityType.PLAYING, "with your feelings", OnlineStatus.ONLINE);
        }

        public JJDASettings(String token, Activity.ActivityType activity, String activityText, OnlineStatus status) {
            this.token = token;
            this.activity = activity;
            this.activityText = activityText;
            this.status = status;
        }

        public String token() {
            return token;
        }

        public Activity.ActivityType activity() {
            return activity;
        }

        public String activityText() {
            return activityText;
        }

        public OnlineStatus status() {
            if (this.status == null || this.status == OnlineStatus.UNKNOWN) {
                this.status = OnlineStatus.ONLINE;
            }
            return status;
        }

        public JJDASettings(String token, String activity, String activityText, String status) {
            this.token = token;
            try {
                this.activity = Activity.ActivityType.valueOf(activity);
            } catch (IllegalArgumentException e) {
                this.activity = Activity.ActivityType.PLAYING;
            }
            this.activityText = activityText;
            this.status = OnlineStatus.valueOf(status);
            if (this.status == OnlineStatus.UNKNOWN) {
                this.status = OnlineStatus.ONLINE;
            }
        }

        public boolean check() {
            return token != null && activity != null && activityText != null && status != null;
        }
    }

    public static class JDatabaseSettings {
        private final String host;
        private final String port;
        private final String user;
        private final String password;
        private final String database;
        private List<JTable> tables;

        public JDatabaseSettings() {
            this("localhost", "3306", "root", "passwd", "utility", null);
        }

        private List<JTable> defaultTables() {
            List<JTable> tables = new ArrayList<>();
            tables.add(new JTable(JTable.ITables.EVENTS, "events"));
            tables.add(new JTable(JTable.ITables.GUILDS, "guilds"));
            return tables;
        }

        public JDatabaseSettings(String host, String port, String user, String password, String database, List<JTable> tables) {
            this.host = host;
            this.port = port;
            this.user = user;
            this.password = password;
            this.database = database;
            this.tables = tables;
            if (tables == null) {
                this.tables = defaultTables();
            }
        }

        public String getURL() {
            return "jdbc:mysql://" + host + ":" + port + "/" + database;
        }

        public boolean check() {
            return host != null && port != null && user != null && password != null && database != null && tables != null;
        }

        public String getTable(JTable.ITables table) {
            for (JTable jtable : tables) {
                if (jtable.getTable() == table) {
                    return jtable.getName();
                }
            }
            return null;
        }

        public String getUsername() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getDatabase() {
            return database;
        }

        public List<JTable> getTables() {
            return tables;
        }
    }

    public static class JConsoleSettings {
        private boolean debug;
        private boolean colored;

        public JConsoleSettings() {
            this(false, true);
        }

        public JConsoleSettings(boolean debug, boolean colored) {
            this.debug = debug;
            this.colored = colored;
        }

        public boolean colored() {
            return colored;
        }

        public boolean debug() {
            return debug;
        }

        public void debug(boolean debug) {
            this.debug = debug;
        }

        public void colored(boolean colored) {
            this.colored = colored;
        }
    }

    public static class JTable {
        private ITables table;
        private final String name;

        public JTable(ITables table, String name) {
            this.table = table;
            this.name = name;
        }

        public JTable(String table, String name) {
            try {
                this.table = ITables.valueOf(table);
            } catch (IllegalArgumentException e) {
                this.table = ITables.GUILDS;
            }
            this.name = name;
        }

        public ITables getTable() {
            return table;
        }

        public String getName() {
            return name;
        }

        public PreparedStatement getCreateQuery(Connection con) {
            try {
                return con.prepareStatement(table.getStatement());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public enum ITables {
            GUILDS("guilds", "(" +
                    "id BIGINT(22) NOT NULL PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "next_event_start JSON," +
                    "permissions JSON," +
                    "users JSON" +
                    ")"),
            EVENTS("events", "(" +
                    "messageId BIGINT(22) NOT NULL PRIMARY KEY," +
                    "guildId BIGINT(22) NOT NULL," +
                    "channelId BIGINT(22) NOT NULL," +
                    "date BIGINT(15) NOT NULL" +
                    ")"),
            ;
            private final String name;
            private final String statement;

            ITables(String name, String statement) {
                this.name = name;
                this.statement = statement;
            }

            public String getName() {
                return name;
            }

            public String getStatement() {
                return statement;
            }

            public static ITables getTable(String name) {
                for (ITables table : values()) {
                    if (table.getName().equalsIgnoreCase(name)) {
                        return table;
                    }
                }
                return null;
            }
        }
    }
}

package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.settings.JSettings;
import com.github.redreaperlp.reaperutility.util.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private long guildId;
    private long channelId;
    private long messageId;
    private LocalDateTime timestamp;
    private Scheduler.EventSchduler currentScheduler;

    public Event(long guildId, long channelId, long messageId, LocalDateTime timestamp) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    public void overwrite(PreparedEvent event) {
        long[] targetMessage = event.getTargetMessage();
        this.guildId = targetMessage[0];
        this.channelId = targetMessage[1];
        this.messageId = targetMessage[2];
        this.timestamp = event.getDate();
        updateToDatabase();
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getMessageId() {
        return messageId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void insertToDatabase() {
        try (Connection con = Main.database.getConnection()) {
            PreparedStatement stmt = con.prepareStatement("INSERT INTO " + Main.settings.databaseSettings().getDatabase() + "." + Main.settings.databaseSettings().getTable(JSettings.JTable.ITables.EVENTS) + " (guildId, channelId, messageId, date) VALUES (?, ?, ?, ?)");
            stmt.setLong(1, guildId);
            stmt.setLong(2, channelId);
            stmt.setLong(3, messageId);
            stmt.setLong(4, timestamp.toEpochSecond(Main.zoneOffset));
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateToDatabase() {
        try (Connection con = Main.database.getConnection()) {
            PreparedStatement stmt = con.prepareStatement("UPDATE " + Main.settings.databaseSettings().getDatabase() + "." + Main.settings.databaseSettings().getTable(JSettings.JTable.ITables.EVENTS) + " SET date = ? WHERE guildId = ? AND channelId = ? AND messageId = ?");
            stmt.setLong(1, timestamp.toEpochSecond(Main.zoneOffset));
            stmt.setLong(2, guildId);
            stmt.setLong(3, channelId);
            stmt.setLong(4, messageId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFromDatabase() {
        try (Connection con = Main.database.getConnection()) {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM " + Main.settings.databaseSettings().getDatabase() + "." + Main.settings.databaseSettings().getTable(JSettings.JTable.ITables.EVENTS) + " WHERE messageId = ?");
            stmt.setLong(1, messageId);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Event> loadAllEvents() {
        try (Connection con = Main.database.getConnection()) {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM " + Main.settings.databaseSettings().getDatabase() + "." + Main.settings.databaseSettings().getTable(JSettings.JTable.ITables.EVENTS));
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<Event> eventList = new ArrayList<>();
            while (rs.next()) {
                long guildId = rs.getLong("guildId");
                long channelId = rs.getLong("channelId");
                long messageId = rs.getLong("messageId");
                long timestamp = rs.getLong("date");
                eventList.add(new Event(guildId, channelId, messageId, LocalDateTime.ofEpochSecond(timestamp, 0, Main.zoneOffset)));
            }
            return eventList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void fire() {
        new Color.Print("Event " + messageId + " in guild " + guildId + " fired!").printDebug();
        removeFromDatabase();
    }

    public void setCurrentScheduler(Scheduler.EventSchduler scheduler) {
        this.currentScheduler = scheduler;
    }

    public class EventReminder {

    }
}

package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.settings.JSettings;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

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

    private List<Long> acceptedUsers = new ArrayList<>();
    private List<Long> declinedUsers = new ArrayList<>();
    private List<Long> undecidedUsers = new ArrayList<>();

    private boolean readUsers = false;


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
        currentScheduler.delete(this);
        Scheduler.schdule(this);
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
            stmt.execute();
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
        try {
            Message message = Main.jda.getGuildById(guildId).getTextChannelById(channelId).retrieveMessageById(messageId).complete();
            MessageEmbed embed = message.getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(embed);
            builder.setTitle(embed.getTitle(), message.getJumpUrl());
            removeExtraFields(builder);
            List<Thread> threads = new ArrayList<>();
            new Color.Print("Firing Event " + embed.getTitle() + " (" + messageId + ")!").printDebug();

            for (long user : acceptedUsers) {
                Thread thread = createReminderThread(user, builder, embed.getTitle());
                threads.add(thread);
                thread.start();
            }

            for (long user : undecidedUsers) {
                Thread thread = createReminderThread(user, builder, embed.getTitle());
                threads.add(thread);
                thread.start();
            }

            waitForThreads(threads);
            removeFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void removeExtraFields(EmbedBuilder builder) {
        int size = builder.getFields().size();
        builder.getFields().remove(size - 1);
        builder.getFields().remove(size - 2);
        builder.getFields().remove(size - 3);
    }

    private Thread createReminderThread(long userId, EmbedBuilder builder, String title) {
        return new Thread(() -> {
            User jdaUser = Main.jda.getUserById(userId);
            new Color.Print(" - Reminding " + jdaUser.getName() + " (" + jdaUser.getId() + ")").printDebug();
            jdaUser.openPrivateChannel().complete().sendMessageEmbeds(builder.build()).setContent("The event " + title + " is starting now!").queue();
        });
    }

    private void waitForThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void setCurrentScheduler(Scheduler.EventSchduler scheduler) {
        this.currentScheduler = scheduler;
    }

    public Scheduler.EventSchduler getCurrentScheduler() {
        return currentScheduler;
    }

    public String getAcceptedString() {
        if (acceptedUsers.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (long id : acceptedUsers) {
            sb.append("<@").append(id).append(">\n");
        }
        return sb.toString();
    }

    public String getDeclinedString() {
        if (declinedUsers.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (long id : declinedUsers) {
            sb.append("<@").append(id).append(">\n");
        }
        return sb.toString();
    }

    public String getUndecidedString() {
        if (undecidedUsers.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (long id : undecidedUsers) {
            sb.append("<@").append(id).append(">\n");
        }
        return sb.toString();
    }


    public void toggleAccept(long idLong) {
        toggleRole(acceptedUsers, declinedUsers, undecidedUsers, idLong);
    }

    public void toggleDecline(long idLong) {
        toggleRole(declinedUsers, acceptedUsers, undecidedUsers, idLong);
    }

    public void toggleUndecided(long idLong) {
        toggleRole(undecidedUsers, acceptedUsers, declinedUsers, idLong);
    }

    public void toggleRole(List<Long> list, List<Long> list2, List<Long> list3, long idLong) {
        if (list.contains(idLong)) {
            list.remove(idLong);
        } else {
            list.add(idLong);
            list2.remove(idLong);
            list3.remove(idLong);
        }
    }

    public void modifyEmbed(EmbedBuilder builder) {
        int fields = builder.getFields().size() - 1;
        builder.getFields().set(fields - 2, new MessageEmbed.Field("Accepted", getAcceptedString(), true));
        builder.getFields().set(fields - 1, new MessageEmbed.Field("Declined", getDeclinedString(), true));
        builder.getFields().set(fields, new MessageEmbed.Field("Undecided", getUndecidedString(), true));
    }

    public boolean hasReadUsers() {
        return readUsers;
    }

    public void readUsers(Message message) {
        readUsers = true;
        List<MessageEmbed.Field> fields = message.getEmbeds().get(0).getFields();
        for (MessageEmbed.Field field : fields) {
            switch (field.getName()) {
                case "Accepted":
                    processUserList(field.getValue(), acceptedUsers);
                    break;
                case "Declined":
                    processUserList(field.getValue(), declinedUsers);
                    break;
                case "Undecided":
                    processUserList(field.getValue(), undecidedUsers);
                    break;
            }
        }
    }

    private void processUserList(String value, List<Long> userList) {
        for (String s : value.split("\n")) {
            if (s.equals("-")) continue;
            userList.add(Long.parseLong(s.substring(2, s.length() - 1)));
        }
    }


    public class EventReminder {

    }
}

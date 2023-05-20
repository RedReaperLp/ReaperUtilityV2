package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PreparedEvent {
    static List<PreparedEvent> preparations = new ArrayList<>();
    static List<PreparedEvent> forgettablePreparations = new ArrayList<>();

    private String name;
    private String description;
    private String location;
    private long date;
    private List<String> notification;
    private long[] targetMessage;
    private int[] color = new int[]{0, 255, 0};

    private long editorId;
    private int timeUntilForget = 50;
    private boolean cancelled = false;
    private static Thread dumpThread;


    public PreparedEvent(long editorId) {
        this.editorId = editorId;
    }

    public static boolean hasPreparation(long idLong) {
        return preparations.stream().anyMatch(preparedEvent -> preparedEvent.getEditorId() == idLong);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public void setLocation(String value) {
        this.location = value;
    }

    public void setDate(long dateLong) {
        date = dateLong;
    }

    public void setDate(String value) {
        this.date = Long.parseLong(value.replaceAll("[<t:f>]", ""));
    }

    public void setNotification(List<String> value) {
        this.notification = value;
    }

    /**
     * Sets the event channel to the channel of the message <br>
     * [0] = guildId <br>
     * [1] = channelId <br>
     * [2] = messageId <br>
     *
     * @param guildId   the guildId of the message
     * @param channelId the channelId of the message
     */
    public void setEventChannel(long guildId, long channelId) {
        this.targetMessage = new long[]{
                guildId,
                channelId,
                0
        };
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getDate() {
        return LocalDateTime.ofEpochSecond(date, 0, Main.zoneOffset);
    }

    public long getDateAsEpoch() {
        return getDate().toEpochSecond(Main.zoneOffset);
    }

    public List<String> getNotification() {
        if (notification == null) {
            notification = new ArrayList<>();
        }
        return notification;
    }

    /**
     * Returns the event target <br>
     * [0] = guildId <br>
     * [1] = channelId <br>
     * [2] = messageId (is being set by {@link PreparedEvent#complete()} <br>
     * set with {@link PreparedEvent#setEventChannel(long, long)}
     *
     * @return the event target as long[]
     */
    public long[] getTargetMessage() {
        return targetMessage;
    }


    public long getEditorId() {
        return editorId;
    }

    private void setEventChannel(String value) {
        GuildMessageChannel channel = Objects.requireNonNull(Main.jda.getTextChannelById(value.replaceAll("[<#>]", "")));
        targetMessage = new long[]{
                channel.getGuild().getIdLong(),
                channel.getIdLong(),
                0
        };
    }

    public MessageEditData modifyEditor(Message message) {
        EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().get(0));
        builder.clearFields();
        builder.addField(PrepareEmbed.FieldKey.NAME.key(), name, false);
        if (description != null && !description.equals("Your Description")) {
            builder.addField(PrepareEmbed.FieldKey.DESCRIPTION.key(), description, false);
        }
        if (location != null && !location.equals("Your Location")) {
            builder.addField(PrepareEmbed.FieldKey.LOCATION.key(), location, false);
        }
        builder.addField(PrepareEmbed.FieldKey.DATE.key(), "<t:" + date + ":f>", false);
        builder.addField(PrepareEmbed.FieldKey.REMAINING.key, "<t:" + date + ":R>", false);
        if (notification != null && notification.size() > 0) {
            builder.addField(PrepareEmbed.FieldKey.NOTIFICATION.key(), String.join("\n", notification), false);
        }
        if (targetMessage != null) {
            builder.addField(PrepareEmbed.FieldKey.EVENT_CHANNEL.key(), "<#" + targetMessage[1] + ">", false);
        }
        builder.setColor(java.awt.Color.decode(String.format("#%02x%02x%02x", color[0], color[1], color[2])));
        builder.setTimestamp(Instant.now());
        return MessageEditBuilder.fromMessage(message).setEmbeds(builder.build()).setComponents(PrepareEmbed.eventSetupActionRow(completePossible())).build();
    }

    public void setColor(int[] rgb) {
        color = rgb;
    }

    public boolean completePossible() {
        if (name == null || name.equals("Events Name")) {
            return false;
        } else if (LocalDateTime.ofEpochSecond(date, 0, Main.zoneOffset).isBefore(LocalDateTime.now(Main.zoneOffset))) {
            return false;
        } else if (LocalDateTime.ofEpochSecond(date, 0, Main.zoneOffset).isAfter(LocalDateTime.now(Main.zoneOffset).plusDays(31))) {
            return false;
        }
        return true;
    }

    public void cancel(MessageChannel event) {
        preparations.remove(this);
        cancelled = true;
        event.retrieveMessageById(editorId).complete().delete().queue();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void complete() {
        Guild tarGuild = Objects.requireNonNull(Main.jda.getGuildById(targetMessage[0]));
        MessageChannel tarChannel = Objects.requireNonNull(tarGuild.getTextChannelById(targetMessage[1]));
        MessageCreateAction create = tarChannel.sendMessageEmbeds(PrepareEmbed.eventCompleted(this));
        create.addComponents(PrepareEmbed.eventActionRow());
        if (notification.size() > 0) {
            String roleMentionString = notification.stream()
                    .filter(roleName -> roleName.startsWith("@"))
                    .map(roleName -> Objects.requireNonNull(tarGuild.getRolesByName(roleName.substring(1), true).get(0)).getAsMention())
                    .collect(Collectors.joining("\n"));
            create.addContent(roleMentionString);
        }
        targetMessage[2] = create.complete().getIdLong();

        Scheduler.scheduleEvent(new Event(targetMessage[0], targetMessage[1], targetMessage[2], LocalDateTime.ofEpochSecond(date, 0, Main.zoneOffset)));
        preparations.remove(this);
    }

    /**
     * Completes the event and edits the message
     *
     * @return true if the event was completed, false if the event was not found
     */
    public boolean completeEdit() {
        Event event = Scheduler.getEvent(targetMessage[2]);
        try {
            if (event != null) {
                Guild tarGuild = Main.jda.getGuildById(targetMessage[0]);
                Message tarMessage = tarGuild.getTextChannelById(targetMessage[1]).retrieveMessageById(targetMessage[2]).complete();
                tarMessage.editMessageEmbeds(PrepareEmbed.eventCompleted(this, event)).setComponents(PrepareEmbed.eventActionRow()).queue();
                event.overwrite(this);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            event.removeFromDatabase();
            return false;
        }
    }


    public int countDown() {
        return timeUntilForget--;
    }

    /**
     * Adds a role to the notification list
     *
     * @param toAdd the role to add
     * @return true if the role was added, false if it was removed
     */
    public boolean addRole(String toAdd) {
        if (toAdd.equals("@everyone")) {
            if (notification.contains("@everyone")) {
                notification.remove("@everyone");
                return false;
            } else {
                notification.clear();
                notification.add("@everyone");
                return true;
            }
        } else if (notification.contains("@everyone")) {
            notification.remove("@everyone");
        }

        if (notification.contains("@" + toAdd)) {
            notification.remove("@" + toAdd);
            return false;
        }

        notification.add("@" + toAdd);
        return true;
    }

    /**
     * Returns the preparation of the message
     *
     * @param message the message (Must contain an embed with the information needed)
     * @return the preparation of the message
     */
    public static PreparedEvent hasPreparation(Message message) {
        for (PreparedEvent preparation : preparations) {
            if (preparation.getEditorId() == message.getIdLong()) {
                forgettablePreparations.remove(preparation);
                return preparation;
            }
        }

        return inititate(message);
    }

    private static PreparedEvent inititate(Message message) {
        MessageEmbed embed = message.getEmbeds().get(0);
        PreparedEvent preparation = new PreparedEvent(message.getIdLong());
        List<MessageEmbed.Field> fields = embed.getFields();
        for (MessageEmbed.Field field : fields) {
            switch (Objects.requireNonNull(PrepareEmbed.FieldKey.fromKey(field.getName()))) {
                case NAME -> preparation.setName(field.getValue());
                case DESCRIPTION -> preparation.setDescription(field.getValue());
                case LOCATION -> preparation.setLocation(field.getValue());
                case DATE -> preparation.setDate(field.getValue());
                case NOTIFICATION -> {
                    List<String> notification = new ArrayList<>(Arrays.asList(field.getValue().split("\n")));
                    preparation.setNotification(notification);
                }
                case EVENT_CHANNEL -> preparation.setEventChannel(field.getValue());
            }
        }
        if (preparation.getNotification() == null) {
            preparation.setNotification(new ArrayList<>());
        }
        int[] rgb = new int[]{embed.getColor().getRed(), embed.getColor().getGreen(), embed.getColor().getBlue()};
        preparation.setColor(rgb);
        if (embed.getUrl() != null && embed.getUrl().contains("https://discord.com/channels/")) {
            String[] url = embed.getUrl().split("/");
            preparation.setTargetMessage(new long[]{Long.parseLong(url[4]), Long.parseLong(url[5]), Long.parseLong(url[6])});
        }
        preparations.add(preparation);
        return preparation;
    }

    public static PreparedEvent initFromEvent(Message event) {
        MessageEmbed embed = event.getEmbeds().get(0);
        PreparedEvent preparation = new PreparedEvent(event.getIdLong());
        preparation.setTargetMessage(event);
        preparation.setName(embed.getTitle());
        preparation.setDescription(embed.getDescription());
        preparation.setColor(new int[]{embed.getColor().getRed(), embed.getColor().getGreen(), embed.getColor().getBlue()});
        for (MessageEmbed.Field field : embed.getFields()) {
            switch (Objects.requireNonNull(PrepareEmbed.FieldKey.fromKey(field.getName()))) {
                case DATE -> preparation.setDate(field.getValue());
                case NOTIFICATION -> {
                    List<String> notification = new ArrayList<>(Arrays.asList(field.getValue().split("\n")));
                    preparation.setNotification(notification);
                }
            }
        }
        preparations.add(preparation);
        return preparation;
    }

    private void setTargetMessage(Message message) {
        targetMessage = new long[]{message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong()};
    }

    private void setTargetMessage(long[] targetMessage) {
        this.targetMessage = targetMessage;
    }

    /**
     * Starts a thread that removes the preparation after {@value timeUntilForget} seconds to free up memory
     */
    public void forgettable() {
        timeUntilForget = 50;
        forgettablePreparations.add(this);
        if (dumpThread == null || !dumpThread.isAlive()) {
            dumpThread = new Thread(() -> {
                new Color.Print("Prepared Events Dump Thread started").printDebug();
                while (!forgettablePreparations.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    List<PreparedEvent> toRemove = new ArrayList<>();
                    for (PreparedEvent preparation : forgettablePreparations) {
                        if (preparation.countDown() <= 0) {
                            toRemove.add(preparation);
                        }
                    }
                    forgettablePreparations.removeAll(toRemove);
                    preparations.removeAll(toRemove);
                }
                new Color.Print("Prepared Events Dump Thread stopped").printDebug();
            });
            dumpThread.start();
        }
    }

    public static void removePreparation(PreparedEvent preparation) {
        preparations.remove(preparation);
    }

    public java.awt.Color color() {
        return java.awt.Color.decode(String.format("#%02x%02x%02x", color[0], color[1], color[2]));
    }

    public void setCurrentEditor(long newMessageId) {
        this.editorId = newMessageId;
    }
}

package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PreparedEvent {
    static List<PreparedEvent> preparations = new ArrayList<>();
    static List<PreparedEvent> forgettablePreparations = new ArrayList<>();
    private String name;
    private String description;
    private String location;
    private long date;
    private String notification;
    private long[] targetMessage;

    private long editorId;
    private int timeUntilForget = 180;
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

    public void setDate(String value) {
        this.date = Long.parseLong(value.replaceAll("[<t:f>]", ""));
    }

    public void setNotification(String value) {
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

    public String getNotification() {
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

    public void modifyEditor(PrivateChannel channel) {
        Message message = channel.retrieveMessageById(editorId).complete();
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
        if (notification != null && !notification.equals("none")) {
            builder.addField(PrepareEmbed.FieldKey.NOTIFICATION.key(), notification, false);
        }
        if (targetMessage != null) {
            builder.addField(PrepareEmbed.FieldKey.EVENT_CHANNEL.key(), "<#" + targetMessage[1] + ">", false);
        }
        message.editMessageEmbeds(builder.build()).queue();
    }

    public void cancel(MessageChannel event) {
        preparations.remove(this);
        event.retrieveMessageById(editorId).complete().delete().queue();
    }

    public void complete() {
        Guild tarGuild = Objects.requireNonNull(Main.jda.getGuildById(targetMessage[0]));
        MessageChannel tarChannel = Objects.requireNonNull(tarGuild.getTextChannelById(targetMessage[1]));
        targetMessage[2] = tarChannel.sendMessageEmbeds(PrepareEmbed.eventCompleted(this)).complete().getIdLong();
        Scheduler.scheduleEvent(new Event(targetMessage[0], targetMessage[1], targetMessage[2], LocalDateTime.ofEpochSecond(date, 0, Main.zoneOffset)));
        preparations.remove(this);
    }

    public int countDown() {
        return timeUntilForget--;
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
                case NOTIFICATION -> preparation.setNotification(field.getValue());
                case EVENT_CHANNEL -> preparation.setEventChannel(field.getValue());
            }
        }
        preparations.add(preparation);
        return preparation;
    }

    /**
     * Starts a thread that removes the preparation after {@value timeUntilForget} seconds to free up memory
     */
    public void forgettable() {
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
}

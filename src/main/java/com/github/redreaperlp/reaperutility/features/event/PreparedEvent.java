package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PreparedEvent {
    static List<PreparedEvent> preparations = new ArrayList<>();
    private String name;
    private String description;
    private String location;
    private long date;
    private String notification;
    private long[] targetMessage;

    private long editorId;


    public PreparedEvent(long editorId) {
        this.editorId = editorId;

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

    public long getDate() {
        return date;
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

    public static PreparedEvent getPreparation(Message message) {
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
        return preparation;
    }

    private void setEventChannel(String value) {
        GuildMessageChannel channel = Objects.requireNonNull(Main.jda.getTextChannelById(value.replaceAll("[<#>]", "")));
        targetMessage = new long[]{
                channel.getGuild().getIdLong(),
                channel.getIdLong(),
                0
        };
    }

    public void complete() {
        Guild tarGuild = Objects.requireNonNull(Main.jda.getGuildById(targetMessage[0]));
        MessageChannel tarChannel = Objects.requireNonNull(tarGuild.getTextChannelById(targetMessage[1]));
        targetMessage[2] = tarChannel.sendMessageEmbeds(PrepareEmbed.eventCompleted(this)).complete().getIdLong();
        Scheduler.scheduleEvent(new Event(targetMessage[0], targetMessage[1], targetMessage[2], date));
        preparations.remove(this);
    }
}

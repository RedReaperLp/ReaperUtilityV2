package com.github.redreaperlp.reaperutility.features.event;

public class Event {
    private long guildId;
    private long channelId;
    private long messageId;
    private long timestamp;

    public Event(long guildId, long channelId, long messageId, long timestamp) {
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
    }
}

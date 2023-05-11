package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.ArrayList;
import java.util.List;

public class PreparedEvent {
    static List<PreparedEvent> preparations = new ArrayList<>();
    private String name;
    private String description;
    private String location;
    private String date;

    private long targetGuildId;
    private long targetChannelId;
    private long targetMessageId;


    public PreparedEvent(long targetGuildId, long targetChannelId, long targetMessageId) {
        this.targetChannelId = targetChannelId;
        this.targetMessageId = targetMessageId;
        this.targetGuildId = targetGuildId;
    }


    public static PreparedEvent initiate(Message message) {
        PreparedEvent event = new PreparedEvent(message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong());
        preparations.add(event);
        return event;
    }
}

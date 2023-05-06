package com.github.redreaperlp.reaperutility.features;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class PrepareEmbed {
    public static MessageEmbed eventSetup(MessageChannel channel) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        ZoneId zone = ZoneId.of("Europe/Berlin");
        ZonedDateTime zonedDateTime = currentDateTime.atZone(zone);
        Instant instant = zonedDateTime.toInstant().plusSeconds(60 * 10);

        return new EmbedBuilder()
                .setTitle("Event Setup", "https://discord.gg/ghhKXDGQhD")
                .addField(FieldKey.NAME.key, "The name of the event", false)
                .addField(FieldKey.DESCRIPTION.key, "The description of the event", false)
                .addField(FieldKey.LOCATION.key, "The location of the event", false)
                .addField(FieldKey.DATE.key, "<t:" + (instant.toEpochMilli() / 1000) + ":f>", false)
                .addField(FieldKey.NOTIFICATION.key, "@everyone", false)
                .addField(FieldKey.REMAINING.key, "<t:" + (instant.toEpochMilli() / 1000) + ":R>", false)
                .addField(FieldKey.EVENT_CHANNEL.key, channel.getAsMention(), false)
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910078680895519/Event_Icon.png")
                .setFooter("Stuck? Click on the ❓ button to get help!")
                .setColor(0x00ff00)
                .setTimestamp(Instant.now())
                .build();
    }

    public static List<ActionRow> eventSetupActionRow() {
        return List.of(
                ActionRow.of(
                        LButtonHandler.ButtonKey.COMPLETE.getButton(),
                        LButtonHandler.ButtonKey.CANCEL.getButton(),
                        LButtonHandler.ButtonKey.HELP.getButton()
                ),
                ActionRow.of(
                        StringSelectMenu.create(LSelectionHandler.SelectionKey.EVENT_SETUP.id())
                                .addOptions(LSelectionHandler.SelectionKey.EVENT_SETUP.options())
                                .setPlaceholder("Select a field to edit")
                                .build()
                )
        );
    }

    public enum FieldKey {
        NAME("Name"),
        DESCRIPTION("Description"),
        LOCATION("Location"),
        DATE("Date"),
        NOTIFICATION("Notification"),
        REMAINING("Remaining"),
        EVENT_CHANNEL("Event Channel"),
        ;

        private final String key;

        FieldKey(String key) {
            this.key = key;
        }
    }
}

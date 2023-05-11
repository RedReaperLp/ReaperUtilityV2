package com.github.redreaperlp.reaperutility.features;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.features.handler.LButtonHandler;
import com.github.redreaperlp.reaperutility.features.handler.LSelectionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

public class PrepareEmbed {
    public static MessageEmbed eventSetup(MessageChannel channel) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        ZoneId zone = ZoneId.of("Europe/Berlin");
        ZonedDateTime zonedDateTime = currentDateTime.atZone(zone);
        Instant instant = zonedDateTime.toInstant().plusSeconds(60 * 10);

        return new EmbedBuilder()
                .setTitle("Event Setup", "https://discord.gg/ghhKXDGQhD")
                .addField(FieldKey.NAME.key, "Events Name", false)
                .addField(FieldKey.DESCRIPTION.key, "Your Description", false)
                .addField(FieldKey.LOCATION.key, channel.getAsMention(), false)
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

    public static MessageEmbed unknownCommandEmbed(SlashCommandInteractionEvent event) {
        return new EmbedBuilder()
                .setTitle("Unknown Command")
                .setDescription("The command you entered is unknown! Please report this to the developer [here](https://discord.gg/ghhKXDGQhD)!")
                .setTimestamp(Instant.now())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909806348939365/Error.png")
                .setAuthor(event.getUser().getName(), "https://discord.gg/ghhKXDGQhD", event.getUser().getEffectiveAvatarUrl())
                .setColor(0xff0000)
                .setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .build();
    }

    public static MessageEmbed noMessagesToClear() {
        return new EmbedBuilder()
                .setTitle("No Messages")
                .setColor(0xffff00)
                .setDescription("There are no messages I can clear!")
                .setTimestamp(Instant.now())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909806348939365/Error.png")
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .build();
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

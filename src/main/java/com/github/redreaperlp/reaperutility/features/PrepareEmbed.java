package com.github.redreaperlp.reaperutility.features;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.features.event.Event;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import com.github.redreaperlp.reaperutility.features.handler.LButtonHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class PrepareEmbed {
    public static MessageEmbed eventSetup(MessageChannel channel) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = currentDateTime.atZone(Main.zoneId);
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

    public static MessageEmbed eventEdit(PreparedEvent event) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Event Setup", "https://discord.com/channels/" + event.getTargetMessage()[0] + "/" + event.getTargetMessage()[1] + "/" + event.getTargetMessage()[2]);
        builder.addField(FieldKey.NAME.key, event.getName(), false);
        if (event.getDescription() != null) {
            builder.addField(FieldKey.DESCRIPTION.key, event.getDescription(), false);
        }
        if (event.getLocation() != null) {
            builder.addField(FieldKey.LOCATION.key, event.getLocation(), false);
        }
        builder.addField(FieldKey.DATE.key, "<t:" + (event.getDateAsEpoch()) + ":f>", false)
                .addField(FieldKey.REMAINING.key, "<t:" + (event.getDateAsEpoch()) + ":R>", false)
                .setColor(event.color())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910078680895519/Event_Icon.png")
                .setFooter("Stuck? Click on the ❓ button to get help!")
                .setTimestamp(Instant.now());
        return builder.build();
    }

    public static MessageEmbed eventCompleted(PreparedEvent event) {
        EmbedBuilder builder = addFields(event);
        builder.addField(FieldKey.ACCEPTED.key, "-", true)
                .addField(FieldKey.DECLINED.key, "-", true)
                .addField(FieldKey.UNSURE.key, "-", true)
                .setColor(event.color())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910078680895519/Event_Icon.png");
        return builder.build();
    }

    public static MessageEmbed eventCompleted(PreparedEvent preparedEvent, Event event) {
        EmbedBuilder builder = addFields(preparedEvent);
        builder.addField(FieldKey.ACCEPTED.key, event.getAcceptedString(), true)
                .addField(FieldKey.DECLINED.key, event.getDeclinedString(), true)
                .addField(FieldKey.UNSURE.key, event.getUndecidedString(), true)
                .setColor(preparedEvent.color())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910078680895519/Event_Icon.png");
        return builder.build();
    }

    private static EmbedBuilder addFields(PreparedEvent preparedEvent) {
        EmbedBuilder builder = new EmbedBuilder().setTitle(preparedEvent.getName());
        if (preparedEvent.getDescription() != null && !preparedEvent.getDescription().equalsIgnoreCase("your description")) {
            builder.setDescription(preparedEvent.getDescription());
        }
        if (preparedEvent.getLocation() != null) {
            builder.addField(FieldKey.LOCATION.key, preparedEvent.getLocation(), false);
        }
        builder.addField(FieldKey.DATE.key, "<t:" + (preparedEvent.getDateAsEpoch()) + ":f>", false)
                .addField(FieldKey.REMAINING.key, "<t:" + (preparedEvent.getDateAsEpoch()) + ":R>", false);
        return builder;
    }

    public static ActionRow eventHelpActionRow(boolean prevEnabled, boolean nextEnabled) {
        return ActionRow.of(
                LButtonHandler.ButtonKey.EVENT_HELP_PREV.getButton().withDisabled(!prevEnabled),
                LButtonHandler.ButtonKey.EVENT_HELP_NEXT.getButton().withDisabled(!nextEnabled),
                LButtonHandler.ButtonKey.DELETE_MESSAGE.getButton()
        );
    }

    public static List<ActionRow> eventSetupActionRow(boolean completeEnabled) {
        return List.of(
                ActionRow.of(
                        LButtonHandler.ButtonKey.COMPLETE.getButton().withDisabled(!completeEnabled),
                        LButtonHandler.ButtonKey.CANCEL.getButton(),
                        LButtonHandler.ButtonKey.SELECT.getButton()
                ),
                ActionRow.of(
                        LButtonHandler.ButtonKey.ENTER_INFOS.getButton(),
                        LButtonHandler.ButtonKey.EVENT_HELP.getButton()
                )
        );
    }

    public static List<ActionRow> eventEditActionRow(boolean completeEnabled) {
        return List.of(
                ActionRow.of(
                        LButtonHandler.ButtonKey.COMPLETE.getButton().withDisabled(!completeEnabled),
                        LButtonHandler.ButtonKey.CANCEL.getButton(),
                        LButtonHandler.ButtonKey.SELECT.getButton()
                ),
                ActionRow.of(
                        LButtonHandler.ButtonKey.ENTER_INFOS.getButton(),
                        LButtonHandler.ButtonKey.EVENT_HELP_EDIT.getButton()
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

    public static List<ActionRow> eventActionRow() {
        return List.of(
                ActionRow.of(
                        LButtonHandler.ButtonKey.EVENT_ACCEPT.getButton(),
                        LButtonHandler.ButtonKey.EVENT_DECLINE.getButton(),
                        LButtonHandler.ButtonKey.EVENT_UNSURE.getButton()
                ),
                ActionRow.of(
                        LButtonHandler.ButtonKey.EVENT_EDIT.getButton(),
                        LButtonHandler.ButtonKey.EVENT_DELETE.getButton()
                ));
    }

    public static enum FieldKey {
        NAME("Name"),
        DESCRIPTION("Description"),
        LOCATION("Location"),
        DATE("Date"),
        NOTIFICATION("Notification"),
        REMAINING("Remaining"),
        EVENT_CHANNEL("Event Channel"),
        ACCEPTED("Accepted"),
        DECLINED("Declined"),
        UNSURE("Unsure"),
        UNKNOWN("Unknown");

        public final String key;

        FieldKey(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }

        public static FieldKey fromKey(String key) {
            for (FieldKey fieldKey : values()) {
                if (fieldKey.key.equals(key)) {
                    return fieldKey;
                }
            }
            return UNKNOWN;
        }
    }
}

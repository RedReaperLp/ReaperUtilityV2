package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.RUser;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class LButtonHandler extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        ButtonKey key = ButtonKey.getByName(event.getButton().getId());
        switch (key) {
            case COMPLETE -> {
                PreparedEvent prepEvent = PreparedEvent.hasPreparation(event.getMessage());
                if (prepEvent.completePossible()) {
                    prepEvent.complete();
                    event.deferEdit().queue();
                } else {
                    prepEvent.modifyEditor(event.getChannel().asPrivateChannel());
                    event.replyEmbeds(new EmbedBuilder().setTitle("Wrong Information")
                            .addField("Date", "You have to provide a ***valid*** date which is ***not in the past***!", false)
                            .build()
                    ).queue();
                }
            }
            case SELECT -> {
                PreparedEvent prepEvent = PreparedEvent.hasPreparation(event.getMessage());
                RUser rUser = RUser.getUser(event.getUser().getIdLong());
                if (!rUser.getRateLimit().addIsRateLimited(3)) {
                    if (rUser.getCurrentEditor() != null && rUser.getCurrentEditor().getEditorId() == prepEvent.getEditorId()) {
                        event.reply("You are already editing this event!").queue();
                        return;
                    }
                    rUser.setCurrentEditor(prepEvent);
                    event.deferEdit().queue();
                } else {
                    event.reply("You are rate limited for " + rUser.getRateLimit().getDiscordFormattedRemaining()).queue();
                }
            }
            case CANCEL -> {
                PreparedEvent prepEvent = PreparedEvent.hasPreparation(event.getMessage());
                prepEvent.cancel(event.getChannel());
                RUser rUser = RUser.getUser(event.getUser().getIdLong());
                rUser.setCurrentEditor(null);
                try {
                    event.deferEdit().complete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case ENTER_INFOS -> {
                RUser rUser = RUser.getUser(event.getUser().getIdLong());
                PreparedEvent prepEvent = PreparedEvent.hasPreparation(event.getMessage());
                if (rUser.getCurrentEditor() == null || !rUser.getCurrentEditor().equals(prepEvent)) {
                    rUser.setCurrentEditor(prepEvent);
                }
                event.replyModal(LModalHandler.ModalKey.EVENT_SETUP.build(prepEvent)).queue();
            }

            case EVENT_HELP_1 -> {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Event Help")
                        .setDescription("This is the help page for the event setup")
                        .addField(MarkdownUtil.codeblock("arm","Step 1"), "Click on the ***Enter Infos*** button to enter your information", false)
                        .addField("```Name```", "Enter the name of the event, this is required to complete the setup", false)
                        .addField("```Description```", "Enter the description of the event, this is optional, to remove the description, just enter ***none***, ***empty*** or ***remove***", false)
                        .addField("```Date```", "Enter the date of the event in the format YYYY-MM-DD HH:MM for example ___***2023-01-01 18:05***___, this is required to complete the setup", false)
                        .addField("```Location```", "Enter the location of the event, this is optional, to remove the location, just enter ***none***, ***empty*** or ***remove***.\n" +
                                "If you want to have a channel as location, you have to put it in brackets. Example: {1086673451777007636} or {myChannelName}", false)
                        .addField("```Color```", "Here you can select the color of the events embed border, this is optional, default is green.\nIf you want to set a new color, you have to enter RGB values, for example 255 0 0 for red", false)
                        .addField("```Step 2```", "Click on the ***Complete*** button to complete the setup", false)
                        .addField("```Conditions```", "You have to provide a ***valid*** date which is ***not in the past***!\nYou have to provide a Name", false)
                        .build();
                event.replyEmbeds(embed).queue();
            }
        }
    }

    public enum ButtonKey {
        COMPLETE_EDIT("Complete", "event.complete.edit", ButtonStyle.SUCCESS),
        COMPLETE("Complete", "event.complete", ButtonStyle.SUCCESS),
        CANCEL("Cancel", "event.cancel", ButtonStyle.DANGER),
        EVENT_HELP_1("❓", "event.help.1", ButtonStyle.SECONDARY),
        SELECT("Select", "event.select", ButtonStyle.PRIMARY),
        ENTER_INFOS("Enter Infos", "event.enter_infos", ButtonStyle.SUCCESS),
        UNKNOWN("UNKNOWN", "UNKNOWN", ButtonStyle.UNKNOWN);

        private final String label;
        private final String id;
        private final ButtonStyle style;
        private String link;

        ButtonKey(String label, String id, ButtonStyle style) {
            this(label, id, style, "");
        }

        ButtonKey(String label, String id, ButtonStyle style, String link) {
            this.label = label;
            this.id = id;
            this.style = style;
            this.link = link;
        }

        public Button getButton() {
            if (style == ButtonStyle.LINK)
                return Button.link(link, label);
            return Button.of(style, id, label);
        }

        public static ButtonKey getByName(String id) {
            for (ButtonKey button : values()) {
                if (button.id.equals(id)) {
                    return button;
                }
            }
            return UNKNOWN;
        }
    }
}

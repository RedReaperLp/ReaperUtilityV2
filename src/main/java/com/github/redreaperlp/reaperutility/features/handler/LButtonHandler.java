package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.RUser;
import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

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
                    Message message = event.getMessage();
                    event.editMessage(prepEvent.modifyEditor(event.getChannel().asPrivateChannel(), message)).queue();
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

            case EVENT_HELP -> {
                event.replyEmbeds(Help.EVENT_HELP.getPage(0)).addComponents(PrepareEmbed.eventHelpActionRow(false, true)).queue();
            }
            case EVENT_HELP_NEXT -> {
                MessageEmbed embed = event.getMessage().getEmbeds().get(0);
                int page = Integer.parseInt(embed.getFooter().getText().split(" ")[1]);
                MessageEditBuilder builder = MessageEditBuilder.fromMessage(event.getMessage());

                embed = Help.EVENT_HELP.getPage(page);

                builder.setEmbeds(embed).setComponents(PrepareEmbed.eventHelpActionRow(page > 0, page < Help.EVENT_HELP.pages() - 1));
                event.editMessage(builder.build()).queue();
            }
            case EVENT_HELP_PREV -> {
                MessageEmbed embed = event.getMessage().getEmbeds().get(0);
                int page = Integer.parseInt(embed.getFooter().getText().split(" ")[1]) - 2;
                MessageEditBuilder builder = MessageEditBuilder.fromMessage(event.getMessage());

                embed = Help.EVENT_HELP.getPage(page);

                builder.setEmbeds(embed).setComponents(PrepareEmbed.eventHelpActionRow(page > 0, page < Help.EVENT_HELP.pages() - 1));
                event.editMessage(builder.build()).queue();
            }
        }
    }

    public enum ButtonKey {
        COMPLETE_EDIT("Complete", "event.complete.edit", ButtonStyle.SUCCESS),
        COMPLETE("Complete", "event.complete", ButtonStyle.SUCCESS),
        CANCEL("Cancel", "event.cancel", ButtonStyle.DANGER),
        SELECT("Select", "event.select", ButtonStyle.PRIMARY),
        ENTER_INFOS("Enter Infos", "event.enter_infos", ButtonStyle.SUCCESS),
        EVENT_HELP("❓", "event.help", ButtonStyle.SECONDARY),
        EVENT_HELP_NEXT("Next Page", "event.help.next", ButtonStyle.SUCCESS),
        EVENT_HELP_PREV("Previous Page", "event.help.prev", ButtonStyle.PRIMARY),
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

    public enum Help {
        EVENT_HELP(
                new EmbedBuilder()
                        .setTitle("Event Help")
                        .setDescription("___***Step 1***___\nBefore clicking the mysterious-looking ***Enter Infos*** button to begin input, take a moment to explore this comprehensive help guide")
                        .addField("___***```Name```***___", "> Get your creative juices flowing and enter the event name in the Modal! This step is ___***absolutely required***___ to finalize the setup", false)
                        .addField("___***```Description```***___", "> Let your event shine with a captivating description, but remember, it's totally ___***optional***___! If you want to remove the description, simply enter ***none***, ***empty***, or ***remove***.", false)
                        .addField("___***```Date```***___", "> Time to mark your calendars! Enter the date of the event in the format ***YYYY-MM-DD HH:MM***, just like ***2023-01-01 18:05***. Remember, this step is ___***required***___ to complete the setup.", false)
                        .addField("___***```Location```***___", "> Time to pinpoint the event location!\n" +
                                "> You can enter the location of the event, but it's ***optional***.\n" +
                                "> If you want to remove the location, simply enter ***none***, ***empty***, or ***remove***.\n" +
                                "> And here's an exciting twist: if you want to set a channel as the location, enclose its ***ID*** or ***name*** in brackets. Don't worry, you only need to type a few letters or numbers to find a matching channel.\n" +
                                "> For example, you can use ***{1086673451777007636}*** or ***{myChannelName}*** to make your event stand out", false)
                        .addField("___***```Color```***___", "> Let your event shine with a touch of color! Customize the embed border by entering RGB values. The default color is green, but feel free to unleash your creativity. For example, enter ***255 0 0*** for a vibrant red hue.", false)
                        .setFooter("Page 1")
                        .setColor(0xffff00).build(),
                new EmbedBuilder()
                        .setTitle("Event Help")
                        .setDescription("___***Step 2***___\nSurely you've noticed that certain options were missing from the previously introduced modal. These additional options are specifically associated with the following actions")
                        .addField("___***```Notification```***___", "> Not done Yet", false)
                        .addField("___***```Event Channel```***___", "> Unlock the power of event channels!\n" +
                                "> To select a channel for your event, simply follow these steps:\n" +
                                "> Go to the desired channel, ___right-click on any user___, navigate to the ***Apps*** section, and click on ***Select Event Channel***. \n" +
                                "> Please note that this feature is ***only*** available if you have previously selected the event and is ***limited to the guild where you created the event***.", false)
                        .addField("___***```But Wait```***___", "> Let's dive deeper into the concept of selecting an Event Setup.\n" +
                                "> When you click on ***Enter Infos*** or manually select it by clicking on ***Select***, the bot registers your choice and understands that you are referring to that particular setup. This is important for utilizing interactions like Slash Commands or using the Apps.\n" +
                                "> It's worth noting that the selection remains active for about 4 minutes, after which it automatically unselects itself. So, make sure to complete your desired actions within that timeframe!", false)
                        .setFooter("Page 2")
                        .setColor(0xffff00).build()),
        ;

        private final MessageEmbed[] pages;

        Help(MessageEmbed... pages) {
            this.pages = pages;
        }

        public MessageEmbed getPage(int page) {
            return pages[page];
        }

        public int pages() {
            return pages.length;
        }
    }
}

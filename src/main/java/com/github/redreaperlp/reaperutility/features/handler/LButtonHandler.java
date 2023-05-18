package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.RUser;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class LButtonHandler extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        ButtonKey key = ButtonKey.getByName(event.getButton().getId());
        switch (key) {
            case COMPLETE, UNKNOWN, HELP -> {
                PreparedEvent prepEvent = PreparedEvent.hasPreparation(event.getMessage());
                prepEvent.complete();
                event.deferEdit().queue();
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
        }
    }

    public enum ButtonKey {
        COMPLETE_EDIT("Complete", "event.complete.edit", ButtonStyle.SUCCESS),
        COMPLETE("Complete", "event.complete", ButtonStyle.SUCCESS),
        CANCEL("Cancel", "event.cancel", ButtonStyle.DANGER),
        HELP("❓", "event.help", ButtonStyle.SECONDARY),
        SELECT("Select", "event.select", ButtonStyle.PRIMARY),
        UNKNOWN("UNKNOWN", "UNKNOWN", ButtonStyle.UNKNOWN);

        private final String label;
        private final String id;
        private final ButtonStyle style;

        ButtonKey(String label, String id, ButtonStyle style) {
            this.label = label;
            this.id = id;
            this.style = style;
        }

        public Button getButton() {
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

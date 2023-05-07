package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.util.Color;
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
                event.reply("This feature is not yet implemented!").complete();
            }
            case CANCEL -> {
                Color.printTest("Button pressed");
                event.getMessage().delete().complete();
                event.deferEdit().complete();
            }
        }
    }

    public enum ButtonKey {
        COMPLETE("Complete", "complete.event", ButtonStyle.SUCCESS),
        CANCEL("Cancel", "cancel.event", ButtonStyle.DANGER),
        HELP("❓", "help.event", ButtonStyle.SECONDARY),
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

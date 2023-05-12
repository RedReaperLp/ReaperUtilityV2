package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.User;
import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import com.github.redreaperlp.reaperutility.util.Color;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
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
                PreparedEvent prepEvent = PreparedEvent.getPreparation(event.getMessage());
                prepEvent.complete();
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(prepEvent));
                event.reply("This feature is not yet implemented!").complete();
            }
            case SELECT -> {
                PreparedEvent prepEvent = PreparedEvent.getPreparation(event.getMessage());
                User user = User.getUser(event.getUser().getIdLong());
                user.setCurrentEditor(prepEvent);
                event.deferEdit().queue();
            }
            case CANCEL -> {
                Color.printTest("Button pressed");
                event.getMessage().delete().complete();
                event.deferEdit().complete();
            }
        }
    }

    public enum ButtonKey {
        COMPLETE_EDIT("Complete", "event.complete.edit", ButtonStyle.SUCCESS),
        COMPLETE("Complete", "event.complete", ButtonStyle.SUCCESS),
        CANCEL("Cancel", "event.cancel", ButtonStyle.DANGER),
        HELP("❓", "event.help", ButtonStyle.SECONDARY),
        SELECT("Select","event.select" , ButtonStyle.PRIMARY),
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

package com.github.redreaperlp.reaperutility.features.handler;

import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class LSelectionHandler extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        event.deferEdit().queue();
    }

    public enum SelectionKey {
        EVENT_SETUP("event.setup", SelectOption.of("1️⃣Name", SelectionValue.NAME.id),
                SelectOption.of("2️⃣Description", SelectionValue.DESCRIPTION.id),
                SelectOption.of("3️⃣Date", SelectionValue.DATE.id),
                SelectOption.of("4️⃣Location", SelectionValue.LOCATION.id),
                SelectOption.of("5️⃣Color", SelectionValue.COLOR.id)),
        EVENT_EDIT("event.edit", SelectOption.of("1️⃣Name", SelectionValue.NAME.id),
                SelectOption.of("2️⃣Description", SelectionValue.DESCRIPTION.id),
                SelectOption.of("3️⃣Date", SelectionValue.DATE.id),
                SelectOption.of("4️⃣Location", SelectionValue.LOCATION.id),
                SelectOption.of("5️⃣Color", SelectionValue.COLOR.id)),
        ;

        private final String id;
        private final SelectOption[] options;

        SelectionKey(String id, SelectOption... options) {
            this.id = id;
            this.options = options;
        }

        public String id() {
            return id;
        }

        public SelectOption[] options() {
            return options;
        }
    }

    public enum SelectionValue {
        NAME("name"),
        DESCRIPTION("description"),
        DATE("date"),
        LOCATION("location"),
        COLOR("color");

        private final String id;

        SelectionValue(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}

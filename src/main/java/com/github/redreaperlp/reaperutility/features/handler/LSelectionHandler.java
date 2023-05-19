package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.RUser;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class LSelectionHandler extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        event.deferEdit().queue();
    }

    public enum SelectionKey {
        UNKNOWN("unknown")
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

        public static SelectionKey getById(String id) {
            for (SelectionKey key : values()) {
                if (key.id().equals(id)) {
                    return key;
                }
            }
            return UNKNOWN;
        }
    }

    public enum SelectionValue {
        NAME("event.name"),
        DESCRIPTION("event.description"),
        DATE("event.date"),
        LOCATION("event.location"),
        COLOR("event.color"),
        UNKNOWN("unknown");

        private final String id;

        SelectionValue(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public static SelectionValue getById(String id) {
            for (SelectionValue value : values()) {
                if (value.id().equals(id)) {
                    return value;
                }
            }
            return UNKNOWN;
        }
    }
}

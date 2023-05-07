package com.github.redreaperlp.reaperutility.features;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LModalHandler extends ListenerAdapter {
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        event.deferEdit().queue();
    }
}

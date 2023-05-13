package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.RUser;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static com.github.redreaperlp.reaperutility.features.handler.LCommandHandler.ECommands.EGuildCommands;

public class LUserContextHandler extends ListenerAdapter {
    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        EGuildCommands command = EGuildCommands.getByName(event.getName());
        switch (command) {
            case SELECT_EVENT_CHANNEL -> {
                event.deferReply().setEphemeral(true).queue();
                RUser rUser = RUser.getUser(event.getUser().getIdLong());
                if (rUser.getCurrentEditor() == null) {
                    event.getHook().sendMessage("You are not editing an event, please select an event first!").queue();
                    return;
                }
                if (rUser.getCurrentEditor().getTargetMessage()[0] != event.getGuild().getIdLong()) {
                    event.getHook().sendMessage("You are not editing an event in this guild, please select an event from that guild first!").queue();
                    return;
                }
                rUser.getCurrentEditor().setEventChannel(event.getGuild().getIdLong(), event.getChannel().getIdLong());
                rUser.getCurrentEditor().modifyEditor(event.getUser().openPrivateChannel().complete());
                event.getHook().sendMessage("Selected this channel as event channel").queue();
            }
        }
    }
}

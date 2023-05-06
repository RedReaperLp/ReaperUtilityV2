package com.github.redreaperlp.reaperutility.features;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LCommandHandler extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.isGlobalCommand()) {
            ECommands.EGlobalCommands command = ECommands.EGlobalCommands.getByName(event.getName());
            switch (command) {
                case CLEAR -> {
                    clear(event);
                }
                case UNKNOWN -> {
                    event.reply("This command is not yet implemented!").queue();
                }
            }
        } else {
            ECommands.EGuildCommands command = ECommands.EGuildCommands.getByName(event.getName());
            switch (command) {
                case EVENT -> {
                    event(event);
                }
                case UNKNOWN -> {
                    event.reply("This command is not yet implemented!").queue();
                }
            }
        }
    }

    private void clear(SlashCommandInteractionEvent event) {
        event.reply("This feature is not yet implemented!").queue();
    }

    private void event(SlashCommandInteractionEvent event) {
        PrivateChannel channel = event.getUser().openPrivateChannel().complete();
        Message message = channel.sendMessageEmbeds(PrepareEmbed.eventSetup(event.getChannel())).addComponents(PrepareEmbed.eventSetupActionRow()).complete();
        event.reply("I have sent you a private [message](" + message.getJumpUrl() + ") to configure your event!").setEphemeral(true).queue();
    }
}

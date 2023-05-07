package com.github.redreaperlp.reaperutility.features;

import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

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
                    event.replyEmbeds(PrepareEmbed.unknownCommandEmbed(event)).queue();
                }
            }
        } else {
            ECommands.EGuildCommands command = ECommands.EGuildCommands.getByName(event.getName());
            switch (command) {
                case EVENT -> {
                    event(event);
                }
                case UNKNOWN -> {
                    event.replyEmbeds(PrepareEmbed.unknownCommandEmbed(event)).queue();
                }
            }
        }
    }

    private void clear(SlashCommandInteractionEvent event) {
        List<OptionMapping> options = event.getOptions();

        for (OptionMapping option : options) {
            Color.printTest(option.getName() + " " + option.getAsString());
        }

        event.reply("Clearing messages...").queue();
//        event.getChannel().getHistory().retrievePast()
    }

    private void event(SlashCommandInteractionEvent event) {
        PrivateChannel channel = event.getUser().openPrivateChannel().complete();
        Message message = channel.sendMessageEmbeds(PrepareEmbed.eventSetup(event.getChannel())).addComponents(PrepareEmbed.eventSetupActionRow()).complete();
        event.reply("I have sent you a private [message](" + message.getJumpUrl() + ") to configure your event!").setEphemeral(true).queue();
    }
}

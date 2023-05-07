package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

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

    public class ECommands {
        public enum EGlobalCommands {
            CLEAR("clear", "Clears up to 100 messages at once",
                    new CommandOption(OptionType.INTEGER, "amount", "The amount of messages to be cleared", true, false),
                    new ECommands.CommandOption(OptionType.USER, "user", "The user whose messages should be cleared"),
                    new ECommands.CommandOption(OptionType.ROLE, "role", "The role whose messages should be cleared")),
            UNKNOWN(null, null);


            private final String command;
            private final String description;
            private final ECommands.CommandOption[] options;

            EGlobalCommands(String command, String description, ECommands.CommandOption... options) {
                this.command = command;
                this.description = description;
                this.options = options;
            }

            public static ECommands.EGlobalCommands getByName(String name) {
                for (ECommands.EGlobalCommands value : values()) {
                    if (value.command.equals(name)) {
                        return value;
                    }
                }
                return UNKNOWN;
            }

            public SlashCommandData getCommand() {
                SlashCommandData data = Commands.slash(command, description);
                for (ECommands.CommandOption option : options) {
                    data.addOption(option.type, option.name, option.description, option.required, option.autocomplete);
                }
                return data;
            }
        }

        public enum EGuildCommands {
            EVENT("event", "You will receive a private message to configure and schedule an event"),
            UNKNOWN(null, null);
            private final String command;
            private final String description;
            private final ECommands.CommandOption[] options;

            EGuildCommands(String command, String description, ECommands.CommandOption... options) {
                this.command = command;
                this.description = description;
                this.options = options;
            }

            public SlashCommandData getCommand() {
                SlashCommandData data = Commands.slash(command, description);
                for (ECommands.CommandOption option : options) {
                    data.addOption(option.type, option.name, option.description, option.required, option.autocomplete);
                }
                return data;
            }

            public static ECommands.EGuildCommands getByName(String name) {
                for (ECommands.EGuildCommands value : values()) {
                    if (value.command.equals(name)) {
                        return value;
                    }
                }
                return UNKNOWN;
            }
        }

        public static class CommandOption {
            private final OptionType type;
            private final String name;
            private final String description;
            private final boolean required;
            private final boolean autocomplete;

            public CommandOption(OptionType type, String name, String description, boolean required, boolean autocomplete) {
                this.type = type;
                this.name = name;
                this.description = description;
                this.required = required;
                this.autocomplete = autocomplete;
            }

            public CommandOption(OptionType type, String name, String description) {
                this(type, name, description, false, false);
            }
        }
    }

}

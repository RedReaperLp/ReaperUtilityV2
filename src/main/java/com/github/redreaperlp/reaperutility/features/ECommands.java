package com.github.redreaperlp.reaperutility.features;

import kotlin.UNINITIALIZED_VALUE;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ECommands {
    public enum EGlobalCommands {
        CLEAR("clear", "Clears up to 100 messages at once",
                new CommandOption(OptionType.INTEGER, "amount", "The amount of messages to be cleared", true, false),
                new CommandOption(OptionType.USER, "user", "The user whose messages should be cleared", false, false),
                new CommandOption(OptionType.ROLE, "role", "The role whose messages should be cleared", false, false)),
        UNKNOWN(null, null);


        private final String command;
        private final String description;
        private final CommandOption[] options;

        EGlobalCommands(String command, String description, CommandOption... options) {
            this.command = command;
            this.description = description;
            this.options = options;
        }

        public static EGlobalCommands getByName(String name) {
            for (EGlobalCommands value : values()) {
                if (value.command.equals(name)) {
                    return value;
                }
            }
            return UNKNOWN;
        }

        public SlashCommandData getCommand() {
            SlashCommandData data = Commands.slash(command, description);
            for (CommandOption option : options) {
                data.addOption(option.type, option.name, option.description, option.required);
            }
            return data;
        }
    }

    public enum EGuildCommands {
        EVENT("event", "You will receive a private message to configure and schedule an event"),
        UNKNOWN(null, null);
        ;
        private final String command;
        private final String description;
        private final CommandOption[] options;

        EGuildCommands(String command, String description, CommandOption... options) {
            this.command = command;
            this.description = description;
            this.options = options;
        }

        public SlashCommandData getCommand() {
            SlashCommandData data = Commands.slash(command, description);
            for (CommandOption option : options) {
                data.addOption(option.type, option.name, option.description, option.required);
            }
            return data;
        }

        public static EGuildCommands getByName(String name) {
            for (EGuildCommands value : values()) {
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

package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.RUser;
import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import com.github.redreaperlp.reaperutility.features.event.Scheduler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.redreaperlp.reaperutility.features.handler.LCommandHandler.ECommands.EGlobalCommands;
import static com.github.redreaperlp.reaperutility.features.handler.LCommandHandler.ECommands.EGuildCommands;

public class LCommandHandler extends ListenerAdapter {
//    @Override
//    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
//        event.getGuild().kickVoiceMember(event.getMember()).queue();
//    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.isGlobalCommand()) {
            EGlobalCommands command = EGlobalCommands.getByName(event.getName());
            switch (command) {
                case CLEAR -> clear(event);
                case SELECT_ROLE_NOTIFY -> selectRoleNotify(event);
                case UNKNOWN -> event.replyEmbeds(PrepareEmbed.unknownCommandEmbed(event)).queue();
            }
        } else {
            EGuildCommands command = EGuildCommands.getByName(event.getName());
            switch (command) {
                case EVENT -> event(event);
                case UNKNOWN -> event.replyEmbeds(PrepareEmbed.unknownCommandEmbed(event)).queue();
            }
        }
    }

    private void selectRoleNotify(SlashCommandInteractionEvent event) {
        RUser rUser = RUser.getUser(event.getUser().getIdLong());
        PreparedEvent preparedEvent = rUser.getCurrentEditor();
        if (preparedEvent == null) {
            event.reply("You are not editing an event").setEphemeral(true).queue();
            return;
        }
        try {
            long roleId = event.getOption("role").getAsLong();
            Role role = Main.jda.getGuildById(preparedEvent.getTargetMessage()[0]).getRoleById(roleId);
            if (role == null) {
                event.reply("Role not found").setEphemeral(true).queue();
                return;
            }
            boolean added = preparedEvent.addRole(role.getName());
            Message message = event.getUser().openPrivateChannel().complete().retrieveMessageById(preparedEvent.getEditorId()).complete();
            message.editMessage(preparedEvent.modifyEditor(message)).queue();
            event.reply("Role " + role.getName() + (added ? " added" : " removed")).setEphemeral(true).queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.reply("Role not found").setEphemeral(true).queue();
        }
    }

    List<Long> clearingChannels = new ArrayList<>();

    private void clear(SlashCommandInteractionEvent event) {
        if (clearingChannels.contains(event.getChannel().getIdLong())) {
            event.reply("Please wait until the current clear command is finished").setEphemeral(true).queue();
            return;
        }
        RUser rUser = RUser.getUser(event.getUser().getIdLong());
        if (rUser.getRateLimit().addIsRateLimited(8)) {
            event.reply("You are rate limited, please wait " + rUser.getRateLimit().getDiscordFormattedRemaining()).setEphemeral(true).queue();
            return;
        }
        clearingChannels.add(event.getChannel().getIdLong());
        event.deferReply().setEphemeral(true).queue();
        new Thread(() -> {
            long timestamp = System.currentTimeMillis();
            int cleared = 0;
            try {
                List<OptionMapping> options = event.getOptions();

                int amount = 0;
                long userId = 0;
                long roleId = 0;

                for (OptionMapping option : options) {
                    if (option.getName().equals("amount")) {
                        amount = option.getAsInt();
                    } else if (option.getName().equals("user")) {
                        userId = option.getAsUser().getIdLong();
                    } else if (option.getName().equals("role")) {
                        roleId = option.getAsRole().getIdLong();
                    }
                }

                if (amount == 0) {
                    event.replyEmbeds(PrepareEmbed.noMessagesToClear()).queue();
                    clearingChannels.remove(event.getChannel().getIdLong());
                    return;
                }
                if (!event.isFromGuild() && amount > 20) {
                    amount = 20;
                }
                if (amount > 100) {
                    amount = 100;
                }

                List<Message> messages = event.getChannel().getHistory().retrievePast(amount).complete();
                if (messages.size() == 0) {
                    event.getHook().sendMessageEmbeds(PrepareEmbed.noMessagesToClear()).queue();
                    clearingChannels.remove(event.getChannel().getIdLong());
                    return;
                }
                List<Message> toDel = new ArrayList<>();
                if (event.isFromGuild()) {
                    for (Message message : messages) {
                        if (message.getAuthor().getIdLong() == Main.jda.getSelfUser().getIdLong() && message.getEmbeds().size() > 0 && Scheduler.hasEvent(message.getIdLong())) {
                            continue;
                        }
                        if (checkMessage(userId, roleId, message, true)) {
                            toDel.add(message);
                        }
                    }
                } else {
                    for (Message message : messages) {
                        if (message.getAuthor().isBot() && !PreparedEvent.hasPreparation(message.getIdLong())) {
                            toDel.add(message);
                        }
                    }
                }

                cleared = toDel.size();
                if (toDel.size() > 0) {
                    List<CompletableFuture<Void>> futures = event.getChannel().purgeMessages(toDel);
                    for (CompletableFuture<Void> future : futures) {
                        future.join();
                    }
                } else {
                    event.getHook().sendMessageEmbeds(PrepareEmbed.noMessagesToClear()).queue();
                    clearingChannels.remove(event.getChannel().getIdLong());
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            clearingChannels.remove(event.getChannel().getIdLong());
            event.getHook().sendMessage("Cleared " + cleared + " messages in " + (System.currentTimeMillis() - timestamp) + "ms").queue();
        }).start();
    }

    public boolean checkMessage(long userId, long roleId, Message message, boolean isFromGuild) {
        if (isFromGuild) {
            if (userId != 0) {
                if (message.getAuthor().getIdLong() != userId) {
                    return false;
                }
            }
            if (roleId != 0) {
                List<Role> roles = message.getMember().getRoles();
                for (Role role : roles) {
                    if (role.getIdLong() == roleId) {
                        return true;
                    }
                }
            }
        } else {
            if (message.getAuthor().isBot()) {
                return false;
            }
        }
        return true;
    }

    private void event(SlashCommandInteractionEvent event) {
        RUser rUser = RUser.getUser(event.getUser().getIdLong());

        event.deferReply().setEphemeral(true).queue();
        PrivateChannel channel = event.getUser().openPrivateChannel().complete();
        Message message = channel.sendMessageEmbeds(PrepareEmbed.eventSetup(event.getChannel())).addComponents(PrepareEmbed.eventSetupActionRow(false)).complete();
        PreparedEvent prep = PreparedEvent.hasPreparation(message);
        event.getHook().sendMessage("I have sent you a private [message](" + message.getJumpUrl() + ") to configure your event!").queue();
        rUser.setCurrentEditor(prep);
    }

    public static class ECommands {
        public enum EGlobalCommands {
            CLEAR("clear", "Clears up to 100 messages at once (In private up to 20)", Command.Type.SLASH,
                    new CommandOption(OptionType.INTEGER, "amount", "The amount of messages to be cleared", true, false),
                    new CommandOption(OptionType.USER, "user", "The user whose messages should be cleared"),
                    new CommandOption(OptionType.ROLE, "role", "The role whose messages should be cleared")),
            SELECT_ROLE_NOTIFY("select-role", "Selects a role to be notified when a new event is created", Command.Type.SLASH,
                    new CommandOption(OptionType.STRING, "role", "The role to be notified", true, true)),
            UNKNOWN(null, null, null);


            private final String command;
            private final String description;
            private final Command.Type type;
            private final CommandOption[] options;

            EGlobalCommands(String command, String description, Command.Type type, CommandOption... options) {
                this.command = command;
                this.description = description;
                this.type = type;
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

            public CommandData getCommand() {
                if (type == Command.Type.SLASH) {
                    SlashCommandData data = Commands.slash(command, description);
                    for (CommandOption option : options) {
                        data.addOption(option.type, option.name, option.description, option.required, option.autocomplete);
                    }
                    return data;
                }
                if (type == Command.Type.USER) {
                    return Commands.context(Command.Type.USER, command).setGuildOnly(false);
                } else {
                    return null;
                }
            }
        }

        public enum EGuildCommands {
            EVENT("event", "You will receive a private message to configure and schedule an event", Command.Type.SLASH),
            SELECT_EVENT_CHANNEL("Select Event Channel", "Selects the channel where the event will be posted", Command.Type.USER),
            UNKNOWN(null, null, null);
            private final String command;
            private final String description;
            private final Command.Type type;
            private final CommandOption[] options;

            EGuildCommands(String command, String description, Command.Type type, CommandOption... options) {
                this.command = command;
                this.description = description;
                this.type = type;
                this.options = options;
            }

            public CommandData getCommand() {
                if (type == Command.Type.SLASH) {
                    SlashCommandData data = Commands.slash(command, description);
                    for (CommandOption option : options) {
                        data.addOption(option.type, option.name, option.description, option.required, option.autocomplete);
                    }
                    return data;
                } else if (type == Command.Type.USER) {
                    return Commands.context(Command.Type.USER, command).setGuildOnly(false);
                } else {
                    return null;
                }
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

}

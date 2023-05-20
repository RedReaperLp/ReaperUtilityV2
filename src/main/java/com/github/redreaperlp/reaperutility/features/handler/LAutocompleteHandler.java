package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.RUser;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.ArrayList;
import java.util.List;

public class LAutocompleteHandler extends ListenerAdapter {
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        RUser rUser = RUser.getUser(event.getUser().getIdLong());
        PreparedEvent preparedEvent = rUser.getCurrentEditor();

        if (preparedEvent == null) {
            event.replyChoice("❌You do not Selected an Event Setup!", "{none}").queue();
            return;
        }

        List<Role> roles = Main.jda.getGuildById(preparedEvent.getTargetMessage()[0]).getRoles();
        List<Command.Choice> toReply = new ArrayList<>();
        boolean everyone = preparedEvent.getNotification().contains("@everyone");
        boolean isRoleById = true;
        long roleId = 0;

        try {
            roleId = event.getOption("role").getAsLong();
        } catch (NumberFormatException e) {
            isRoleById = false;
        }

        String roleName = event.getOption("role").getAsString().toLowerCase();

        for (Role role : roles) {
            if (roles.size() > 24) break;
            boolean isMatchingRole = isRoleById ? role.getId().contains(String.valueOf(roleId)) : role.getName().toLowerCase().contains(roleName);
            String before = everyone ? "✅" : preparedEvent.getNotification().stream()
                    .anyMatch(s -> s.contains(role.getName())) ? "✅" : "❌";
            if (isMatchingRole) {
                toReply.add(new Command.Choice(before + role.getName(), role.getId()));
            }
        }

        event.replyChoices(toReply).queue();
    }
}
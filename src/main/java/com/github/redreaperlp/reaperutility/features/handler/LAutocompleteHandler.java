package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.RUser;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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
            boolean isMatchingRole = isRoleById ? role.getId().contains(String.valueOf(roleId)) : role.getName().toLowerCase().contains(roleName);
            String before = everyone ? "✅" : preparedEvent.getNotification().stream()
                    .anyMatch(s -> s.contains(role.getName())) ? "✅" : "❌";
            if (isMatchingRole) {
                toReply.add(new Command.Choice(before + role.getName(), role.getId()));
            }
        }
        toReply.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        //shortening toReply to 25 entries
        if (toReply.size() > 25) {
            toReply = toReply.subList(0, 25);
        }
        event.replyChoices(toReply).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        try {
            URL url = new URL("https://api.quotable.io/random");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new java.io.InputStreamReader(con.getInputStream()));
            JSONObject responseJson = new JSONObject(in.readLine());
            System.out.println(responseJson.toString(2));
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Quote of the Second");
            embedBuilder.setDescription(responseJson.getString("content"));
            embedBuilder.setFooter(responseJson.getString("author"));
            event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(embedBuilder.build()).complete();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
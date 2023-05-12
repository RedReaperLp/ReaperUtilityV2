package com.github.redreaperlp.reaperutility;

import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

import java.util.ArrayList;
import java.util.List;

public class User {
    private static List<User> users = new ArrayList<>();

    private long id;
    private PreparedEvent currentEditor;

    public User(long id) {
        this.id = id;
        users.add(this);
    }

    public long getId() {
        return id;
    }

    public PreparedEvent getCurrentEditor() {
        return currentEditor;
    }

    public void setCurrentEditor(PreparedEvent newEditor) {
        System.out.println(new Gson().toJson(newEditor));
        System.out.println(new Gson().toJson(currentEditor));
        try {
            PrivateChannel channel = Main.jda.getUserById(id).openPrivateChannel().complete();
            if (currentEditor != null) {
                Message oldMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
                oldMessage.editMessageEmbeds(new EmbedBuilder(oldMessage.getEmbeds().get(0)).setTitle("Event Setup - Click Select to edit").build()).queue();
                oldMessage.editMessageComponents(PrepareEmbed.eventSetupActionRow(false, true)).queue();
                currentEditor = newEditor;
            }
            currentEditor = newEditor;
            if (currentEditor != null) {
                Message newMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
                newMessage.editMessageEmbeds(new EmbedBuilder(newMessage.getEmbeds().get(0)).setTitle("Event Setup").build()).queue();
                newMessage.editMessageComponents(PrepareEmbed.eventSetupActionRow(false, false)).queue();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User getUser(long id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return new User(id);
    }

    public static List<User> getUsers() {
        return users;
    }

    public void remove() {
        users.remove(this);
        if (currentEditor != null) {
            PrivateChannel channel = Main.jda.getUserById(id).openPrivateChannel().complete();
            Message oldMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
            oldMessage.editMessageEmbeds(new EmbedBuilder(oldMessage.getEmbeds().get(0)).setTitle("Event Setup - Click Select to edit").build()).queue();
            oldMessage.editMessageComponents(PrepareEmbed.eventSetupActionRow(false, true)).queue();
        }
    }
}

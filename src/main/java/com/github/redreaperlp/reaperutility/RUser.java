package com.github.redreaperlp.reaperutility;

import com.github.redreaperlp.reaperutility.features.PrepareEmbed;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

import java.util.ArrayList;
import java.util.List;

public class RUser {
    private static List<RUser> RUsers = new ArrayList<>();

    private long id;
    private PreparedEvent currentEditor;
    private int currentEditorCount = 0;

    public RUser(long id) {
        this.id = id;
        RUsers.add(this);
    }

    public long getId() {
        return id;
    }

    public PreparedEvent getCurrentEditor() {
        return currentEditor;
    }

    public void increasePrepCount() {
        currentEditorCount++;
    }

    public void decreasePrepCount() {
        if (currentEditorCount > 0) {
            currentEditorCount--;
        }
    }

    public int getPrepCount() {
        return currentEditorCount;
    }

    public void setCurrentEditor(PreparedEvent newEditor) {
        try {
            PrivateChannel channel = Main.jda.getUserById(id).openPrivateChannel().complete();
            if (currentEditor != null) {
                try {
                    Message oldMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
                    oldMessage.editMessageEmbeds(new EmbedBuilder(oldMessage.getEmbeds().get(0)).setTitle("Event Setup - Click Select to edit").build()).queue();
                } catch (Exception e) {
                    System.out.println("Could not edit old message");
                }
            }
            currentEditor = newEditor;
            if (currentEditor != null) {
                Message newMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
                newMessage.editMessageEmbeds(new EmbedBuilder(newMessage.getEmbeds().get(0)).setTitle("Event Setup").build()).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RUser getUser(long id) {
        for (RUser RUser : RUsers) {
            if (RUser.getId() == id) {
                return RUser;
            }
        }
        return new RUser(id);
    }

    public static List<RUser> getUsers() {
        return RUsers;
    }

    public void remove() {
        System.out.println("Removing user " + id);
        RUsers.remove(this);
        if (currentEditor != null) {
            PrivateChannel channel = Main.jda.getUserById(id).openPrivateChannel().complete();
            Message oldMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
            oldMessage.editMessageEmbeds(new EmbedBuilder(oldMessage.getEmbeds().get(0)).setTitle("Event Setup - Click Select to edit").build()).complete();
        }
    }
}

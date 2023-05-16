package com.github.redreaperlp.reaperutility;

import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

import java.util.ArrayList;
import java.util.List;

public class RUser {
    private static List<RUser> rUsers = new ArrayList<>();
    private static Thread dumpThread;

    private long id;
    private PreparedEvent currentEditor;
    private int untilDelete = 180;

    /**
     * Creates a new RUser and adds it to the list of RUsers<br>
     * Also starts the dumpThread if it is not already running to remove unused RUsers after {@value #untilDelete} seconds
     * @param id the id of the user
     */
    public RUser(long id) {
        this.id = id;
        rUsers.add(this);
        if (dumpThread == null || !dumpThread.isAlive()) {
            dumpThread = new Thread(() -> {
                new Color.Print("Dumping Thread started").printDebug();
                while (rUsers.size() > 0) {
                    List<RUser> toRemove = new ArrayList<>();
                    for (RUser rUser : rUsers) {
                        if (rUser.countDown() <= 0) {
                            toRemove.add(rUser);
                        }
                    }
                    toRemove.forEach(RUser::remove);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                new Color.Print("Dumping Thread stopped").printDebug();
            });
            dumpThread.start();
        }
    }

    private int countDown() {
        return untilDelete--;
    }

    private void resetCountDown() {
        untilDelete = 180;
    }

    public long getId() {
        return id;
    }

    public PreparedEvent getCurrentEditor() {
        return currentEditor;
    }

    public void setCurrentEditor(PreparedEvent newEditor) {
        try {
            PrivateChannel channel = Main.jda.getUserById(id).openPrivateChannel().complete();
            if (currentEditor != null) {
                try {
                    Message oldMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
                    if (oldMessage == null) return;
                    oldMessage.editMessageEmbeds(new EmbedBuilder(oldMessage.getEmbeds().get(0)).setTitle("Event Setup - Click Select to edit").build()).queue();
                    currentEditor.forgettable();
                } catch (Exception e) {
                    System.out.println("Could not edit old message");
                }
            }
            currentEditor = newEditor;
            if (currentEditor != null) {
                Message newMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
                if (newMessage == null) return;
                newMessage.editMessageEmbeds(new EmbedBuilder(newMessage.getEmbeds().get(0)).setTitle("Event Setup").build()).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RUser getUser(long id) {
        for (RUser rUser : rUsers) {
            if (rUser.getId() == id) {
                rUser.resetCountDown();
                return rUser;
            }
        }
        return new RUser(id);
    }

    public static RUser userByEditorId(long id) {
        for (RUser rUser : rUsers) {
            if (rUser.getCurrentEditor() != null && rUser.getCurrentEditor().getEditorId() == id) {
                return rUser;
            }
        }
        return null;
    }

    public void remove() {
        rUsers.remove(this);
        new Thread(() -> {
            System.out.println("Removing user " + id);
            if (currentEditor != null) {
                setCurrentEditor(null);
            }
        }).start();
    }
}

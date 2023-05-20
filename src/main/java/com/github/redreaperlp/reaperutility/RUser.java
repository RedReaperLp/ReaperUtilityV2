package com.github.redreaperlp.reaperutility;

import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import com.github.redreaperlp.reaperutility.util.Color;
import com.github.redreaperlp.reaperutility.util.RateLimit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RUser {
    private static final List<RUser> rUsers = new ArrayList<>();
    private static Thread dumpThread;

    private final long id;
    private PreparedEvent currentEditor;
    private LocalDateTime untilDelete = LocalDateTime.now().plusSeconds(180);
    private final RateLimit rateLimit = new RateLimit();

    /**
     * Creates a new RUser and adds it to the list of RUsers<br>
     * Also starts the dumpThread if it is not already running to remove unused RUsers after 180 seconds
     * @param id the id of the user
     */
    public RUser(long id) {
        this.id = id;
        rUsers.add(this);
        if (dumpThread == null || !dumpThread.isAlive()) {
            dumpThread = new Thread(() -> {
                new Color.Print("Dumping Thread started").printDebug();
                int smallestTime = 180;
                while (rUsers.size() > 0) {
                    try {
                        Thread.sleep((smallestTime > 0 ? smallestTime : 1) * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    List<RUser> toRemove = new ArrayList<>();
                    smallestTime = 180;
                    for (RUser rUser : rUsers) {
                        smallestTime = Math.min(smallestTime, (int) (rUser.untilDelete.toEpochSecond(Main.zoneOffset) - LocalDateTime.now().toEpochSecond(Main.zoneOffset)));
                        if (rUser.deleteable()) {
                            toRemove.add(rUser);
                        }
                    }
                    toRemove.forEach(RUser::remove);
                }
                new Color.Print("Dumping Thread stopped").printDebug();
            });
            dumpThread.start();
        }
    }

    private boolean deleteable() {
        return LocalDateTime.now().isAfter(untilDelete) && !rateLimit().isLimited();
    }

    private RateLimit rateLimit() {
        return rateLimit;
    }

    private void resetCountDown() {
        untilDelete = LocalDateTime.now().plusSeconds(10);
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
            if (currentEditor != null && !currentEditor.isCancelled()) {
                try {
                    currentEditor.forgettable();
                    Message oldMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
                    if (oldMessage == null) return;
                    String url = oldMessage.getEmbeds().get(0).getUrl();
                    oldMessage.editMessageEmbeds(new EmbedBuilder(oldMessage.getEmbeds().get(0)).setTitle("Event Setup - Click Select to edit", url).build()).queue();
                } catch (Exception e) {
                    channel.sendMessage("Couldn't edit old message").queue();
                }
            }
            currentEditor = newEditor;
            if (currentEditor != null) {
                Message newMessage = channel.retrieveMessageById(currentEditor.getEditorId()).complete();
                if (newMessage == null) return;
                String url = newMessage.getEmbeds().get(0).getUrl();
                newMessage.editMessageEmbeds(new EmbedBuilder(newMessage.getEmbeds().get(0)).setTitle("Event Setup", url).build()).queue();
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
            new Color.Print("Removing user " + id + " from cache").printDebug();
            if (currentEditor != null) {
                setCurrentEditor(null);
            }
        }).start();
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }
}

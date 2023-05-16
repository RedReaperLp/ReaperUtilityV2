package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.util.Color;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Scheduler {
    static List<Event> eventList = new ArrayList<>();
    static Thread schedulerThread;

    public static void scheduleEvent(Event event) {
        eventList.add(event);
        event.insertToDatabase();
        startScheduler();
    }

    public static boolean hasEvent(long messageId) {
        for (Event event : eventList) {
            if (event.getMessageId() == messageId) {
                return true;
            }
        }
        return false;
    }

    public static void rescheduleAllEvents() {
        eventList = Event.loadAllEvents();
        if (eventList != null) new Color.Print("Reloaded " + eventList.size() + " events").printDebug();
        startScheduler();
    }

    public static void startScheduler() {
        if (schedulerThread == null || !schedulerThread.isAlive()) {
            schedulerThread = new Thread(() -> {
                while (eventList.size() > 0) {
                    try {
                        Thread.sleep(5000);
                        List<Event> toFire = new ArrayList<>();
                        for (Event event : eventList) {
                            if (LocalDateTime.now().isAfter(event.getTimestamp())) {
                                toFire.add(event);
                            }
                        }
                        for (Event event : toFire) {
                            event.fire();
                            eventList.remove(event);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            schedulerThread.start();
        }
    }
}

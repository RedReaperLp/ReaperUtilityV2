package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.entities.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    static List<Event> eventList = new ArrayList<>();
    static Thread schedulerThread;

    private static final EventSchduler hourScheduler = new EventSchduler(60 * 60);
    private static final EventSchduler halfHourScheduler = new EventSchduler(60 * 30);
    private static final EventSchduler tenMinuteScheduler = new EventSchduler(60 * 10);
    private static final EventSchduler fiveMinuteScheduler = new EventSchduler(60 * 5);
    private static final EventSchduler minuteScheduler = new EventSchduler(60);
    private static final EventSchduler fiveSecondScheduler = new EventSchduler(5);


    public static void scheduleEvent(Event event) {
        eventList.add(event);
        event.insertToDatabase();
        schdule(event);
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
        if (eventList != null) {
            new Color.Print("Reloaded " + eventList.size() + " event" + (eventList.size() == 1 ? "" : "s") + " from database", Color.LIGHT_GRAY).printDebug();
            schedule(eventList);
        }
    }

    public static void schedule(List<Event> toSchedule) {
        for (Event event : List.copyOf(toSchedule)) {
            schdule(event);
        }
    }

    /**
     * Schedules an event to the correct scheduler (Used to update the scheduler of an event)
     * @param toSchedule The event to schedule
     */
    public static void schdule(Event toSchedule) {
        if (!eventList.contains(toSchedule)) {
            eventList.add(toSchedule);
        }
        LocalDateTime now = LocalDateTime.now();
        long seconds = toSchedule.getTimestamp().toEpochSecond(Main.zoneOffset) - now.toEpochSecond(Main.zoneOffset);
        if (seconds > 60 * 60) {
            toSchedule.setCurrentScheduler(hourScheduler);
            hourScheduler.scheduleEvent(toSchedule);
        } else if (seconds > 60 * 30) {
            toSchedule.setCurrentScheduler(halfHourScheduler);
            halfHourScheduler.scheduleEvent(toSchedule);
        } else if (seconds > 60 * 10) {
            toSchedule.setCurrentScheduler(tenMinuteScheduler);
            tenMinuteScheduler.scheduleEvent(toSchedule);
        } else if (seconds > 60 * 5) {
            toSchedule.setCurrentScheduler(fiveMinuteScheduler);
            fiveMinuteScheduler.scheduleEvent(toSchedule);
        } else if (seconds > 60) {
            toSchedule.setCurrentScheduler(minuteScheduler);
            minuteScheduler.scheduleEvent(toSchedule);
        } else if (seconds > 5){
            toSchedule.setCurrentScheduler(fiveSecondScheduler);
            fiveSecondScheduler.scheduleEvent(toSchedule);
        } else {
            if (toSchedule.getTimestamp().isBefore(now)) {
                eventList.remove(toSchedule);
                if (!toSchedule.hasReadUsers()) {
                    toSchedule.readUsers(Main.jda.getTextChannelById(toSchedule.getChannelId()).retrieveMessageById(toSchedule.getMessageId()).complete());
                }
                toSchedule.fire();
                return;
            }
            new Thread(() -> {
                try {
                    Thread.sleep(seconds * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                eventList.remove(toSchedule);
                if (!toSchedule.hasReadUsers()) {
                    toSchedule.readUsers(Main.jda.getTextChannelById(toSchedule.getChannelId()).retrieveMessageById(toSchedule.getMessageId()).complete());
                }
                toSchedule.fire();
            }).start();
        }
    }

    public static Event getEvent(long idLong) {
        for (Event event : eventList) {
            if (event.getMessageId() == idLong) {
                return event;
            }
        }
        return null;
    }

    public static Event getEventByMessage(Message message) {
        Event event = getEvent(message.getIdLong());
        if (event != null && !event.hasReadUsers()) {
            Color.printTest("Reading users for event " + event.getMessageId());
            event.readUsers(message);
        }
        return event;
    }

    public static void deleteEvent(Event deleteEvent) {
        deleteEvent.getCurrentScheduler().delete(deleteEvent);
        deleteEvent.removeFromDatabase();
        eventList.remove(deleteEvent);
    }

    public static class EventSchduler {
        private final List<Event> eventList = new ArrayList<>();
        private Thread schedulerThread;

        private final int timeToWait;

        public EventSchduler(int timeToWait) {
            this.timeToWait = timeToWait;
        }

        public void scheduleEvent(Event event) {
            eventList.add(event);
            Color.printTest("Scheduled event " + event.getMessageId() + " for " + timeToWait + " seconds");
            startScheduler();
        }

        public void startScheduler() {
            if (schedulerThread == null || !schedulerThread.isAlive()) {
                schedulerThread = new Thread(() -> {
                    new Color.Print("Started scheduler for " + timeToWait + " seconds", Color.LIGHT_GRAY).printDebug();
                    while (eventList.size() > 0) {
                        try {
                            Thread.sleep(timeToWait * 1000L);
                            List<Event> toReschedule = new ArrayList<>();
                            for (Event event : eventList) {
                                if (LocalDateTime.now().isAfter(event.getTimestamp().minusSeconds(timeToWait))) {
                                    toReschedule.add(event);
                                }
                            }
                            eventList.removeAll(toReschedule);
                            Scheduler.schedule(toReschedule);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    new Color.Print("Stopped scheduler for " + timeToWait + " seconds", Color.LIGHT_GRAY).printDebug();
                });
                schedulerThread.start();
            }
        }

        public void delete(Event event) {
            eventList.remove(event);
            Color.printTest("Deleted event " + event.getMessageId() + " from scheduler for " + timeToWait + " seconds");
        }
    }
}

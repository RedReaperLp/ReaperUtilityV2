package com.github.redreaperlp.reaperutility.features.event;

import com.github.redreaperlp.reaperutility.util.Color;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    static List<Event> eventList = new ArrayList<>();

    public static void scheduleEvent(Event event) {
        eventList.add(event);
        Color.printTest("Scheduled event" + new Gson().toJson(event));
    }
}

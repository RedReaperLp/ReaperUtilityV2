package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.features.event.Scheduler;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LReconnectHandler extends ListenerAdapter {
    @Override
    public void onSessionResume(SessionResumeEvent event) {
        Scheduler.rescheduleAllEvents();
    }
}

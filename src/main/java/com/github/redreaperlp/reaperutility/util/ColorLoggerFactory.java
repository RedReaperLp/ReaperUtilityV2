package com.github.redreaperlp.reaperutility.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import com.github.redreaperlp.reaperutility.Main;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.Objects;

public class ColorLoggerFactory implements Appender<ILoggingEvent> {

    static String error;
    static int countDown = 2;
    static Thread sender;
    @Override
    public String getName() {
        return "ColorLogger";
    }

    @Override
    public void doAppend(ILoggingEvent event) throws LogbackException {
        if (event.getLevel() == Level.DEBUG) {
            new Color.Print(event.getFormattedMessage()).printDebug();
            return;
        } else if (event.getLevel() == Level.INFO) {
            new Color.Print(event.getFormattedMessage()).printInfo();
            return;
        } else if (event.getLevel() == Level.WARN) {
            new Color.Print(event.getFormattedMessage()).printWarning();
            return;
        } else if (event.getLevel() == Level.ERROR) {
            new Color.Print(event.getFormattedMessage()).printError();
            for (StackTraceElement trace : event.getCallerData()) {
                Color.RED.printError(trace.toString());
            }
            appendError(event);
            return;
        }
        new Color.Print(event.getLevel() + " - " + event.getFormattedMessage()).printInfo();
    }

    public void appendError(ILoggingEvent event) {
        error += event.getFormattedMessage() + "\n";
        if (sender == null) {
            sender = new Thread(() -> {
                while (countDown > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    countDown--;
                }
                if (Main.jda != null) {
                    Objects.requireNonNull(Main.jda.getTextChannelById(1086673451777007636L)).sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Exception in thread " + event.getThreadName() + ": " + event.getMessage())
                                    .setDescription("```" + error + "```")
                                    .build()
                    ).queue();
                }
                error = "";
                countDown = 2;
                sender = null;
            });
            sender.start();
        } else {
            countDown = 2;
        }
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void addStatus(Status status) {

    }

    @Override
    public void addInfo(String msg) {
        Color.RED.printInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        Color.RED.printInfo(msg);
    }

    @Override
    public void addWarn(String msg) {
        Color.RED.printInfo(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        Color.RED.printInfo(msg);
    }

    @Override
    public void addError(String msg) {
        Color.RED.printInfo(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        Color.RED.printInfo(msg);
    }

    @Override
    public void addFilter(Filter<ILoggingEvent> newFilter) {

    }

    @Override
    public void clearAllFilters() {

    }

    @Override
    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return null;
    }

    @Override
    public FilterReply getFilterChainDecision(ILoggingEvent event) {
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return true;
    }
}

package com.github.redreaperlp.reaperutility;

import ch.qos.logback.classic.Logger;
import com.github.redreaperlp.reaperutility.database.Database;
import com.github.redreaperlp.reaperutility.features.ECommands;
import com.github.redreaperlp.reaperutility.features.LButtonHandler;
import com.github.redreaperlp.reaperutility.features.LCommandHandler;
import com.github.redreaperlp.reaperutility.features.LSelectionHandler;
import com.github.redreaperlp.reaperutility.settings.JSettings;
import com.github.redreaperlp.reaperutility.util.Color;
import com.github.redreaperlp.reaperutility.util.ColorLoggerFactory;
import com.github.redreaperlp.reaperutility.util.Restarter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static boolean debug = true;
    public static boolean colored = true;
    public static boolean exitOnAnyKey = false;
    public static JSettings settings;
    public static JDA jda;


    public static void main(String[] args) {
        Main main = new Main();
        Restarter restarter = new Restarter();
        restarter.uncaughtHandler();
        new Thread(restarter).start();
        main.prepareSettings();
        colored = settings.consoleSettings().colored();
        debug = settings.consoleSettings().debug();
        new Color.Print("---------------------------------", Color.GREEN)
                .appendLine(" Etablishing Database Connection", Color.GREEN)
                .appendLine("---------------------------------", Color.GREEN).printInfo();
        new Database();
        main.start();
    }

    public static void exit() {
        new Color.Print("Press any key to exit or [r]estart to restart...").printInfo();
        exitOnAnyKey = true;
        throw new RuntimeException("Exiting");
    }

    private void start() {
        Logger logger = (Logger) LoggerFactory.getLogger("net.dv8tion.jda");
        logger.getLoggerContext().reset();
        logger.addAppender(new ColorLoggerFactory());
        logger.getAppender("ColorLogger").start();

        new Color.Print("--------------", Color.GREEN)
                .appendLine(" Starting Bot", Color.GREEN)
                .appendLine("--------------", Color.GREEN).printInfo();
        JSettings.JJDASettings jdaSettings = settings.jdaSettings();

        JDABuilder builder = JDABuilder.createDefault(jdaSettings.token())
                .setActivity(Activity.of(jdaSettings.activity(), jdaSettings.activityText()))
                .setStatus(jdaSettings.status());

        enableIntents(builder);
        enableListeners(builder);
        try {
            new Color.Print("Connecting to Discord").printDebug();
            jda = builder.build();
            jda.awaitReady();
        } catch (InvalidTokenException e) {
            System.out.println();
            new Color.Print("*** Something went wrong ***", Color.ORANGE)
                    .appendLine("Invalid Token, please check your settings")
                    .appendLine("Your token can be found at https://discord.com/developers/applications").printError();
            exit();
        } catch (Exception e) {
            System.out.println();
            new Color.Print("*** Something went wrong ***", Color.ORANGE)
                    .appendLine("Failed to connect to Discord (\"" + e.getMessage() + "\")").printError();
            exit();
        }
        enableCommands();
        new Color.Print("------------------------------------------", Color.GREEN)
                .appendLine(" Bot Started, entering main functionality", Color.GREEN)
                .appendLine("------------------------------------------", Color.GREEN).printInfo();
    }

    private void enableListeners(JDABuilder builder) {
        builder.addEventListeners(new LCommandHandler(), new LSelectionHandler(), new LButtonHandler());
    }

    private void enableCommands() {
        new Color.Print("Updating global commands").printDebug();
        for (ECommands.EGlobalCommands command : ECommands.EGlobalCommands.values()) {
            if (command != ECommands.EGlobalCommands.UNKNOWN)
                jda.upsertCommand(command.getCommand()).queue();
        }

        for (Guild guild : jda.getGuilds()) {
            new Color.Print("Updating commands for guild " + guild.getName()).printDebug();
            List<CommandData> data = new ArrayList<>();
            for (ECommands.EGuildCommands command : ECommands.EGuildCommands.values()) {
                if (command != ECommands.EGuildCommands.UNKNOWN)
                    data.add(command.getCommand());
            }
            guild.updateCommands().addCommands(data).queue();
        }
    }

    public void enableIntents(JDABuilder build) {
        build.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        build.enableIntents(GatewayIntent.GUILD_MEMBERS);
        build.enableIntents(GatewayIntent.GUILD_PRESENCES);
        build.enableIntents(GatewayIntent.GUILD_MESSAGES);
        build.enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        build.enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        build.enableIntents(GatewayIntent.DIRECT_MESSAGES);
        build.enableIntents(GatewayIntent.GUILD_VOICE_STATES);
        build.enableIntents(GatewayIntent.SCHEDULED_EVENTS);
    }

    public void prepareSettings() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File("settings.json");
        if (!file.exists()) {
            Color.RED.printWarning("Settings file not found. Creating new one...");
            settings = new JSettings();
            String json = gson.toJson(settings);
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                FileReader reader = new FileReader(file);
                settings = gson.fromJson(reader, JSettings.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (JsonSyntaxException e) {
                Color.RED.printError("Settings file is corrupted. Please delete or fix it and restart the bot.");
                throw new RuntimeException(e);
            }
        }
    }
}
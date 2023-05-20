package com.github.redreaperlp.reaperutility.util;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.RUser;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Restarter implements Runnable {
    private static final String EXIT_COMMAND = "exit";
    private static final String RESTART_COMMAND = "restart";
    private static final String DEBUG_COMMAND = "debug";
    private static final String COLOR_COMMAND = "colored";

    private static String oldHash = "";

    public Restarter() {
        new Thread(() -> {
            if (System.console() == null) {
                restart();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    String line = reader.readLine();
                    switch (line.toLowerCase()) {
                        case EXIT_COMMAND:
                            Color.YELLOW.printWarning("Exiting Restart Manager");
                            System.exit(0);
                            break;
                        case RESTART_COMMAND, "r":
                            restart();
                            break;
                        case DEBUG_COMMAND:
                            Main.debug = !Main.debug;
                            Main.settings.consoleSettings().debug(Main.debug);
                            Color.YELLOW.printWarning("Debug Mode: " + Main.debug);
                            Main.settings.save();
                            break;
                        case COLOR_COMMAND:
                            Main.colored = !Main.colored;
                            Main.settings.consoleSettings().colored(Main.colored);
                            Color.YELLOW.printWarning("Color Mode: " + Main.colored);
                            Main.settings.save();
                            break;
                        default:
                            if (Main.exitOnAnyKey) {
                                System.exit(0);
                            }
                            Color.RED.printError("Unknown Command");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void run() {
        oldHash = digest();
        try {
            while (true) {
                TimeUnit.SECONDS.sleep(2);
                String newHash = digest();
                if (!oldHash.equals(newHash)) {
                    new Color.Print("Found new Version", Color.ORANGE)
                            .appendLine("Waiting for build to finish...", Color.ORANGE)
                            .printWarning();
                    while (true) {
                        try {
                            oldHash = newHash;
                            TimeUnit.MILLISECONDS.sleep(1000);
                            newHash = digest();
                            if (oldHash.equals(newHash)) {
                                restart();
                                break;
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void restart() {
        try {
            TimeUnit.SECONDS.sleep(2);
            new Color.Print("Restarting...", Color.GREEN).printWarning();
            File file = new File("start.bat");
            File startHelper = new File("startHelper.bat");
            if (!file.exists()) {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8888 -jar " + System.getProperty("java.class.path") + " && exit");
                writer.close();
            }
            if (!startHelper.exists()) {
                startHelper.createNewFile();
                FileWriter writer = new FileWriter(startHelper);
                writer.write("timeout /t 1 && start.bat && exit");
                writer.close();
            }
            String pathToBatchFile = file.getPath();
            Runtime.getRuntime().exec("cmd /c start " + pathToBatchFile);
        } catch (Exception e) {
            File file = new File("log.txt");
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(e.getMessage());
                writer.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        System.exit(0);
    }


    public static String digest() {
        File file = new File(System.getProperty("sun.java.command").split(" ")[0]);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream stream = new FileInputStream(file);
            byte[] bytesArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = stream.read(bytesArray)) > 0) {
                md.update(bytesArray, 0, bytesCount);
            }
            stream.close();

            byte[] bytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Program was Specified Wrong, please check \"programConfig.yaml\"");
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void uncaughtHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                if (e.getMessage().contains("Exiting")) {
                    return;
                }
                e.printStackTrace();
                Color.RED.printError("Exception in thread " + t.getName() + ": " + e.getMessage());
                StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement str : e.getStackTrace()) {
                    stackTrace.append(str.toString()).append("\n");
                }
                if (Main.jda != null) {
                    Objects.requireNonNull(Main.jda.getTextChannelById(1086673451777007636L)).sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Exception in thread " + t.getName() + ": " + e.getMessage())
                                    .setDescription("```" + stackTrace + "```")
                                    .build()
                    ).queue();
                }
            }
        });
    }
}
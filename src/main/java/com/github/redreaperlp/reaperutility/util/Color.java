package com.github.redreaperlp.reaperutility.util;

import com.github.redreaperlp.reaperutility.Main;

import java.util.Arrays;

public enum Color {
    RED("\u001B[31m"),

    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    ORANGE("\u001B[38;5;208m"),
    BLUE("\u001B[34m"),
    LIGHT_BLUE("\u001B[38;5;39m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    GRAY("\u001B[38;5;240m"),
    WHITE("\u001B[37m"),
    RESET("\u001B[0m");

    private static int counter = 0;
    private static int testCounter = 0;

    private final String color;

    Color(String color) {
        this.color = color;
    }

    public String col() {
        return color;
    }

    public static void printTest(String message) {
        if (message != null) Arrays.stream(message.split("\n")).toList().forEach(line -> {
            testCounter++;
            if (Main.colored) {
                System.out.print(RED.col() + "<" + GRAY.col() + Print.counter() + RED.col() + "> " + YELLOW.col() + "[" + ORANGE.col() + testCounter + YELLOW.col() + "] " + line + RESET.col());
            } else {
                System.out.print("<" + Print.counter() + "> [" + testCounter + "] " + line);
            }
        });
        System.out.print("\n");
    }

    public static void printTest(String message, Color col) {
        if (Main.colored) {
            printTest(col.col() + message + RESET.col());
        } else {
            printTest(message);
        }
    }

    public void printSuccess(String message) {
        printSuccess(message, false, false);
    }

    public void printError(String message) {
        printError(message, false, false);
    }

    public void printWarning(String message) {
        printWarning(message, false, false);
    }

    public void printInfo(String message) {
        printInfo(message, false, false);
    }

    public void printDebug(String message) {
        printDebug(message, false, false);
    }

    public void printSuccess(String message, boolean isAppender, boolean getsAppender) {
        new Print(message, this).printSuccess(isAppender, getsAppender);
    }

    public void printError(String message, boolean isAppender, boolean getsAppender) {
        new Print(message, this).printError(isAppender, getsAppender);
    }

    public void printWarning(String message, boolean isAppender, boolean getsAppender) {
        new Print(message, this).printWarning(isAppender, getsAppender);
    }

    public void printInfo(String message, boolean isAppender, boolean getsAppender) {
        new Print(message, this).printInfo(isAppender, getsAppender);
    }

    public void printDebug(String message, boolean isAppender, boolean getsAppender) {
        new Print(message, this).printDebug(isAppender, getsAppender);
    }


    public static class Print {
        private String message = "";

        public Print(String message, Color color) {
            if (Main.colored) {
                this.message = color.col() + message + Color.RESET.col();
            } else {
                this.message = message;
            }
        }

        public Print(String message) {
            this.message = message;
        }

        public Print append(String message, Color color) {
            if (Main.colored) {
                this.message += color.col() + message + Color.RESET.col();
            } else {
                this.message += message;
            }
            return this;
        }

        public Print append(String message) {
            return append(message, Color.RESET);
        }

        public Print appendLine(String message, Color color) {
            if (Main.colored) {
                this.message += "\n" + color.col() + message + Color.RESET.col();
            } else {
                this.message += "\n" + message;
            }
            return this;
        }

        public Print appendLine(String message) {
            return appendLine(message, Color.RESET);
        }

        public void printSuccess(boolean isAppender, boolean getsAppender) {
            if (message != null) Arrays.stream(message.split("\n")).toList().forEach(
                    line -> {
                        if (Main.colored) {
                            System.out.print((isAppender ? "" : (CYAN.col() + "<" + GREEN.col() + counter() + CYAN.col() + "> ")) + GREEN.col() + line + RESET.col());
                        } else {
                            System.out.print((isAppender ? "" : ("<" + counter() + "> ")) + line);
                        }
                        if (!getsAppender) {
                            System.out.println();
                        }
                    }
            );
            else System.out.println((getsAppender || !isAppender ? ("<" + counter() + "> ") : "") + "null");
        }

        public void printWarning(boolean isAppender, boolean getsAppender) {
            if (message != null) Arrays.stream(message.split("\n")).toList().forEach(
                    line -> {
                        if (Main.colored) {
                            System.out.print((isAppender ? "" : (CYAN.col() + "<" + YELLOW.col() + counter() + CYAN.col() + "> " + YELLOW.col() + "WARN  ")) + RESET.col() + line + RESET.col());
                        } else {
                            System.out.print((isAppender ? "" : ("<" + counter() + "> WARN  ")) + line);
                        }
                        if (!getsAppender) {
                            System.out.println();
                        }
                    }
            );
            else System.out.println((getsAppender || !isAppender ? ("<" + counter() + "> ") : "") + "null");
        }

        public void printError(boolean isAppender, boolean getsAppender) {
            if (message != null) Arrays.stream(message.split("\n")).toList().forEach(
                    line -> {
                        if (Main.colored) {
                            System.out.print((isAppender ? "" : (CYAN.col() + "<" + RED.col() + counter() + CYAN.col() + "> " + RED.col() + "ERROR ")) + RESET.col() + line + RESET.col());
                        } else {
                            System.out.print((isAppender ? "" : ("<" + counter() + "> ERROR ")) + line);
                        }
                        if (!getsAppender) {
                            System.out.println();
                        }
                    }
            );
            else System.out.println((getsAppender || !isAppender ? ("<" + counter() + "> ") : "") + "null");
        }

        public void printInfo(boolean isAppender, boolean getsAppender) {
            if (message != null) Arrays.stream(message.split("\n")).toList().forEach(
                    line -> {
                        if (Main.colored) {
                            System.out.print((isAppender ? "" : (CYAN.col() + "<" + LIGHT_BLUE.col() + counter() + CYAN.col() + "> " + LIGHT_BLUE.col() + "INFO  ")) + RESET.col() + line + RESET.col());
                        } else {
                            System.out.print((isAppender ? "" : ("<" + counter() + "> INFO  ")) + line);
                        }
                        if (!getsAppender) {
                            System.out.println();
                        }
                    }
            );
            else System.out.println((getsAppender || !isAppender ? ("<" + counter() + "> ") : "") + "null");
        }

        public void printDebug(boolean isAppender, boolean getsAppender) {
            if (Main.debug) {
                if (message != null) {
                    Arrays.stream(message.split("\n")).toList().forEach(
                            line -> {
                                if (Main.colored) {
                                    System.out.print((isAppender ? "" : (CYAN.col() + "<" + ORANGE.col() + counter() + CYAN.col() + ">" + ORANGE.col() + " DEBUG ")) + GRAY.col() + line + RESET.col());
                                } else {
                                    System.out.print((isAppender ? "" : ("<" + counter() + "> DEBUG ")) + line);
                                }
                                if (!getsAppender) {
                                    System.out.println();
                                }
                            }
                    );
                } else {
                    System.out.println((getsAppender || !isAppender ? ("<" + counter() + "> ") : "") + "null");
                }
            }
        }


        public void printInfo() {
            printInfo(false, false);
        }

        public void printWarning() {
            printWarning(false, false);
        }

        public void printDebug() {
            printDebug(false, false);
        }

        public void printError() {
            printError(false, false);
        }

        public void printSuccess() {
            printSuccess(false, false);
        }

        private static String counter() {
            counter++;
            String underspaces = "____";
            String sCounter = String.valueOf(counter);
            if (sCounter.length() > 3) {
                for (int i = 0; i < sCounter.length() - 3; i++) {
                    underspaces += "_";
                }
            }
            underspaces = underspaces.substring(0, underspaces.length() - sCounter.length());
            return counter + underspaces;
        }
    }
}

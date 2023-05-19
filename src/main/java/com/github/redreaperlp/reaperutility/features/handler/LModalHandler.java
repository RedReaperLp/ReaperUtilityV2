package com.github.redreaperlp.reaperutility.features.handler;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.RUser;
import com.github.redreaperlp.reaperutility.features.event.PreparedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LModalHandler extends ListenerAdapter {
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        ModalKey key = ModalKey.getByKey(event.getModalId());
        RUser user = RUser.getUser(event.getUser().getIdLong());
        if (key.key().contains("event.")) {
            PreparedEvent preparedEvent = user.getCurrentEditor();
            if (preparedEvent == null) {
                event.reply("You are not editing an event, please select an event first!").queue();
                return;
            }
            int dateResult = 404;
            boolean colorFailed = false;
            for (ModalMapping value : event.getValues()) {
                switch (EventSetupKey.getByKey(value.getId())) {
                    case NAME -> {
                        if (!value.getAsString().isEmpty() || !value.getAsString().isBlank()) {
                            preparedEvent.setName(value.getAsString());
                        }
                    }
                    case DESCRIPTION -> setDescription(value.getAsString(), preparedEvent);
                    case LOCATION -> setLocation(value.getAsString(), preparedEvent);
                    case DATE -> dateResult = setDate(value.getAsString(), preparedEvent);
                    case COLOR -> colorFailed = setColor(value.getAsString(), preparedEvent);
                }
            }
            if (dateResult != 0 || colorFailed) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Wrong Information");
                switch (dateResult) {
                    case 1 -> builder.addField("Date", "You have to provide a Date ***and*** Time!", false);
                    case 2 ->
                            builder.addField("Date", "You have to provide a ***YEAR***, ***MONTH*** and ***DAY***!", false);
                    case 3 -> builder.addField("Date", "You have to provide a ***HOUR*** and ***MINUTE***!", false);
                    case 4 ->
                            builder.addField("Date", "Keep ***OUT*** non-numeric characters except for the ***-***!", false);
                    case 5 ->
                            builder.addField("Date", "You have to provide a ***valid*** date which is ***not in the past***!", false);
                }
                if (colorFailed) {
                    builder.addField("Color", "You have to provide a ***RGB*** color code!", false);
                }
                event.replyEmbeds(builder.build()).queue();
                preparedEvent.modifyEditor(event.getChannel().asPrivateChannel());
                return;
            }
            preparedEvent.modifyEditor(event.getChannel().asPrivateChannel());
            event.deferEdit().queue();
        }
    }

    private void setDescription(String value, PreparedEvent preparedEvent) {
        if (value.isEmpty() || value.isBlank()) {
            return;
        } else if (value.equalsIgnoreCase("none") || value.equalsIgnoreCase("empty") || value.equalsIgnoreCase("remove")) {
            preparedEvent.setDescription("Your Description");
            return;
        }
        preparedEvent.setDescription(value);
    }

    private void setLocation(String value, PreparedEvent preparedEvent) {
        if (value.isEmpty() || value.isBlank()) {
            return;
        } else if (value.equalsIgnoreCase("none") || value.equalsIgnoreCase("empty") || value.equalsIgnoreCase("remove")) {
            preparedEvent.setLocation("Your Location");
            return;
        }

        List<String> extracted = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{(.*?)}");
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            extracted.add(matcher.group(1));
        }
        Guild guild = Main.jda.getGuildById(preparedEvent.getTargetMessage()[0]);
        for (String extract : extracted) {
            try {
                long id = Long.parseLong(extract);
                for (MessageChannel channel : guild.getTextChannels()) {
                    if (channel.getId().contains(String.valueOf(id))) {
                        value = value.replace("{" + extract + "}", channel.getAsMention());
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                for (MessageChannel channel : guild.getTextChannels()) {
                    if (channel.getName().contains(extract)) {
                        value = value.replace("{" + extract + "}", channel.getAsMention());
                        break;
                    }
                }
            }
        }
        preparedEvent.setLocation(value);
    }

    private int setDate(String value, PreparedEvent preparedEvent) {
        if (value.isEmpty() || value.isBlank()) {
            return 0;
        }
        String[] dayAndTime = value.split(" ");
        if (dayAndTime.length != 2) {
            return 1;
        }
        String[] day = dayAndTime[0].split("-");
        if (day.length != 3) {
            return 2;
        }
        String[] time = dayAndTime[1].split(":");
        if (time.length != 2) {
            return 3;
        }
        int[] date = new int[5];
        for (int i = 0; i < 3; i++) {
            try {
                date[i] = Integer.parseInt(day[i]);
            } catch (NumberFormatException e) {
                return 4;
            }
        }
        for (int i = 0; i < 2; i++) {
            try {
                date[i + 3] = Integer.parseInt(time[i]);
            } catch (NumberFormatException e) {
                return 4;
            }
        }
        LocalDateTime eventDate = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        long epoch = eventDate.toEpochSecond(Main.zoneOffset);
        preparedEvent.setDate(epoch);
        return eventDate.isBefore(LocalDateTime.now()) ? 5 : 0;
    }

    private boolean setColor(String value, PreparedEvent preparedEvent) {
        if (value.isEmpty() || value.isBlank()) {
            return false;
        }
        int[] rgb = new int[3];
        String[] split = value.split(" ");
        if (split.length != 3) {
            return true;
        }
        for (int i = 0; i < 3; i++) {
            try {
                rgb[i] = Integer.parseInt(split[i]);
            } catch (NumberFormatException e) {
                return true;
            }
        }
        preparedEvent.setColor(rgb);
        return false;
    }

    public enum ModalKey {
        EVENT_SETUP("Empty field keeps value", "event.setup",
                TextInput.create("event.name", "1️⃣Name", TextInputStyle.SHORT).setRequired(false).setPlaceholder("The name of the event").setMaxLength(32),
                TextInput.create("event.description", "2️⃣Description", TextInputStyle.PARAGRAPH).setRequired(false).setPlaceholder("The description of the event (Not Required)").setMaxLength(500),
                TextInput.create("event.date", "3️⃣Date", TextInputStyle.SHORT).setRequired(false).setPlaceholder("Date of Event. Example: \"2023-01-01 01:05\"").setMaxLength(32),
                TextInput.create("event.location", "4️⃣Location", TextInputStyle.PARAGRAPH).setRequired(false).setPlaceholder("The location of the event (Not Required)").setMaxLength(200),
                TextInput.create("event.color", "5️⃣Color", TextInputStyle.SHORT).setRequired(false).setPlaceholder("The border color e.g.: \"255 255 255\" (Not Required)").setMaxLength(11)
        ),
        UNKNOWN("UNKNOWN", "unknown");

        private final String title;
        private final String key;
        private final TextInput.Builder[] components;

        ModalKey(String title, String key, TextInput.Builder... components) {
            this.title = title;
            this.key = key;
            this.components = components;
        }

        public String title() {
            return title;
        }

        public String key() {
            return key;
        }

        public Modal.Builder builder(PreparedEvent prepEvent) {
            List<ActionRow> rows = new ArrayList<>();
            for (TextInput.Builder component : components) {
                EventSetupKey eventSetupKey = EventSetupKey.getByKey(component.getId());
                switch (eventSetupKey) {
                    case NAME -> component.setValue(prepEvent.getName());
                    case DESCRIPTION -> component.setValue(prepEvent.getDescription());
                    case DATE ->
                            component.setValue(prepEvent.getDate().isBefore(LocalDateTime.now()) ? LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : prepEvent.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    case LOCATION -> component.setValue(prepEvent.getLocation());
                    case COLOR ->
                            component.setValue(prepEvent.color().getRed() + " " + prepEvent.color().getGreen() + " " + prepEvent.color().getBlue());
                }
                rows.add(ActionRow.of(component.build()));
            }
            return Modal.create(this.key, this.title).addComponents(rows);
        }

        public static ModalKey getByKey(String key) {
            for (ModalKey modalKey : values()) {
                if (modalKey.key().equals(key)) {
                    return modalKey;
                }
            }
            return UNKNOWN;
        }

        public Modal build(PreparedEvent prepEvent) {
            return builder(prepEvent).build();
        }
    }

    public enum EventSetupKey {
        NAME("event.name"),
        DESCRIPTION("event.description"),
        DATE("event.date"),
        LOCATION("event.location"),
        COLOR("event.color"),
        UNKNOWN("unknown");

        private final String key;

        EventSetupKey(String key) {
            this.key = key;
        }

        public static EventSetupKey getByKey(String id) {
            for (EventSetupKey key : values()) {
                if (key.key().equals(id)) {
                    return key;
                }
            }
            return UNKNOWN;
        }

        public String key() {
            return key;
        }
    }
}

package com.github.redreaperlp.reaperutility.util;

import com.github.redreaperlp.reaperutility.Main;

import java.time.LocalDateTime;

public class RateLimit {
    private LocalDateTime timestamp = LocalDateTime.now().minusSeconds(10);
    private LocalDateTime selectCooldown = LocalDateTime.now();
    private boolean isLimited;

    public synchronized boolean addIsRateLimited(int impact) {
        boolean isLimited = isLimited();
        LocalDateTime now = LocalDateTime.now();
        int diff = (int) (timestamp.toEpochSecond(Main.zoneOffset) - now.toEpochSecond(Main.zoneOffset));
        int amount = impact;
        if (diff > 40) {
            amount = impact  * 2;
        } else if (diff > 20) {
            amount = (int) (impact  * 1.4f);
        }

        if (timestamp.isAfter(now)) {
            timestamp = timestamp.plusSeconds(amount);
        } else {
            timestamp = now.plusSeconds(5);
        }
        if (timestamp.isAfter(now.plusSeconds(15))) {
            this.isLimited = true;
        }
        return isLimited;
    }

    public synchronized boolean isLimited() {
        if (isLimited && timestamp.isBefore(LocalDateTime.now())) {
            isLimited = false;
        }
        return isLimited;
    }

    public LocalDateTime getTime() {
        return timestamp;
    }

    public String getDiscordFormattedRemaining() {
        return "<t:" + timestamp.toEpochSecond(Main.zoneOffset) + ":R>";
    }

    public void selectCooldown() {
        selectCooldown = LocalDateTime.now().plusSeconds(5);
    }
    public boolean isSelectCooldown() {
        return selectCooldown.isAfter(LocalDateTime.now());
    }
}

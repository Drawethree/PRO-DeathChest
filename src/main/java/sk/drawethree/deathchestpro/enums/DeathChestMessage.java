package sk.drawethree.deathchestpro.enums;

import org.bukkit.ChatColor;
import sk.drawethree.deathchestpro.DeathChestPro;

public enum DeathChestMessage {

    PREFIX("prefix"),
    DEATHCHEST_LIST_INV_TITLE("deathchest_list_inv_title"),
    DEATHCHEST_LOCATED("deathchest_located"),
    DEATHCHEST_WILL_DISAPPEAR("deathchest_will_disappear"),
    DEATHCHEST_DISAPPEARED("deathchest_disappeared"),
    DEATHCHEST_CANNOT_BREAK("deathchest_cannot_break"),
    DEATHCHEST_CANNOT_OPEN("deathchest_cannot_open"),
    DEATHCHEST_LOCKED("deathchest_locked"),
    DEATHCHEST_UNLOCKED("deathchest_unlocked"),
    NO_PERMISSION("no_permission"),
    INVALID_USAGE("invalid_usage"),
    YEARS("time.years"),
    DAYS("time.days"),
    HOURS("time.hours"),
    MINUTES("time.minutes"),
    SECONDS("time.seconds"),
    DEATHCHEST_TELEPORTED("deathchest_teleported"),
    DEATHCHEST_FASTLOOT_COMPLETE("deathchest_fastloot_complete"),
    DEATHCHEST_LOCATED_HOVER("deathchest_located_hover"),
    DEATHCHEST_TELEPORT_NO_MONEY("deathchest_teleport_no_money"),
    DEATHCHEST_TELEPORT_COOLDOWN("deathchest_teleport_cooldown");

    private String path;
    private String message;

    DeathChestMessage(String path) {
        this.path = path;
        this.message = ChatColor.translateAlternateColorCodes('&', DeathChestPro.getInstance().getFileManager().getConfig("messages.yml").get().getString(path));
    }

    public String getMessage() {
        return message;
    }

    private String getPath() {
        return path;
    }

    public String getChatMessage() {
        return PREFIX.getMessage() + getMessage();
    }

    public static void reload() {
        for (DeathChestMessage m : values()) {
            m.setMessage(ChatColor.translateAlternateColorCodes('&', DeathChestPro.getInstance().getFileManager().getConfig("messages.yml").get().getString(m.getPath())));
        }
    }

    private void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.getMessage();
    }
}

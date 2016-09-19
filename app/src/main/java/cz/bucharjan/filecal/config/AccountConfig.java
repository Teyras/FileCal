package cz.bucharjan.filecal.config;

import java.io.Serializable;

/**
 * A common ancestor for account configuration objects
 */
abstract public class AccountConfig implements Serializable, Cloneable {
    private String name;

    private boolean importAlarms;

    private boolean keepLocalChanges;

    private Integer calendarId = null;

    public AccountConfig(String name, boolean importAlarms, boolean keepLocalChanges) {
        this.name = name;
        this.importAlarms = importAlarms;
        this.keepLocalChanges = keepLocalChanges;
    }

    public AccountConfig withCalendar(Integer calendarId) {
        AccountConfig config = null;

        try {
            config = (AccountConfig) clone();
        } catch (CloneNotSupportedException e) {
            // doesn't happen
            return null;
        }

        config.calendarId = calendarId;
        return config;
    }

    public String getName() {
        return name;
    }

    public Integer getCalendarId() {
        return calendarId;
    }
}

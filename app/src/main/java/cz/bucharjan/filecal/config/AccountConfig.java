package cz.bucharjan.filecal.config;

import java.io.Serializable;

/**
 * A common ancestor for account configuration objects
 */
abstract public class AccountConfig implements Serializable {
    private String name;

    private boolean importAlarms;

    private boolean keepLocalChanges;

    public AccountConfig(String name, boolean importAlarms, boolean keepLocalChanges) {
        this.name = name;
        this.importAlarms = importAlarms;
        this.keepLocalChanges = keepLocalChanges;
    }

    public String getName() {
        return name;
    }
}

package cz.bucharjan.filecal.config;

import android.net.Uri;

/**
 * Configuration for a Local ICS file calendar
 */
public class LocalIcsConfig extends AccountConfig {
    private Uri calendarFile;

    private boolean syncBidirectional;

    public LocalIcsConfig(String name, String calendarFile, boolean importAlarms, boolean keepLocalChanges, boolean syncBidirectional) {
        super(name, importAlarms, keepLocalChanges);
        this.calendarFile = Uri.parse(calendarFile);
        this.syncBidirectional = syncBidirectional;
    }
}

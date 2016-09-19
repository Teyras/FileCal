package cz.bucharjan.filecal.data;

import java.util.Collection;
import java.util.Date;

/**
 * A container for event data
 */
public class Event {
    private String uid;
    private Long localId;

    private Date start;
    private Date end;
    private String duration;

    private String title; // summary
    private String description;

    /** A textual description of the event location */
    private String location;

    /** E-mail address of the organizer */
    private String organizer;

    /** True if the event doesn't block the user for other events */
    private boolean available; // transp

    private Collection<String> recurrenceRules;
    private Collection<Date> recurrenceDates;

    private Collection<String> recurrenceExclusionRules;
    private Collection<Date> recurrenceExclusionDates;

    public Event(String uid, Long localId, Date start, Date end, String duration,
                 String title, String description, String location,
                 String organizer, boolean available, Collection<String> recurrenceRules,
                 Collection<Date> recurrenceDates, Collection<String> recurrenceExclusionRules,
                 Collection<Date> recurrenceExclusionDates) {
        this.uid = uid;
        this.localId = localId;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.title = title;
        this.description = description;
        this.location = location;
        this.organizer = organizer;
        this.available = available;
        this.recurrenceRules = recurrenceRules;
        this.recurrenceDates = recurrenceDates;
        this.recurrenceExclusionRules = recurrenceExclusionRules;
        this.recurrenceExclusionDates = recurrenceExclusionDates;
    }

    public String getUid() {
        return uid;
    }

    public Long getLocalId() {
        return localId;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getOrganizer() {
        return organizer;
    }

    public boolean isAvailable() {
        return available;
    }

    public Collection<String> getRecurrenceRules() {
        return recurrenceRules;
    }

    public Collection<Date> getRecurrenceDates() {
        return recurrenceDates;
    }

    public Collection<String> getRecurrenceExclusionRules() {
        return recurrenceExclusionRules;
    }

    public Collection<Date> getRecurrenceExclusionDates() {
        return recurrenceExclusionDates;
    }

    public String getDuration() {
        return duration;
    }
}

package cz.bucharjan.filecal.data;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.TimeFormatException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import cz.bucharjan.filecal.FileCalException;

/**
 * Provides access to events in an Android calendar
 */
public class AndroidEventRepository implements EventRepository {
    private final static String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Events._ID,
            CalendarContract.Events.UID_2445,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DURATION,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.ORGANIZER,
            CalendarContract.Events.AVAILABILITY,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.RDATE,
            CalendarContract.Events.EXRULE,
            CalendarContract.Events.EXDATE,
    };

    private static final int EVENT_LOCAL_ID = 0;
    private static final int EVENT_ID = 1;
    private static final int EVENT_ALL_DAY = 2;
    private static final int EVENT_DTSTART = 3;
    private static final int EVENT_DTEND = 4;
    private static final int EVENT_DURATION = 5;
    private static final int EVENT_TITLE = 6;
    private static final int EVENT_DESCRIPTION = 7;
    private static final int EVENT_LOCATION = 8;
    private static final int EVENT_ORGANIZER = 9;
    private static final int EVENT_AVAILABILITY = 10;
    private static final int EVENT_RRULE = 11;
    private static final int EVENT_RDATE = 12;
    private static final int EVENT_EXRULE = 13;
    private static final int EVENT_EXDATE = 14;

    private static final String ROW_SEPARATOR = "\n";

    private ContentResolver resolver;

    private long calendarId;

    private Account account;

    public AndroidEventRepository(ContentResolver resolver, long calendarId) {
        this.resolver = resolver;
        this.calendarId = calendarId;
    }

    @Override
    public Collection<Event> getAll() throws FileCalException {
        String selection = String.format("(%s = ?)",
                CalendarContract.Events.CALENDAR_ID
        );
        String[] selectionArgs = new String[]{
                String.valueOf(calendarId)
        };

        Cursor cursor = resolver.query(getEventUri(), EVENT_PROJECTION, selection, selectionArgs, null);
        Collection<Event> result = new ArrayList<>();

        while (cursor.moveToNext()) {
            Event event = new Event(
                    cursor.getString(EVENT_ID),
                    cursor.getLong(EVENT_LOCAL_ID),
                    new Date(Long.parseLong(cursor.getString(EVENT_DTSTART))),
                    new Date(Long.parseLong(cursor.getString(EVENT_DTEND))),
                    cursor.getString(EVENT_DURATION),
                    cursor.getString(EVENT_TITLE),
                    cursor.getString(EVENT_DESCRIPTION),
                    cursor.getString(EVENT_LOCATION),
                    cursor.getString(EVENT_ORGANIZER),
                    cursor.getInt(EVENT_AVAILABILITY) != CalendarContract.Events.AVAILABILITY_BUSY,
                    Arrays.asList(cursor.getString(EVENT_RRULE).split(ROW_SEPARATOR)),
                    parseRecurrenceDates(cursor.getString(EVENT_RDATE)),
                    Arrays.asList(cursor.getString(EVENT_EXRULE).split(ROW_SEPARATOR)),
                    parseRecurrenceDates(cursor.getString(EVENT_EXDATE))
            );

            result.add(event);
        }

        return result;
    }

    /**
     *
     * Mostly copied from com.android.calendarcommon2.RecurrenceSet
     * @param values
     * @return
     * @throws FileCalException
     */
    private List<Date> parseRecurrenceDates(String values) throws FileCalException {
        List<Date> result = new ArrayList<>();

        for (String value : values.split(ROW_SEPARATOR)) {
                String tz = Time.TIMEZONE_UTC;
            int tzidx = value.indexOf(";");
            if (tzidx != -1) {
                tz = value.substring(0, tzidx);
                value = value.substring(tzidx + 1);
            }
            Time time = new Time(tz);
            String[] rawDates = value.split(",");
            int n = rawDates.length;
            for (String rawDate : rawDates) {
                // The timezone is updated to UTC if the time string specified 'Z'.
                try {
                    time.parse(rawDate);
                } catch (TimeFormatException e) {
                    throw new FileCalException(
                            "TimeFormatException thrown when parsing time " + rawDate
                                    + " in recurrence " + value, e);

                }
                result.add(new Date(time.toMillis(false /* use isDst */)));
                time.timezone = tz;
            }
        }

        return result;
    }

    @Override
    public void removeAll(Collection<Event> events) throws FileCalException {
        for (Event event : events) {
            String selection = String.format(
                    "((%1$s IS NOT NULL AND %1$s = ?) OR (%2$s IS NOT NULL AND %2$s = ?))",
                    CalendarContract.Events._ID,
                    CalendarContract.Events.UID_2445
            );
            String[] selectionArgs = new String[]{
                    String.valueOf(event.getLocalId()),
                    String.valueOf(event.getUid())
            };

            resolver.delete(getEventUri(), selection, selectionArgs);
        }
    }

    @Override
    public void insertAll(Collection<Event> events) throws FileCalException {
        for (Event event : events) {
            ContentValues values = eventToContentValues(event);

            if (event.getLocalId() != null) {
                Uri uri = ContentUris.withAppendedId(getEventUri(), event.getLocalId());
                resolver.update(uri, values, null, null);
            } else {
                resolver.insert(getEventUri(), values);
            }
        }
    }

    private ContentValues eventToContentValues(Event event) {
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.UID_2445, event.getUid());
        values.put(CalendarContract.Events._ID, event.getLocalId());
        values.put(CalendarContract.Events.DTSTART, event.getStart().getTime());
        values.put(CalendarContract.Events.DTEND, event.getEnd().getTime());
        values.put(CalendarContract.Events.DURATION, event.getDuration());
        values.put(CalendarContract.Events.TITLE, event.getTitle());
        values.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
        values.put(CalendarContract.Events.EVENT_LOCATION, event.getLocation());
        values.put(CalendarContract.Events.ORGANIZER, event.getOrganizer());
        values.put(CalendarContract.Events.AVAILABILITY, event.isAvailable()
                 ? CalendarContract.Events.AVAILABILITY_FREE
                 : CalendarContract.Events.AVAILABILITY_BUSY);
        values.put(CalendarContract.Events.RRULE, joinStrings(event.getRecurrenceRules()));
        values.put(CalendarContract.Events.RDATE, joinDates(event.getRecurrenceDates()));
        values.put(CalendarContract.Events.EXRULE, joinStrings(event.getRecurrenceExclusionRules()));
        values.put(CalendarContract.Events.EXDATE, joinDates(event.getRecurrenceExclusionDates()));

        return values;
    }

    private Uri getEventUri() {
        return CalendarContract.Events.CONTENT_URI.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type).build();

    }

    private static String joinStrings(Collection<String> strings) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (String string : strings) {
            if (first) {
                builder.append(string);
                first = false;
            } else {
                builder.append(ROW_SEPARATOR);
                builder.append(string);
            }
        }

        return builder.toString();
    }

    private static String joinDates(Collection<Date> dates) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (Date date: dates) {
            if (first) {
                builder.append(date.getTime());
                builder.append("Z");
                first = false;
            } else {
                builder.append(ROW_SEPARATOR);
                builder.append(date.getTime());
                builder.append("Z");
            }
        }

        return builder.toString();
    }

    @Override
    public void saveChanges() throws FileCalException {

    }
}

package cz.bucharjan.filecal.data;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import cz.bucharjan.filecal.FileCalException;

/**
 * Provides access to events in an ics file
 */
public abstract class IcsEventRepository implements EventRepository {
    private InputStream input;
    private OutputStream output;

    protected Collection<Event> data = null;

    public IcsEventRepository(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    private void load() throws FileCalException {
        if (data != null) {
            return;
        }

        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar;

        try {
            calendar = builder.build(input);
        } catch (IOException | ParserException e) {
            throw new FileCalException("Loading events failed", e);
        }

        data = new ArrayList<>();

        for (Object o : calendar.getComponents()) {
            Component component = (Component) o;

            if (!(component instanceof VEvent)) {
                continue;
            }

            VEvent icalEvent = (VEvent) component;

            Event event = new Event(
                    getString(icalEvent.getUid()),
                    null, getDate(icalEvent.getStartDate()),
                    getDate(icalEvent.getEndDate()),
                    getString(icalEvent.getDuration()),
                    getString(icalEvent.getSummary()),
                    getString(icalEvent.getDescription()),
                    getString(icalEvent.getLocation()),
                    getString(icalEvent.getOrganizer()),
                    Transp.TRANSPARENT.equals(icalEvent.getTransparency()),
                    getStringList(component, Property.RRULE),
                    getDateList(component, Property.RDATE),
                    getStringList(component, Property.EXRULE),
                    getDateList(component, Property.EXDATE)
            );

            data.add(event);
        }
    }

    private String getString(Property property) {
        if (property == null) {
            return null;
        }

        return property.getValue();
    }

    private Date getDate(DateProperty property) {
        if (property == null) {
            return null;
        }

        return property.getDate();
    }

    private List<String> getStringList(Component component, String propertyName) {
        List<String> result = new ArrayList<>();

        for (Object o : component.getProperties(propertyName)) {
            Property property = (Property) o;
            result.add(property.getValue());
        }

        return result;
    }

    private List<Date> getDateList(Component component, String propertyName) throws FileCalException {
        List<Date> result = new ArrayList<>();
        DateListProperty dates = (DateListProperty) component.getProperty(propertyName);

        if (dates == null) {
            return result;
        }

        for (Object o : dates.getDates()) {
            if (!(o instanceof DateTime)) {
                throw new FileCalException("unexpected value in a date list");
            }

            result.add((Date) o);
        }

        return result;
    }

    @Override
    public Collection<Event> getAll() throws FileCalException {
        load();
        return data;
    }

    @Override
    public void removeAll(Collection<Event> events) throws FileCalException {
        load();
        // TODO
    }

    @Override
    public void insertAll(Collection<Event> events) throws FileCalException {
        load();
        // TODO
    }

    @Override
    public void saveChanges() throws FileCalException {
        if (output == null) {
            throw new FileCalException("No output stream given");
        }

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Jan Buchar//FileCal 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        for (Event event : data) {
            PropertyList properties = new PropertyList();

            if (event.getUid() != null) {
                properties.add(new Uid(event.getUid()));
            }

            if (event.getStart() != null) {
                properties.add(new DtStart(new net.fortuna.ical4j.model.Date(event.getStart())));
            }

            if (event.getDuration() != null) {
                properties.add(new Duration(new Dur(event.getDuration())));
            } else if (event.getEnd() != null){
                properties.add(new DtEnd(new net.fortuna.ical4j.model.Date(event.getEnd())));
            }

            if (event.getTitle() != null) {
                properties.add(new Summary(event.getTitle()));
            }

            if (event.getDescription() != null) {
                properties.add(new Description(event.getDescription()));
            }

            if (event.getLocation() != null) {
                properties.add(new Location(event.getLocation()));
            }

            if (event.getOrganizer() != null) {
                try {
                    properties.add(new Organizer(event.getOrganizer()));
                } catch (URISyntaxException e) {
                    throw new FileCalException("error when saving organizer e-mail", e);
                }
            }

            properties.add(event.isAvailable() ? Transp.TRANSPARENT : Transp.OPAQUE);

            for (String rule : event.getRecurrenceRules()) {
                try {
                    properties.add(new RRule(rule));
                } catch (ParseException e) {
                    throw new FileCalException("error when saving recurrence rules", e);
                }
            }

            if (event.getRecurrenceDates().size() > 0) {
                DateList dates = new DateList();

                for (Date date : event.getRecurrenceDates()) {
                    dates.add(date);
                }

                properties.add(new RDate(dates));
            }

            for (String rule : event.getRecurrenceExclusionRules()) {
                try {
                    properties.add(new ExRule(new Recur(rule)));
                } catch (ParseException e) {
                    throw new FileCalException("error when saving recurrence exclustion rules", e);
                }
            }

            if (event.getRecurrenceExclusionDates().size() > 0) {
                DateList dates = new DateList();

                for (Date date : event.getRecurrenceExclusionDates()) {
                    dates.add(date);
                }

                properties.add(new ExDate(dates));
            }

            calendar.getComponents().add(new VEvent(properties));
        }

        CalendarOutputter out = new CalendarOutputter();
        try {
            out.output(calendar, output);
        } catch (ValidationException e) {
            throw new FileCalException("invalid value in exported ical file", e);
        } catch (IOException e) {
            throw new FileCalException("error writing to the destination file", e);
        }
    }
}

package org.toulibre.capitoledulibre.api;

/**
 * This class contains all CDL Urls
 *
 * @author Christophe Beyls
 */
public class Urls {

    private static final String SCHEDULE_URL = "https://participez-2018.capitoledulibre.org/schedule/xml/";

    //private static final String EVENT_URL_FORMAT = "https://participez-2018.capitoledulibre.org/programme/presentation/%d/";

    //private static final String PERSON_URL_FORMAT = "https://participez-2018.capitoledulibre.org/speaker/profile/%d/";

    public static String getSchedule() {
        return SCHEDULE_URL;
    }

    public static String getEvent(long id) {
        return null;//String.format(Locale.US, EVENT_URL_FORMAT, id);
    }

    public static String getPerson(long id) {
        return null;//String.format(Locale.US, PERSON_URL_FORMAT, id);
    }
}

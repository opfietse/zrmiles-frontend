package net.opfietse.zrmiles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static net.opfietse.zrmiles.Constants.DATE_PREFERENCE_EUROPEAN;
import static net.opfietse.zrmiles.Constants.FORMATTER_DATE_FORMAT_AMERICAN;
import static net.opfietse.zrmiles.Constants.FORMATTER_DATE_FORMAT_EUROPEAN;

//@TemplateExtension
public class DateExtension {
    public static final DateTimeFormatter EUROPEAN_DATE_FORMAT = DateTimeFormatter.ofPattern(FORMATTER_DATE_FORMAT_EUROPEAN);
    public static final DateTimeFormatter AMERICAN_DATE_FORMAT = DateTimeFormatter.ofPattern(FORMATTER_DATE_FORMAT_AMERICAN);

    public static String formatLocalDate(LocalDate date, String formatPreference) {
        DateTimeFormatter formatter = AMERICAN_DATE_FORMAT;
        if (formatPreference != null && formatPreference.contains(DATE_PREFERENCE_EUROPEAN)) {
            formatter = EUROPEAN_DATE_FORMAT;
        }

        return date.format(formatter);
    }
}

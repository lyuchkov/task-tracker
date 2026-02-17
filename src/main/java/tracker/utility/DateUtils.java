package tracker.utility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {
    private final static String DURATION_PATTERN = "%02d ч. %02d м.";
    private final static DateTimeFormatter LOCAL_DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String getPrettyString(Duration duration) {
        if (duration == null) return "";

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        return String.format(DURATION_PATTERN, hours, minutes);
    }

    public static String getPrettyString(LocalDateTime localDateTime) {
        if (localDateTime == null) return "";

        return localDateTime.format(LOCAL_DATE_TIME_PATTERN);
    }
}

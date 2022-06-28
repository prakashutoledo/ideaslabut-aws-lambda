/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.extractor.util;

/**
 * Utility class for formatting millis into string representation
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 28, 2022
 */
final class FormatterUtil {
    private final static int TOTAL_MILLIS_IN_A_SECOND = 1000;
    private final static int MAX_MINUTES_OR_SECONDS = 60;

    private final static int MINUTE_TO_MILLIS = TOTAL_MILLIS_IN_A_SECOND * MAX_MINUTES_OR_SECONDS;
    private final static int HOUR_TO_MILLIS = MINUTE_TO_MILLIS * MAX_MINUTES_OR_SECONDS;

    private static final String DAY_HOUR_MINUTE_SECONDS_MILLIS_FORMAT = "%02d:%02d:%02d:%03d";

    /**
     * Formats the given millis into String represented by {@link FormatterUtil#DAY_HOUR_MINUTE_SECONDS_MILLIS_FORMAT}
     *
     * @param millis a millis to format
     *
     * @return a formatted string representation of given millis
     */
    static String formattedMillis(long millis) {
        long hrs = millis / HOUR_TO_MILLIS;
        millis = millis % HOUR_TO_MILLIS;

        long minutes = millis / MINUTE_TO_MILLIS;
        millis = millis % MINUTE_TO_MILLIS;

        long seconds = millis / TOTAL_MILLIS_IN_A_SECOND;
        millis = millis % TOTAL_MILLIS_IN_A_SECOND;

        return String.format(DAY_HOUR_MINUTE_SECONDS_MILLIS_FORMAT, hrs, minutes, seconds, millis);
    }

    private FormatterUtil() {
    }
}

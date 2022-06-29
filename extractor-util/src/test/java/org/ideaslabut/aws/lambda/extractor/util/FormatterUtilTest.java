/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.extractor.util;

import static org.ideaslabut.aws.lambda.extractor.util.FormatterUtil.formattedMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit test for {@link FormatterUtil#formattedMillis(long)}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 29, 2022
 */
class FormatterUtilTest {
    @Test
    void formatInvalidMillis() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> formattedMillis(-1),
            "Millis is negative which will throw exception"
        );

        assertNotNull(exception, "Exception should not be null");
        assertEquals("Millis should be greater than zero", exception.getMessage(), "Exception message");
    }

    @CsvSource(
        value = {
            "0,00:00:00:000",
            "1,00:00:00:001",
            "1000,00:00:01:000",
            "9999,00:00:09:999",
            "10999,00:00:10:999",
            "86399999,23:59:59:999"
        },
        delimiter = ','
    )
    @ParameterizedTest
    void formatValidMillis(int millis, String expectedFormattedMillis) {
        assertEquals(expectedFormattedMillis, formattedMillis(millis), "Formatted millis to string");
    }
}

/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.extractor.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

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
            () -> FormatterUtil.formattedMillis(-1),
            "Millis is negative which will throw exception"
        );

        assertNotNull(exception, "Exception should not be null");
        assertEquals("Millis should be greater than zero", exception.getMessage(), "Exception message");
    }

    @ParameterizedTest
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
    void formatValidMillis(int millis, String expectedFormat) {
        var formattedMillis = FormatterUtil.formattedMillis(millis);
        assertEquals(expectedFormat, formattedMillis, "Formatted millis");
    }
}

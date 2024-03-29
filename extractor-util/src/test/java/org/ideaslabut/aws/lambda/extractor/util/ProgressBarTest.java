/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.extractor.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;

/**
 * Unit test for {@link ProgressBar}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 29, 2022
 */
class ProgressBarTest {
    @Test
    void validProgressBarBuilder() {
        assertNotNull(ProgressBar.builder(), "Progress bar builder shouldn't be null");
        var progressBar = ProgressBar.builder().withMaxStep(100).withDelimiter(',').withPrefix(null).build();
        assertNotNull(progressBar, "Created progress bar should not be null");
    }

    @Test
    void invalidProgressBarBuilder() {
        assertThrows(IllegalArgumentException.class, () -> ProgressBar.builder().withMaxStep(-1));
        assertThrows(IllegalArgumentException.class, () -> ProgressBar.builder().withMaxStep(101));
        assertThrows(IllegalArgumentException.class, () -> ProgressBar.builder().withTotalElement(-1));
        assertThrows(NullPointerException.class, () -> ProgressBar.builder().withPrintStream(null));
    }

    @Test
    void failedProgressBarTest() {
        var progressBar = ProgressBar.builder().build();
        assertThrows(NullPointerException.class,() -> progressBar.initStartTime(null));
    }

    @Test
    void validProgressBar() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        var progressBar = ProgressBar
            .builder()
            .withMaxStep(5)
            .withPrefix("Some Prefix")
            .withDelimiter('#')
            .withPrintStream(new PrintStream(outputStream))
            .withTotalElement(5)
            .build();

        var first = Instant.now();
        var second = first.plusSeconds(2).plusMillis(999);
        var third = second.plusSeconds(71).plusMillis(123);
        var fourth = third.plusSeconds(32).plusMillis(222);

        var instantMock = mockStatic(Instant.class);
        instantMock.when(Instant::now).thenReturn(first, second, third, fourth);

        progressBar.initStartTime();

        progressBar.updateTo(2);
        assertEquals(
            "Some Prefix [##   ] [2/5] [ 40%] [00:00:02:999] [|]",
            outputStream.toString().trim(),
            "Makes current element size to 2 which is 40% progress"
        );

        outputStream.reset();
        progressBar.updateBy(1);
        assertEquals(
            "Some Prefix [###  ] [3/5] [ 60%] [00:01:14:122] [-]",
            outputStream.toString().trim(),
            "Adds 1 to current size making it 3 to be 60% complete"
        );

        outputStream.reset();
        progressBar.updateBy(100);
        assertEquals(
            "Some Prefix [#####] [5/5] [100%] [00:01:46:344] [done...]",
            outputStream.toString().trim(),
            "Adds 100 to current element size making greater than 5 which is 100% complete"
        );
        instantMock.verify(Instant::now, times(4));
    }
}

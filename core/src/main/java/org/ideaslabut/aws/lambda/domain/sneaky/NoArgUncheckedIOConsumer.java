/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.sneaky;

import java.io.IOException;

/**
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public interface NoArgUncheckedIOConsumer<E extends IOException> {
    void accept() throws E;

    static NoArgConsumer wrap(NoArgUncheckedIOConsumer<? super IOException> noArgConsumer) {
        return () -> {
            try {
                noArgConsumer.accept();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        };
    }
}

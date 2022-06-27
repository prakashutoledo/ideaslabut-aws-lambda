/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.sneaky;

/**
 * A no argument consumer
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
@FunctionalInterface
public interface NoArgConsumer {
    /**
     * Performs the operation
     */
    void accept();
}

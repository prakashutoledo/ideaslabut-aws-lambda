/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link WebSocketService}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jul 01, 2022
 */
class WebsocketServiceTest {
    @Test
    void getInstance() {
        assertNotNull(WebSocketService.getInstance(), "Singleton instance should be created");
    }
}

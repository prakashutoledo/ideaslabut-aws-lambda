/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ElasticsearchService}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
public class ElasticsearchServiceTest {
    @Test
    void getInstance() {
        assertNotNull(ElasticsearchService.getInstance(), "Singleton instance should be created");
    }
}

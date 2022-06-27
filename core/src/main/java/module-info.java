/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
/**
 * @author Prakash Khadka <br>
 *         Created on: Jan 30, 2022
 */
module ideaslabut.aws.lambda.core {
    requires transitive java.net.http;
    requires transitive com.fasterxml.jackson.databind;

    // Below 4 are automatic modules
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.apigatewaymanagementapi;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.http.urlconnection;

    exports org.ideaslabut.aws.lambda.domain.elasticsearch;
    exports org.ideaslabut.aws.lambda.domain.elasticsearch.request;
    exports org.ideaslabut.aws.lambda.domain.websocket;
    exports org.ideaslabut.aws.lambda.service;
    exports org.ideaslabut.aws.lambda.domain.sneaky;
}
module ideaslabut.aws.lambda.core {
    requires transitive java.net.http;
    requires transitive com.fasterxml.jackson.databind;

    // Below 3 are automatic modules
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.apigatewaymanagementapi;
    requires software.amazon.awssdk.regions;

    exports org.ideaslabut.aws.lambda.domain.elasticsearch.request;
    exports org.ideaslabut.aws.lambda.domain.websocket;
    exports org.ideaslabut.aws.lambda.domain.elasticsearch;
    exports org.ideaslabut.aws.lambda.service;
}
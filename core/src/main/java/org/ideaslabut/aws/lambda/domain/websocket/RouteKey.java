/*
 * Copyright 2022 IDEAS Lab @ University of Toledo.. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.websocket;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Route key selection for api gateway websocket connection
 *
 * @author Prakash Khadka <br>
 *     Created on: Jan 30, 2022
 */
public enum RouteKey {
    CONNECT("$connect"),
    DISCONNECT("$disconnect"),
    DEFAULT("$default"),
    SEND_MESSAGE("sendMessage");

    private static final Map<String, RouteKey> ACTION_ROUTE_KEY_MAP;

    static {
        ACTION_ROUTE_KEY_MAP = Arrays.stream(RouteKey.values()).collect(toMap(RouteKey::getAction, identity()));
    }

    public static Optional<RouteKey> fromAction(String action) {
        return Optional.ofNullable(ACTION_ROUTE_KEY_MAP.get(action));
    }
    private final String action;

    RouteKey(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}

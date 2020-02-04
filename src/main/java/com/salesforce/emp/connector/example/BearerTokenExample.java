/*
 * Copyright (c) 2016, salesforce.com, inc. All rights reserved. Licensed under the BSD 3-Clause license. For full
 * license text, see LICENSE.TXT file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.emp.connector.example;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.salesforce.emp.connector.BayeuxParameters;
import com.salesforce.emp.connector.EmpConnector;
import com.salesforce.emp.connector.TopicSubscription;
import org.cometd.bayeux.Channel;
import org.eclipse.jetty.util.ajax.JSON;

/**
 * An example of using the EMP connector using bearer tokens
 *
 * @author hal.hildebrand
 * @since API v37.0
 * https://login.salesforce.com/ 
 /topic/ExternalRoutingPSR
 */
public class BearerTokenExample {
	private final static String login = "https://na174.salesforce.com";
	private final static String token = "00D6g000000FMJm!ARsAQNKojZywZYGfIMP7d4SI8Gd_XHmAZmzRcNEwGOkj2wHhMJeoUCmkASHJUdDqwYG9bXS2BeukC3YUHiqfxWUZXWT8wszD";
	private final static String topic = "/topic/ExternalRoutingPSR";
	private final static long replayFrom = EmpConnector.REPLAY_FROM_EARLIEST;
    public static void main(String[] argv) throws Exception {
       

        BayeuxParameters params = new BayeuxParameters() {

            @Override
            public String bearerToken() {
                return token;
            }

            @Override
            public URL host() {
                try {
                    return new URL(login);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(String.format("Unable to create url: %s", login), e);
                }
            }
        };

        Consumer<Map<String, Object>> consumer = event -> System.out.println(String.format("Received:\n%s", JSON.toString(event)));
        EmpConnector connector = new EmpConnector(params);

        connector.addListener(Channel.META_CONNECT, new LoggingListener(true, true))
        .addListener(Channel.META_DISCONNECT, new LoggingListener(true, true))
        .addListener(Channel.META_HANDSHAKE, new LoggingListener(true, true));

        connector.start().get(5, TimeUnit.SECONDS);

        TopicSubscription subscription = connector.subscribe(topic, replayFrom, consumer).get(5, TimeUnit.SECONDS);

        System.out.println(String.format("Subscribed: %s", subscription));
    }
}

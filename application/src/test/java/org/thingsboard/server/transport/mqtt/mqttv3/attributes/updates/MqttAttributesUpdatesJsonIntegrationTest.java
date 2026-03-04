/**
 * Copyright © 2016-2026 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.mqtt.mqttv3.attributes.updates;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.server.common.data.TransportPayloadType;
import org.thingsboard.server.common.msg.session.FeatureType;
import org.thingsboard.server.dao.service.DaoSqlTest;
import org.thingsboard.server.transport.mqtt.MqttTestConfigProperties;
import org.thingsboard.server.transport.mqtt.mqttv3.attributes.AbstractMqttAttributesIntegrationTest;

import static org.thingsboard.server.common.data.device.profile.MqttTopics.DEVICE_ATTRIBUTES_SHORT_JSON_TOPIC;
import static org.thingsboard.server.common.data.device.profile.MqttTopics.DEVICE_ATTRIBUTES_SHORT_TOPIC;
import static org.thingsboard.server.common.data.device.profile.MqttTopics.DEVICE_ATTRIBUTES_TOPIC;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.transport.mqtt.mqttv3.MqttTestCallback;
import org.thingsboard.server.transport.mqtt.mqttv3.MqttTestClient;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@DaoSqlTest
public class MqttAttributesUpdatesJsonIntegrationTest extends AbstractMqttAttributesIntegrationTest {

    private static final String SHARED_ATTRIBUTES_PAYLOAD =
            "{\"sharedStr\":\"value1\",\"sharedBool\":true,\"sharedDbl\":42.0,\"sharedLong\":73," +
                    "\"sharedJson\":{\"someNumber\":42,\"someArray\":[1,2,3],\"someNestedObject\":{\"key\":\"value\"}}}";

    @Before
    public void beforeTest() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Subscribe to attribute updates")
                .gatewayName("Gateway Test Subscribe to attribute updates")
                .transportPayloadType(TransportPayloadType.JSON)
                .build();
        processBeforeTest(configProperties);
    }

    @Test
    public void testJsonSubscribeToAttributesUpdatesFromTheServer() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(DEVICE_ATTRIBUTES_TOPIC);
    }

    @Test
    public void testJsonSubscribeToAttributesUpdatesFromTheServerOnShortTopic() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(DEVICE_ATTRIBUTES_SHORT_TOPIC);
    }

    @Test
    public void testJsonSubscribeToAttributesUpdatesFromTheServerOnShortJsonTopic() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(DEVICE_ATTRIBUTES_SHORT_JSON_TOPIC);
    }

    @Test
    public void testJsonSubscribeToAttributesUpdatesFromTheServerGateway() throws Exception {
        processJsonGatewayTestSubscribeToAttributesUpdates();
    }

    @Test
    public void testSubscribeToAttributesUpdatesRecordsTopicWithStub() throws Exception {
        MqttTestClient client = new MqttTestClient();
        client.connectAndWait(accessToken);

        TopicCapturingMqttTestCallback callback = new TopicCapturingMqttTestCallback();
        client.setCallback(callback);

        subscribeAndWait(client, DEVICE_ATTRIBUTES_TOPIC, savedDevice.getId(), FeatureType.ATTRIBUTES);

        doPostAsync("/api/plugins/telemetry/DEVICE/" + savedDevice.getId().getId()
                + "/attributes/SHARED_SCOPE", SHARED_ATTRIBUTES_PAYLOAD, String.class, status().isOk());

        assertThat(callback.getSubscribeLatch().await(DEFAULT_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .as("await callback").isTrue();

        assertNotNull(callback.getPayloadBytes());
        assertEquals(JacksonUtil.toJsonNode(SHARED_ATTRIBUTES_PAYLOAD),
                JacksonUtil.fromBytes(callback.getPayloadBytes()));
        assertEquals(DEVICE_ATTRIBUTES_TOPIC, callback.getLastTopic());

        client.disconnect();
    }

    private static class TopicCapturingMqttTestCallback extends MqttTestCallback {
        private String lastTopic;

        @Override
        public void messageArrived(String requestTopic, MqttMessage mqttMessage) {
            super.messageArrived(requestTopic, mqttMessage);
            lastTopic = requestTopic;
        }

        public String getLastTopic() {
            return lastTopic;
        }
    }
}

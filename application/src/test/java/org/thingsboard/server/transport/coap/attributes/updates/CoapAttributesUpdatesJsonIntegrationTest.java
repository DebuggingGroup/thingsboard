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
package org.thingsboard.server.transport.coap.attributes.updates;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.server.common.data.CoapDeviceType;
import org.thingsboard.server.common.data.TransportPayloadType;
import org.thingsboard.server.common.msg.session.FeatureType;
import org.thingsboard.server.dao.service.DaoSqlTest;
import org.thingsboard.server.transport.coap.CoapTestCallback;
import org.thingsboard.server.transport.coap.CoapTestClient;
import org.thingsboard.server.transport.coap.CoapTestConfigProperties;
import org.thingsboard.server.transport.coap.attributes.AbstractCoapAttributesIntegrationTest;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
@DaoSqlTest
public class CoapAttributesUpdatesJsonIntegrationTest extends AbstractCoapAttributesIntegrationTest {

    private static final String SHARED_ATTRIBUTES_PAYLOAD = "{\"sharedStr\":\"value1\",\"sharedBool\":true,\"sharedDbl\":42.0,\"sharedLong\":73," +
            "\"sharedJson\":{\"someNumber\":42,\"someArray\":[1,2,3],\"someNestedObject\":{\"key\":\"value\"}}}";
    @Before
    public void beforeTest() throws Exception {
        CoapTestConfigProperties configProperties = CoapTestConfigProperties.builder()
                .deviceName("Test Subscribe to attribute updates")
                .coapDeviceType(CoapDeviceType.DEFAULT)
                .transportPayloadType(TransportPayloadType.JSON)
                .build();
        processBeforeTest(configProperties);
    }

    @After
    public void afterTest() throws Exception {
        processAfterTest();
    }

    @Test
    public void testSubscribeToAttributesUpdatesFromTheServer() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(false);
    }

    @Test
    public void testSubscribeToAttributesUpdatesFromTheServerWithEmptyCurrentStateNotification() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(true);
    }

    private static class CountingCoapTestCallback extends CoapTestCallback {
        private int loadCount = 0;

        @Override
        public void onLoad(CoapResponse response) {
            super.onLoad(response);
            loadCount++;
        }

        public int getLoadCount() {
            return loadCount;
        }
    }

    @Test
    public void testSubscribeToAttributesUpdatesCountsNotificationsWithStub() throws Exception {
        client = new CoapTestClient(accessToken, FeatureType.ATTRIBUTES);
        CountingCoapTestCallback callbackCoap = new CountingCoapTestCallback();

        CoapObserveRelation observeRelation = client.getObserveRelation(callbackCoap);

        await("initial notification")
                .atMost(DEFAULT_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .until(() -> ResponseCode.CONTENT.equals(callbackCoap.getResponseCode())
                        && callbackCoap.getObserve() != null
                        && callbackCoap.getObserve() == 0);

        assertEquals(1, callbackCoap.getLoadCount());

        int expectedObserve = callbackCoap.getObserve() + 1;
        doPostAsync("/api/plugins/telemetry/DEVICE/" + savedDevice.getId().getId()
                + "/attributes/SHARED_SCOPE", SHARED_ATTRIBUTES_PAYLOAD, String.class, status().isOk());

        await("attribute update notification")
                .atMost(DEFAULT_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .until(() -> ResponseCode.CONTENT.equals(callbackCoap.getResponseCode())
                        && callbackCoap.getObserve() != null
                        && callbackCoap.getObserve() == expectedObserve);

        assertEquals(2, callbackCoap.getLoadCount());

        observeRelation.proactiveCancel();
        assertTrue(observeRelation.isCanceled());
    }
}

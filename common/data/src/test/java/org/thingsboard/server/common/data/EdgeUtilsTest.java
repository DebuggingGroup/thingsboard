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
package org.thingsboard.server.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.edge.EdgeEvent;
import org.thingsboard.server.common.data.edge.EdgeEventActionType;
import org.thingsboard.server.common.data.edge.EdgeEventType;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EdgeId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EdgeUtils - Edge event utility methods")
class EdgeUtilsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("nextPositiveInt() - should return a non-negative integer")
    void testNextPositiveInt() {
        int result = EdgeUtils.nextPositiveInt();

        assertThat(result).isGreaterThanOrEqualTo(0);
        assertThat(result).isLessThan(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("nextPositiveInt() - multiple calls return values in valid range")
    void testNextPositiveIntMultipleCalls() {
        for (int i = 0; i < 100; i++) {
            int result = EdgeUtils.nextPositiveInt();
            assertThat(result).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("getEdgeEventTypeByEntityType() - DEVICE maps to DEVICE EdgeEventType")
    void testGetEdgeEventTypeDevice() {
        EdgeEventType result = EdgeUtils.getEdgeEventTypeByEntityType(EntityType.DEVICE);

        assertThat(result).isEqualTo(EdgeEventType.DEVICE);
    }

    @Test
    @DisplayName("getEdgeEventTypeByEntityType() - DASHBOARD maps to DASHBOARD EdgeEventType")
    void testGetEdgeEventTypeDashboard() {
        EdgeEventType result = EdgeUtils.getEdgeEventTypeByEntityType(EntityType.DASHBOARD);

        assertThat(result).isEqualTo(EdgeEventType.DASHBOARD);
    }

    @Test
    @DisplayName("getEdgeEventTypeByEntityType() - ALARM maps to ALARM EdgeEventType")
    void testGetEdgeEventTypeAlarm() {
        EdgeEventType result = EdgeUtils.getEdgeEventTypeByEntityType(EntityType.ALARM);

        assertThat(result).isEqualTo(EdgeEventType.ALARM);
    }

    @Test
    @DisplayName("getEdgeEventTypeByEntityType() - ASSET maps to ASSET EdgeEventType")
    void testGetEdgeEventTypeAsset() {
        EdgeEventType result = EdgeUtils.getEdgeEventTypeByEntityType(EntityType.ASSET);

        assertThat(result).isEqualTo(EdgeEventType.ASSET);
    }

    @Test
    @DisplayName("getEdgeEventTypeByEntityType() - USER maps to USER EdgeEventType")
    void testGetEdgeEventTypeUser() {
        EdgeEventType result = EdgeUtils.getEdgeEventTypeByEntityType(EntityType.USER);

        assertThat(result).isEqualTo(EdgeEventType.USER);
    }

    @Test
    @DisplayName("getEdgeEventActionTypeByActionType() - ADDED maps to ADDED EdgeEventActionType")
    void testGetEdgeEventActionTypeAdded() {
        EdgeEventActionType result = EdgeUtils.getEdgeEventActionTypeByActionType(ActionType.ADDED);

        assertThat(result).isEqualTo(EdgeEventActionType.ADDED);
    }

    @Test
    @DisplayName("getEdgeEventActionTypeByActionType() - UPDATED maps to UPDATED EdgeEventActionType")
    void testGetEdgeEventActionTypeUpdated() {
        EdgeEventActionType result = EdgeUtils.getEdgeEventActionTypeByActionType(ActionType.UPDATED);

        assertThat(result).isEqualTo(EdgeEventActionType.UPDATED);
    }

    @Test
    @DisplayName("getEdgeEventActionTypeByActionType() - DELETED maps to DELETED EdgeEventActionType")
    void testGetEdgeEventActionTypeDeleted() {
        EdgeEventActionType result = EdgeUtils.getEdgeEventActionTypeByActionType(ActionType.DELETED);

        assertThat(result).isEqualTo(EdgeEventActionType.DELETED);
    }

    @Test
    @DisplayName("getEdgeEventActionTypeByActionType() - CREDENTIALS_UPDATED maps correctly")
    void testGetEdgeEventActionTypeCredentialsUpdated() {
        EdgeEventActionType result = EdgeUtils.getEdgeEventActionTypeByActionType(ActionType.CREDENTIALS_UPDATED);

        assertThat(result).isEqualTo(EdgeEventActionType.CREDENTIALS_UPDATED);
    }

    @Test
    @DisplayName("constructEdgeEvent() - should populate all fields correctly")
    void testConstructEdgeEvent() {
        TenantId tenantId = TenantId.fromUUID(UUID.randomUUID());
        EdgeId edgeId = new EdgeId(UUID.randomUUID());
        DeviceId deviceId = new DeviceId(UUID.randomUUID());
        ObjectNode body = mapper.createObjectNode();
        body.put("key", "value");

        EdgeEvent event = EdgeUtils.constructEdgeEvent(
                tenantId, edgeId, EdgeEventType.DEVICE,
                EdgeEventActionType.ADDED, deviceId, body);

        assertThat(event.getTenantId()).isEqualTo(tenantId);
        assertThat(event.getEdgeId()).isEqualTo(edgeId);
        assertThat(event.getType()).isEqualTo(EdgeEventType.DEVICE);
        assertThat(event.getAction()).isEqualTo(EdgeEventActionType.ADDED);
        assertThat(event.getEntityId()).isEqualTo(deviceId.getId());
        assertThat(event.getBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("constructEdgeEvent() - null entityId should leave entityId field null")
    void testConstructEdgeEventNullEntityId() {
        TenantId tenantId = TenantId.fromUUID(UUID.randomUUID());
        EdgeId edgeId = new EdgeId(UUID.randomUUID());

        EdgeEvent event = EdgeUtils.constructEdgeEvent(
                tenantId, edgeId, EdgeEventType.ALARM,
                EdgeEventActionType.UPDATED, null, null);

        assertThat(event.getTenantId()).isEqualTo(tenantId);
        assertThat(event.getEdgeId()).isEqualTo(edgeId);
        assertThat(event.getEntityId()).isNull();
        assertThat(event.getBody()).isNull();
    }

    @Test
    @DisplayName("constructEdgeEvent() - null body is acceptable")
    void testConstructEdgeEventNullBody() {
        TenantId tenantId = TenantId.fromUUID(UUID.randomUUID());
        EdgeId edgeId = new EdgeId(UUID.randomUUID());
        DeviceId deviceId = new DeviceId(UUID.randomUUID());

        EdgeEvent event = EdgeUtils.constructEdgeEvent(
                tenantId, edgeId, EdgeEventType.DEVICE,
                EdgeEventActionType.DELETED, deviceId, null);

        assertThat(event.getEntityId()).isEqualTo(deviceId.getId());
        assertThat(event.getBody()).isNull();
    }

    @Test
    @DisplayName("createErrorMsgFromRootCauseAndStackTrace() - formats exception with stack trace")
    void testCreateErrorMsgFromException() {
        RuntimeException exception = new RuntimeException("Test error");

        String errorMsg = EdgeUtils.createErrorMsgFromRootCauseAndStackTrace(exception);

        assertThat(errorMsg).startsWith("Test error");
        assertThat(errorMsg).contains("\n");
    }

    @Test
    @DisplayName("createErrorMsgFromRootCauseAndStackTrace() - handles nested exception")
    void testCreateErrorMsgNestedException() {
        RuntimeException root = new RuntimeException("Root cause");
        RuntimeException wrapper = new RuntimeException("Wrapper", root);

        String errorMsg = EdgeUtils.createErrorMsgFromRootCauseAndStackTrace(wrapper);

        assertThat(errorMsg).startsWith("Root cause");
    }

    @Test
    @DisplayName("createErrorMsgFromRootCauseAndStackTrace() - limits stack trace frames")
    void testCreateErrorMsgStackTraceLimit() {
        RuntimeException exception = new RuntimeException("Error with stack");

        String errorMsg = EdgeUtils.createErrorMsgFromRootCauseAndStackTrace(exception);

        long lineCount = errorMsg.lines().count();
        // Stack trace limit is 10 + 1 for the message itself = max 12 lines
        assertThat(lineCount).isLessThanOrEqualTo(12);
    }

    @Test
    @DisplayName("createErrorMsgFromRootCauseAndStackTrace() - null message produces empty prefix")
    void testCreateErrorMsgNullMessage() {
        RuntimeException exception = new RuntimeException((String) null);

        String errorMsg = EdgeUtils.createErrorMsgFromRootCauseAndStackTrace(exception);

        assertThat(errorMsg).isNotNull();
    }
}

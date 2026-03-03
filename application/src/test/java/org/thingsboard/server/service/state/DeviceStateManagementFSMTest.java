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
package org.thingsboard.server.service.state;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.msg.queue.TopicPartitionInfo;
import org.thingsboard.server.dao.attributes.AttributesService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.timeseries.TimeseriesService;
import org.thingsboard.server.queue.discovery.PartitionService;
import org.thingsboard.server.cluster.TbClusterService;
import org.thingsboard.server.common.msg.notification.NotificationRuleProcessor;
import org.thingsboard.server.service.telemetry.TelemetrySubscriptionService;
import org.thingsboard.server.common.stats.TbApiUsageReportClient;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListeningExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Device State Management FSM - States & Transitions")
public class DeviceStateManagementFSMTest {

    @Mock private DeviceService deviceService;
    @Mock private AttributesService attributesService;
    @Mock private TimeseriesService tsService;
    @Mock private TbClusterService clusterService;
    @Mock private PartitionService partitionService;
    @Mock private TelemetrySubscriptionService telemetrySubscriptionService;
    @Mock private NotificationRuleProcessor notificationRuleProcessor;
    @Mock private TbApiUsageReportClient apiUsageReportClient;
    
    private DefaultDeviceStateService service;
    private ListeningExecutorService executor;
    
    private TenantId tenantId;
    private DeviceId deviceId;
    private TopicPartitionInfo tpi;
    
    private static final long DEFAULT_INACTIVITY_TIMEOUT_MS = Duration.ofMinutes(10).toMillis();
    
    @BeforeEach
    void setUp() {
        tenantId = TenantId.fromUUID(UUID.randomUUID());
        deviceId = new DeviceId(UUID.randomUUID());
        
        tpi = TopicPartitionInfo.builder()
                .topic("tb_core")
                .partition(0)
                .myPartition(true)
                .build();
        
        service = spy(new DefaultDeviceStateService(
            deviceService, attributesService, tsService, clusterService, 
            partitionService, null, null, apiUsageReportClient, notificationRuleProcessor
        ));
        
        executor = MoreExecutors.newDirectExecutorService();
        
        ReflectionTestUtils.setField(service, "tsSubService", telemetrySubscriptionService);
        ReflectionTestUtils.setField(service, "defaultInactivityTimeoutMs", DEFAULT_INACTIVITY_TIMEOUT_MS);
        ReflectionTestUtils.setField(service, "deviceStateExecutor", executor);
    }
    
    @AfterEach
    void cleanup() {
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Test
    @DisplayName("State 1: DISCONNECTED_INACTIVE - Initial state properties")
    void testState_DisconnectedInactive() {
        DeviceState state = DeviceState.builder()
                .active(false)
                .lastConnectTime(0L)
                .lastActivityTime(0L)
                .lastDisconnectTime(0L)
                .lastInactivityAlarmTime(0L)
                .inactivityTimeout(DEFAULT_INACTIVITY_TIMEOUT_MS)
                .build();
        
        assertThat(state.isActive()).isFalse();
        assertThat(state.getLastConnectTime()).isZero();
        assertThat(state.getLastActivityTime()).isZero();
        assertThat(state.getLastDisconnectTime()).isZero();
    }
    

    @Test
    @DisplayName("State 2: CONNECTED_INACTIVE - Device connected without activity")
    void testState_ConnectedInactive() {
        long connectTime = System.currentTimeMillis();
        
        DeviceState state = DeviceState.builder()
                .active(false)
                .lastConnectTime(connectTime)
                .lastActivityTime(0L)
                .lastDisconnectTime(0L)
                .inactivityTimeout(DEFAULT_INACTIVITY_TIMEOUT_MS)
                .build();
        
        assertThat(state.isActive()).isFalse();
        assertThat(state.getLastConnectTime()).isGreaterThan(0);
        assertThat(state.getLastConnectTime()).isGreaterThan(state.getLastDisconnectTime());
        assertThat(state.getLastActivityTime()).isZero();
    }
    

    @Test
    @DisplayName("State 3: CONNECTED_ACTIVE - Device connected and active")
    void testState_ConnectedActive() {
        long currentTime = System.currentTimeMillis();
        long connectTime = currentTime - 10000;
        long activityTime = currentTime - 2000;
        
        DeviceState state = DeviceState.builder()
                .active(true)
                .lastConnectTime(connectTime)
                .lastActivityTime(activityTime)
                .lastDisconnectTime(0L)
                .lastInactivityAlarmTime(0L)
                .inactivityTimeout(DEFAULT_INACTIVITY_TIMEOUT_MS)
                .build();
        
        assertThat(state.isActive()).isTrue();
        assertThat(state.getLastConnectTime()).isGreaterThan(state.getLastDisconnectTime());
        assertThat(state.getLastActivityTime()).isGreaterThan(0);
        assertThat(currentTime - state.getLastActivityTime())
                .isLessThan(state.getInactivityTimeout());
    }
    
    @Test
    @DisplayName("State 4: DISCONNECTED_ACTIVE - Disconnected but recently active")
    void testState_DisconnectedActive() {
        long currentTime = System.currentTimeMillis();
        long disconnectTime = currentTime - 3000;
        long activityTime = currentTime - 5000;
        
        DeviceState state = DeviceState.builder()
                .active(true)
                .lastConnectTime(currentTime - 15000)
                .lastActivityTime(activityTime)
                .lastDisconnectTime(disconnectTime)
                .inactivityTimeout(DEFAULT_INACTIVITY_TIMEOUT_MS)
                .build();
        
        assertThat(state.isActive()).isTrue();
        assertThat(state.getLastDisconnectTime()).isGreaterThan(state.getLastConnectTime());
        assertThat(currentTime - state.getLastActivityTime())
                .isLessThan(state.getInactivityTimeout());
    }
    
    @Test
    @DisplayName("State 5: ALARMED - Inactivity timeout exceeded")
    void testState_Alarmed() {
        long currentTime = System.currentTimeMillis();
        long oldActivityTime = currentTime - DEFAULT_INACTIVITY_TIMEOUT_MS - 10000;
        long alarmTime = currentTime - 5000;
        
        DeviceState state = DeviceState.builder()
                .active(false)
                .lastActivityTime(oldActivityTime)
                .lastInactivityAlarmTime(alarmTime)
                .inactivityTimeout(DEFAULT_INACTIVITY_TIMEOUT_MS)
                .build();
        
        assertThat(state.isActive()).isFalse();
        assertThat(state.getLastInactivityAlarmTime()).isGreaterThan(0);
        assertThat(state.getLastInactivityAlarmTime()).isGreaterThan(state.getLastActivityTime());
        assertThat(currentTime - state.getLastActivityTime())
                .isGreaterThan(state.getInactivityTimeout());
    }
}

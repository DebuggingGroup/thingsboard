package org.thingsboard.server.service.entitiy.device;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.service.entitiy.TbLogEntityActionService;
import org.thingsboard.server.dao.device.DeviceCredentialsService;
import org.thingsboard.server.dao.device.ClaimDevicesService;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class DefaultTbDeviceServiceTest {

    private DefaultTbDeviceService tbDeviceService;
    private DeviceService deviceService;
    private TbLogEntityActionService logEntityActionService;

    private final TenantId tenantId = TenantId.fromUUID(UUID.randomUUID());
    private final DeviceId deviceId = new DeviceId(UUID.randomUUID());
    private final CustomerId customerId = new CustomerId(UUID.randomUUID());

    @Before
    public void setUp() {
        deviceService = mock(DeviceService.class);
        logEntityActionService = mock(TbLogEntityActionService.class);

        tbDeviceService = new DefaultTbDeviceService(
                deviceService,
                mock(DeviceCredentialsService.class),
                mock(ClaimDevicesService.class));
        setField(tbDeviceService, "logEntityActionService", logEntityActionService);
    }

    @Test
    public void whenDeviceDeleted_thenDeleteAndLogActionAreCalled() {
        Device device = new Device();
        device.setId(deviceId);
        device.setTenantId(tenantId);
        device.setCustomerId(customerId);

        User user = new User();

        tbDeviceService.delete(device, user);

        verify(deviceService).deleteDevice(tenantId, deviceId);

        verify(logEntityActionService).logEntityAction(
                tenantId, deviceId, device, customerId,
                ActionType.DELETED, user, deviceId.toString());
    }
}
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
package org.thingsboard.server.dao.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.thingsboard.server.cache.TbTransactionalCache;
import org.thingsboard.server.cache.user.UserCacheKey;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.security.event.UserCredentialsInvalidationEvent;
import org.thingsboard.server.dao.entity.EntityCountService;
import org.thingsboard.server.dao.eventsourcing.DeleteEntityEvent;
import org.thingsboard.server.dao.pat.ApiKeyService;
import org.thingsboard.server.dao.user.UserAuthSettingsDao;
import org.thingsboard.server.dao.user.UserCredentialsDao;
import org.thingsboard.server.dao.user.UserDao;
import org.thingsboard.server.dao.user.UserSettingsDao;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    // Explicitly declared so verify() can be called on it.
    @Mock
    ApplicationEventPublisher eventPublisher;

    // AbstractCachedEntityService.handleEvictEvent calls cache.evict(); must be a mock to avoid NPE.
    @Mock
    TbTransactionalCache<UserCacheKey, User> cache;

    // DAOs called inside deleteUser – each must be declared so Mockito injects
    // a non-null stub (default behaviour: all void methods are no-ops).
    @Mock
    UserDao userDao;
    @Mock
    UserCredentialsDao userCredentialsDao;
    @Mock
    UserAuthSettingsDao userAuthSettingsDao;
    @Mock
    UserSettingsDao userSettingsDao;
    @Mock
    ApiKeyService apiKeyService;
    @Mock
    EntityCountService countService;

    // All remaining dependencies (TbTenantProfileCache, DataValidator, etc.)
    // are not invoked by any method under test and are left as implicit nulls.

    /**
     * Mockito's @InjectMocks does not always traverse superclass @Autowired
     * fields.  AbstractCachedEntityService.cache is one such field, so we
     * set it explicitly before each test to guarantee it is non-null.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "cache", cache);
    }

    /**
     * Verifies that deleteUser publishes exactly two Spring application events:
     * a UserCredentialsInvalidationEvent (to terminate all active sessions for
     * the user) and a DeleteEntityEvent (for audit / cache fan-out).
     *
     * Direct equality assertions cannot be used here because both event classes
     * embed ts = System.currentTimeMillis() in their equals() check, meaning
     * two separately constructed instances are never equal.  ArgumentCaptor
     * lets us capture the actual objects that were passed to publishEvent() and
     * inspect their fields individually instead.
     */
    @Test
    void deleteUser_publishesBothEvents() {
        // GIVEN – a minimal User whose ID and tenant are known in advance
        TenantId tenantId = TenantId.fromUUID(UUID.randomUUID());
        UserId userId = new UserId(UUID.randomUUID());
        User user = new User();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setEmail("test@example.com");

        // WHEN
        userService.deleteUser(tenantId, user);

        // THEN – capture every object passed to publishEvent()
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        List<Object> events = captor.getAllValues();

        // First event: session invalidation – the ID must match the deleted user
        assertThat(events.get(0)).isInstanceOf(UserCredentialsInvalidationEvent.class);
        UserCredentialsInvalidationEvent invalidationEvent =
                (UserCredentialsInvalidationEvent) events.get(0);
        assertThat(invalidationEvent.getId()).isEqualTo(userId.toString());

        // Second event: entity deletion – must carry the correct tenant, entity
        // ID, and the full User object so downstream consumers can act on it
        assertThat(events.get(1)).isInstanceOf(DeleteEntityEvent.class);
        DeleteEntityEvent<?> deleteEvent = (DeleteEntityEvent<?>) events.get(1);
        assertThat(deleteEvent.getEntityId()).isEqualTo(userId);
        assertThat(deleteEvent.getTenantId()).isEqualTo(tenantId);
        assertThat(deleteEvent.getEntity()).isEqualTo(user);
    }
}

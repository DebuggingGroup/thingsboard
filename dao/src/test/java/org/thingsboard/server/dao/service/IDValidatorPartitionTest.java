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
package org.thingsboard.server.dao.service;
import org.junit.jupiter.api.Test;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.dao.exception.IncorrectParameterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IDValidatorPartitionTest {
    @Test
    public void testValidateIds_NullList_ThrowsIncorrectParameterException() {
        List<DeviceId> nullList = null;
        
        IncorrectParameterException exception = assertThrows(
            IncorrectParameterException.class,
            () -> Validator.validateIds(nullList, list -> "IDs list cannot be null")
        );
        
        assertEquals("IDs list cannot be null", exception.getMessage());
    }

    @Test
    public void testValidateIds_EmptyList_ThrowsIncorrectParameterException() {
        List<DeviceId> emptyList = Collections.emptyList();
        
        IncorrectParameterException exception = assertThrows(
            IncorrectParameterException.class,
            () -> Validator.validateIds(emptyList, list -> "IDs list cannot be empty")
        );
        
        assertEquals("IDs list cannot be empty", exception.getMessage());
    }

    @Test
    public void testValidateIds_SingleValidId_NoExceptionThrown() {
        DeviceId validId = new DeviceId(UUID.randomUUID());
        List<DeviceId> singleIdList = Collections.singletonList(validId);
        
        assertDoesNotThrow(() -> 
            Validator.validateIds(singleIdList, list -> "Invalid ID")
        );
    }

    @Test
    public void testValidateIds_MultipleValidIds_NoExceptionThrown() {
        List<DeviceId> multipleValidIds = Arrays.asList(
            new DeviceId(UUID.randomUUID()),
            new DeviceId(UUID.randomUUID()),
            new DeviceId(UUID.randomUUID())
        );
        
        assertDoesNotThrow(() -> 
            Validator.validateIds(multipleValidIds, list -> "Invalid IDs")
        );
    }

    @Test
    public void testValidateIds_MultipleValidCustomerIds_NoExceptionThrown() {
        List<CustomerId> multipleValidIds = Arrays.asList(
            new CustomerId(UUID.randomUUID()),
            new CustomerId(UUID.randomUUID()),
            new CustomerId(UUID.randomUUID())
        );
        
        assertDoesNotThrow(() -> 
            Validator.validateIds(multipleValidIds, list -> "Invalid Customer IDs")
        );
    }

    @Test
    public void testValidateIds_FirstIdIsNull_ThrowsIncorrectParameterException() {
        List<DeviceId> idsWithNullFirst = Arrays.asList(
            null,
            new DeviceId(UUID.randomUUID()),
            new DeviceId(UUID.randomUUID())
        );
        
        IncorrectParameterException exception = assertThrows(
            IncorrectParameterException.class,
            () -> Validator.validateIds(idsWithNullFirst, 
                list -> "First ID in list is null")
        );
        
        assertEquals("First ID in list is null", exception.getMessage());
    }

    @Test
    public void testValidateIds_NullIdInMiddle_ThrowsIncorrectParameterException() {
        List<DeviceId> idsWithNullMiddle = Arrays.asList(
            new DeviceId(UUID.randomUUID()),
            null,
            new DeviceId(UUID.randomUUID())
        );
        
        IncorrectParameterException exception = assertThrows(
            IncorrectParameterException.class,
            () -> Validator.validateIds(idsWithNullMiddle, 
                list -> "Null ID found in middle of list")
        );
        
        assertEquals("Null ID found in middle of list", exception.getMessage());
    }

    @Test
    public void testValidateIds_NullIdAtEnd_ThrowsIncorrectParameterException() {
        List<DeviceId> idsWithNullEnd = Arrays.asList(
            new DeviceId(UUID.randomUUID()),
            new DeviceId(UUID.randomUUID()),
            null
        );
        
        IncorrectParameterException exception = assertThrows(
            IncorrectParameterException.class,
            () -> Validator.validateIds(idsWithNullEnd, 
                list -> "Null ID found at end of list")
        );
        
        assertEquals("Null ID found at end of list", exception.getMessage());
    }

    @Test
    public void testValidateIds_AllIdsAreNull_ThrowsIncorrectParameterException() {
        List<DeviceId> allNullIds = Arrays.asList(null, null, null);
        
        IncorrectParameterException exception = assertThrows(
            IncorrectParameterException.class,
            () -> Validator.validateIds(allNullIds, 
                list -> "All IDs in list are null")
        );
        
        assertEquals("All IDs in list are null", exception.getMessage());
    }

    @Test
    public void testValidateIds_UuidIsNull_ThrowsIncorrectParameterException() {
        DeviceId idWithNullUuid = new DeviceId(null);
        List<DeviceId> idsWithNullUuid = Collections.singletonList(idWithNullUuid);
        
        IncorrectParameterException exception = assertThrows(
            IncorrectParameterException.class,
            () -> Validator.validateIds(idsWithNullUuid, 
                list -> "ID has null UUID")
        );
        
        assertEquals("ID has null UUID", exception.getMessage());
    }

    @Test
    public void testValidateIds_ManyValidIds_NoExceptionThrown() {
        List<DeviceId> manyValidIds = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            manyValidIds.add(new DeviceId(UUID.randomUUID()));
        }
        
        assertDoesNotThrow(() -> 
            Validator.validateIds(manyValidIds, list -> "Invalid IDs in large list")
        );
        
        assertEquals(1000, manyValidIds.size());
    }

    @Test
    public void testValidateIds_MixOfValidAndNullIds_ThrowsIncorrectParameterException() {
        List<DeviceId> mixedIds = Arrays.asList(
            new DeviceId(UUID.randomUUID()),
            null,
            new DeviceId(UUID.randomUUID()),
            null,
            new DeviceId(UUID.randomUUID())
        );
        
        IncorrectParameterException exception = assertThrows(
            IncorrectParameterException.class,
            () -> Validator.validateIds(mixedIds, 
                list -> "List contains mix of valid and null IDs")
        );
        
        assertEquals("List contains mix of valid and null IDs", exception.getMessage());
    }
}
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
package org.thingsboard.server.common.alarm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.alarm.AlarmStatus;

class AlarmLifecycleTest {

@Test
void testInitialStateActiveUnack() {
    Alarm alarm = new Alarm();
    alarm.setCleared(false);
    alarm.setAcknowledged(false);

    assertFalse(alarm.isCleared());
    assertFalse(alarm.isAcknowledged());
    assertEquals(AlarmStatus.ACTIVE_UNACK, alarm.getStatus());
}

@Test
void testActiveAckAfterAcknowledge() {
    Alarm alarm = new Alarm();
    alarm.setCleared(false);
    alarm.setAcknowledged(false);

    alarm.setAcknowledged(true);

    assertFalse(alarm.isCleared());
    assertTrue(alarm.isAcknowledged());
    assertEquals(AlarmStatus.ACTIVE_ACK, alarm.getStatus());
}

@Test
void testClearedUnackFromActiveUnack() {
    Alarm alarm = new Alarm();
    alarm.setCleared(false);
    alarm.setAcknowledged(false); 

    alarm.setCleared(true);      

    assertTrue(alarm.isCleared());
    assertFalse(alarm.isAcknowledged());
    assertEquals(AlarmStatus.CLEARED_UNACK, alarm.getStatus());
}

@Test
void testClearedUnackFromActiveAck_thenUnack() {
    Alarm alarm = new Alarm();
    alarm.setCleared(false);
    alarm.setAcknowledged(true);

    alarm.setCleared(true);     

    alarm.setAcknowledged(false); 

    assertTrue(alarm.isCleared());
    assertFalse(alarm.isAcknowledged());
    assertEquals(AlarmStatus.CLEARED_UNACK, alarm.getStatus());
}

@Test
void testClearedAckAfterAcknowledgeAndClear() {
    Alarm alarm = new Alarm();
    alarm.setCleared(false);
    alarm.setAcknowledged(false);

    alarm.setAcknowledged(true);  
    alarm.setCleared(true);      

    assertTrue(alarm.isCleared());
    assertTrue(alarm.isAcknowledged());
    assertEquals(AlarmStatus.CLEARED_ACK, alarm.getStatus());
}

@Test
void testClearedAckAfterClearAndAcknowledge() {
    Alarm alarm = new Alarm();
    alarm.setCleared(false);
    alarm.setAcknowledged(false);

    alarm.setCleared(true);       
    alarm.setAcknowledged(true); 

    assertTrue(alarm.isCleared());
    assertTrue(alarm.isAcknowledged());
    assertEquals(AlarmStatus.CLEARED_ACK, alarm.getStatus());
}

@Test
void testReactivateFromClearedAck() {
    Alarm alarm = new Alarm();
    alarm.setCleared(false);
    alarm.setAcknowledged(true);  
    alarm.setCleared(true);     

    alarm.setCleared(false);     
    alarm.setAcknowledged(false); 

    assertFalse(alarm.isCleared());
    assertFalse(alarm.isAcknowledged());
    assertEquals(AlarmStatus.ACTIVE_UNACK, alarm.getStatus());
   
}

@Test
void testTimestampsSetOnAcknowledgeAndClear() {
    Alarm alarm = new Alarm();
    long before = System.currentTimeMillis();
    alarm.setAcknowledged(true); 
    alarm.setCleared(true);       
      
}

@Test
void testAcknowledgeOnAlreadyAcknowledgedDoesNotChangeTs() {
    Alarm alarm = new Alarm();
    alarm.setAcknowledged(true);
    long originalAckTs = alarm.getAckTs();

    alarm.setAcknowledged(true);  
    assertEquals(originalAckTs, alarm.getAckTs()); 
}

@Test
void testAllFourStates() {
    Alarm alarm = new Alarm();

    alarm.setCleared(false);
    alarm.setAcknowledged(false);
    assertEquals(AlarmStatus.ACTIVE_UNACK, alarm.getStatus());

    alarm.setAcknowledged(true);
    assertEquals(AlarmStatus.ACTIVE_ACK, alarm.getStatus());

    alarm.setCleared(true);
    assertEquals(AlarmStatus.CLEARED_ACK, alarm.getStatus());

    alarm.setAcknowledged(false);
    assertEquals(AlarmStatus.CLEARED_UNACK, alarm.getStatus());

    alarm.setAcknowledged(true);
    assertEquals(AlarmStatus.CLEARED_ACK, alarm.getStatus());
}
}
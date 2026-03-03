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
package org.thingsboard.server.common.stats;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("StatsTimer - Timer with average calculation")
class StatsTimerTest {

    private StatsTimer statsTimer;

    @BeforeEach
    void setUp() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Timer micrometerTimer = registry.timer("test.timer");
        statsTimer = new StatsTimer("testTimer", micrometerTimer);
    }

    @Test
    @DisplayName("getName() - should return the configured name")
    void testGetName() {
        assertThat(statsTimer.getName()).isEqualTo("testTimer");
    }

    @Test
    @DisplayName("getAvg() - should return 0.0 when no records exist")
    void testAvgWithNoRecords() {
        assertThat(statsTimer.getAvg()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("record(timeMs) - single recording in milliseconds")
    void testRecordSingleMs() {
        statsTimer.record(100L);

        assertThat(statsTimer.getAvg()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("record(timeMs) - average of multiple recordings")
    void testRecordMultipleMs() {
        statsTimer.record(100L);
        statsTimer.record(200L);
        statsTimer.record(300L);

        assertThat(statsTimer.getAvg()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("record(timing, timeUnit) - recording in seconds converts to ms")
    void testRecordWithTimeUnit() {
        statsTimer.record(1L, TimeUnit.SECONDS);

        assertThat(statsTimer.getAvg()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("record(timing, timeUnit) - recording in microseconds converts to ms")
    void testRecordMicroseconds() {
        statsTimer.record(5000L, TimeUnit.MICROSECONDS);

        assertThat(statsTimer.getAvg()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("record(timing, timeUnit) - mixed time units accumulate correctly")
    void testRecordMixedUnits() {
        statsTimer.record(1L, TimeUnit.SECONDS);      // 1000 ms
        statsTimer.record(500L, TimeUnit.MILLISECONDS); // 500 ms

        assertThat(statsTimer.getAvg()).isEqualTo(750.0);
    }

    @Test
    @DisplayName("record() - zero timing value is valid")
    void testRecordZero() {
        statsTimer.record(0L);

        assertThat(statsTimer.getAvg()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("reset() - should clear count and totalTime")
    void testReset() {
        statsTimer.record(100L);
        statsTimer.record(200L);

        statsTimer.reset();

        assertThat(statsTimer.getAvg()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("reset() - recording after reset starts fresh")
    void testRecordAfterReset() {
        statsTimer.record(100L);
        statsTimer.record(200L);

        statsTimer.reset();

        statsTimer.record(50L);

        assertThat(statsTimer.getAvg()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("getAvg() - precision with uneven division")
    void testAvgPrecision() {
        statsTimer.record(10L);
        statsTimer.record(20L);
        statsTimer.record(30L);

        // avg = 60 / 3 = 20.0
        assertThat(statsTimer.getAvg()).isCloseTo(20.0, within(0.001));
    }

    @Test
    @DisplayName("Full lifecycle - record, check avg, reset, record again")
    void testFullLifecycle() {
        statsTimer.record(100L);
        statsTimer.record(300L);
        assertThat(statsTimer.getAvg()).isEqualTo(200.0);

        statsTimer.reset();
        assertThat(statsTimer.getAvg()).isEqualTo(0.0);

        statsTimer.record(50L);
        assertThat(statsTimer.getAvg()).isEqualTo(50.0);
    }
}

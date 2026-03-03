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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultMessagesStats - Message statistics tracking")
class DefaultMessagesStatsTest {

    private DefaultMessagesStats stats;

    @BeforeEach
    void setUp() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        StatsCounter total = createStatsCounter(registry, "total");
        StatsCounter successful = createStatsCounter(registry, "successful");
        StatsCounter failed = createStatsCounter(registry, "failed");
        stats = new DefaultMessagesStats(total, successful, failed);
    }

    private StatsCounter createStatsCounter(SimpleMeterRegistry registry, String name) {
        Counter counter = registry.counter("test." + name);
        return new StatsCounter(new AtomicInteger(0), counter, name);
    }

    @Test
    @DisplayName("Initial state - all counters should be zero")
    void testInitialState() {
        assertThat(stats.getTotal()).isZero();
        assertThat(stats.getSuccessful()).isZero();
        assertThat(stats.getFailed()).isZero();
    }

    @Test
    @DisplayName("incrementTotal(amount) - should increase total counter")
    void testIncrementTotal() {
        stats.incrementTotal(5);

        assertThat(stats.getTotal()).isEqualTo(5);
        assertThat(stats.getSuccessful()).isZero();
        assertThat(stats.getFailed()).isZero();
    }

    @Test
    @DisplayName("incrementTotal() - default increment of 1")
    void testIncrementTotalDefault() {
        stats.incrementTotal();

        assertThat(stats.getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("incrementSuccessful(amount) - should increase successful counter")
    void testIncrementSuccessful() {
        stats.incrementSuccessful(3);

        assertThat(stats.getSuccessful()).isEqualTo(3);
        assertThat(stats.getTotal()).isZero();
        assertThat(stats.getFailed()).isZero();
    }

    @Test
    @DisplayName("incrementSuccessful() - default increment of 1")
    void testIncrementSuccessfulDefault() {
        stats.incrementSuccessful();

        assertThat(stats.getSuccessful()).isEqualTo(1);
    }

    @Test
    @DisplayName("incrementFailed(amount) - should increase failed counter")
    void testIncrementFailed() {
        stats.incrementFailed(2);

        assertThat(stats.getFailed()).isEqualTo(2);
        assertThat(stats.getTotal()).isZero();
        assertThat(stats.getSuccessful()).isZero();
    }

    @Test
    @DisplayName("incrementFailed() - default increment of 1")
    void testIncrementFailedDefault() {
        stats.incrementFailed();

        assertThat(stats.getFailed()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters are independent - incrementing one does not affect others")
    void testCountersAreIndependent() {
        stats.incrementTotal(10);
        stats.incrementSuccessful(7);
        stats.incrementFailed(3);

        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getSuccessful()).isEqualTo(7);
        assertThat(stats.getFailed()).isEqualTo(3);
    }

    @Test
    @DisplayName("reset() - should clear all three counters to zero")
    void testReset() {
        stats.incrementTotal(10);
        stats.incrementSuccessful(7);
        stats.incrementFailed(3);

        stats.reset();

        assertThat(stats.getTotal()).isZero();
        assertThat(stats.getSuccessful()).isZero();
        assertThat(stats.getFailed()).isZero();
    }

    @Test
    @DisplayName("reset() - on already-zero counters remains zero")
    void testResetOnZero() {
        stats.reset();

        assertThat(stats.getTotal()).isZero();
        assertThat(stats.getSuccessful()).isZero();
        assertThat(stats.getFailed()).isZero();
    }

    @Test
    @DisplayName("Accumulation - multiple increments add up correctly")
    void testAccumulation() {
        stats.incrementTotal(5);
        stats.incrementTotal(3);
        stats.incrementSuccessful(2);
        stats.incrementSuccessful(4);
        stats.incrementFailed(1);
        stats.incrementFailed(1);

        assertThat(stats.getTotal()).isEqualTo(8);
        assertThat(stats.getSuccessful()).isEqualTo(6);
        assertThat(stats.getFailed()).isEqualTo(2);
    }

    @Test
    @DisplayName("Full lifecycle - increment, check, reset, increment again")
    void testFullLifecycle() {
        stats.incrementTotal(100);
        stats.incrementSuccessful(90);
        stats.incrementFailed(10);

        assertThat(stats.getTotal()).isEqualTo(100);
        assertThat(stats.getSuccessful()).isEqualTo(90);
        assertThat(stats.getFailed()).isEqualTo(10);

        stats.reset();

        assertThat(stats.getTotal()).isZero();
        assertThat(stats.getSuccessful()).isZero();
        assertThat(stats.getFailed()).isZero();

        stats.incrementTotal(50);
        stats.incrementSuccessful(45);
        stats.incrementFailed(5);

        assertThat(stats.getTotal()).isEqualTo(50);
        assertThat(stats.getSuccessful()).isEqualTo(45);
        assertThat(stats.getFailed()).isEqualTo(5);
    }
}

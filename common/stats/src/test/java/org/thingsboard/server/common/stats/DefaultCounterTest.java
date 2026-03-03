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

@DisplayName("DefaultCounter - Atomic counter with micrometer integration")
class DefaultCounterTest {

    private AtomicInteger atomicCounter;
    private Counter micrometerCounter;
    private DefaultCounter defaultCounter;

    @BeforeEach
    void setUp() {
        atomicCounter = new AtomicInteger(0);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        micrometerCounter = registry.counter("test.counter");
        defaultCounter = new DefaultCounter(atomicCounter, micrometerCounter);
    }

    @Test
    @DisplayName("Initial state - counter should be zero")
    void testInitialState() {
        assertThat(defaultCounter.get()).isZero();
    }

    @Test
    @DisplayName("increment() - should increase both atomic and micrometer counters by 1")
    void testIncrement() {
        defaultCounter.increment();

        assertThat(defaultCounter.get()).isEqualTo(1);
        assertThat(micrometerCounter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("increment() - multiple increments accumulate correctly")
    void testMultipleIncrements() {
        defaultCounter.increment();
        defaultCounter.increment();
        defaultCounter.increment();

        assertThat(defaultCounter.get()).isEqualTo(3);
        assertThat(micrometerCounter.count()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("add(delta) - should add delta to both counters")
    void testAdd() {
        defaultCounter.add(5);

        assertThat(defaultCounter.get()).isEqualTo(5);
        assertThat(micrometerCounter.count()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("add(delta) - should work with zero delta")
    void testAddZero() {
        defaultCounter.add(0);

        assertThat(defaultCounter.get()).isZero();
    }

    @Test
    @DisplayName("add(delta) - should accumulate with prior increments")
    void testAddAfterIncrement() {
        defaultCounter.increment();
        defaultCounter.add(10);

        assertThat(defaultCounter.get()).isEqualTo(11);
        assertThat(micrometerCounter.count()).isEqualTo(11.0);
    }

    @Test
    @DisplayName("clear() - should reset atomic counter to zero")
    void testClear() {
        defaultCounter.increment();
        defaultCounter.increment();

        defaultCounter.clear();

        assertThat(defaultCounter.get()).isZero();
    }

    @Test
    @DisplayName("clear() - on already-zero counter should remain zero")
    void testClearOnZero() {
        defaultCounter.clear();

        assertThat(defaultCounter.get()).isZero();
    }

    @Test
    @DisplayName("getAndClear() - should return value then reset to zero")
    void testGetAndClear() {
        defaultCounter.increment();
        defaultCounter.increment();
        defaultCounter.increment();

        int value = defaultCounter.getAndClear();

        assertThat(value).isEqualTo(3);
        assertThat(defaultCounter.get()).isZero();
    }

    @Test
    @DisplayName("getAndClear() - on empty counter returns zero")
    void testGetAndClearOnEmpty() {
        int value = defaultCounter.getAndClear();

        assertThat(value).isZero();
        assertThat(defaultCounter.get()).isZero();
    }

    @Test
    @DisplayName("get() - should not modify the counter value")
    void testGetDoesNotModify() {
        defaultCounter.add(7);

        int first = defaultCounter.get();
        int second = defaultCounter.get();

        assertThat(first).isEqualTo(7);
        assertThat(second).isEqualTo(7);
    }

    @Test
    @DisplayName("Full lifecycle - increment, add, get, clear, verify")
    void testFullLifecycle() {
        defaultCounter.increment();
        assertThat(defaultCounter.get()).isEqualTo(1);

        defaultCounter.add(4);
        assertThat(defaultCounter.get()).isEqualTo(5);

        int snapshot = defaultCounter.getAndClear();
        assertThat(snapshot).isEqualTo(5);
        assertThat(defaultCounter.get()).isZero();

        defaultCounter.increment();
        assertThat(defaultCounter.get()).isEqualTo(1);
    }
}

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

@DisplayName("StatsCounter - Named counter extending DefaultCounter")
class StatsCounterTest {

    private SimpleMeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
    }

    private StatsCounter createCounter(String name) {
        Counter counter = registry.counter("test." + name);
        return new StatsCounter(new AtomicInteger(0), counter, name);
    }

    @Test
    @DisplayName("getName() - should return the configured name")
    void testGetName() {
        StatsCounter counter = createCounter("myCounter");

        assertThat(counter.getName()).isEqualTo("myCounter");
    }

    @Test
    @DisplayName("Inherits increment from DefaultCounter")
    void testInheritedIncrement() {
        StatsCounter counter = createCounter("inc");

        counter.increment();
        counter.increment();

        assertThat(counter.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Inherits add from DefaultCounter")
    void testInheritedAdd() {
        StatsCounter counter = createCounter("add");

        counter.add(10);

        assertThat(counter.get()).isEqualTo(10);
    }

    @Test
    @DisplayName("Inherits clear from DefaultCounter")
    void testInheritedClear() {
        StatsCounter counter = createCounter("clear");

        counter.add(5);
        counter.clear();

        assertThat(counter.get()).isZero();
    }

    @Test
    @DisplayName("Inherits getAndClear from DefaultCounter")
    void testInheritedGetAndClear() {
        StatsCounter counter = createCounter("gac");

        counter.add(7);
        int value = counter.getAndClear();

        assertThat(value).isEqualTo(7);
        assertThat(counter.get()).isZero();
    }

    @Test
    @DisplayName("Multiple named counters are independent")
    void testMultipleCountersIndependent() {
        StatsCounter counter1 = createCounter("counter1");
        StatsCounter counter2 = createCounter("counter2");

        counter1.add(10);
        counter2.add(20);

        assertThat(counter1.get()).isEqualTo(10);
        assertThat(counter1.getName()).isEqualTo("counter1");
        assertThat(counter2.get()).isEqualTo(20);
        assertThat(counter2.getName()).isEqualTo("counter2");
    }
}

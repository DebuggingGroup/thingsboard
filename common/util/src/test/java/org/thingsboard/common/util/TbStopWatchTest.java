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
package org.thingsboard.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TbStopWatchTest {

    // -----------------------------------------------------------------------
    // create() — no-arg factory starts the watch immediately
    // -----------------------------------------------------------------------

    @Test
    void create_noArg_isRunning() {
        TbStopWatch sw = TbStopWatch.create();
        // A running watch has exactly one task in progress; isRunning() is true
        assertThat(sw.isRunning()).isTrue();
    }

    @Test
    void create_noArg_stopAndGetTotalTimeMillis_returnsNonNegative() {
        TbStopWatch sw = TbStopWatch.create();
        long millis = sw.stopAndGetTotalTimeMillis();
        assertThat(millis).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void create_noArg_stopAndGetTotalTimeNanos_returnsNonNegative() {
        TbStopWatch sw = TbStopWatch.create();
        long nanos = sw.stopAndGetTotalTimeNanos();
        assertThat(nanos).isGreaterThanOrEqualTo(0L);
    }

    // -----------------------------------------------------------------------
    // create(String taskName) — named factory
    // -----------------------------------------------------------------------

    @Test
    void create_withTaskName_isRunning() {
        TbStopWatch sw = TbStopWatch.create("myTask");
        assertThat(sw.isRunning()).isTrue();
    }

    @Test
    void create_withTaskName_currentTaskNameMatches() {
        TbStopWatch sw = TbStopWatch.create("myTask");
        assertThat(sw.currentTaskName()).isEqualTo("myTask");
    }

    @Test
    void create_withTaskName_stopAndGetLastTaskTimeMillis_returnsNonNegative() {
        TbStopWatch sw = TbStopWatch.create("myTask");
        long millis = sw.stopAndGetLastTaskTimeMillis();
        assertThat(millis).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void create_withTaskName_stopAndGetLastTaskTimeNanos_returnsNonNegative() {
        TbStopWatch sw = TbStopWatch.create("myTask");
        long nanos = sw.stopAndGetLastTaskTimeNanos();
        assertThat(nanos).isGreaterThanOrEqualTo(0L);
    }

    // -----------------------------------------------------------------------
    // stopAndGetTotalTimeMillis — watch is not running after stop
    // -----------------------------------------------------------------------

    @Test
    void stopAndGetTotalTimeMillis_watchStopsAfterCall() {
        TbStopWatch sw = TbStopWatch.create();
        sw.stopAndGetTotalTimeMillis();
        assertThat(sw.isRunning()).isFalse();
    }

    @Test
    void stopAndGetTotalTimeNanos_watchStopsAfterCall() {
        TbStopWatch sw = TbStopWatch.create("t");
        sw.stopAndGetTotalTimeNanos();
        assertThat(sw.isRunning()).isFalse();
    }

    @Test
    void stopAndGetLastTaskTimeMillis_watchStopsAfterCall() {
        TbStopWatch sw = TbStopWatch.create("t");
        sw.stopAndGetLastTaskTimeMillis();
        assertThat(sw.isRunning()).isFalse();
    }

    @Test
    void stopAndGetLastTaskTimeNanos_watchStopsAfterCall() {
        TbStopWatch sw = TbStopWatch.create("t");
        sw.stopAndGetLastTaskTimeNanos();
        assertThat(sw.isRunning()).isFalse();
    }

    // -----------------------------------------------------------------------
    // startNew(String) — stops current task and starts a new one
    // -----------------------------------------------------------------------

    @Test
    void startNew_switchesTask_watchIsRunningWithNewName() throws InterruptedException {
        TbStopWatch sw = TbStopWatch.create("first");
        Thread.sleep(1);
        sw.startNew("second");
        assertThat(sw.isRunning()).isTrue();
        assertThat(sw.currentTaskName()).isEqualTo("second");
    }

    @Test
    void startNew_firstTaskRecorded_totalExcludesOngoingTask() throws InterruptedException {
        TbStopWatch sw = TbStopWatch.create("first");
        Thread.sleep(1);
        sw.startNew("second");
        // After startNew, "first" is complete; task count includes it
        assertThat(sw.getTaskCount()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Elapsed time ordering — total >= last task for single-task watch
    // -----------------------------------------------------------------------

    @Test
    void totalTimeAtLeastLastTaskTime_forSingleTask() throws InterruptedException {
        TbStopWatch sw = TbStopWatch.create("only");
        Thread.sleep(1);
        long last = sw.stopAndGetLastTaskTimeMillis();
        long total = sw.getTotalTimeMillis();
        // For a single task watch total == last
        assertThat(total).isGreaterThanOrEqualTo(last);
    }

    // -----------------------------------------------------------------------
    // Calling stop when already stopped throws (normal Spring StopWatch behaviour)
    // -----------------------------------------------------------------------

    @Test
    void callingStopTwice_throws() {
        TbStopWatch sw = TbStopWatch.create();
        sw.stopAndGetTotalTimeMillis(); // first stop
        assertThatThrownBy(sw::stopAndGetTotalTimeMillis)
                .isInstanceOf(IllegalStateException.class);
    }
}

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
package org.thingsboard.rule.engine;

/**
 * A test stub that extends TestDbCallbackExecutor and overrides execute(Runnable)
 * to record how many times the executor's execute method was called.
 *
 * This lets a test assert that the node under test actually dispatched work
 * through the executor, rather than bypassing it entirely.
 *
 * The stubbed method is execute(Runnable): its behavior is changed from just
 * running the command to also incrementing a counter before delegating.
 */
public class RecordingDbCallbackExecutor extends TestDbCallbackExecutor {

    private int executeCallCount = 0;

    /**
     * Stubbed version of execute: records the invocation then delegates to the
     * original synchronous implementation so the test still runs to completion.
     */
    @Override
    public void execute(Runnable command) {
        executeCallCount++;
        super.execute(command);
    }

    public int getExecuteCallCount() {
        return executeCallCount;
    }

}

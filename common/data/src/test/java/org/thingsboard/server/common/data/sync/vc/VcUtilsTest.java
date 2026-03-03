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
package org.thingsboard.server.common.data.sync.vc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("VcUtils - Git branch name validation")
class VcUtilsTest {

    @Test
    @DisplayName("Valid branch name - should not throw")
    void testValidBranchName() {
        assertDoesNotThrow(() -> VcUtils.checkBranchName("main"));
    }

    @Test
    @DisplayName("Valid branch name with slashes - should not throw")
    void testValidBranchNameWithSlash() {
        assertDoesNotThrow(() -> VcUtils.checkBranchName("feature/my-feature"));
    }

    @Test
    @DisplayName("Valid branch name with hyphens and underscores")
    void testValidBranchNameHyphensUnderscores() {
        assertDoesNotThrow(() -> VcUtils.checkBranchName("my-branch_v2"));
    }

    @Test
    @DisplayName("Empty string - should not throw (returns early)")
    void testEmptyString() {
        assertDoesNotThrow(() -> VcUtils.checkBranchName(""));
    }

    @Test
    @DisplayName("Null string - should not throw (returns early)")
    void testNullString() {
        assertDoesNotThrow(() -> VcUtils.checkBranchName(null));
    }

    @Test
    @DisplayName("Branch with whitespace - should throw IllegalArgumentException")
    void testWhitespace() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("my branch"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch with tab character - should throw IllegalArgumentException")
    void testTabCharacter() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("my\tbranch"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch with double dot (..) - should throw IllegalArgumentException")
    void testDoubleDot() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("branch..name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch with tilde (~) - should throw IllegalArgumentException")
    void testTilde() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("branch~1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch with caret (^) - should throw IllegalArgumentException")
    void testCaret() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("branch^2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch with colon (:) - should throw IllegalArgumentException")
    void testColon() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("branch:name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch with backslash (\\) - should throw IllegalArgumentException")
    void testBackslash() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("branch\\name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch ending with slash (/) - should throw IllegalArgumentException")
    void testEndsWithSlash() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("branch/"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch ending with .lock - should throw IllegalArgumentException")
    void testEndsWithDotLock() {
        assertThatThrownBy(() -> VcUtils.checkBranchName("branch.lock"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name is invalid");
    }

    @Test
    @DisplayName("Branch with .lock in middle - should not throw")
    void testDotLockInMiddle() {
        assertDoesNotThrow(() -> VcUtils.checkBranchName("branch.lockfile"));
    }

    @Test
    @DisplayName("Branch with single dot - should not throw")
    void testSingleDot() {
        assertDoesNotThrow(() -> VcUtils.checkBranchName("v1.0"));
    }

    @Test
    @DisplayName("Branch with valid complex name - should not throw")
    void testComplexValidName() {
        assertDoesNotThrow(() -> VcUtils.checkBranchName("release/v2.1.0-rc1"));
    }
}

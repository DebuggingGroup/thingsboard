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

import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class RegexUtilsTest {

    // -----------------------------------------------------------------------
    // replace(String, Pattern, UnaryOperator)
    // -----------------------------------------------------------------------

    @Test
    void replaceWithPattern_noMatch_returnsOriginal() {
        Pattern digits = Pattern.compile("\\d+");
        String result = RegexUtils.replace("hello world", digits, s -> "[" + s + "]");
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void replaceWithPattern_singleMatch_replacesCorrectly() {
        Pattern digits = Pattern.compile("\\d+");
        String result = RegexUtils.replace("abc 123 def", digits, s -> "NUM");
        assertThat(result).isEqualTo("abc NUM def");
    }

    @Test
    void replaceWithPattern_multipleMatches_replacesAll() {
        Pattern digits = Pattern.compile("\\d+");
        String result = RegexUtils.replace("1 plus 2 equals 3", digits, s -> "N");
        assertThat(result).isEqualTo("N plus N equals N");
    }

    @Test
    void replaceWithPattern_uuidPattern_replacesAllUuids() {
        UUID uuid1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID uuid2 = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        String input = "device/" + uuid1 + "/sensor/" + uuid2;
        String result = RegexUtils.replace(input, RegexUtils.UUID_PATTERN, u -> "UUID");
        assertThat(result).isEqualTo("device/UUID/sensor/UUID");
    }

    @Test
    void replaceWithPattern_uuidPatternApplied_usingFromStringRoundtrip() {
        String original = "550e8400-e29b-41d4-a716-446655440000";
        UUID mapped = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        String result = RegexUtils.replace(original, RegexUtils.UUID_PATTERN,
                uuid -> mapped.toString());
        assertThat(result).isEqualTo(mapped.toString());
    }

    // -----------------------------------------------------------------------
    // replace(String, String pattern, Function<MatchResult, String>)
    // -----------------------------------------------------------------------

    @Test
    void replaceWithStringPattern_singleMatch_replacesCorrectly() {
        String result = RegexUtils.replace("value=42", "\\d+", m -> "[" + m.group() + "]");
        assertThat(result).isEqualTo("value=[42]");
    }

    @Test
    void replaceWithStringPattern_noMatch_returnsOriginal() {
        String result = RegexUtils.replace("hello", "\\d+", m -> "X");
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void replaceWithStringPattern_cachedPatternReuse_sameResult() {
        // Call twice to exercise the cache path
        String result1 = RegexUtils.replace("a1b2c3", "[a-z]", m -> m.group().toUpperCase());
        String result2 = RegexUtils.replace("d4e5f6", "[a-z]", m -> m.group().toUpperCase());
        assertThat(result1).isEqualTo("A1B2C3");
        assertThat(result2).isEqualTo("D4E5F6");
    }

    // -----------------------------------------------------------------------
    // matches(String, Pattern)
    // -----------------------------------------------------------------------

    @Test
    void matches_fullMatch_returnsTrue() {
        Pattern emailish = Pattern.compile("[a-z]+@[a-z]+\\.[a-z]+");
        assertThat(RegexUtils.matches("user@example.com", emailish)).isTrue();
    }

    @Test
    void matches_partialMatchOnly_returnsFalse() {
        // matches() requires a FULL string match, not just find()
        Pattern digits = Pattern.compile("\\d+");
        assertThat(RegexUtils.matches("abc123", digits)).isFalse();
    }

    @Test
    void matches_emptyInput_emptyPattern_returnsTrue() {
        assertThat(RegexUtils.matches("", Pattern.compile(""))).isTrue();
    }

    @Test
    void matches_uuidPattern_validUuid_returnsTrue() {
        assertThat(RegexUtils.matches("550e8400-e29b-41d4-a716-446655440000", RegexUtils.UUID_PATTERN)).isTrue();
    }

    @Test
    void matches_uuidPattern_invalidUuid_returnsFalse() {
        assertThat(RegexUtils.matches("not-a-uuid", RegexUtils.UUID_PATTERN)).isFalse();
    }

    // -----------------------------------------------------------------------
    // getMatch(String, Pattern, int group)
    // -----------------------------------------------------------------------

    @Test
    void getMatch_groupZero_returnsFullMatch() {
        Pattern pattern = Pattern.compile("(\\w+)@(\\w+)");
        String result = RegexUtils.getMatch("user@host", pattern, 0);
        assertThat(result).isEqualTo("user@host");
    }

    @Test
    void getMatch_group1_returnsFirstCapturingGroup() {
        Pattern pattern = Pattern.compile("(\\w+)@(\\w+)");
        String result = RegexUtils.getMatch("user@host", pattern, 1);
        assertThat(result).isEqualTo("user");
    }

    @Test
    void getMatch_group2_returnsSecondCapturingGroup() {
        Pattern pattern = Pattern.compile("(\\w+)@(\\w+)");
        String result = RegexUtils.getMatch("user@host", pattern, 2);
        assertThat(result).isEqualTo("host");
    }

    @Test
    void getMatch_noMatch_returnsNull() {
        Pattern pattern = Pattern.compile("\\d{4}");
        String result = RegexUtils.getMatch("abc", pattern, 0);
        assertThat(result).isNull();
    }

    @Test
    void getMatch_groupOutOfRange_returnsNull() {
        // group index beyond capturing groups — should return null (ignored exception)
        Pattern pattern = Pattern.compile("(\\w+)");
        String result = RegexUtils.getMatch("hello", pattern, 99);
        assertThat(result).isNull();
    }

    @Test
    void getMatch_uuidInLargerString_extractsUuid() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        String result = RegexUtils.getMatch("prefix-" + uuid + "-suffix", RegexUtils.UUID_PATTERN, 0);
        assertThat(result).isEqualTo(uuid);
    }
}

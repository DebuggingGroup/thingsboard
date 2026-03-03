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
package org.thingsboard.server.common.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "\000", "\u0000", " \000", " \000 ", "\000 ", "\000\000", "\000 \000",
            "世\000界", "F0929906\000\000\000\000\000\000\000\000\000",
    })
    void testContains0x00_thenTrue(String sample) {
        assertThat(StringUtils.contains0x00(sample)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "abc", "世界", "\001", "\uD83D\uDC0C"})
    void testContains0x00_thenFalse(String sample) {
        assertThat(StringUtils.contains0x00(sample)).isFalse();
    }

    @Test
    void testTruncate() {
        int maxLength = 5;
        assertThat(StringUtils.truncate(null, maxLength)).isNull();
        assertThat(StringUtils.truncate("", maxLength)).isEmpty();
        assertThat(StringUtils.truncate("123", maxLength)).isEqualTo("123");
        assertThat(StringUtils.truncate("1234567", maxLength)).isEqualTo("12345...[truncated 2 symbols]");
        assertThat(StringUtils.truncate("1234567", 0)).isEqualTo("1234567");
    }

    // -----------------------------------------------------------------------
    // isEmpty / isNotEmpty
    // -----------------------------------------------------------------------

    @Test
    void isEmpty_nullAndEmpty_returnsTrue() {
        assertThat(StringUtils.isEmpty(null)).isTrue();
        assertThat(StringUtils.isEmpty("")).isTrue();
    }

    @Test
    void isEmpty_nonEmpty_returnsFalse() {
        assertThat(StringUtils.isEmpty(" ")).isFalse();
        assertThat(StringUtils.isEmpty("abc")).isFalse();
    }

    @Test
    void isNotEmpty_nullAndEmpty_returnsFalse() {
        assertThat(StringUtils.isNotEmpty(null)).isFalse();
        assertThat(StringUtils.isNotEmpty("")).isFalse();
    }

    @Test
    void isNotEmpty_nonEmpty_returnsTrue() {
        assertThat(StringUtils.isNotEmpty(" ")).isTrue();
        assertThat(StringUtils.isNotEmpty("hello")).isTrue();
    }

    // -----------------------------------------------------------------------
    // isBlank / isNotBlank
    // -----------------------------------------------------------------------

    @Test
    void isBlank_nullEmptyAndWhitespace_returnsTrue() {
        assertThat(StringUtils.isBlank(null)).isTrue();
        assertThat(StringUtils.isBlank("")).isTrue();
        assertThat(StringUtils.isBlank("   ")).isTrue();
    }

    @Test
    void isBlank_nonBlank_returnsFalse() {
        assertThat(StringUtils.isBlank("a")).isFalse();
        assertThat(StringUtils.isBlank(" x ")).isFalse();
    }

    @Test
    void isNotBlank_nullEmptyAndWhitespace_returnsFalse() {
        assertThat(StringUtils.isNotBlank(null)).isFalse();
        assertThat(StringUtils.isNotBlank("")).isFalse();
        assertThat(StringUtils.isNotBlank("  ")).isFalse();
    }

    @Test
    void isNotBlank_nonBlank_returnsTrue() {
        assertThat(StringUtils.isNotBlank("hi")).isTrue();
    }

    // -----------------------------------------------------------------------
    // notBlankOrDefault
    // -----------------------------------------------------------------------

    @Test
    void notBlankOrDefault_blankInput_returnsDefault() {
        assertThat(StringUtils.notBlankOrDefault(null, "def")).isEqualTo("def");
        assertThat(StringUtils.notBlankOrDefault("", "def")).isEqualTo("def");
        assertThat(StringUtils.notBlankOrDefault("  ", "def")).isEqualTo("def");
    }

    @Test
    void notBlankOrDefault_nonBlankInput_returnsInput() {
        assertThat(StringUtils.notBlankOrDefault("val", "def")).isEqualTo("val");
    }

    // -----------------------------------------------------------------------
    // removeStart
    // -----------------------------------------------------------------------

    @Test
    void removeStart_prefixPresent_removesIt() {
        assertThat(StringUtils.removeStart("foobar", "foo")).isEqualTo("bar");
    }

    @Test
    void removeStart_prefixAbsent_returnsOriginal() {
        assertThat(StringUtils.removeStart("foobar", "baz")).isEqualTo("foobar");
    }

    @Test
    void removeStart_nullStr_returnsNull() {
        assertThat(StringUtils.removeStart(null, "foo")).isNull();
    }

    @Test
    void removeStart_emptyRemove_returnsOriginal() {
        assertThat(StringUtils.removeStart("foobar", "")).isEqualTo("foobar");
    }

    // -----------------------------------------------------------------------
    // substringBefore
    // -----------------------------------------------------------------------

    @Test
    void substringBefore_separatorFound_returnsPartBefore() {
        assertThat(StringUtils.substringBefore("a/b/c", "/")).isEqualTo("a");
    }

    @Test
    void substringBefore_separatorNotFound_returnsWholeString() {
        assertThat(StringUtils.substringBefore("abc", "/")).isEqualTo("abc");
    }

    @Test
    void substringBefore_emptySeparator_returnsEmpty() {
        assertThat(StringUtils.substringBefore("abc", "")).isEqualTo("");
    }

    @Test
    void substringBefore_nullString_returnsNull() {
        assertThat(StringUtils.substringBefore(null, "/")).isNull();
    }

    // -----------------------------------------------------------------------
    // substringBetween
    // -----------------------------------------------------------------------

    @Test
    void substringBetween_delimitersPresent_returnsContent() {
        assertThat(StringUtils.substringBetween("a(b)c", "(", ")")).isEqualTo("b");
    }

    @Test
    void substringBetween_openMissing_returnsNull() {
        assertThat(StringUtils.substringBetween("abc", "(", ")")).isNull();
    }

    @Test
    void substringBetween_nullArgs_returnsNull() {
        assertThat(StringUtils.substringBetween(null, "(", ")")).isNull();
        assertThat(StringUtils.substringBetween("abc", null, ")")).isNull();
    }

    // -----------------------------------------------------------------------
    // equalsIgnoreCase
    // -----------------------------------------------------------------------

    @Test
    void equalsIgnoreCase_sameValueDifferentCase_returnsTrue() {
        assertThat(StringUtils.equalsIgnoreCase("Hello", "hello")).isTrue();
    }

    @Test
    void equalsIgnoreCase_differentValues_returnsFalse() {
        assertThat(StringUtils.equalsIgnoreCase("Hello", "World")).isFalse();
    }

    @Test
    void equalsIgnoreCase_bothNull_returnsTrue() {
        assertThat(StringUtils.equalsIgnoreCase(null, null)).isTrue();
    }

    @Test
    void equalsIgnoreCase_oneNull_returnsFalse() {
        assertThat(StringUtils.equalsIgnoreCase(null, "x")).isFalse();
    }

    // -----------------------------------------------------------------------
    // equalsAny / equalsAnyIgnoreCase
    // -----------------------------------------------------------------------

    @Test
    void equalsAny_matchInList_returnsTrue() {
        assertThat(StringUtils.equalsAny("b", "a", "b", "c")).isTrue();
    }

    @Test
    void equalsAny_noMatch_returnsFalse() {
        assertThat(StringUtils.equalsAny("z", "a", "b", "c")).isFalse();
    }

    @Test
    void equalsAnyIgnoreCase_matchWithDifferentCase_returnsTrue() {
        assertThat(StringUtils.equalsAnyIgnoreCase("Hello", "world", "HELLO")).isTrue();
    }

    @Test
    void equalsAnyIgnoreCase_noMatch_returnsFalse() {
        assertThat(StringUtils.equalsAnyIgnoreCase("z", "a", "b")).isFalse();
    }

    // -----------------------------------------------------------------------
    // containedByAny
    // -----------------------------------------------------------------------

    @Test
    void containedByAny_searchStringPresentInOne_returnsTrue() {
        assertThat(StringUtils.containedByAny("bc", "abc", "xyz")).isTrue();
    }

    @Test
    void containedByAny_searchStringNotPresent_returnsFalse() {
        assertThat(StringUtils.containedByAny("zz", "abc", "xyz")).isFalse();
    }

    @Test
    void containedByAny_nullSearchString_returnsFalse() {
        assertThat(StringUtils.containedByAny(null, "abc")).isFalse();
    }

    // -----------------------------------------------------------------------
    // obfuscate
    // -----------------------------------------------------------------------

    @Test
    void obfuscate_longString_replacesMiddleWithStars() {
        // "1234567890" — obfuscate positions 0..9 with margin=2
        String result = StringUtils.obfuscate("1234567890", 2, '*', 0, 10);
        assertThat(result).startsWith("12");
        assertThat(result).endsWith("90");
        assertThat(result).contains("******");
    }

    @Test
    void obfuscate_shortStringBelowDoubleMargin_fullObfuscation() {
        // part length (2) <= seenMargin*2 (4) → entire part replaced
        String result = StringUtils.obfuscate("ab", 2, '*', 0, 2);
        assertThat(result).isEqualTo("**");
    }

    // -----------------------------------------------------------------------
    // splitByCommaWithoutQuotes
    // -----------------------------------------------------------------------

    @Test
    void splitByCommaWithoutQuotes_singleQuoted_stripsQuotes() {
        List<String> result = StringUtils.splitByCommaWithoutQuotes("'a','b','c'");
        assertThat(result).containsExactly("a", "b", "c");
    }

    @Test
    void splitByCommaWithoutQuotes_doubleQuoted_stripsQuotes() {
        List<String> result = StringUtils.splitByCommaWithoutQuotes("\"x\",\"y\"");
        assertThat(result).containsExactly("x", "y");
    }

    @Test
    void splitByCommaWithoutQuotes_unquoted_returnsRawSplit() {
        List<String> result = StringUtils.splitByCommaWithoutQuotes("a, b, c");
        assertThat(result).containsExactly("a", "b", "c");
    }

    @Test
    void splitByCommaWithoutQuotes_mixedQuoteStyles_returnsRawSplit() {
        // Different quote styles — method falls back to raw split
        List<String> result = StringUtils.splitByCommaWithoutQuotes("'a',\"b\"");
        assertThat(result).containsExactlyInAnyOrder("'a'", "\"b\"");
    }

    // -----------------------------------------------------------------------
    // generateSafeToken
    // -----------------------------------------------------------------------

    @Test
    void generateSafeToken_defaultLength_returnsNonBlankString() {
        String token = StringUtils.generateSafeToken();
        assertThat(token).isNotBlank();
    }

    @Test
    void generateSafeToken_customLength_returnsNonBlankString() {
        String token = StringUtils.generateSafeToken(16);
        assertThat(token).isNotBlank();
    }

    @Test
    void generateSafeToken_uniqueEachCall_tokensDiffer() {
        assertThat(StringUtils.generateSafeToken()).isNotEqualTo(StringUtils.generateSafeToken());
    }

}

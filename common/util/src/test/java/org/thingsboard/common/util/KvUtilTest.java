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
import org.thingsboard.server.common.data.kv.BooleanDataEntry;
import org.thingsboard.server.common.data.kv.DoubleDataEntry;
import org.thingsboard.server.common.data.kv.JsonDataEntry;
import org.thingsboard.server.common.data.kv.LongDataEntry;
import org.thingsboard.server.common.data.kv.StringDataEntry;

import static org.assertj.core.api.Assertions.assertThat;

class KvUtilTest {

    // -----------------------------------------------------------------------
    // getStringValue
    // -----------------------------------------------------------------------

    @Test
    void getStringValue_longEntry_returnsStringRepresentation() {
        assertThat(KvUtil.getStringValue(new LongDataEntry("k", 42L))).isEqualTo("42");
    }

    @Test
    void getStringValue_doubleEntry_returnsStringRepresentation() {
        assertThat(KvUtil.getStringValue(new DoubleDataEntry("k", 3.14))).isEqualTo("3.14");
    }

    @Test
    void getStringValue_booleanEntryTrue_returnsStringTrue() {
        assertThat(KvUtil.getStringValue(new BooleanDataEntry("k", true))).isEqualTo("true");
    }

    @Test
    void getStringValue_booleanEntryFalse_returnsStringFalse() {
        assertThat(KvUtil.getStringValue(new BooleanDataEntry("k", false))).isEqualTo("false");
    }

    @Test
    void getStringValue_stringEntry_returnsStringValue() {
        assertThat(KvUtil.getStringValue(new StringDataEntry("k", "hello"))).isEqualTo("hello");
    }

    @Test
    void getStringValue_jsonEntry_returnsJsonString() {
        assertThat(KvUtil.getStringValue(new JsonDataEntry("k", "{\"a\":1}"))).isEqualTo("{\"a\":1}");
    }

    // -----------------------------------------------------------------------
    // getDoubleValue
    // -----------------------------------------------------------------------

    @Test
    void getDoubleValue_longEntry_returnsDoubleEquivalent() {
        assertThat(KvUtil.getDoubleValue(new LongDataEntry("k", 10L))).isEqualTo(10.0);
    }

    @Test
    void getDoubleValue_doubleEntry_returnsSameValue() {
        assertThat(KvUtil.getDoubleValue(new DoubleDataEntry("k", 2.5))).isEqualTo(2.5);
    }

    @Test
    void getDoubleValue_booleanTrue_returnsOnePointZero() {
        assertThat(KvUtil.getDoubleValue(new BooleanDataEntry("k", true))).isEqualTo(1.0);
    }

    @Test
    void getDoubleValue_booleanFalse_returnsZeroPointZero() {
        assertThat(KvUtil.getDoubleValue(new BooleanDataEntry("k", false))).isEqualTo(0.0);
    }

    @Test
    void getDoubleValue_stringParseable_returnsDouble() {
        assertThat(KvUtil.getDoubleValue(new StringDataEntry("k", "3.14"))).isEqualTo(3.14);
    }

    @Test
    void getDoubleValue_stringNotParseable_returnsNull() {
        assertThat(KvUtil.getDoubleValue(new StringDataEntry("k", "notANumber"))).isNull();
    }

    @Test
    void getDoubleValue_jsonParseable_returnsDouble() {
        assertThat(KvUtil.getDoubleValue(new JsonDataEntry("k", "7.0"))).isEqualTo(7.0);
    }

    @Test
    void getDoubleValue_jsonNotParseable_returnsNull() {
        assertThat(KvUtil.getDoubleValue(new JsonDataEntry("k", "{\"a\":1}"))).isNull();
    }

    // -----------------------------------------------------------------------
    // getLongValue
    // -----------------------------------------------------------------------

    @Test
    void getLongValue_longEntry_returnsSameValue() {
        assertThat(KvUtil.getLongValue(new LongDataEntry("k", 99L))).isEqualTo(99L);
    }

    @Test
    void getLongValue_doubleEntry_returnsTruncatedLong() {
        assertThat(KvUtil.getLongValue(new DoubleDataEntry("k", 9.9))).isEqualTo(9L);
    }

    @Test
    void getLongValue_booleanTrue_returnsOne() {
        assertThat(KvUtil.getLongValue(new BooleanDataEntry("k", true))).isEqualTo(1L);
    }

    @Test
    void getLongValue_booleanFalse_returnsZero() {
        assertThat(KvUtil.getLongValue(new BooleanDataEntry("k", false))).isEqualTo(0L);
    }

    @Test
    void getLongValue_stringParseable_returnsLong() {
        assertThat(KvUtil.getLongValue(new StringDataEntry("k", "123"))).isEqualTo(123L);
    }

    @Test
    void getLongValue_stringNotParseable_returnsNull() {
        assertThat(KvUtil.getLongValue(new StringDataEntry("k", "abc"))).isNull();
    }

    @Test
    void getLongValue_jsonParseable_returnsLong() {
        assertThat(KvUtil.getLongValue(new JsonDataEntry("k", "456"))).isEqualTo(456L);
    }

    @Test
    void getLongValue_jsonNotParseable_returnsNull() {
        assertThat(KvUtil.getLongValue(new JsonDataEntry("k", "[1,2]"))).isNull();
    }

    // -----------------------------------------------------------------------
    // getBoolValue
    // -----------------------------------------------------------------------

    @Test
    void getBoolValue_longNonZero_returnsTrue() {
        assertThat(KvUtil.getBoolValue(new LongDataEntry("k", 5L))).isTrue();
    }

    @Test
    void getBoolValue_longZero_returnsFalse() {
        assertThat(KvUtil.getBoolValue(new LongDataEntry("k", 0L))).isFalse();
    }

    @Test
    void getBoolValue_doubleNonZero_returnsTrue() {
        assertThat(KvUtil.getBoolValue(new DoubleDataEntry("k", 0.1))).isTrue();
    }

    @Test
    void getBoolValue_doubleZero_returnsFalse() {
        assertThat(KvUtil.getBoolValue(new DoubleDataEntry("k", 0.0))).isFalse();
    }

    @Test
    void getBoolValue_booleanTrue_returnsTrue() {
        assertThat(KvUtil.getBoolValue(new BooleanDataEntry("k", true))).isTrue();
    }

    @Test
    void getBoolValue_booleanFalse_returnsFalse() {
        assertThat(KvUtil.getBoolValue(new BooleanDataEntry("k", false))).isFalse();
    }

    @Test
    void getBoolValue_stringTrue_returnsTrue() {
        assertThat(KvUtil.getBoolValue(new StringDataEntry("k", "true"))).isTrue();
    }

    @Test
    void getBoolValue_stringFalse_returnsFalse() {
        assertThat(KvUtil.getBoolValue(new StringDataEntry("k", "false"))).isFalse();
    }

    @Test
    void getBoolValue_stringUnparseable_returnsFalse() {
        // Boolean.parseBoolean returns false for anything that isn't "true"
        assertThat(KvUtil.getBoolValue(new StringDataEntry("k", "maybe"))).isFalse();
    }

    @Test
    void getBoolValue_jsonTrue_returnsTrue() {
        assertThat(KvUtil.getBoolValue(new JsonDataEntry("k", "true"))).isTrue();
    }

    @Test
    void getBoolValue_jsonFalse_returnsFalse() {
        assertThat(KvUtil.getBoolValue(new JsonDataEntry("k", "false"))).isFalse();
    }
}

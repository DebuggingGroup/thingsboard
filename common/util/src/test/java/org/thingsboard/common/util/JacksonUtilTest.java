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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.id.AssetId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonUtilTest {

    @Test
    public void allowUnquotedFieldMapperTest() {
        String data = "{data: 123}";
        JsonNode actualResult = JacksonUtil.toJsonNode(data, JacksonUtil.ALLOW_UNQUOTED_FIELD_NAMES_MAPPER); // should be: {"data": 123}
        ObjectNode expectedResult = JacksonUtil.newObjectNode();
        expectedResult.put("data", 123); // {"data": 123}
        Assertions.assertEquals(expectedResult, actualResult);
        Assertions.assertThrows(IllegalArgumentException.class, () -> JacksonUtil.toJsonNode(data)); // syntax exception due to missing quotes in the field name!
    }

    @Test
    public void failOnUnknownPropertiesMapperTest() {
        Asset asset = new Asset();
        asset.setId(new AssetId(UUID.randomUUID()));
        asset.setName("Test");
        asset.setType("type");
        String serializedAsset = JacksonUtil.toString(asset);
        JsonNode jsonNode = JacksonUtil.toJsonNode(serializedAsset);
        // case: add new field to serialized Asset string and check for backward compatibility with original Asset object
        Assertions.assertNotNull(jsonNode);
        ((ObjectNode) jsonNode).put("test", (String) null);
        serializedAsset = JacksonUtil.toString(jsonNode);
        // deserialize with FAIL_ON_UNKNOWN_PROPERTIES = false
        Asset result = JacksonUtil.fromString(serializedAsset, Asset.class, true);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(asset.getId(), result.getId());
        Assertions.assertEquals(asset.getName(), result.getName());
        Assertions.assertEquals(asset.getType(), result.getType());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "false", "\"", "\"\"", "\"This is a string with double quotes\"", "Path: /home/developer/test.txt",
            "First line\nSecond line\n\nFourth line", "Before\rAfter", "Tab\tSeparated\tValues", "Test\bbackspace", "[]",
            "[1, 2, 3]", "{\"key\": \"value\"}", "{\n\"temperature\": 25.5,\n\"humidity\": 50.2\n\"}", "Expression: (a + b) * c",
            "世界", "Україна", "\u1F1FA\u1F1E6", "🇺🇦"})
    public void toPlainTextTest(String original) {
         String serialized = JacksonUtil.toString(original);
        Assertions.assertNotNull(serialized);
        Assertions.assertEquals(original, JacksonUtil.toPlainText(serialized));
    }

    @Test
    public void optionalMappingJDK8ModuleTest() {
        // To address the issue: Java 8 optional type `java.util.Optional` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jdk8" to enable handling
        assertThat(JacksonUtil.writeValueAsString(Optional.of("hello"))).isEqualTo("\"hello\"");
        assertThat(JacksonUtil.writeValueAsString(List.of(Optional.of("abc")))).isEqualTo("[\"abc\"]");
        assertThat(JacksonUtil.writeValueAsString(Set.of(Optional.empty()))).isEqualTo("[null]");
    }

    // -----------------------------------------------------------------------
    // fromBytes(byte[], Class)
    // -----------------------------------------------------------------------

    @Test
    public void fromBytesClassTest() {
        Asset asset = new Asset();
        asset.setName("ByteAsset");
        byte[] bytes = JacksonUtil.writeValueAsBytes(asset);
        Asset result = JacksonUtil.fromBytes(bytes, Asset.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("ByteAsset", result.getName());
    }

    @Test
    public void fromBytesClass_nullBytes_returnsNull() {
        Assertions.assertNull(JacksonUtil.fromBytes((byte[]) null, Asset.class));
    }

    // -----------------------------------------------------------------------
    // fromBytes(byte[]) → JsonNode
    // -----------------------------------------------------------------------

    @Test
    public void fromBytesJsonNode_validJson_returnsNode() {
        byte[] bytes = "{\"key\":\"value\"}".getBytes();
        JsonNode node = JacksonUtil.fromBytes(bytes);
        assertThat(node).isNotNull();
        assertThat(node.get("key").asText()).isEqualTo("value");
    }

    // -----------------------------------------------------------------------
    // writeValueAsBytes
    // -----------------------------------------------------------------------

    @Test
    public void writeValueAsBytes_roundtrip() {
        ObjectNode obj = JacksonUtil.newObjectNode();
        obj.put("num", 42);
        byte[] bytes = JacksonUtil.writeValueAsBytes(obj);
        assertThat(bytes).isNotEmpty();
        JsonNode parsed = JacksonUtil.fromBytes(bytes);
        assertThat(parsed.get("num").asInt()).isEqualTo(42);
    }

    // -----------------------------------------------------------------------
    // toPrettyString
    // -----------------------------------------------------------------------

    @Test
    public void toPrettyString_containsIndentedOutput() {
        ObjectNode obj = JacksonUtil.newObjectNode();
        obj.put("a", 1);
        obj.put("b", 2);
        String pretty = JacksonUtil.toPrettyString(obj);
        // Pretty-printed output should contain newlines and both fields
        assertThat(pretty).contains("\n");
        assertThat(pretty).contains("\"a\"");
        assertThat(pretty).contains("\"b\"");
    }

    // -----------------------------------------------------------------------
    // toCanonicalString
    // -----------------------------------------------------------------------

    @Test
    public void toCanonicalString_keysAreSorted() {
        ObjectNode obj = JacksonUtil.newObjectNode();
        obj.put("z", "last");
        obj.put("a", "first");
        String canonical = JacksonUtil.toCanonicalString(obj);
        assertThat(canonical.indexOf("\"a\"")).isLessThan(canonical.indexOf("\"z\""));
    }

    @Test
    public void toCanonicalString_nullValue_returnsNull() {
        assertThat(JacksonUtil.toCanonicalString(null)).isNull();
    }

    @Test
    public void toCanonicalString_nullFieldsOmitted() {
        ObjectNode obj = JacksonUtil.newObjectNode();
        obj.put("present", "yes");
        obj.putNull("absent");
        String canonical = JacksonUtil.toCanonicalString(obj);
        assertThat(canonical).doesNotContain("absent");
        assertThat(canonical).contains("present");
    }

    // -----------------------------------------------------------------------
    // getSafely
    // -----------------------------------------------------------------------

    @Test
    public void getSafely_existingPath_returnsNode() {
        ObjectNode root = JacksonUtil.newObjectNode();
        ObjectNode child = JacksonUtil.newObjectNode();
        child.put("leaf", "hello");
        root.set("child", child);
        JsonNode result = JacksonUtil.getSafely(root, "child", "leaf");
        assertThat(result).isNotNull();
        assertThat(result.asText()).isEqualTo("hello");
    }

    @Test
    public void getSafely_missingIntermediateKey_returnsNull() {
        ObjectNode root = JacksonUtil.newObjectNode();
        root.put("a", "v");
        assertThat(JacksonUtil.getSafely(root, "missing", "leaf")).isNull();
    }

    @Test
    public void getSafely_nullNode_returnsNull() {
        assertThat(JacksonUtil.getSafely(null, "any")).isNull();
    }

    // -----------------------------------------------------------------------
    // newObjectNode / newArrayNode
    // -----------------------------------------------------------------------

    @Test
    public void newObjectNode_returnsEmptyObjectNode() {
        ObjectNode node = JacksonUtil.newObjectNode();
        assertThat(node).isNotNull();
        assertThat(node.isObject()).isTrue();
        assertThat(node.size()).isEqualTo(0);
    }

    @Test
    public void newArrayNode_returnsEmptyArrayNode() {
        ArrayNode node = JacksonUtil.newArrayNode();
        assertThat(node).isNotNull();
        assertThat(node.isArray()).isTrue();
        assertThat(node.size()).isEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // clone
    // -----------------------------------------------------------------------

    @Test
    public void clone_producesEqualButDistinctObject() {
        Asset original = new Asset();
        original.setName("CloneTest");
        original.setType("sensor");
        Asset cloned = JacksonUtil.clone(original);
        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getName()).isEqualTo(original.getName());
        assertThat(cloned.getType()).isEqualTo(original.getType());
    }

    // -----------------------------------------------------------------------
    // valueToTree
    // -----------------------------------------------------------------------

    @Test
    public void valueToTree_mapsObjectToJsonNode() {
        Asset asset = new Asset();
        asset.setName("TreeTest");
        JsonNode node = JacksonUtil.valueToTree(asset);
        assertThat(node).isNotNull();
        assertThat(node.get("name").asText()).isEqualTo("TreeTest");
    }

    // -----------------------------------------------------------------------
    // asObject
    // -----------------------------------------------------------------------

    @Test
    public void asObject_withObjectNode_returnsSameNode() {
        ObjectNode obj = JacksonUtil.newObjectNode();
        obj.put("k", "v");
        ObjectNode result = JacksonUtil.asObject(obj);
        assertThat(result).isSameAs(obj);
    }

    @Test
    public void asObject_withNullNode_returnsEmptyObjectNode() {
        ObjectNode result = JacksonUtil.asObject(null);
        assertThat(result).isNotNull();
        assertThat(result.isObject()).isTrue();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void asObject_withArrayNode_returnsNewEmptyObjectNode() {
        ArrayNode array = JacksonUtil.newArrayNode();
        array.add("item");
        ObjectNode result = JacksonUtil.asObject(array);
        assertThat(result.isObject()).isTrue();
        assertThat(result.size()).isEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // toFlatMap
    // -----------------------------------------------------------------------

    @Test
    public void toFlatMap_nestedObject_flattensWithDotNotation() {
        ObjectNode root = JacksonUtil.newObjectNode();
        ObjectNode child = JacksonUtil.newObjectNode();
        child.put("leaf", "val");
        root.set("parent", child);
        root.put("top", "direct");
        Map<String, String> flat = JacksonUtil.toFlatMap(root);
        assertThat(flat).containsEntry("parent.leaf", "val");
        assertThat(flat).containsEntry("top", "direct");
    }

    @Test
    public void toFlatMap_emptyObject_returnsEmptyMap() {
        Map<String, String> flat = JacksonUtil.toFlatMap(JacksonUtil.newObjectNode());
        assertThat(flat).isEmpty();
    }

    // -----------------------------------------------------------------------
    // replaceUuidsRecursively
    // -----------------------------------------------------------------------

    @Test
    public void replaceUuidsRecursively_replacesUuidsInTextFields() {
        UUID original = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID replacement = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        ObjectNode node = JacksonUtil.newObjectNode();
        node.put("id", original.toString());
        JacksonUtil.replaceUuidsRecursively(node, Set.of(), null, u -> replacement, true);
        assertThat(node.get("id").asText()).isEqualTo(replacement.toString());
    }

    @Test
    public void replaceUuidsRecursively_skippedRootField_notReplaced() {
        UUID original = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        ObjectNode node = JacksonUtil.newObjectNode();
        node.put("skipped", original.toString());
        JacksonUtil.replaceUuidsRecursively(node, Set.of("skipped"), null, u -> UUID.randomUUID(), true);
        assertThat(node.get("skipped").asText()).isEqualTo(original.toString());
    }

    @Test
    public void replaceUuidsRecursively_uuidsInArrayElements_replaced() {
        UUID original = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        UUID replacement = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        ObjectNode root = JacksonUtil.newObjectNode();
        ArrayNode arr = JacksonUtil.newArrayNode();
        arr.add(original.toString());
        root.set("ids", arr);
        JacksonUtil.replaceUuidsRecursively(root, Set.of(), null, u -> replacement, true);
        assertThat(root.get("ids").get(0).asText()).isEqualTo(replacement.toString());
    }

}
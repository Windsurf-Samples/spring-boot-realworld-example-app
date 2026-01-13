package io.spring.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FieldFilter {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.findAndRegisterModules();
  }

  public static Object filterFields(Object data, String fieldsParam) {
    if (fieldsParam == null || fieldsParam.isEmpty()) {
      return data;
    }

    Set<String> requestedFields = parseFields(fieldsParam);
    JsonNode jsonNode = objectMapper.valueToTree(data);
    JsonNode filteredNode = filterNode(jsonNode, requestedFields, "");
    return filteredNode;
  }

  private static Set<String> parseFields(String fieldsParam) {
    Set<String> fields = new HashSet<>();
    String[] fieldArray = fieldsParam.split(",");
    for (String field : fieldArray) {
      fields.add(field.trim());
    }
    return fields;
  }

  private static JsonNode filterNode(JsonNode node, Set<String> requestedFields, String prefix) {
    if (node.isObject()) {
      return filterObjectNode((ObjectNode) node, requestedFields, prefix);
    } else if (node.isArray()) {
      return filterArrayNode((ArrayNode) node, requestedFields, prefix);
    }
    return node;
  }

  private static ObjectNode filterObjectNode(
      ObjectNode node, Set<String> requestedFields, String prefix) {
    ObjectNode result = objectMapper.createObjectNode();
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> entry = fields.next();
      String fieldName = entry.getKey();
      String fullPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

      if (shouldIncludeField(fullPath, requestedFields)) {
        JsonNode childNode = entry.getValue();
        if (childNode.isObject()) {
          JsonNode filteredChild =
              filterObjectNode((ObjectNode) childNode, requestedFields, fullPath);
          if (filteredChild.size() > 0 || hasExactMatch(fullPath, requestedFields)) {
            result.set(fieldName, filteredChild);
          }
        } else if (childNode.isArray()) {
          result.set(fieldName, filterArrayNode((ArrayNode) childNode, requestedFields, fullPath));
        } else {
          result.set(fieldName, childNode);
        }
      }
    }

    return result;
  }

  private static ArrayNode filterArrayNode(
      ArrayNode node, Set<String> requestedFields, String prefix) {
    ArrayNode result = objectMapper.createArrayNode();
    for (JsonNode element : node) {
      if (element.isObject()) {
        result.add(filterObjectNode((ObjectNode) element, requestedFields, prefix));
      } else {
        result.add(element);
      }
    }
    return result;
  }

  private static boolean shouldIncludeField(String fieldPath, Set<String> requestedFields) {
    for (String requested : requestedFields) {
      if (fieldPath.equals(requested)
          || fieldPath.startsWith(requested + ".")
          || requested.startsWith(fieldPath + ".")) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasExactMatch(String fieldPath, Set<String> requestedFields) {
    return requestedFields.contains(fieldPath);
  }
}

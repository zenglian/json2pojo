package liwey.json2pojo;

import static liwey.json2pojo.ConfigUtil.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import org.jboss.dna.common.text.Inflector;

/**
 * Contains the code to generate Java POJO classes from given JSON text.
 */
class Generator {
  private static final JsonParser jsonParser = new JsonParser();

  private final String moduleSourceRoot;
  private final String packageName;
  private final JTextField resultTextField;

  private Map<String, JDefinedClass> definedClasses = new HashMap<>();
  private JType deferredClass;
  private JType deferredList;
  private FieldComparator fieldComparator;
  private Map<JDefinedClass, Set<FieldInfo>> fieldMap = new HashMap<>();

  Generator(final String packageName, final String moduleSourceRoot, final JTextField
        resultTextField) {
    this.moduleSourceRoot = moduleSourceRoot;
    this.packageName = packageName;
    this.resultTextField = resultTextField;
  }

  /**
   * Generates POJOs from source JSON text.
   *
   * @param rootName the name of the root class to generate.
   * @param json     the source JSON text.
   */
  int generateFromJson(String rootName, String json) throws Exception {
    fieldComparator = new FieldComparator();

    // Create code model and package
    JCodeModel codeModel = new JCodeModel();
    JPackage codeModelPackage = codeModel._package(packageName);

    // Create deferrable types
    deferredClass = codeModel.ref(Deferred.class);
    deferredList = codeModel.ref(List.class).narrow(Deferred.class);

    // Parse the JSON data
    JsonElement rootNode = jsonParser.parse(json);

    // Recursively generate
    int classCount = generate(rootNode.getAsJsonObject(), formatClassName(rootName), codeModelPackage);

    // Build
    codeModel.build(new File(moduleSourceRoot));
    return classCount;
  }

  /**
   * Generates all of the sub-objects and fields for a given class.
   *
   * @param rootNode         the JSON class node in the JSON syntax tree.
   * @param rootName         the name of the root class to generate.
   * @param codeModelPackage the code model package to generate the class in.
   * @throws Exception if an error occurs.
   */
  private int generate(final JsonObject rootNode, final String rootName, final JPackage codeModelPackage) throws Exception {
    // First create all referenced sub-types and collect field data
    parseObject(rootNode, rootName, codeModelPackage);

    // Now create the actual fields
    for (JDefinedClass clazz : definedClasses.values()) {
      // Generate the fields
      SwingUtilities.invokeLater(() -> {
        if (resultTextField != null) {
          resultTextField.setText(R.get("generating", clazz.name()));
        }
      });
      List<GeneratedField> fields = generateFields(clazz, fieldMap.get(clazz), codeModelPackage.owner());
    }
    return definedClasses.size();
  }

  /**
   * Generates all of the sub-objects for a given class.
   *
   * @param classNode        the JSON object node in the JSON syntax tree.
   * @param className        the name of the class to create for this node.
   * @param codeModelPackage the code model package to generate the class in.
   * @throws Exception if an error occurs.
   */
  private void parseObject(final JsonObject classNode, final String className, final JPackage
        codeModelPackage) throws Exception {
    // Find the class if it exists, or create it if it doesn't
    JDefinedClass clazz;
    if (definedClasses.containsKey(className)) {
      clazz = definedClasses.get(className);
    } else {
      clazz = codeModelPackage._class(className);
      annotateClass(clazz);
      definedClasses.put(className, clazz);
      fieldMap.put(clazz, new TreeSet<>(fieldComparator));
    }

    // Iterate over all of the fields in this object
    Set<Map.Entry<String, JsonElement>> fieldsIterator = classNode.entrySet();
    for (Map.Entry<String, JsonElement> entry : fieldsIterator) {
      String childProperty = entry.getKey();
      JsonElement childNode = entry.getValue();

      // Recurse into objects and arrays
      if (childNode.isJsonObject()) {
        String childName = formatClassName(childProperty);
        parseObject(childNode.getAsJsonObject(), childName, codeModelPackage);
      } else if (childNode.isJsonArray()) {
        String childName = formatClassName(Inflector.getInstance().singularize(childProperty));
        parseArray(childNode.getAsJsonArray(), childName, codeModelPackage);
      }

      // Now attempt to create the field and add it to the field set
      FieldInfo field = getFieldInfoFromNode(childNode, childProperty, codeModelPackage.owner());
      if (field != null) {
        fieldMap.get(clazz).add(field);
      }
    }
  }

  /**
   * Generates all of the sub-objects for a given array node.
   *
   * @param arrayNode        the JSON array node in the JSON syntax tree.
   * @param className        the formatted name of the class we might generate from this array.
   * @param codeModelPackage the code model package to generate the class in.
   * @throws Exception if an error occurs.
   */
  private void parseArray(JsonArray arrayNode, String className, JPackage codeModelPackage) throws
        Exception {
    // Retrieve the first non-null element of the array
    Iterator<JsonElement> elementsIterator = arrayNode.iterator();
    while (elementsIterator.hasNext()) {
      JsonElement element = elementsIterator.next();

      // Recurse on the first object or array
      if (element.isJsonObject()) {
        parseObject(element.getAsJsonObject(), className, codeModelPackage);
        break;
      } else if (element.isJsonArray()) {
        parseArray(element.getAsJsonArray(), className, codeModelPackage);
        break;
      }
    }
  }

  /**
   * Creates a field in the given class.
   *
   * @param node         the JSON node describing the field.
   * @param propertyName the name of the field to create.
   * @param codeModel    the code model to use for generation.
   * @return a {@link FieldInfo} representing the new field.
   * @throws Exception if an error occurs.
   */
  private FieldInfo getFieldInfoFromNode(JsonElement node, String propertyName, JCodeModel
        codeModel) throws Exception {
    // Switch on node type
    if (node.isJsonArray()) {
      // Singularize the class name of a single element
      String newClassName = formatClassName(Inflector.getInstance().singularize(propertyName));

      // Get the array type
      JsonArray array = node.getAsJsonArray();
      if (array.iterator().hasNext()) {
        JsonElement firstNode = array.iterator().next();
        if (firstNode.isJsonObject()) {
          // Get the already-created class from the class map
          JDefinedClass newClass = definedClasses.get(newClassName);

          // Now return the field referring to a list of the new class
          return new FieldInfo(codeModel.ref(List.class).narrow(newClass), propertyName);
        } else if (firstNode.isJsonArray()) {
          // Recurse to get the field info of this node
          FieldInfo fi = getFieldInfoFromNode(firstNode, propertyName, codeModel);

          // Make a List<> of the recursed type
          return new FieldInfo(codeModel.ref(List.class).narrow(fi.type), propertyName);
        } else if (firstNode.isJsonNull()) {
          // Null values? Return List<Deferred>.
          return new FieldInfo(deferredList, propertyName);
        } else if (firstNode.isJsonPrimitive()) {
          JsonPrimitive primitiveNode = firstNode.getAsJsonPrimitive();
          if (primitiveNode.isNumber()) {
            Number n = node.getAsNumber();
            if (n.doubleValue() == n.longValue()) {
              return new FieldInfo(codeModel.ref(List.class).narrow(Long.class), propertyName);
            } else {
              return new FieldInfo(codeModel.ref(List.class).narrow(Double.class), propertyName);
            }
          } else if (primitiveNode.isJsonNull()) {
            // Null values? Return List<Deferred>.
            return new FieldInfo(deferredList, propertyName);
          } else if (primitiveNode.isString()) {
            // Now return the field referring to a list of strings
            return new FieldInfo(codeModel.ref(List.class).narrow(String.class), propertyName);
          }
        }
      } else {
        // No elements? Return List<Deferred>.
        return new FieldInfo(deferredList, propertyName);
      }
    } else if (node.isJsonPrimitive()) {
      return getPrimitiveFieldInfo(codeModel, node.getAsJsonPrimitive(), propertyName);
    } else if (node.isJsonNull()) {
      // Defer the type reference until later
      return new FieldInfo(deferredClass, propertyName);
    } else if (node.isJsonObject()) {
      // Get the already-created class from the class map
      String newClassName = formatClassName(propertyName);
      JDefinedClass newClass = definedClasses.get(newClassName);

      // Now return the field as a defined class
      return new FieldInfo(newClass, propertyName);
    }

    // If all else fails, return null
    return null;
  }

  private FieldInfo getPrimitiveFieldInfo(JCodeModel codeModel, JsonPrimitive node, String
        propertyName) {
    if (node.isBoolean()) {
      return new FieldInfo(config.isFieldTypePrimitive() ? codeModel.BOOLEAN
            : codeModel.ref(Boolean.class), propertyName);
    } else if (node.isNumber()) {
      Number n = node.getAsNumber();
      if (n.doubleValue() == n.longValue()) {
        return new FieldInfo(config.isFieldTypePrimitive() ? codeModel.LONG :
              codeModel.ref(Long.class), propertyName);
      } else {
        return new FieldInfo(config.isFieldTypePrimitive() ? codeModel.DOUBLE
              : codeModel.ref(Double.class), propertyName);
      }
    } else if (node.isString()) {
      return new FieldInfo(codeModel.ref(String.class), propertyName);
    }
    return new FieldInfo(deferredClass, propertyName);
  }


  /**
   * Generates all of the fields for a given class.
   *
   * @param clazz     the class to generate sub-objects and fields for.
   * @param fields    the set of fields to generate.
   * @param codeModel the code model.
   * @return a list of generated fields.
   * @throws Exception if an error occurs.
   */
  private List<GeneratedField> generateFields(JDefinedClass clazz, Set<FieldInfo> fields,
                                              JCodeModel codeModel) throws Exception {
    List<GeneratedField> generatedFields = new ArrayList<>();

    // Get sorted list of field names
    for (FieldInfo fieldInfo : fields) {
      // Create field with correct naming scheme
      String fieldName = formatFieldName(fieldInfo.propertyName);

      // Resolve deferred types
      JFieldVar newField;
      if (fieldInfo.type.equals(deferredClass)) {
        // Attempt to get the class from the class map
        String newClassName = formatClassName(fieldInfo.propertyName);
        JDefinedClass newClass = definedClasses.get(newClassName);

        // Now return the field for the actual class type
        if (newClass != null) {
          newField = clazz.field(JMod.PRIVATE, newClass, fieldName);
        } else {
          // Otherwise, just make a field of type Object
          newField = clazz.field(JMod.PRIVATE, codeModel.ref(Object.class), fieldName);
        }
      } else if (fieldInfo.type.equals(deferredList)) {
        // Attempt to get the class from the class map
        String newClassName = formatClassName(Inflector.getInstance()
              .singularize(fieldInfo.propertyName));
        JDefinedClass newClass = definedClasses.get(newClassName);

        // Now return the field referring to a list of the new class
        if (newClass != null) {
          newField = clazz.field(JMod.PRIVATE, codeModel.ref(List.class).narrow(newClass),
                fieldName);
        } else {
          // Otherwise, just make a field of type List<Object>
          newField = clazz.field(JMod.PRIVATE, codeModel.ref(List.class).narrow(Object.class),
                fieldName);
        }
      } else {
        // The type should already be defined so just use it
        newField = clazz.field(JMod.PRIVATE, fieldInfo.type, fieldName);
      }

      if (newField != null) {
        // Annotate field
        annotateField(newField, fieldInfo.propertyName);

        // Create getter/setter if lombok.Data is not present.
        if (!config.isLombokData()) {
          createGetter(clazz, newField, fieldInfo.propertyName);
          createSetter(clazz, newField, fieldInfo.propertyName);
        }

        // Add field to return list
        generatedFields.add(new GeneratedField(newField, fieldInfo.propertyName));
      }
    }

    return generatedFields;
  }

  /**
   * Adds the annotations to the class.
   *
   * @param clazz the class to annotate.
   */
  private static void annotateClass(final JDefinedClass clazz) throws ClassNotFoundException {
    if (config.isLombokNoArgsConstructor()) {
      clazz.annotate(NoArgsConstructor.class);
    }

    if (config.isLombokRequiredArgsConstructor()) {
      clazz.annotate(RequiredArgsConstructor.class);
    }

    if (config.isLombokAllArgsConstructor()) {
      clazz.annotate(AllArgsConstructor.class);
    }

    if (config.isLombokData()) {
      clazz.annotate(Data.class);
    }

    if (config.useAccessors()) {
      JAnnotationUse annotation = clazz.annotate(Accessors.class);
      if (config.isLombokAccessorsFluent()) {
        annotation.param("fluent", true);
      }
      if (config.isLombokAccessorsChain()) {
        annotation.param("chain", true);
      }
      if (!config.getLombokAccessorsPrefix().trim().isEmpty()) {
        annotation.param("prefix", config.getLombokAccessorsPrefix());
      }
    }

    if (config.isLombokBuilder()) {
      clazz.annotate(Builder.class);
    }

    List<String> suppressWarnings = Arrays.asList(config.getSuppressWarnings().trim()
          .split("\\,|\\;|\\ "))
          .stream().filter(x -> !x.isEmpty()).collect(Collectors.toList());
    if (suppressWarnings.size() > 0) {
      JAnnotationUse annotation = clazz.annotate(SuppressWarnings.class);
      if (suppressWarnings.size() == 1) {
        annotation.param("value", suppressWarnings.get(0));
      } else {
        JAnnotationArrayMember member = annotation.paramArray("value");
        for (String value : suppressWarnings) {
          if (!value.isEmpty()) {
            member.param(value);
          }
        }
      }
    }
  }

  /**
   * Adds potentially the {@link SerializedName} annotation to a given
   * field - the latter is applied only if the property name differs from the field name.
   *
   * @param field        the field to annotate.
   * @param propertyName the original JSON property name.
   */
  private static void annotateField(JFieldVar field, String propertyName) {
    // Use the SerializedName annotation if the field name doesn't match the property name
    if (!field.name().equals(propertyName)) {
      if (config.getFieldNameAnnotation() == 1) {
        field.annotate(SerializedName.class).param("value", propertyName);
      } else if (config.getFieldNameAnnotation() == 2) {
        field.annotate(JsonProperty.class).param("value", propertyName);
      }
    }
  }

  /**
   * Generates a getter for the given class, field, and property name.
   *
   * @param clazz        the class to generate a getter in.
   * @param field        the field to return.
   * @param propertyName the name of the property.
   * @return a {@link JMethod} which is a getter for the given field.
   */
  private static JMethod createGetter(final JDefinedClass clazz, final JFieldVar field,
                                      final String propertyName) {
    // Method name should start with "get" and then the uppercased class name.
    JMethod getter = clazz.method(JMod.PUBLIC, field.type(), "get" + formatClassName(propertyName));

    // Return the field
    JBlock body = getter.body();
    body._return(field);
    return getter;
  }

  /**
   * Generates a setter for the given class, field, and property name.
   *
   * @param clazz        the class to generate a setter in.
   * @param field        the field to set.
   * @param propertyName the name of the property.
   * @return a {@link JMethod} which is a setter for the given field.
   */
  private static JMethod createSetter(JDefinedClass clazz, JFieldVar field, String propertyName) {
    // Method name should start with "set" and then the uppercased class name
    JMethod setter = clazz.method(JMod.PUBLIC, void.class, "set" + formatClassName(propertyName));

    // Set parameter name to lower camel case
    String paramName = sanitizePropertyName(propertyName);
    JVar param = setter.param(field.type(), paramName);

    // Assign to field name
    JBlock body = setter.body();
    if (field.name().equals(paramName)) {
      // Assign this.FieldName = paramName
      body.assign(JExpr._this().ref(field), param);
    } else {
      // Safe to just assign FieldName = paramName
      body.assign(field, param);
    }
    return setter;
  }

  /**
   * Formats the given property name into a more standard class name.
   *
   * @param propertyName the original property name.
   * @return the formatted class name.
   */
  static String formatClassName(String propertyName) {
    return StringUtils.capitalize(sanitizePropertyName(propertyName));
  }

  /**
   * Formats the given property name into a more standard field name.
   *
   * @param propertyName the original property name.
   * @return the formatted field name.
   */
  static String formatFieldName(String propertyName) {
    return config.getLombokAccessorsPrefix().trim() + sanitizePropertyName(propertyName);
  }

  /**
   * Given a property name as a string, creates a valid identifier by removing non-alphanumeric
   * characters and uppercasing the letters after non-alphanumeric characters.
   *
   * @param propertyName the property name to format.
   * @return a String containing uppercased words, with underscores removed.
   */
  private static String sanitizePropertyName(String propertyName) {
    final StringBuilder formattedName = new StringBuilder();
    boolean uppercaseNext = false;

    // Avoid invalid starting characters for class / field names
    if (Character.isJavaIdentifierStart(propertyName.charAt(0))) {
      formattedName.append(Character.toLowerCase(propertyName.charAt(0)));
    }

    // Iterate over the other characters
    for (int charIndex = 1; charIndex < propertyName.length(); charIndex++) {
      // Append valid characters
      Character c = propertyName.charAt(charIndex);
      if (Character.isAlphabetic(c)) {
        if (uppercaseNext) {
          // Uppercase this letter
          formattedName.append(Character.toUpperCase(c));
          uppercaseNext = false;
        } else {
          // Retain case, lowers for first
          formattedName.append(formattedName.length() == 0 ? Character.toLowerCase(c) : c);
        }
      } else if (Character.isDigit(c)) {
        // Append as is
        formattedName.append(c);
      } else {
        // Don't append non-alphanumeric parts and uppercase next letter
        uppercaseNext = formattedName.length() > 0;
      }
    }

    if (formattedName.length() == 0) {
      throw new IllegalArgumentException("Illegal property name: \"" + propertyName + "\".");
    }

    return formattedName.toString();
  }

  /**
   * A class type that indicates that we don't yet know the type of data this field represents.
   */
  private static class Deferred {

  }

  /**
   * A comparator that sorts field data objects by field name, case insensitive.
   */
  private static class FieldComparator implements Comparator<FieldInfo> {
    @Override
    public int compare(FieldInfo left, FieldInfo right) {
      // Sort by formatted field name, not the property names
      return formatFieldName(left.propertyName)
            .compareTo(formatFieldName(right.propertyName));
    }
  }

  /**
   * A simple representation of a field to be created.
   */
  @Data
  @AllArgsConstructor
  private static class FieldInfo {
    private JType type;
    private String propertyName;
  }

  /**
   * A pair containing a generated {@link JFieldVar} field and its original property name.
   */
  @Data
  @AllArgsConstructor
  private static class GeneratedField {
    private JFieldVar field;
    private String propertyName;
  }
}

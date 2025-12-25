package games.rednblack.editor.tools;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.generator.impl.DefinitionKey;
import com.github.victools.jsonschema.generator.naming.SchemaDefinitionNamingStrategy;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.h2d.extension.spine.SpineVO;
import games.rednblack.h2d.extension.talos.TalosVO;
import games.rednblack.h2d.extension.tinyvg.TinyVGVO;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchemaGeneratorTool {
    private static final ObjectMapper defaultMapper = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .build();

    private static final Map<Class<?>, String> TAGS = new HashMap<>();
    static {
        TAGS.put(CompositeItemVO.class, "CompositeItemVO");
        TAGS.put(LightVO.class, "LightVO");
        TAGS.put(ParticleEffectVO.class, "ParticleEffectVO");
        TAGS.put(SimpleImageVO.class, "SimpleImageVO");
        TAGS.put(SpriteAnimationVO.class, "SpriteAnimationVO");
        TAGS.put(LabelVO.class, "LabelVO");
        TAGS.put(Image9patchVO.class, "Image9patchVO");
        TAGS.put(ColorPrimitiveVO.class, "ColorPrimitiveVO");
        TAGS.put(SpineVO.class, "SpineVO");
        TAGS.put(TalosVO.class, "TalosVO");
        TAGS.put(TinyVGVO.class, "TinyVGVO");
    }
    private static final Map<Class<?>, Object> prototypeCache = new HashMap<>();

    public static void main(String[] args) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2019_09,
                OptionPreset.PLAIN_JSON
        );

        configBuilder.with(
                Option.PUBLIC_NONSTATIC_FIELDS,
                Option.NONPUBLIC_NONSTATIC_FIELDS_WITHOUT_GETTERS,
                Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES,
                Option.DEFINITIONS_FOR_ALL_OBJECTS,
                Option.ALLOF_CLEANUP_AT_THE_END,
                Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT
        );

        configBuilder.with(new JacksonModule());
        configBuilder.forFields().withPropertyNameOverrideResolver(field -> field.getName());

        configBuilder.forTypesInGeneral().withSubtypeResolver((declaredType, context) -> {
            if (declaredType.getErasedType().getSimpleName().equals("MainItemVO")) {
                return TAGS.keySet().stream()
                        .map(sub -> context.getTypeContext().resolve(sub))
                        .collect(Collectors.toList());
            }
            return null;
        });

        configBuilder.forTypesInGeneral().withTypeAttributeOverride((node, scope, context) -> {
            Class<?> rawClass = scope.getType().getErasedType();
            if (TAGS.containsKey(rawClass)) {
                ObjectNode props = node.has("properties")
                        ? (ObjectNode) node.get("properties")
                        : node.putObject("properties");

                ObjectNode classProperty = props.putObject("class");
                classProperty.put("type", "string");

                ArrayNode enumArray = classProperty.putArray("enum");
                enumArray.add(TAGS.get(rawClass));
            }
        });

        configBuilder.forTypesInGeneral().withCustomDefinitionProvider((javaType, context) -> {
            Class<?> rawClass = javaType.getErasedType();

            if (Array.class.isAssignableFrom(rawClass)) {
                ResolvedType contentType = getGeneric(javaType, 0, context);
                return new CustomDefinition(
                        context.createDefinitionReference(context.getTypeContext().resolve(List.class, contentType))
                );
            }

            if (ObjectMap.class.isAssignableFrom(rawClass) || Map.class.isAssignableFrom(rawClass)) {
                ResolvedType valueType = getGeneric(javaType, 1, context);
                ObjectNode mapNode = context.getGeneratorConfig().getObjectMapper().createObjectNode();
                mapNode.put("type", "object");
                mapNode.set("additionalProperties", context.createDefinitionReference(valueType));

                return new CustomDefinition(mapNode,
                        CustomDefinition.DefinitionType.STANDARD,
                        CustomDefinition.AttributeInclusion.YES);
            }

            return null;
        });

        configBuilder.forFields().withCustomDefinitionProvider((field, context) -> {
            if (field.getName().equals("content") &&
                    field.getDeclaringType().getErasedType().getSimpleName().equals("CompositeItemVO")) {

                var mapper = context.getGeneratorConfig().getObjectMapper();
                ObjectNode contentMapNode = mapper.createObjectNode();

                contentMapNode.put("type", "object");
                ObjectNode propertiesNode = contentMapNode.putObject("properties");

                for (Map.Entry<Class<?>, String> entry : TAGS.entrySet()) {
                    Class<?> voClass = entry.getKey();
                    String tagName = entry.getValue();
                    ResolvedType specificListType = context.getTypeContext().resolve(List.class, voClass);
                    propertiesNode.set(tagName, context.createDefinitionReference(specificListType));
                }

                contentMapNode.put("additionalProperties", false);

                return new CustomPropertyDefinition(contentMapNode);
            }
            return null;
        });

        configBuilder.forFields().withDefaultResolver(fieldScope -> {
            if (fieldScope.isFakeContainerItemScope()) {
                return null;
            }

            try {
                java.lang.reflect.Field field = fieldScope.getRawMember();
                Class<?> declaringClass = field.getDeclaringClass();
                Class<?> classToInstantiate = declaringClass;

                if (java.lang.reflect.Modifier.isAbstract(declaringClass.getModifiers())) {
                    classToInstantiate = TAGS.keySet().stream()
                            .filter(declaringClass::isAssignableFrom)
                            .findFirst()
                            .orElse(null);
                }

                if (classToInstantiate == null) {
                    return null;
                }

                Object prototypeInstance = prototypeCache.computeIfAbsent(classToInstantiate, cls -> {
                    try {
                        java.lang.reflect.Constructor<?> constructor = cls.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        return constructor.newInstance();
                    } catch (Exception e) {
                        return null;
                    }
                });

                if (prototypeInstance == null) {
                    return null;
                }

                field.setAccessible(true);
                Object defaultValue = field.get(prototypeInstance);

                if (defaultValue instanceof Float && ((Float) defaultValue).isNaN()) return null;
                if (defaultValue instanceof Double && ((Double) defaultValue).isNaN()) return null;

                if (defaultValue == null
                        || defaultValue instanceof Map
                        || defaultValue instanceof Iterable) {
                    return null;
                }

                Class<?> defaultClass = defaultValue.getClass();
                if (defaultClass.getName().startsWith("games.rednblack") && !defaultClass.isEnum()) {
                    return null;
                }

                if (defaultValue instanceof String && ((String) defaultValue).isEmpty()) {
                    return "";
                }

                return defaultMapper.valueToTree(defaultValue);
            } catch (Exception e) {
                return null;
            }
        });

        configBuilder.forTypesInGeneral().withDefinitionNamingStrategy(new SchemaDefinitionNamingStrategy() {
            @Override
            public String getDefinitionNameForKey(DefinitionKey key, SchemaGenerationContext context) {
                ResolvedType type = key.getType();
                return generateSafeName(type);
            }

            @Override
            public void adjustDuplicateNames(Map<DefinitionKey, String> subschemasWithDuplicateNames, SchemaGenerationContext context) {
                int index = 1;
                for (Map.Entry<DefinitionKey, String> singleEntry : subschemasWithDuplicateNames.entrySet()) {
                    singleEntry.setValue(singleEntry.getValue() + "_" + index);
                    index++;
                }
            }

            private String generateSafeName(ResolvedType type) {
                String baseName = type.getErasedType().getSimpleName();
                List<ResolvedType> typeParameters = type.getTypeParameters();

                if (typeParameters.isEmpty()) return baseName;

                if (baseName.equals("Array")) {
                    return generateSafeName(typeParameters.get(0)) + "Array";
                }

                if (baseName.contains("Map")) {
                    ResolvedType valueType = typeParameters.size() > 1 ? typeParameters.get(1) : typeParameters.get(0);
                    return "MapOfStringTo" + generateSafeName(valueType);
                }

                return baseName + "Of" + generateSafeName(typeParameters.get(0));
            }
        });

        try {
            SchemaGenerator generator = new SchemaGenerator(configBuilder.build());

            JsonNode jsonSchema = generator.generateSchema(ProjectInfoVO.class);
            Files.write(Paths.get("project.schema.json"), jsonSchema.toPrettyString().getBytes());

            JsonNode jsonSchemaScene = generator.generateSchema(SceneVO.class);
            Files.write(Paths.get("scene.schema.json"), jsonSchemaScene.toPrettyString().getBytes());

            System.out.println("JSON Schema successfully generated");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ResolvedType getGeneric(ResolvedType type, int index, SchemaGenerationContext context) {
        List<ResolvedType> params = type.getTypeParameters();
        if (params.size() > index) return params.get(index);
        return context.getTypeContext().resolve(Object.class);
    }
}
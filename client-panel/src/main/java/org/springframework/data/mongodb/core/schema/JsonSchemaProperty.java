package org.springframework.data.mongodb.core.schema;

import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.ArrayJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.BooleanJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.DateJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.NullJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.NumericJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.ObjectJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.StringJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.TimestampJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.IdentifiableJsonSchemaProperty.UntypedJsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.TypedJsonSchemaObject.NumericJsonSchemaObject;
import org.springframework.data.mongodb.core.schema.TypedJsonSchemaObject.ObjectJsonSchemaObject;

/**
 * A {@literal property} or {@literal patternProperty} within a {@link JsonSchemaObject} of {@code type : 'object'}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.1
 */
public interface JsonSchemaProperty extends JsonSchemaObject {

    /**
     * The identifier can be either the property name or the regex expression properties have to match when used along
     * with {@link ObjectJsonSchemaObject#patternProperties(JsonSchemaProperty...)}.
     *
     * @return never {@literal null}.
     */
    String getIdentifier();

    /**
     * Creates a new {@link UntypedJsonSchemaProperty} with given {@literal identifier} without {@code type}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link UntypedJsonSchemaProperty}.
     */
    static UntypedJsonSchemaProperty untyped(String identifier) {
        return new UntypedJsonSchemaProperty(identifier, JsonSchemaObject.untyped());
    }

    /**
     * Creates a new {@link StringJsonSchemaProperty} with given {@literal identifier} of {@code type : 'string'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link StringJsonSchemaProperty}.
     */
    static StringJsonSchemaProperty string(String identifier) {
        return new StringJsonSchemaProperty(identifier, JsonSchemaObject.string());
    }

    /**
     * Creates a new {@link ObjectJsonSchemaProperty} with given {@literal identifier} of {@code type : 'object'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link ObjectJsonSchemaProperty}.
     */
    static ObjectJsonSchemaProperty object(String identifier) {
        return new ObjectJsonSchemaProperty(identifier, JsonSchemaObject.object());
    }

    /**
     * Creates a new {@link NumericJsonSchemaProperty} with given {@literal identifier} of {@code type : 'number'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link NumericJsonSchemaProperty}.
     */
    static NumericJsonSchemaProperty number(String identifier) {
        return new NumericJsonSchemaProperty(identifier, JsonSchemaObject.number());
    }

    /**
     * Creates a new {@link NumericJsonSchemaProperty} with given {@literal identifier} of {@code bsonType : 'int'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link NumericJsonSchemaProperty}.
     */
    static NumericJsonSchemaProperty int32(String identifier) {
        return new NumericJsonSchemaProperty(identifier, new NumericJsonSchemaObject(Type.intType()));
    }

    /**
     * Creates a new {@link NumericJsonSchemaProperty} with given {@literal identifier} of {@code bsonType : 'long'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link NumericJsonSchemaProperty}.
     */
    static NumericJsonSchemaProperty int64(String identifier) {
        return new NumericJsonSchemaProperty(identifier, new NumericJsonSchemaObject(Type.longType()));
    }

    /**
     * Creates a new {@link NumericJsonSchemaProperty} with given {@literal identifier} of {@code bsonType : 'double'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link NumericJsonSchemaProperty}.
     */
    static NumericJsonSchemaProperty float64(String identifier) {
        return new NumericJsonSchemaProperty(identifier, new NumericJsonSchemaObject(Type.doubleType()));
    }

    /**
     * Creates a new {@link NumericJsonSchemaProperty} with given {@literal identifier} of
     * {@code bsonType : 'decimal128'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link NumericJsonSchemaProperty}.
     */
    static NumericJsonSchemaProperty decimal128(String identifier) {
        return new NumericJsonSchemaProperty(identifier, new NumericJsonSchemaObject(Type.bigDecimalType()));
    }

    /**
     * Creates a new {@link ArrayJsonSchemaProperty} with given {@literal identifier} of {@code type : 'array'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link ArrayJsonSchemaProperty}.
     */
    static ArrayJsonSchemaProperty array(String identifier) {
        return new ArrayJsonSchemaProperty(identifier, JsonSchemaObject.array());
    }

    /**
     * Creates a new {@link BooleanJsonSchemaProperty} with given {@literal identifier} of {@code type : 'boolean'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link BooleanJsonSchemaProperty}.
     */
    static BooleanJsonSchemaProperty bool(String identifier) {
        return new BooleanJsonSchemaProperty(identifier, JsonSchemaObject.bool());
    }

    /**
     * Creates a new {@link BooleanJsonSchemaProperty} with given {@literal identifier} of {@code type : 'null'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link NullJsonSchemaProperty}.
     */
    static NullJsonSchemaProperty nil(String identifier) {
        return new NullJsonSchemaProperty(identifier, JsonSchemaObject.nil());
    }

    /**
     * Creates a new {@link DateJsonSchemaProperty} with given {@literal identifier} of {@code type : 'date'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link DateJsonSchemaProperty}.
     */
    static DateJsonSchemaProperty date(String identifier) {
        return new DateJsonSchemaProperty(identifier, JsonSchemaObject.date());
    }

    /**
     * Creates a new {@link TimestampJsonSchemaProperty} with given {@literal identifier} of {@code type : 'timestamp'}.
     *
     * @param identifier the {@literal property} name or {@literal patternProperty} regex. Must not be {@literal null} nor
     *          {@literal empty}.
     * @return new instance of {@link TimestampJsonSchemaProperty}.
     */
    static TimestampJsonSchemaProperty timestamp(String identifier) {
        return new TimestampJsonSchemaProperty(identifier, JsonSchemaObject.timestamp());
    }

    /**
     * Obtain a builder to create a {@link JsonSchemaProperty}.
     *
     * @param identifier
     * @return
     */
    static JsonSchemaPropertyBuilder named(String identifier) {
        return new JsonSchemaPropertyBuilder(identifier);
    }

    /**
     * Builder for {@link IdentifiableJsonSchemaProperty}.
     */
    class JsonSchemaPropertyBuilder {

        private final String identifier;

        JsonSchemaPropertyBuilder(String identifier){
            this.identifier = identifier;
        }

        /**
         * Configure a {@link Type} for the property.
         *
         * @param type must not be {@literal null}.
         * @return
         */
        public IdentifiableJsonSchemaProperty<TypedJsonSchemaObject> ofType(Type type) {
            return new IdentifiableJsonSchemaProperty<>(identifier, TypedJsonSchemaObject.of(type));
        }

        /**
         * Configure a {@link TypedJsonSchemaObject} for the property.
         *
         * @param schemaObject must not be {@literal null}.
         * @return
         */
        public IdentifiableJsonSchemaProperty<TypedJsonSchemaObject> with(TypedJsonSchemaObject schemaObject) {
            return new IdentifiableJsonSchemaProperty<>(identifier, schemaObject);
        }

        /**
         * @return an untyped {@link IdentifiableJsonSchemaProperty}.
         */
        public IdentifiableJsonSchemaProperty<UntypedJsonSchemaObject> withoutType() {
            return new IdentifiableJsonSchemaProperty<>(identifier, UntypedJsonSchemaObject.newInstance());
        }
    }

}
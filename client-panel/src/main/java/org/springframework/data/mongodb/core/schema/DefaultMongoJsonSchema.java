package org.springframework.data.mongodb.core.schema;


import org.bson.Document;

/**
 * Value object representing a MongoDB-specific JSON schema which is the default {@link MongoJsonSchema} implementation.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.1
 */
class DefaultMongoJsonSchema implements MongoJsonSchema {

    private JsonSchemaObject root;

    public DefaultMongoJsonSchema(){

    }

    public DefaultMongoJsonSchema(JsonSchemaObject root){
        this.root = root;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.core.schema.MongoJsonSchema#toDocument()
     */
    @Override
    public Document toDocument() {
        return new Document("$jsonSchema", root.toDocument());
    }
}
package org.springframework.data.mongodb.core.schema;


import org.bson.Document;

/**
 * JSON schema backed by a {@link org.bson.Document} object.
 *
 * @author Mark Paluch
 * @since 2.1
 */
class DocumentJsonSchema implements MongoJsonSchema {

    private Document document;

    public DocumentJsonSchema(){

    }

    public DocumentJsonSchema(Document document){
        this.document = document;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.core.schema.MongoJsonSchema#toDocument()
     */
    @Override
    public Document toDocument() {
        return new Document("$jsonSchema", new Document(document));
    }
}
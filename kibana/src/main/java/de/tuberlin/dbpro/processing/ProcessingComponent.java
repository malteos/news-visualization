package de.tuberlin.dbpro.processing;

import de.tuberlin.dbpro.model.Document;

public interface ProcessingComponent {

    /**
     *
     * @param _document
     * @return the updated document
     * @throws ProcessingException
     */
    public abstract Document process(Document _document) throws ProcessingException;
}

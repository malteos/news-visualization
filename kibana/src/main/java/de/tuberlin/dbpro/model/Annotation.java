package de.tuberlin.dbpro.model;

import org.apache.log4j.Logger;

@Deprecated
public abstract class Annotation {
    private static Logger LOG = Logger.getLogger(Annotation.class);

    public final static Class[] ANNOTATIONS = {
            SentimentAnnotation.class,
            NERAnnotation.class };

}


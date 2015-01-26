package de.tuberlin.dbpro.model;

import org.apache.log4j.Logger;

@Deprecated
public class HeatmapAnnotation extends Annotation {
    private static Logger LOG = Logger.getLogger(HeatmapAnnotation.class);

    private String heatmapDate = "";

    public String getHeatmapDate() {
        return heatmapDate;
    }

    public void setHeatmapDate(String heatmapDate) {
        this.heatmapDate = heatmapDate;
    }

}

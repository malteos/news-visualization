package de.tuberlin.dbpro.model;

import java.util.List;

@Deprecated
public class NERAnnotation extends Annotation {

    private List<String> nerLoc;
    private List<String> nerMisc;
    private List<String> nerOrg;
    private List<String> nerPer;

    private int nerLocCount;
    private int nerMiscCount;
    private int nerOrgCount;
    private int nerPerCount;


    public int getNerLocCount() {
        return nerLocCount;
    }

    public void setNerLocCount(int nerLocCount) {
        this.nerLocCount = nerLocCount;
    }

    public int getNerMiscCount() {
        return nerMiscCount;
    }

    public void setNerMiscCount(int nerMiscCount) {
        this.nerMiscCount = nerMiscCount;
    }

    public int getNerOrgCount() {
        return nerOrgCount;
    }

    public void setNerOrgCount(int nerOrgCount) {
        this.nerOrgCount = nerOrgCount;
    }

    public int getNerPerCount() {
        return nerPerCount;
    }

    public void setNerPerCount(int nerPerCount) {
        this.nerPerCount = nerPerCount;
    }

    public List<String> getNerLoc() {
        return nerLoc;
    }

    public void setNerLoc(List<String> nerLoc) {
        this.nerLoc = nerLoc;
    }

    public List<String> getNerMisc() {
        return nerMisc;
    }

    public void setNerMisc(List<String> nerMisc) {
        this.nerMisc = nerMisc;
    }

    public List<String> getNerOrg() {
        return nerOrg;
    }

    public void setNerOrg(List<String> nerOrg) {
        this.nerOrg = nerOrg;
    }

    public List<String> getNerPer() {
        return nerPer;
    }

    public void setNerPer(List<String> nerPer) {
        this.nerPer = nerPer;
    }

}

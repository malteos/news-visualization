package de.tuberlin.dbpro.model;


import de.tuberlin.dbpro.storage.StorageException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Document {
    /**
     * Logger for this class.
     */
//    private static Logger LOG = Logger.getLogger(Document.class);

    public static enum Status {
        NEW, PROCESSED, FAILED, SENT, UNSENT
    }

    ;

    public static String[] FIELDS = {"dateString", "news", "pubDate", "srcName", "srcUrl", "subtitle", "title", "status"};


    private String index;
    private String type;
    private String id;
    private String dateString;
    private String message;
    private String news;
    private Long pubDate;
    private String srcName;
    private String srcUrl;
    private String subtitle;
    private String title;
    private String user;
    private Status status;

    private List<Annotation> annotations = new ArrayList<Annotation>();

    public Document(SearchHit res) {
        index = res.getIndex();
        type = res.getType();
        id = res.getId();

//        try {
        title = (String) res.field("title").getValue();
        news = (String) res.field("news").getValue();
        //pubDate = Long.valueOf((String)res.field("pubDate").getValue());
        srcName = (String) res.field("srcName").getValue();
        srcUrl = (String) res.field("srcUrl").getValue();
        subtitle = (String) res.field("subtitle").getValue();
        dateString = (String) res.field("dateString").getValue();
//        } catch(Exception e) {
//            LOG.error("Cannot create document from " + res.getSourceAsString());
//        }

        try {
            setStatus((String) res.field("status").getValue());
        } catch (NullPointerException e) {
            setStatus(Status.NEW);
        }
    }

    public Document(GetResponse res) throws StorageException {

        try {

            index = res.getIndex();
            type = res.getType();
            id = res.getId();

            dateString = (String) res.getField("dateString").getValue();
            title = (String) res.getField("title").getValue();
            news = (String) res.getField("news").getValue();
            //pubDate = Long.valueOf((String) res.getField("pubDate").getValue());
            srcName = (String) res.getField("srcName").getValue();
            srcUrl = (String) res.getField("srcUrl").getValue();
            subtitle = (String) res.getField("subtitle").getValue();
        } catch (NullPointerException e) {
            throw new StorageException("Document not found: " + getPath());
        }

        try {
            setStatus((String) res.getField("status").getValue());
        } catch (NullPointerException e) {
            setStatus(Status.NEW);
        }
    }

    public Document(String index, String type, String id) {
        this.index = index;
        this.type = type;
        this.id = id;
    }


    public Document(String index, String type, String id, String dateString, String news,
                    Long pubDate, String srcName,
                    String srcUrl, String subtitle, String title) {

        this.index = index;
        //this.docID = IOUtils.getMd5Hex(url);
        this.type = type;
        this.id = id;
        this.dateString = dateString;
        this.news = news;
        this.pubDate = pubDate;
        this.srcName = srcName;
        this.srcUrl = srcUrl;
        this.subtitle = subtitle;
        this.title = title;

        this.status = Status.NEW;
        annotations = new ArrayList<Annotation>();
    }

    //    public Document(String url, String fullText, String category, Status status) {
//        this.url = url;
//        //this.docID = IOUtils.getMd5Hex(url);
//        this.fullText = fullText;
//        this.category = category;
//        this.status = status;
//        annotations = new ArrayList<Annotation>();
//    }
    public String getPath() {
        return index + "/" + type + "/" + id;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getDateString() {
        return dateString;
    }

    public String getNews() {
        return news;
    }

    public Long getPubDate() {
        return pubDate;
    }

    public String getSrcName() {
        return srcName;
    }

    public String getScrUrl() {
        return srcUrl;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }


    public String getFullText() {
        return news;
    }

    public void setFullText(String text) {
        news = text;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public String getCategory() {
//        return category;
//    }
//
//    /**
//     * @param category
//     *            the category to set
//     */
//    public void setCategory(String category) {
//        this.category = category;
//    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public <T extends Annotation> List<T> getAnnotation(Class<T> cls) {
        List<T> result = new ArrayList<T>();
        for (Annotation annotation : annotations) {
            if (cls.isInstance(annotation)) {
                result.add((T) annotation);
            }
        }
        return result;

    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        /*
         * StorageManager store = StorageManager.getInstance();
         * store.addAnnotation(getDocID(), annotation);
         */
    }

    /**
     * @return the url
     */
//    public String getUrl() {
//        return this.url;
//    }
//
//    /**
//     * @return the status
//     */
    public Status getStatus() {
        return this.status;
    }
//    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    public void setStatus(String s) {
        if (s.equals("PROCESSED"))
            status = Status.PROCESSED;
        else if (s.equals("FAILED"))
            status = Status.FAILED;
        else
            status = Status.NEW;
    }

    public String fieldsToString() {
        return "status: " + status + ";" +
                "title: " + title + ";" +
                "srcName: " + srcName + ";" +
                "srcUrl: " + srcUrl + ";";
    }

    public String toString() {
        return "Document (" + index + "/" + type + "/" + id + ")";
    }
}

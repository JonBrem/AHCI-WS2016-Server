package de.ur.ahci.meme_crawler;

import de.ur.ahci.model.Meme;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

/**
 * Created by jonbr on 27.10.2015.
 */
public abstract class CrawlSite {

    protected Document doc;
    protected String url;

    public CrawlSite(String url, Document doc) {
        this.doc = doc;
        this.url = url;
    }

    public abstract String[] getNextSites();

    public abstract List<Meme> getMemes();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

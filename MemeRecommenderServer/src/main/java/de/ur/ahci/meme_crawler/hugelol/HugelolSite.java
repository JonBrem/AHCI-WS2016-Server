package de.ur.ahci.meme_crawler.hugelol;

import de.ur.ahci.meme_crawler.CrawlSite;
import de.ur.ahci.model.Meme;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.List;

public class HugelolSite extends CrawlSite {

    private Document doc;

    public HugelolSite(String url, Document doc) {
        super(url, doc);
        this.doc = doc;
    }

    @Override
    public String[] getNextSites() {
        Element nextButton = doc.getElementById("instant_navigation_go_next");
        if(nextButton != null) {
            return new String[] {nextButton.attr("href")};
        }
        return null;
    }

    @Override
    public List<Meme> getMemes() {
        Meme meme = new Meme();

        meme.setUrl(url);
        meme.setTitle(getTitle(doc));
        meme.setImgUrl(getImgUrl(doc));
        meme.addTag("hugelol");

        return Collections.singletonList(meme);
    }

    private String getImgUrl(Document doc) {
        Element img = doc.getElementById("lol-image");
        if(img != null) {
            return img.attr("src");
        } else {
            return "";
        }
    }

    private String getTitle(Document doc) {
        Element headElement = doc.getElementById("head");
        if(headElement != null) {
            Elements titleElements = headElement.getElementsByTag("h1");
            return titleElements.text();
        }
        return "";
    }
}

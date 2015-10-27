package de.ur.ahci.meme_crawler.ninegag;

import de.ur.ahci.meme_crawler.CrawlSite;
import de.ur.ahci.model.Meme;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonbr on 27.10.2015.
 */
public class NineGagSite extends CrawlSite {

    private String tag;

    public NineGagSite(String url, Document doc, String tag) {
        super(url, doc);
        this.tag = tag;
    }

    @Override
    public String[] getNextSites() {
        Elements elements = doc.select(".post-nav .badge-next-post-entry");
        if(elements.size() > 0) {
            String attr = elements.get(0).attr("href");
            String url = "http://9gag.com" + attr;
            return new String[]{url};
        } else {
            return null;
        }
    }

    @Override
    public List<Meme> getMemes() {
        Meme m = new Meme();
        m.addTag(this.tag);
        m.addTag("9gag");
        m.setTitle(getTitle());
        m.setUrl(this.url);
        m.setImgUrl(getImgUrl());

        List<Meme> memes = new ArrayList<>();
        memes.add(m);
        return memes;
    }

    private String getTitle() {
        Elements elements = doc.select("#individual-post .badge-item-title");
        if(elements.size() > 0) {
            return elements.get(0).text();
        } else {
            return "no title";
        }
    }

    private String getImgUrl() {
        Elements elements = doc.select("#individual-post .badge-item-img");
        if(elements.size() > 0) {
            String src = elements.get(0).attr("src");
            return src;
        } else {
            return null;
        }
    }
}

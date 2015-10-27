package de.ur.ahci.meme_crawler;

import de.ur.ahci.model.Meme;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.Const;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by jonbr on 27.10.2015.
 */
public abstract class AbstractCrawler {

    private static Random rnd;

    static {
        rnd = new Random();
    }


    protected List<String> uRLsToCrawl;
    protected Set<String> crawledURLs;

    public AbstractCrawler() {
    }

    protected abstract void readCrawledURLsFromURLdB();

    protected abstract void readURLsToCrawlFromURLFrontier();

    public abstract void writeURLtoURLDB(String url);

    public abstract void removeFromFrontier(String url);

    public abstract boolean alreadyCrawled(String url);

    public abstract void addToFrontier(String url);

    public abstract void storeMeme(Meme meme);

    public void startCrawl() {
        if(uRLsToCrawl.size() > 0) {
            new Thread(new SiteCrawler(uRLsToCrawl.get(0))).start();
        } else {
            Const.log(Const.LEVEL_ERROR, "Crawler could not find any URL to crawl.");
        }
    }

    private synchronized void onSiteCrawled(String url, Document doc) {
        Const.log(Const.LEVEL_VERBOSE, "Site crawled: " + url);

        if(doc == null) {
            Const.log(Const.LEVEL_ERROR, "error crawling: " + url);
            return;
        }

        CrawlSite cs = convertToCrawlSite(url, doc);
        cs.getMemes().forEach(this::storeMeme);
        writeURLtoURLDB(url);
        for(String site : cs.getNextSites()) {
            if(!alreadyCrawled(site)) {
                uRLsToCrawl.add(site);
                addToFrontier(site);
            }
        }

        uRLsToCrawl.remove(url);
        removeFromFrontier(url);

        new Thread(new SleepThread()).start();
    }

    private synchronized void onWaitFinished() {
        Const.log(Const.LEVEL_VERBOSE, "Waiting finished, starting new crawl");
        startCrawl();
    }

    protected abstract CrawlSite convertToCrawlSite(String url, Document doc);

    private class SiteCrawler implements Runnable {

        private String url;

        public SiteCrawler(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            Const.log(Const.LEVEL_VERBOSE, "Crawling " + url);
            try {
                URL urlObj = new URL(url);
                Document doc = Jsoup.parse(urlObj, 10000);
                onSiteCrawled(url, doc);
                return;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            onSiteCrawled(url, null);
        }

    }

    /**
     * In order to support "niceness" (must-have for any crawler),
     * this thread waits for a random time between 3 and 8 seconds before calling the next site.
     */
    private class SleepThread implements Runnable {

        public SleepThread() {

        }

        @Override
        public void run() {
            try {
                Thread.sleep((Math.abs(rnd.nextLong()) + 3000l) % 8000l + 2000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onWaitFinished();
        }

    }

}

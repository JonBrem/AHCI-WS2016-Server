package de.ur.ahci.meme_crawler.ninegag;

import de.ur.ahci.meme_crawler.AbstractCrawler;
import de.ur.ahci.meme_crawler.CrawlSite;
import de.ur.ahci.model.Meme;
import org.jsoup.nodes.Document;
import util.Const;
import util.FileUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jonbr on 27.10.2015.
 */
public class NineGagCrawler extends AbstractCrawler {

    private static final String FRONTIER = "crawl_dummy_db/nine_gag/url_frontier/frontier_XTAGX.txt";
    public static final String CRAWLED_SITES = "crawl_dummy_db/nine_gag/crawled_sites";
    public static final String MEME_FOLDER = "crawl_dummy_db/nine_gag/memes";

    public static void main(String... args) {
        new NineGagCrawler("cute").startCrawl();
    }

    private FileUtility fileUtility;
    private String tag;

    private String frontier;

    public NineGagCrawler(String tag) {
        super();
        fileUtility = new FileUtility();
        this.tag = tag;
        this.frontier = FRONTIER.replaceFirst("XTAGX", this.tag);

        readCrawledURLsFromURLdB();
        readURLsToCrawlFromURLFrontier();
    }

    @Override
    protected void readCrawledURLsFromURLdB() {
        Const.log(Const.LEVEL_VERBOSE, "Reading crawled URLS from db");
        this.crawledURLs = new HashSet<>();
        File urlDBFolder = new File(CRAWLED_SITES);
        for(File file : urlDBFolder.listFiles()) {
            fileUtility.forEveryLineIn(file.getAbsolutePath(), line -> crawledURLs.add(line));
            fileUtility.closeReader(file.getAbsolutePath());
        }
    }

    @Override
    protected void readURLsToCrawlFromURLFrontier() {
        this.uRLsToCrawl = new ArrayList<>();
        fileUtility.forEveryLineIn(frontier, url -> this.uRLsToCrawl.add(url));
        fileUtility.closeReader(frontier);
    }

    @Override
    public void writeURLtoURLDB(String url) {
        Const.log(Const.LEVEL_VERBOSE, "Storing url " + url);
        String fileName = getFileNameInDBDir(CRAWLED_SITES);
        fileUtility.writeLine(fileName, url);
        fileUtility.closeWriter(fileName);
    }

    @Override
    public void removeFromFrontier(String url) {
        Const.log(Const.LEVEL_VERBOSE, "Removing url from frontier: " + url);

        List<String> linesInFrontier = getLinesInFrontier();

        linesInFrontier.remove(url);

        linesInFrontier.forEach(line -> fileUtility.writeLine(frontier, line));
        fileUtility.closeWriter(frontier);
    }

    @Override
    public boolean alreadyCrawled(String url) {
        return this.crawledURLs.contains(url);

    }

    @Override
    public void addToFrontier(String url) {
        Const.log(Const.LEVEL_VERBOSE, "Adding url to frontier: " + url);

        List<String> linesInFrontier = getLinesInFrontier();
        linesInFrontier.add(url);

        linesInFrontier.forEach(line -> fileUtility.writeLine(frontier, line));
        fileUtility.closeWriter(frontier);
    }

    @Override
    public void storeMeme(Meme meme) {
        Const.log(Const.LEVEL_VERBOSE, "Storing meme at " + meme.getUrl());

        String fileName = getFileNameInDBDir(MEME_FOLDER);

        fileUtility.writeLine(fileName, meme.toString());
        fileUtility.closeWriter(fileName);
    }

    private String nDigits(int n, int num) {
        String s = "" + num;
        while(s.length() < n) {
            s = "0" + s;
        }
        return s;
    }

    @Override
    protected CrawlSite convertToCrawlSite(String url, Document doc) {
        return new NineGagSite(url, doc, this.tag);
    }

    private List<String> getLinesInFrontier() {
        return getLinesInFile(frontier);
    }

    private List<String> getLinesInFile(String file) {
        List<String> linesInFrontier = new ArrayList<>();
        fileUtility.forEveryLineIn(file, linesInFrontier::add);
        fileUtility.closeReader(file);
        return linesInFrontier;

    }

    private String getFileNameInDBDir(String dir) {
        File urlDBFolder = new File(dir);

        String fileName;
        File[] filesInUrlDBFolder = urlDBFolder.listFiles();

        if(filesInUrlDBFolder.length == 0) {
            fileName = dir + "/" + nDigits(8, 0) + ".txt";
        } else {
            File lastFile = filesInUrlDBFolder[filesInUrlDBFolder.length - 1];
            List<String> linesInLastFile = getLinesInFile(lastFile.getAbsolutePath());
            String number = lastFile.getName().substring(lastFile.getName().indexOf("."));
            if(linesInLastFile.size() >= 100) {
                fileName = dir + "/" + nDigits(8, (Integer.parseInt(number) + 1)) + ".txt";
            } else {
                linesInLastFile.forEach(line -> fileUtility.writeLine(lastFile.getAbsolutePath(), line));
                fileName = lastFile.getAbsolutePath();
            }
        }
        return fileName;
    }
}

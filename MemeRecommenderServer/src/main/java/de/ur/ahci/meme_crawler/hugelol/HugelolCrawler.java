package de.ur.ahci.meme_crawler.hugelol;

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

public class HugelolCrawler extends AbstractCrawler {

    private static final String FRONTIER = "crawl_dummy_db/hugelol/url_frontier/hugelol_frontier.txt";
    public static final String CRAWLED_SITES = "crawl_dummy_db/hugelol/crawled_sites";

    private FileUtility fileUtility;

    public static void main(String... args) {
        new HugelolCrawler().startCrawl();
    }

    public HugelolCrawler() {
        super();
        this.fileUtility = new FileUtility();

        readURLsToCrawlFromURLFrontier();
        readCrawledURLsFromURLdB();
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
        fileUtility.forEveryLineIn(FRONTIER, url -> this.uRLsToCrawl.add(url));
        fileUtility.closeReader(FRONTIER);
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

        linesInFrontier.forEach(line -> fileUtility.writeLine(FRONTIER, line));
        fileUtility.closeWriter(FRONTIER);
    }

    private List<String> getLinesInFrontier() {
        return getLinesInFile(FRONTIER);
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

        linesInFrontier.forEach(line -> fileUtility.writeLine(FRONTIER, line));
        fileUtility.closeWriter(FRONTIER);
    }

    @Override
    public void storeMeme(Meme meme) {
        new Thread(new MemeStoreThread("http://localhost:8080/crawler/insert", meme)).start();
    }

    @Override
    protected CrawlSite convertToCrawlSite(String url, Document doc) {
        return new HugelolSite(url, doc);
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
            String number = lastFile.getName().substring(0, lastFile.getName().indexOf("."));
            if(linesInLastFile.size() >= 100) {
                fileName = dir + "/" + nDigits(8, (Integer.parseInt(number) + 1)) + ".txt";
            } else {
                linesInLastFile.forEach(line -> fileUtility.writeLine(lastFile.getAbsolutePath(), line));
                fileName = lastFile.getAbsolutePath();
            }
        }
        return fileName;
    }

    private List<String> getLinesInFile(String file) {
        List<String> linesInFrontier = new ArrayList<>();
        fileUtility.forEveryLineIn(file, linesInFrontier::add);
        fileUtility.closeReader(file);
        return linesInFrontier;

    }

    private String nDigits(int n, int num) {
        String s = "" + num;
        while(s.length() < n) {
            s = "0" + s;
        }
        return s;
    }
}

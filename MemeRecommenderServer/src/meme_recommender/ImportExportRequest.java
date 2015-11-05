package meme_recommender;

import util.Const;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * This class should be run from within the IDE while syncing the project.
 *
 * You can specify the BASE_PATH containing the .dat files.
 *
 * Basically, you could write all this as an URL
 * <br>
 * <pre>
 *  path: /exp_imp,
 *  params:
 *      type: (export|import)
 *      table_name:
 *  </pre>
 */
public class ImportExportRequest implements Runnable {

    private static final String BASE_PATH = "C:/users/jonbr/Desktop/";

    public static void main(String... args) {
//        new Thread(ImportExportRequest.exportRequest("memes", "C:/users/jonbr/Desktop/memes.dat")).start();
//        new Thread(ImportExportRequest.exportRequest("tags", "C:/users/jonbr/Desktop/tags.dat")).start();
//        new Thread(ImportExportRequest.exportRequest("meme_tags", "C:/users/jonbr/Desktop/meme_tags.dat")).start();

//        new Thread(ImportExportRequest.importRequest("tags", BASE_PATH + "tags.dat", "ID,TAG_NAME", "2,1")).start();
//        new Thread(ImportExportRequest.importRequest("memes", BASE_PATH + "memes.dat", "ID,URL,IMG_URL,TITLE", "4,1,2,3")).start();
//        new Thread(ImportExportRequest.importRequest("meme_tags", BASE_PATH + "meme_tags.dat", "ID,MEME_ID,TAG_ID", "3,1,2")).start();
    }

    private boolean export;
    private String tableName,
            file,
            columnNames,
            columnIndices;

    public static ImportExportRequest exportRequest(String tableName, String fileTo) {
        ImportExportRequest importExportRequest = new ImportExportRequest();
        importExportRequest.export = true;
        importExportRequest.tableName = tableName;
        importExportRequest.file = fileTo;
        return importExportRequest;
    }

    public static ImportExportRequest importRequest(String tableName, String fileFrom, String columnNames, String columnIndices) {
        ImportExportRequest importExportRequest = new ImportExportRequest();
        importExportRequest.export = false;

        importExportRequest.tableName = tableName;
        importExportRequest.file = fileFrom;
        importExportRequest.columnNames = columnNames;
        importExportRequest.columnIndices = columnIndices;

        return importExportRequest;
    }

    @Override
    public void run() {
        String url = "http://localhost:8080/exp_imp?type";
        if (this.export) {
            url += "=export&table_name=" + tableName + "&to_file=" + file;
        } else {
            url += "=import&table_name=" + tableName + "&from_file=" + file + "&columns=" + columnNames + "&column_indices=" + columnIndices;
        }

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            Const.log(Const.LEVEL_VERBOSE, "EXP_IMP Server Response: " + urlConnection.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

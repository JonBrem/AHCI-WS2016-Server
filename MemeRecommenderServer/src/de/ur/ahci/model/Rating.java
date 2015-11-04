package de.ur.ahci.model;

/**
 * Created by jonbr on 27.10.2015.
 */
public class Rating {

   public static final String CREATE_TABLE = "CREATE TABLE ratings(" +
        "id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), " +
        "user_id INTEGER, " +
        "meme_id INTEGER, " +
        "rating FLOAT" +
        ")";

}

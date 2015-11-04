package de.ur.ahci.model;

/**
 * Created by jonbr on 27.10.2015.
 */
public class User {

    public static final String CREATE_TABLE = "CREATE TABLE users(" +
            "id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1)" +
            ")";

    public static final String CREATE_TABLE_USER_FACE_LANDMARKS = "CREATE TABLE users_face_landmarks(" +
            "user_id INTEGER, " +
            "face_lm_id INTEGER, " +
            "x FLOAT," +
            "y FLOAT" +
            ")";
}
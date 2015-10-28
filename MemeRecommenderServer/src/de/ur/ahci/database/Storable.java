package de.ur.ahci.database;

/**
 * Created by jonbr on 27.10.2015.
 */
public abstract class Storable {

    public abstract String getTableName();
    public abstract String getColumnsString();
    public abstract String getValuesString();
}

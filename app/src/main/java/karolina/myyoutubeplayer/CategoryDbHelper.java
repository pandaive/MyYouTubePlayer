package karolina.myyoutubeplayer;

import static karolina.myyoutubeplayer.DbConstants.COLUMN_IMAGE;
import static karolina.myyoutubeplayer.DbConstants.COLUMN_LABEL;
import static karolina.myyoutubeplayer.DbConstants.COLUMN_NAME;
import static karolina.myyoutubeplayer.DbConstants.DATABASE_NAME;
import static karolina.myyoutubeplayer.DbConstants.TABLE_NAME;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zonka on 20.12.2015.
 */
public class CategoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_LABEL + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_IMAGE + " BLOB);";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public CategoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }
}

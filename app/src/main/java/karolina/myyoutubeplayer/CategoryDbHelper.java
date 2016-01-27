package karolina.myyoutubeplayer;

import static karolina.myyoutubeplayer.DbConstants.COLUMN_ID;
import static karolina.myyoutubeplayer.DbConstants.COLUMN_IMAGE;
import static karolina.myyoutubeplayer.DbConstants.COLUMN_LABEL;
import static karolina.myyoutubeplayer.DbConstants.COLUMN_NAME;
import static karolina.myyoutubeplayer.DbConstants.DATABASE_NAME;
import static karolina.myyoutubeplayer.DbConstants.TABLE_NAME;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

/**
 * Created by zonka on 20.12.2015.
 */
public class CategoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    Context context;
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LABEL + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_IMAGE + " BLOB);";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public CategoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        createDefaultCategories(db);
    }

    private void createDefaultCategories(SQLiteDatabase db){
        Bitmap pandasBmp = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.pandas)).getBitmap();
        Bitmap catsBmp = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.cats)).getBitmap();
        Bitmap dronesBmp = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.drones)).getBitmap();
        Bitmap norwayBmp = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.norway)).getBitmap();

        addCategory("Pandy", "pandas", pandasBmp, db);
        addCategory("Koty", "cats", catsBmp, db);
        addCategory("Drony", "drones", dronesBmp, db);
        addCategory("Norwegia", "norway", norwayBmp, db);
    }

    private void addCategory(String name, String label, Bitmap bmp, SQLiteDatabase db){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LABEL, label);
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_IMAGE, stream.toByteArray());
        db.insert(TABLE_NAME, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }
}

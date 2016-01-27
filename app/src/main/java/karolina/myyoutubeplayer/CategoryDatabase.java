package karolina.myyoutubeplayer;

import static karolina.myyoutubeplayer.DbConstants.COLUMN_ID;
import static karolina.myyoutubeplayer.DbConstants.COLUMN_IMAGE;
import static karolina.myyoutubeplayer.DbConstants.COLUMN_LABEL;
import static karolina.myyoutubeplayer.DbConstants.COLUMN_NAME;
import static karolina.myyoutubeplayer.DbConstants.DATABASE_NAME;
import static karolina.myyoutubeplayer.DbConstants.TABLE_NAME;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Blob;

/**
 * Created by zonka on 20.12.2015.
 */
public class CategoryDatabase implements CategoryProvider {

    private CategoryDbHelper categoryDbHelper;

    public CategoryDatabase(Context context){
        this.categoryDbHelper = new CategoryDbHelper(context);
    }

    public void addCategory(Category category){
        SQLiteDatabase sqLiteDatabase = categoryDbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LABEL, category.getCategoryLabel());
        contentValues.put(COLUMN_NAME, category.getCategoryName());
        contentValues.put(COLUMN_IMAGE, category.getCategoryImage());

        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
    }

    public void deleteCategory(Category category) {
        SQLiteDatabase sqLiteDatabase = categoryDbHelper.getWritableDatabase();

        sqLiteDatabase.delete(TABLE_NAME, COLUMN_ID + "=" + category.getId(), null);
    }

    @Override
    public Category getCategory(int position) {
        SQLiteDatabase sqLiteDatabase = categoryDbHelper.getReadableDatabase();

        String projection[] = {
                COLUMN_ID, COLUMN_LABEL, COLUMN_NAME, COLUMN_IMAGE
        };

        Cursor cursor = sqLiteDatabase.query(
          TABLE_NAME, projection, null, null, null, null, null
        );

        cursor.moveToPosition(position);

        Integer id = cursor.getInt(0);
        String label = cursor.getString(1);
        String name = cursor.getString(2);
        byte[] image = cursor.getBlob(3);

        return new Category(id, label, name, image);
    }

    @Override
    public int getCategoriesNumber() {
        SQLiteDatabase sqLiteDatabase = categoryDbHelper.getReadableDatabase();

        return (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, TABLE_NAME, null, null);
    }
}

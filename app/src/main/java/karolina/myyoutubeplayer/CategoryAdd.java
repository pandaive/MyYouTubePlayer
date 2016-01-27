package karolina.myyoutubeplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CategoryAdd extends AppCompatActivity {

    private Integer PICK_IMAGE = 1;
    private Integer minutes, seconds;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_add);

        //teksty
        final EditText categoryLabel = (EditText) findViewById(R.id.category_add_label);
        final EditText categoryName = (EditText) findViewById(R.id.category_add_name);

        //obrazek
        final Intent getCategoryImage = new Intent();
        getCategoryImage.setType("image/*");
        getCategoryImage.setAction(Intent.ACTION_GET_CONTENT);

        //wybierz obrazek z galerii
        final Button categoryImage = (Button) findViewById(R.id.category_add_image);
        categoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(getCategoryImage, "Select picture"), PICK_IMAGE);
            }
        });


        //dodaj kategorię
        Button categoryAdd = (Button) findViewById(R.id.save_category_button);
        categoryAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryLabelString = categoryLabel.getText().toString();
                String categoryNameString = categoryName.getText().toString();
                byte[] categoryImageFile = {};
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (bitmap != null)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                else {
                    Bitmap bitmap2 = Bitmap.createBitmap(40,40, Bitmap.Config.ARGB_4444);
                    bitmap2.compress(Bitmap.CompressFormat.PNG, 100, stream);
                }
                try {
                    categoryImageFile = stream.toByteArray();
                    if (categoryImageFile.length > 1000000)
                        throw new OutOfMemoryError();
                }
                catch (OutOfMemoryError e) {
                    Toast.makeText(CategoryAdd.this, "Obraz zbyt duży, aby go użyć :(", Toast.LENGTH_SHORT).show();
                    stream.reset();
                    Bitmap bitmap2 = Bitmap.createBitmap(40,40, Bitmap.Config.ARGB_4444);
                    bitmap2.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    categoryImageFile = stream.toByteArray();
                }
                if (categoryNameString.equals(""))
                    Toast.makeText(CategoryAdd.this, "Ustaw nazwę kategorii", Toast.LENGTH_SHORT).show();
                else {
                    if (categoryLabelString.equals(""))
                        categoryLabelString = categoryNameString;
                    saveCategory(new Category(categoryLabelString, categoryNameString, categoryImageFile));
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null)
                try {
                    //ustaw obrazek po wczytaniu na podgląd
                    InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                    Uri uri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    ImageView imageView = (ImageView) findViewById(R.id.selected_image);
                    imageView.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void saveCategory(Category category){
        CategoryDatabase categoryDatabase = new CategoryDatabase(this);
        categoryDatabase.addCategory(category);

        Toast.makeText(this, "Category " + category.getCategoryName() + " saved", Toast.LENGTH_SHORT).show();

        finish();
    }
}

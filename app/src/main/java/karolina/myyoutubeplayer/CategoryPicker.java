package karolina.myyoutubeplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class CategoryPicker extends AppCompatActivity {

    public static final String EXTRA_MINUTES = "minutes";
    public static final String EXTRA_SECONDS = "seconds";
    private Integer minutes, seconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_picker);

        initializeList();

        Intent intent = getIntent();
        minutes = (Integer) intent.getExtras().getSerializable(EXTRA_MINUTES);
        seconds = (Integer) intent.getExtras().getSerializable(EXTRA_SECONDS);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_category: {
                Intent addCategoryIntent = new Intent(this, CategoryAdd.class);
                startActivity(addCategoryIntent);
                return true;
            }
            case R.id.refresh: {
                initializeList();
                return true;
            }
            case R.id.change_time: {
                finish();
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        initializeList();
    }

    private void initializeList(){
        ListView list = (ListView) findViewById(R.id.category_list);
        registerForContextMenu(list);

        final CategoryAdapter categoryAdapter = new CategoryAdapter(this);

        list.setAdapter(categoryAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category = categoryAdapter.getItem(position);
                showCategory(category);
            }
        });
    }

    private void showCategory(Category category) {

        Intent intent = new Intent(this, YoutubePlayer.class);
        intent.putExtra(YoutubePlayer.EXTRA_YOUTUBE_CATEGORY, category.getCategoryLabel().replaceAll("\\s", ""));
        intent.putExtra(YoutubePlayer.EXTRA_MINUTES, minutes);
        intent.putExtra(YoutubePlayer.EXTRA_SECONDS, seconds);

        startActivity(intent);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.category_list) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuItem = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        CategoryDatabase categoryDatabase = new CategoryDatabase(this);
        Category category = categoryDatabase.getCategory(((AdapterView.AdapterContextMenuInfo) menuItem).position);
        categoryDatabase.deleteCategory(category);
        initializeList();
        return true;
    }
}

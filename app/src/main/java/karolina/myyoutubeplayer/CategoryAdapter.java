package karolina.myyoutubeplayer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by zonka on 20.12.2015.
 */
public class CategoryAdapter extends BaseAdapter {

    private CategoryProvider provider;
    private Context context;

    public CategoryAdapter(Context context) {
        this.context=context;
        this.provider = new CategoryDatabase(context);
    }
    @Override
    public int getCount() {
        return provider.getCategoriesNumber();
    }

    @Override
    public Category getItem(int position) {
        return provider.getCategory(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View categoryView;

        if (convertView == null) {
            categoryView = LayoutInflater.from(context).inflate(R.layout.category_row, parent, false);
        }
        else {
            categoryView = convertView;
        }

        bindCategoryToView(getItem(position), categoryView);

        return categoryView;
    }

    private void bindCategoryToView (Category category, View categoryView) {
        ImageView categoryIcon = (ImageView) categoryView.findViewById(R.id.category_icon);
        categoryIcon.setImageBitmap(BitmapFactory.decodeByteArray(category.getCategoryImage(), 0,
                category.getCategoryImage().length));

        TextView categoryName = (TextView) categoryView.findViewById(R.id.category_name);
        categoryName.setText(category.getCategoryName());
    }
}

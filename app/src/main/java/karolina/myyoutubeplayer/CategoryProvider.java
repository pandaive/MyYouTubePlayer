package karolina.myyoutubeplayer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zonka on 20.12.2015.
 */
public interface CategoryProvider {

    Category getCategory(int position);

    int getCategoriesNumber();
}
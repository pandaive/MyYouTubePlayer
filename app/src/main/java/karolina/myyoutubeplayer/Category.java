package karolina.myyoutubeplayer;

import android.media.Image;

import java.io.Serializable;

/**
 * Created by zonka on 20.12.2015.
 */
public class Category implements Serializable {
    private Integer id;
    private String categoryLabel;
    private String categoryName;
    private byte[] categoryImage;

    public Category(int id, String categoryLabel, String categoryName, byte[] categoryImage) {
        this.id = id;
        this.categoryLabel = categoryLabel;
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
    }

    public Category(String categoryLabel, String categoryName, byte[] categoryImage) {
        this.categoryLabel = categoryLabel;
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
    }

    public int getId() { return id; }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public byte[] getCategoryImage() {
        return categoryImage;
    }

}

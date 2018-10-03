package no.fritjof.barcodescanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class Product {
    private String store;
    private long id;
    private String title;
    private String subTitle;
    private String categoryName;
    private float price;
    private float pricePerUnit;
    private float recycle;
    private String unit;
    private Bitmap image;

    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "Store: %s%nID: %d%nName: %s%nSubName: %s%nCategory: %s%nPrice: kr %s%nPrice per unit: %s", store, id, title, subTitle, categoryName, getPricePlusRecycle(), getCompareUnitPriceWithUnit());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(float pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(String src) {
        this.image = getBitmapFromURL(src);
    }

    public String getCompareUnitPriceWithUnit() {
        return String.format(Locale.getDefault(), "kr %f/%s", pricePerUnit, unit);
    }

    public float getRecycle() {
        return recycle;
    }

    public void setRecycle(float recycle) {
        this.recycle = recycle;
    }

    private String getPricePlusRecycle(){
        if(recycle != 0)
            return String.format(Locale.getDefault(), "%f + %f(pant)", (price-recycle), recycle);
        else
            return String.format(Locale.getDefault(), "%f", price);
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }
}

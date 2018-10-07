package no.fritjof.barcodescanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class Product implements Parcelable {
    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
    private String store;
    private long id;
    private String title;
    private String subTitle;
    private String categoryName;
    private float price;
    private float pricePerUnit;
    private float recycle;
    private String unit;
    private String image;
    private String url;

    public Product() {

    }

    public Product(Parcel in) {
        store = in.readString();
        id = in.readLong();
        title = in.readString();
        subTitle = in.readString();
        categoryName = in.readString();
        price = in.readFloat();
        pricePerUnit = in.readFloat();
        recycle = in.readFloat();
        unit = in.readString();
        image = in.readString();
        url = in.readString();
    }

    public static Bitmap getBitmapFromURL(String src) {
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
        return getBitmapFromURL(image);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public URL getUrl() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            // Log exception
            return null;
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCompareUnitPriceWithUnit() {
        return String.format(Locale.getDefault(), "kr %s/%s", pricePerUnit, unit);
    }

    public float getRecycle() {
        return recycle;
    }

    public void setRecycle(float recycle) {
        this.recycle = recycle;
    }

    public String getPricePlusRecycle() {
        if (recycle != 0)
            return String.format(Locale.getDefault(), "%s + %s", (price - recycle), recycle);
        else
            return String.format(Locale.getDefault(), "%s", price);
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(store);
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(subTitle);
        dest.writeString(categoryName);
        dest.writeFloat(price);
        dest.writeFloat(pricePerUnit);
        dest.writeFloat(recycle);
        dest.writeString(unit);
        dest.writeString(image);
        dest.writeString(url);
    }
}

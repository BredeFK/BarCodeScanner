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
    private float comparePricePerUnit;
    private float pricePerUnit;
    private float recycleValue;
    private String compareUnit;
    private String unit;
    private String imageName;
    private String url;
    private boolean isOffer;

    public Product() {

    }

    public Product(Parcel in) {
        store = in.readString();
        id = in.readLong();
        title = in.readString();
        subTitle = in.readString();
        categoryName = in.readString();
        price = in.readFloat();
        comparePricePerUnit = in.readFloat();
        pricePerUnit = in.readFloat();
        recycleValue = in.readFloat();
        compareUnit = in.readString();
        unit = in.readString();
        imageName = in.readString();
        url = in.readString();
        isOffer = in.readInt() != 0;
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
        return String.format(Locale.getDefault(), "Store:  %s%nID:  %d%nName:  %s%nSubName:  %s%nCategory:  %s%nPrice:  %s%nPrice per compareUnit:  %s", store, id, title, subTitle, categoryName, getPriceFormatted(), getCompareUnitPriceWithUnit());
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

    public float getComparePricePerUnit() {
        return comparePricePerUnit;
    }

    public void setComparePricePerUnit(float comparePricePerUnit) {
        this.comparePricePerUnit = comparePricePerUnit;
    }

    public String getCompareUnit() {
        return compareUnit;
    }

    public void setCompareUnit(String compareUnit) {
        this.compareUnit = compareUnit;
    }

    public Bitmap getImageName() {
        return getBitmapFromURL(imageName);
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
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

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isOffer() {
        return isOffer;
    }

    public void setOffer(boolean offer) {
        isOffer = offer;
    }

    public float getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(float pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getCompareUnitPriceWithUnit() {
        if (comparePricePerUnit != 0)
            return String.format(Locale.getDefault(), "kr %s/%s", comparePricePerUnit, compareUnit);
        else
            return "";
    }

    public float getRecycleValue() {
        return recycleValue;
    }

    public void setRecycleValue(float recycleValue) {
        this.recycleValue = recycleValue;
    }

    public String getPriceFormatted() {
        float tempPrice = (isOffer) ? pricePerUnit : price;
        boolean onlyPricePerUnit = (compareUnit.isEmpty() && !unit.isEmpty());

        if (recycleValue != 0)
            return String.format(Locale.getDefault(), "kr %s + %s", tempPrice, recycleValue);
        else if (onlyPricePerUnit)
            return String.format(Locale.getDefault(), "kr %s/%s", tempPrice, unit);
        else
            return String.format(Locale.getDefault(), "kr %s", tempPrice);
    }

    public float getCorrectPrice() {
        return (this.isOffer) ? this.pricePerUnit : this.price;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public boolean isCheaperThan(Product product) {
        return this.getCorrectPrice() < product.getCorrectPrice();
    }

    public boolean isMatchingItem(Product product) {
        return this.getId() == product.getId();
    }

    public float getDifferenceInPrice(Product product) {
        if (this.getCorrectPrice() < product.getCorrectPrice())
            return product.getCorrectPrice() - this.getCorrectPrice();
        else if (this.getCorrectPrice() > product.getCorrectPrice())
            return this.getCorrectPrice() - product.getCorrectPrice();
        else return this.getCorrectPrice();
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
        dest.writeFloat(comparePricePerUnit);
        dest.writeFloat(pricePerUnit);
        dest.writeFloat(recycleValue);
        dest.writeString(compareUnit);
        dest.writeString(unit);
        dest.writeString(imageName);
        dest.writeString(url);
        dest.writeInt((isOffer ? 1 : 0));
    }
}

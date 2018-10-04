package no.fritjof.barcodescanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOW_QUALITY_IMAGE = 1;
    private static final int REQUEST_HIGH_QUALITY_IMAGE = 2;
    private static final int NUMBER_OF_STORES = 1;
    private ImageView imageView;
    private TextView resultView;
    private EditText editText;
    private Store[] stores;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stores = new Store[NUMBER_OF_STORES];

        // Create new store Spar
        stores[0] = new Store("Spar");
        stores[0].setImageURL("https://res.cloudinary.com/norgesgruppen/image/upload/b_white,c_pad,f_auto,h_584,q_50,w_584/");
        stores[0].setSearchURL("https://nettbutikk.spar.no/api/products/search?numberofhitsfortype=products&numberofhitsfortype=recipes&page=1&perpage=20&query=");

        FloatingActionButton actionButton = findViewById(R.id.actionID);
        imageView = findViewById(R.id.imageID);
        resultView = findViewById(R.id.textViewID);
        editText = findViewById(R.id.editTextID);
        Button button = findViewById(R.id.buttonSok);


        // TODO : Remove this code and put it in async later
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // TODO : change this to picture from phone
        // Bitmap barcode = BitmapFactory.decodeResource(this.getResources(), R.drawable.barcode);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().isEmpty()) {
                    Product product = parseJSONtoProduct(stores[0], editText.getText().toString());

                    if (product != null) {
                        resultView.setText(product.toString());
                        imageView.setImageBitmap(product.getImage());
                    }
                }
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // lowQualityImage();
                highQualityImage();
            }
        });
    }

    private void highQualityImage(){
        System.out.println("Snart");
    }


    public void lowQualityImage() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_LOW_QUALITY_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOW_QUALITY_IMAGE && resultCode == RESULT_OK) {

        }
    }

    private String getRawValueFromBarcode(Bitmap image) {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();

        Frame frame = new Frame.Builder().setBitmap(image).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
        if (barcodes.size() > 0) {
            return barcodes.valueAt(0).rawValue;
        }

        return "Could not read barcode :(";
    }

    private JSONObject getJsonObjectFromURL(URL url) throws IOException {
        InputStream is = url.openStream();
        StringBuilder sb = new StringBuilder();
        int cp;
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
            return new JSONObject(jsonText);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
        return null;
    }

    private Product parseJSONtoProduct(Store store, String searchItem) {

        Product product = new Product();
        try {
            URL url = new URL(store.getSearchURL() + searchItem);
            JSONObject jsonObject = getJsonObjectFromURL(url);
            assert jsonObject != null;
            JSONArray param = jsonObject.getJSONArray("products");
            JSONObject object = param.getJSONObject(0);

            product.setStore(store.getName());
            product.setId(object.getInt("id"));
            product.setTitle(object.getString("title"));
            product.setSubTitle(object.getString("subtitle"));
            product.setCategoryName(object.getString("categoryname"));
            product.setPrice((float) object.getDouble("price"));
            product.setPricePerUnit((float) object.getDouble("comparepriceperunit"));
            product.setUnit(object.getString("compareunit"));
            product.setRecycle((float) object.getDouble("recycleValue"));
            product.setImage(store.getImageURL() + object.getString("imagename"));

            return product;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}

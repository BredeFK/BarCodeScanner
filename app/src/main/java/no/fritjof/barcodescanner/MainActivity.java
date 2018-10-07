package no.fritjof.barcodescanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOW_QUALITY_IMAGE = 2;
    private static final int REQUEST_HIGH_QUALITY_IMAGE = 1;
    private static final int NUMBER_OF_STORES = 1;
    private ImageView imageView;
    private TextView resultView;
    private EditText editText;
    private Store[] stores;
    private Uri imageUri = null;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stores = new Store[NUMBER_OF_STORES];

        // Create new store Spar
        stores[0] = new Store("Spar");
        stores[0].setImageURL("https://res.cloudinary.com/norgesgruppen/image/upload/b_white,c_pad,f_auto,h_400,q_50,w_400/");
        stores[0].setSearchURL("https://nettbutikk.spar.no/api/products/search?numberofhitsfortype=products&numberofhitsfortype=recipes&page=1&perpage=20&query=");
        stores[0].setItemURL("https://spar.no/nettbutikk");

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
                searchStoresForProducts(stores[0], editText.getText().toString());
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

    private void searchStoresForProducts(Store store, String barcode){
        if (!barcode.isEmpty()) {
            Product[] products = parseJSONtoProducts(store, barcode);

            if (products != null) {
                if (products.length == 1) {
                    resultView.setText(products[0].toString());
                    imageView.setImageBitmap(products[0].getImage());
                } else if (products.length > 1) {
                    // Display list in Product_List activity
                    Intent intent = new Intent(MainActivity.this, Product_List.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("products_length", products.length);
                    for (int i = 0; i < products.length; i++) {
                        bundle.putParcelable("products" + i, products[i]);
                    }

                    intent.putExtras(bundle);
                    startActivity(intent);

                } else {
                    Toast.makeText(MainActivity.this, "Could not find any products", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    private void highQualityImage() {
        Intent hqIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (hqIntent.resolveActivity(getPackageManager()) != null) {
            File file = null;
            try {
                file = createImageFile();
            } catch (IOException e) {
                e.getStackTrace();
            }
            if (file != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "no.fritjof.android.fileprovider",
                        file);
                hqIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(hqIntent, REQUEST_HIGH_QUALITY_IMAGE);
            }
        }
    }


    public void lowQualityImage() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_LOW_QUALITY_IMAGE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_LOW_QUALITY_IMAGE:

                    break;
                case REQUEST_HIGH_QUALITY_IMAGE:
                    String barcode = getRawValueFromBarcode(getPicture());
                    searchStoresForProducts(stores[0], barcode);
                    break;
                default:
                    break;
            }
        }

    }

    private Bitmap getPicture() {
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, options);
        int photoWidth = options.outWidth;
        int photoHeight = options.outHeight;

        int scale = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);

        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;

        return BitmapFactory.decodeFile(currentPhotoPath, options);
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

    private Product[] parseJSONtoProducts(Store store, String searchItem) {


        try {
            URL url = new URL(store.getSearchURL() + searchItem);
            JSONObject jsonObject = getJsonObjectFromURL(url);
            assert jsonObject != null;
            JSONArray param = jsonObject.getJSONArray("products");

            Product[] products = new Product[param.length()];
            for (int i = 0; i < products.length; i++) {
                JSONObject object = param.getJSONObject(i);
                products[i] = new Product();

                products[i].setStore(store.getName());
                products[i].setId(object.getInt("id"));
                products[i].setTitle(object.getString("title"));
                products[i].setSubTitle(object.getString("subtitle"));
                products[i].setCategoryName(object.getString("categoryname"));
                products[i].setPrice((float) object.getDouble("price"));
                products[i].setPricePerUnit((float) object.getDouble("comparepriceperunit"));
                products[i].setUnit(object.getString("compareunit"));
                products[i].setRecycle((float) object.getDouble("recycleValue"));
                products[i].setImage(store.getImageURL() + object.getString("imagename"));
                products[i].setUrl(store.getItemURL() + object.getString("slugifiedurl"));
            }

            return products;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new Product[0];
    }

}

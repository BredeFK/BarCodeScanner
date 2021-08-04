package no.fritjof.barcodescanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_HIGH_QUALITY_IMAGE = 1;
    private static final int NUMBER_OF_STORES = 2;
    private int selectedItem = 0;
    private ImageView imageView;
    private TextView resultView;
    private EditText searchEntry;
    private FloatingActionButton actionButton;
    private final ArrayList<Store> stores = new ArrayList<>();
    private String currentPhotoPath;
    private Spinner dropDownStores;
    private ConstraintLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getStores();
        getComponents();
        overrideNetworkOnMainThreadException();
        fillSpinnerAndSetOnListeners();
        checkForCompareProduct();
    }

    private void getStores() {

        // Create new store: Spar
        Store spar = new Store("Spar");
        spar.setImageURL("https://res.cloudinary.com/norgesgruppen/image/upload/b_white,c_pad,f_auto,h_400,q_50,w_400/");
        spar.setSearchURL("https://nettbutikk.spar.no/api/products/search?numberofhitsfortype=products&numberofhitsfortype=recipes&page=1&perpage=20&query=");
        spar.setItemURL("https://spar.no/nettbutikk");

        // Create new store: Joker
        Store joker = new Store("Joker");
        joker.setImageURL("https://res.cloudinary.com/norgesgruppen/image/upload/b_white,c_pad,f_auto,h_400,q_50,w_400/");
        joker.setSearchURL("https://nettbutikk.joker.no/api/products/search?numberofhitsfortype=products&numberofhitsfortype=recipes&page=1&perpage=20&query=");
        joker.setItemURL("https://joker.no/nettbutikk");

        // Create new store: Meny
        Store meny = new Store("Meny");
        meny.setImageURL("");
        meny.setSearchURL("");
        meny.setItemURL("");

        stores.add(spar);
        //stores.add(joker);
        //stores.add(meny);
    }

    private void getComponents() {
        mainLayout = findViewById(R.id.mainLayout);
        actionButton = findViewById(R.id.pictureButton);
        imageView = findViewById(R.id.pictureView);
        resultView = findViewById(R.id.productDetails);
        searchEntry = findViewById(R.id.searchEntry);
        dropDownStores = findViewById(R.id.dropdownStores);
    }

    private void checkForCompareProduct() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String barcode = bundle.getString("compareObject");
            if (barcode != null) {
                selectedItem = NUMBER_OF_STORES;
                dropDownStores.setSelection(selectedItem);
                searchEntry.setText(barcode);
                searchStoresForProducts(stores, barcode);
            }
        }
    }

    private void hideKeyboardAndCursor(ConstraintLayout layout, final EditText editText) {
        // Hide cursor
        editText.setCursorVisible(false);

        // Show cursor if clicked on
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setCursorVisible(true);
            }
        });

        // Hide keyboard
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(layout.getWindowToken(), 0);
        }
    }

    private void fillSpinnerAndSetOnListeners() {
        searchEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If search button on keyboard is pressed
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    // Hide keyboard and cursor
                    hideKeyboardAndCursor(mainLayout, searchEntry);

                    resultView.setText("");
                    imageView.setImageDrawable(null);

                    // Search for the product(s)
                    searchStoresForProducts(stores, searchEntry.getText().toString());
                    return true;
                }
                return false;
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highQualityImage();
            }
        });


        ArrayList<String> names = new ArrayList<>();
        for (Store store : stores)
            names.add(store.getName());

        names.add(getString(R.string.cheapest));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropDownStores.setAdapter(adapter);

        dropDownStores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO : save selectedItem to sharedPrefs
                selectedItem = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void overrideNetworkOnMainThreadException() {
        // TODO : Remove this code and put it in async later : https://stackoverflow.com/a/9289190/8883030
        // https://developer.android.com/reference/android/os/AsyncTask
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void searchStoresForProducts(ArrayList<Store> stores, String barcode) {
        if (!barcode.isEmpty()) {

            Product[] products = null;
            String difference = "";

            // If Cheapest is chosen (Highest in stores array is NUMBER_OF_STORES - 1)
            if (selectedItem == NUMBER_OF_STORES) {
                // TODO : make this dynamic
                Product[] sparProducts = parseJSONtoProducts(stores.get(0), barcode);
                Product[] jokerProducts = parseJSONtoProducts(stores.get(1), barcode);

                // If non of the objects are not null
                if (sparProducts != null) {

                    // If both got one match
                    if (sparProducts.length == 1 && jokerProducts.length == 1) {

                        // If it's the same item
                        if (sparProducts[0].isMatchingItem(jokerProducts[0])) {

                            // Check if spar is cheaper
                            if (sparProducts[0].isCheaperThan(jokerProducts[0])) {
                                products = sparProducts;
                            } else {
                                // else joker is cheaper
                                products = jokerProducts;
                            }

                            difference = String.format("\nDifference: kr %s", sparProducts[0].getDifferenceInPrice(jokerProducts[0]));
                        }
                    } else if (sparProducts.length > 1 || jokerProducts.length > 1) {
                        // TODO : make merged list from joker and spar here
                        // TODO : and then, make it dynamic
                        products = sparProducts;
                    }
                }
            } else {
                products = parseJSONtoProducts(stores.get(selectedItem), barcode);
            }

            if (products != null) {
                if (products.length == 1) {
                    resultView.setText(String.format("%s%s", products[0].toString(), difference));
                    imageView.setImageBitmap(products[0].getImageName());
                } else if (products.length > 1) {
                    // Display list in ProductList activity
                    Intent intent = new Intent(MainActivity.this, ProductList.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("products_length", products.length);
                    bundle.putBoolean("cheapest", selectedItem == NUMBER_OF_STORES);

                    for (int i = 0; i < products.length; i++) {
                        bundle.putParcelable("products" + i, products[i]);
                    }

                    intent.putExtras(bundle);
                    startActivity(intent);

                } else {
                    Toast.makeText(MainActivity.this, "Could not find any products", Toast.LENGTH_LONG).show();
                    resultView.setText("");
                    imageView.setImageDrawable(null);
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
                System.out.println("Image error: " + e.getMessage());
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",    /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_HIGH_QUALITY_IMAGE) {
                String barcode = getRawValueFromBarcode(getPicture());
                if (!barcode.isEmpty()) {
                    searchEntry.setText(barcode);
                    imageView.setImageDrawable(null);
                    searchStoresForProducts(stores, barcode);
                } else {
                    Toast.makeText(getBaseContext(), "Could not get barcode!", Toast.LENGTH_LONG).show();
                }
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

        return "";
    }

    private JSONObject getJsonObjectFromURL(URL url) throws IOException {
        /* TODO : Use these
        Map<String, String> parameters = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        parameters.put("types", "");
        parameters.put("search", "");
        parameters.put("page_size", "");
        parameters.put("suggest", "");
        parameters.put("full_response", "");
        parameters.put("popularity", "");
        parameters.put("showNotForSale", "");

        headers.put("Accept", "");
        headers.put("Accept-Encoding", "");
        headers.put("Accept-Language", "");
        headers.put("Content-Type", "");
        headers.put("Host", "");
        headers.put("User-Agent", "");
        headers.put("x-csrf-token", "");
         */

        try (InputStream is = url.openStream()) {
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                int cp;
                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }
                String jsonText = sb.toString();
                return new JSONObject(jsonText);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Product[] parseJSONtoProducts(Store store, String searchItem) {


        try {
            URL url = new URL(store.getSearchURL() + searchItem);
            JSONObject jsonObject = getJsonObjectFromURL(url);

            // Only proceed if jsonObject isn't null
            if (jsonObject != null) {
                JSONArray param = jsonObject.getJSONArray("products");

                Product[] products = new Product[param.length()];
                for (int i = 0; i < products.length; i++) {
                    JSONObject object = param.getJSONObject(i);
                    products[i] = new Product();

                    object.get("comparepriceperunit");
                    float pricePrUnit = (float) object.getDouble("comparepriceperunit");
                    object.get("compareunit");
                    String compareUnit = object.getString("compareunit");

                    products[i].setStore(store.getName());
                    products[i].setId(object.getInt("id"));
                    products[i].setTitle(object.getString("title"));
                    products[i].setSubTitle(object.getString("subtitle"));
                    products[i].setCategoryName(object.getString("categoryname"));
                    products[i].setPrice((float) object.getDouble("price"));
                    products[i].setComparePricePerUnit(pricePrUnit);
                    products[i].setCompareUnit(compareUnit);
                    products[i].setPricePerUnit((float) object.getDouble("priceperunit"));
                    products[i].setOffer(object.getBoolean("isoffer"));
                    products[i].setUnit(object.getString("unit"));
                    products[i].setRecycleValue((float) object.getDouble("recycleValue"));
                    products[i].setImageName(store.getImageURL() + object.getString("imagename"));
                    products[i].setUrl(store.getItemURL() + object.getString("slugifiedurl"));
                }

                return products;
            } else {
                System.out.println("Error: jsonObject is null");
            }
        } catch (IOException | JSONException e) {
            System.out.println("JSON error: " + e.getMessage());
        }
        return new Product[0];
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

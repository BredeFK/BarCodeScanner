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
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_HIGH_QUALITY_IMAGE = 1;
    private static final int NUMBER_OF_STORES = 2;
    private int selectedItem = 0;
    private ImageView imageView;
    private TextView resultView;
    private EditText searchEntry;
    private FloatingActionButton actionButton;
    private Store[] stores;
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
        stores = new Store[NUMBER_OF_STORES];

        // Create new store: Spar
        stores[0] = new Store("Spar");
        stores[0].setImageURL("https://res.cloudinary.com/norgesgruppen/image/upload/b_white,c_pad,f_auto,h_400,q_50,w_400/");
        stores[0].setSearchURL("https://nettbutikk.spar.no/api/products/search?numberofhitsfortype=products&numberofhitsfortype=recipes&page=1&perpage=20&query=");
        stores[0].setItemURL("https://spar.no/nettbutikk");

        // Create new store: Joker
        stores[1] = new Store("Joker");
        stores[1].setImageURL("https://res.cloudinary.com/norgesgruppen/image/upload/b_white,c_pad,f_auto,h_400,q_50,w_400/");
        stores[1].setSearchURL("https://nettbutikk.joker.no/api/products/search?numberofhitsfortype=products&numberofhitsfortype=recipes&page=1&perpage=20&query=");
        stores[1].setItemURL("https://joker.no/nettbutikk");

        // Create new store: Meny

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


        ArrayList<String> array = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_STORES; i++) {
            array.add(stores[i].getName());
        }

        array.add(getString(R.string.cheapest));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, array);
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

    private void searchStoresForProducts(Store[] stores, String barcode) {
        if (!barcode.isEmpty()) {

            Product[] products = null;
            String difference = "";

            // If Cheapest is chosen (Highest in stores array is NUMBER_OF_STORES - 1)
            if (selectedItem == NUMBER_OF_STORES) {
                // TODO : make this dynamic
                Product[] sparProducts = parseJSONtoProducts(stores[0], barcode);
                Product[] jokerProducts = parseJSONtoProducts(stores[1], barcode);

                // If non of the objects are not null
                if (sparProducts != null && jokerProducts != null) {

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
                products = parseJSONtoProducts(stores[selectedItem], barcode);
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
                    if (selectedItem == NUMBER_OF_STORES) {
                        bundle.putBoolean("cheapest", true);
                    } else {
                        bundle.putBoolean("cheapest", false);
                    }

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
            switch (requestCode) {
                case REQUEST_HIGH_QUALITY_IMAGE:
                    String barcode = getRawValueFromBarcode(getPicture());
                    if (!barcode.isEmpty()) {
                        searchEntry.setText(barcode);
                        imageView.setImageDrawable(null);
                        searchStoresForProducts(stores, barcode);
                    } else {
                        Toast.makeText(getBaseContext(), "Could not get barcode!", Toast.LENGTH_LONG).show();
                    }

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

        return "";
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

            // Only proceed if jsonObject isn't null
            if (jsonObject != null) {
                JSONArray param = jsonObject.getJSONArray("products");

                Product[] products = new Product[param.length()];
                for (int i = 0; i < products.length; i++) {
                    JSONObject object = param.getJSONObject(i);
                    products[i] = new Product();

                    float pricePrUnit = (!object.get("comparepriceperunit").equals(null)) ? (float) object.getDouble("comparepriceperunit") : 0;
                    String compareUnit = (!object.get("compareunit").equals(null)) ? object.getString("compareunit") : "";

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

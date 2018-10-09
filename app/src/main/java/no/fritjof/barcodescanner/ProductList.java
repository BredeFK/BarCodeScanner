package no.fritjof.barcodescanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

public class ProductList extends AppCompatActivity {
    private boolean cheapestSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        fillProductList();
    }


    private Product[] getProductArray() {
        Bundle bundle = getIntent().getExtras();
        int length;
        if (bundle != null) {
            length = bundle.getInt("products_length");
            cheapestSelected = bundle.getBoolean("cheapest", false);
            Product[] products = new Product[length];
            for (int i = 0; i < products.length; i++) {
                products[i] = bundle.getParcelable("products" + i);
            }
            return products;
        }
        return new Product[0];
    }

    private void fillProductList() {
        ListView productListView = findViewById(R.id.product_list);
        Product[] products = getProductArray();
        ProductAdapter adapter = new ProductAdapter(ProductList.this, R.layout.product_view, cheapestSelected);
        adapter.clear();
        for (Product product : products) {
            adapter.add(product);
        }
        productListView.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

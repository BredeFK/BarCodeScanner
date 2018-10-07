// Author: Brede Fritjof Klausen

package no.fritjof.barcodescanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductAdapter extends ArrayAdapter<Product> {
    private Activity activity;

    public ProductAdapter(Context context, int id) {
        super(context, id);
        this.activity = (Activity) context;
    }

    @Override
    @NonNull
    public View getView(int pos, View convertView, @NonNull ViewGroup container) {

        final Product productRow = this.getItem(pos);

        if (productRow == null) {
            throw new NullPointerException();
        }

        // TODO : Use AsyncTask here [https://developer.android.com/training/improving-layouts/smooth-scrolling#java]
        View view = this.activity.getLayoutInflater().inflate(R.layout.product_view, container, false);
        ImageView productImage = view.findViewById(R.id.product_image);
        TextView productTitle = view.findViewById(R.id.product_title);
        TextView productSubtitle = view.findViewById(R.id.product_subtitle);
        TextView productPrice = view.findViewById(R.id.product_price);
        TextView productPricePrUnit = view.findViewById(R.id.product_price_pr_unit);
        Button productLink = view.findViewById(R.id.product_link);

        productImage.setImageBitmap(productRow.getImage());
        productTitle.setText(productRow.getTitle());
        productSubtitle.setText(productRow.getSubTitle());
        productPrice.setText(productRow.getPricePlusRecycle());
        productPricePrUnit.setText(productRow.getCompareUnitPriceWithUnit());

        final View finalView = view;
        productLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(getContext(), StorePageActivity.class);
                bundle.putString("itemURL", productRow.getUrl().toString());
                intent.putExtras(bundle);
                finalView.getContext().startActivity(intent);
            }
        });

        return view;
    }
}

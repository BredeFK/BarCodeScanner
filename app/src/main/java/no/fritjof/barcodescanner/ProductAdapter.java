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
import android.widget.Toast;

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
        convertView = this.activity.getLayoutInflater().inflate(R.layout.product_view, container, false);
        ImageView product_image = convertView.findViewById(R.id.product_image);
        TextView product_title = convertView.findViewById(R.id.product_title);
        TextView product_subtitle = convertView.findViewById(R.id.product_subtitle);
        TextView product_price = convertView.findViewById(R.id.product_price);
        TextView product_pricePrUnit = convertView.findViewById(R.id.product_price_pr_unit);
        Button product_link = convertView.findViewById(R.id.product_link);

        product_image.setImageBitmap(productRow.getImage());
        product_title.setText(productRow.getTitle());
        product_subtitle.setText(productRow.getSubTitle());
        product_price.setText(String.format("Kr %s", productRow.getPrice()));
        product_pricePrUnit.setText(productRow.getCompareUnitPriceWithUnit());

        final View finalConvertView = convertView;
        product_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(getContext(), StorePageActivity.class);
                bundle.putString("itemURL", productRow.getUrl().toString());
                intent.putExtras(bundle);
                finalConvertView.getContext().startActivity(intent);
            }
        });

        return convertView;
    }
}

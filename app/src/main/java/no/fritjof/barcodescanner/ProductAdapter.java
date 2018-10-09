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

class ViewHolder {
    ImageView image;
    TextView title;
    TextView subTitle;
    TextView price;
    TextView pricePerUnit;
    Button link;
    Button compare;
}

public class ProductAdapter extends ArrayAdapter<Product> {
    private Activity activity;
    private boolean cheapestSelected;

    ProductAdapter(Context context, int id, boolean cheapestSelected) {
        super(context, id);
        this.activity = (Activity) context;
        this.cheapestSelected = cheapestSelected;
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        final Product productRow = this.getItem(position);

        if (productRow == null) {
            throw new NullPointerException();
        }

        // This simple line makes the ListView way smoother, thanks to
        // https://stackoverflow.com/a/25381936/8883030
        if (convertView == null) {

            convertView = this.activity.getLayoutInflater().inflate(R.layout.product_view, parent, false);

            ViewHolder holder = new ViewHolder();

            holder.image = convertView.findViewById(R.id.product_image);
            holder.title = convertView.findViewById(R.id.product_title);
            holder.subTitle = convertView.findViewById(R.id.product_subtitle);
            holder.price = convertView.findViewById(R.id.product_price);
            holder.pricePerUnit = convertView.findViewById(R.id.product_price_pr_unit);
            holder.link = convertView.findViewById(R.id.product_link);
            holder.compare = convertView.findViewById(R.id.compare);
            convertView.setTag(holder);


            holder.image.setImageBitmap(productRow.getImageName());
            holder.title.setText(productRow.getTitle());
            holder.subTitle.setText(productRow.getSubTitle());
            holder.price.setText(productRow.getPriceFormatted());
            holder.pricePerUnit.setText(productRow.getCompareUnitPriceWithUnit());

            final View finalView = convertView;

            if(cheapestSelected){
                holder.compare.setVisibility(View.VISIBLE);
                holder.compare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("compareObject", productRow.getTitle() + " " + productRow.getSubTitle());
                        intent.putExtras(bundle);
                        finalView.getContext().startActivity(intent);
                    }
                });
            } else{
                holder.compare.setVisibility(View.GONE);
                holder.compare.setOnClickListener(null);
            }

            holder.link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(getContext(), StorePageActivity.class);
                    bundle.putString("itemURL", productRow.getUrl().toString());
                    intent.putExtras(bundle);
                    finalView.getContext().startActivity(intent);
                }
            });

        }
        return convertView;
    }
}

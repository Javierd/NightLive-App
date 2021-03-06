package com.javierd.nightlive.Flyer;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.zxing.WriterException;
import com.javierd.nightlive.R;
import com.javierd.nightlive.Utils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class FlyerPreviewAdapter extends RecyclerView.Adapter<FlyerPreviewAdapter.FlyerViewHolder>{
    private List<Flyer> items;

    static class FlyerViewHolder extends  RecyclerView.ViewHolder{
        ImageView imageView;
        TextView nameTextView;
        TextView infoTextView;
        TextView datesTextView;
        TextView priceTextView;
        ImageView qrImageView;
        ExpandableLayout expandableLayout;

        FlyerViewHolder(View v){
            super(v);
            imageView = (ImageView) v.findViewById(R.id.imageView);
            nameTextView = (TextView) v.findViewById(R.id.nameTextView);
            infoTextView = (TextView) v.findViewById(R.id.infoTextView);
            datesTextView = (TextView) v.findViewById(R.id.datesTextView);
            priceTextView = (TextView) v.findViewById(R.id.priceTextView);
            qrImageView = (ImageView) v.findViewById(R.id.qrImageView);
            expandableLayout = (ExpandableLayout) v.findViewById(R.id.expandableLayout);
        }
    }

    public FlyerPreviewAdapter(List<Flyer> items){
        this.items = items;
    }

    @Override
    public int getItemCount(){
        return items.size();
    }

    @Override
    public FlyerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_flyer, viewGroup, false);
        return new FlyerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final FlyerViewHolder viewHolder, int i){
        String sDate, endDate, price;
        int color = Color.LTGRAY;
        if(items.get(i).getColor() != null){
            color = Color.parseColor(items.get(i).getColor());
        }

        sDate = Utils.milisToDate(items.get(i).getStartTimestamp(), Utils.dateFormat);
        endDate = Utils.milisToDate(items.get(i).getEndTimestamp(), Utils.dateFormat);

        viewHolder.nameTextView.setTextColor(Utils.getTextColor(color));
        viewHolder.datesTextView.setTextColor(Utils.getTextColor(color));
        viewHolder.priceTextView.setTextColor(Utils.getTextColor(color));

        viewHolder.nameTextView.setText(items.get(i).getName());
        viewHolder.infoTextView.setText(items.get(i).getInfo());
        viewHolder.datesTextView.setText(sDate+" - "+endDate);

        System.out.println(items.get(i).getInfo());

        /*Set the QR image*/
        QRGEncoder qrgEncoder = new QRGEncoder(items.get(i).getQr(), null, QRGContents.Type.TEXT, 500);
        try {
            viewHolder.qrImageView.setImageBitmap(qrgEncoder.encodeAsBitmap());
            Log.i("QR",items.get(i).getQr());
        } catch (WriterException e) {
            Log.i("Error","Error al mostrar el QR: "+items.get(i).getQr());
        }


        /*Set the currency*/
        if(items.get(i).getCurrency() != null){
            price = String.valueOf(items.get(i).getPrice()) + " " + items.get(i).getCurrency();
        }else{
            price = String.valueOf(items.get(i).getPrice());
        }
        viewHolder.priceTextView.setText(price);

        RequestOptions options = new RequestOptions()
                .fitCenter()
                .error(R.drawable.app_name);

        if(items.get(i).getColor() != null) {
            viewHolder.imageView.setBackgroundColor(color);
            ColorDrawable colorDrawable = new ColorDrawable(color);
            options = options.placeholder(colorDrawable);
        }

        Glide.with(viewHolder.imageView.getContext())
                .load(items.get(i).getImage())
                .transition(withCrossFade())
                .apply(options)
                .into(viewHolder.imageView);

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.expandableLayout.toggle();
            }
        });
    }
}
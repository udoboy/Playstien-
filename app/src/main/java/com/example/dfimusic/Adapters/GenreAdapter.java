package com.example.dfimusic.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dfimusic.DisplayItemsActivity;
import com.example.dfimusic.Models.GenreModel;
import com.example.dfimusic.R;

import java.util.List;
import java.util.Random;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder>{
    List<GenreModel> genreModelList;
    Context context;
    int genNumber=0;

    public GenreAdapter(List<GenreModel> genreList, Context context) {
        this.genreModelList = genreList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.genre_item, parent, false);
        return new GenreAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
       GenreModel genreModel = genreModelList.get(position);
        holder.giBackgroundImage.setImageBitmap(getBackgroundImage());
        holder.giNameOfGenre.setText(genreModel.getGenreName());
        if (genreModel.getNumberOfGenreSongs() >1){
            holder.giNumberOfSong.setText(genreModel.getNumberOfGenreSongs() + " Songs");
        }
        else{
            holder.giNumberOfSong.setText(genreModel.getNumberOfGenreSongs() + " Song");
        }


        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (genreModel.getNumberOfGenreSongs() >0){
                    if (genreModel.getGenreName()== null || genreModel.getGenreId() == null){
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("Cannot open genre");
                        alertDialog.setMessage("This genre is invalid and might cause your app to stop");

                        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        alertDialog.setCancelable(true);
                        alertDialog.show();

                    }
                    else{
                        Intent intent = new Intent(context, DisplayItemsActivity.class);
                        intent.putExtra("nameList", "genre");
                        intent.putExtra("focusName", genreModel.getGenreName());
                        intent.putExtra("genreId", genreModel.getGenreId());
                        context.startActivity(intent);
                    }
                    
                }
                else{
                    Toast.makeText(context, "There are no songs in this genre", Toast.LENGTH_SHORT).show();
                }
              
            }
        });

    }

    public Bitmap getBackgroundImage(){
        Random random = new Random();
        genNumber =random.nextInt(6);
        if (genNumber==1){
            return  decodeSampledBitmapFromResource(context.getResources(), R.drawable.imgone, 100, 100);
        }
        else if (genNumber ==2){
            return decodeSampledBitmapFromResource(context.getResources(), R.drawable.imgtwo, 100, 100);
        }
        else if (genNumber == 3){
            return decodeSampledBitmapFromResource(context.getResources(), R.drawable.imgthree, 100, 100);
        }
        else if (genNumber== 4){
            return decodeSampledBitmapFromResource(context.getResources(), R.drawable.imgfour, 100, 100);
        }
        else if (genNumber ==5){
            return decodeSampledBitmapFromResource(context.getResources(), R.drawable.imgfiv, 100, 100);
        }
        else{
            genNumber =0;
            return decodeSampledBitmapFromResource(context.getResources(), R.drawable.imgsix, 100, 100);

        }


    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /** Used to load a smaller version of the image to avoid lagging**/
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    @Override
    public int getItemCount() {
        return genreModelList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView giBackgroundImage;
        TextView giNameOfGenre, giNumberOfSong;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            giNameOfGenre = itemView.findViewById(R.id.giNameOfGenre);
            giNumberOfSong = itemView.findViewById(R.id.giNumberOfSongs);
            giBackgroundImage = itemView.findViewById(R.id.giBackgroundImage);

        }
    }
}

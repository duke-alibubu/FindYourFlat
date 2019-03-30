package com.example.androidhdb2.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidhdb2.R;
import com.example.androidhdb2.controllers.UserController;
import com.example.androidhdb2.model.Bookmark;
import com.example.androidhdb2.model.Flat;
import com.example.androidhdb2.model.PastBtoFlat;
import com.example.androidhdb2.model.User;


import java.util.ArrayList;
import java.util.List;

public class BtoAdapter extends RecyclerView.Adapter<BtoAdapter.ViewHolder> {
    private ArrayList<PastBtoFlat> btoFlatList;
    private Context mContext;
    private String userid;

    public BtoAdapter(Context c, ArrayList<PastBtoFlat> l , String userid) {
        this.mContext = c;
        this.btoFlatList = l;
        this.userid = userid;

    }

    @NonNull
    @Override
    public BtoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.list_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final PastBtoFlat flat = btoFlatList.get(i);

        viewHolder.tvName.setText(flat.getLocation());
        viewHolder.tvPrice.setText("$" + String.valueOf(flat.getPrice()));
        viewHolder.tvDetails.setText(flat.getFlatSize() + "\n" + flat.getRegion());

        // insert fragment for image here
    }

    @Override
    public int getItemCount() {
        return btoFlatList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvPrice;
        public TextView tvDetails;
        public ImageView tvUn;
        // other variables in the current Activity
        public View mView;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.itemName);
            tvPrice = itemView.findViewById(R.id.itemPrice);
            tvDetails = itemView.findViewById(R.id.itemDetails);
            tvUn = itemView.findViewById(R.id.bookmarkx);
            // casting for other variables
            mView = itemView;
            tvUn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = UserController.importUser(mContext,mContext.getFilesDir(),userid);
                    Flat flat = btoFlatList.get(getAdapterPosition());
                    if (user.getBookmarkList().contains(new Bookmark(flat))) {
                        Toast.makeText(mContext, "Unbookmarked this flat", Toast.LENGTH_SHORT).show();
                        UserController.removeUserBookmark(mContext,mContext.getFilesDir(), userid, flat);
                    } else {
                        Toast.makeText(mContext, "Bookmarked this flat", Toast.LENGTH_SHORT).show();
                        UserController.addUserBookmark(mContext, mContext.getFilesDir(), userid, flat);
                    }
                }
            });
        }
    }
}

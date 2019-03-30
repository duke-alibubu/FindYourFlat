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
import com.example.androidhdb2.model.SBFlat;
import com.example.androidhdb2.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SBFAdapter extends RecyclerView.Adapter<SBFAdapter.ViewHolder> {
    private List<SBFlat> sbFlatList;
    private Context mContext;
    private String userid;

    public SBFAdapter(Context applicationContext, List<SBFlat> flatArrayList , String userid) {
        this.mContext = applicationContext;
        this.userid = userid;
        if (flatArrayList.size() == 0){
            SBFlat flat = new SBFlat("x", "NO FLAT FOUND", "", 0, 0, new HashMap<String, Integer>(),"NO FLAT FOUND");
            List<SBFlat> l = new ArrayList<SBFlat>();
            l.add(flat);
            this.sbFlatList = l;
        }
        else {
            this.sbFlatList = flatArrayList;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.list_item_sbf, viewGroup, false);
        SBFAdapter.ViewHolder vh = new SBFAdapter.ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final SBFlat flat = sbFlatList.get(i);

        viewHolder.tvName.setText(flat.getRegion() + "\n" + flat.getFlatSize());
        viewHolder.tvPrice.setText("Minimum Price:"+'\n' + "$" + flat.getPrice());
        viewHolder.tvDetails.setText("Flat Supply: " + flat.getFlatSupply() + '\n' +
                "Ethnic Quota: " + flat.getEthnicQuota());

    }

    @Override
    public int getItemCount() {
        return sbFlatList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tvName;
        public TextView tvPrice;
        public TextView tvDetails;
        public View mView;
        public ImageView tvUn;

        public ViewHolder(@NonNull View itemView) {
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
                    Flat flat = sbFlatList.get(getAdapterPosition());
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

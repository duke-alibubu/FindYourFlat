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
import com.example.androidhdb2.model.ResaleFlat;
import com.example.androidhdb2.model.SBFlat;
import com.example.androidhdb2.model.UpcomingBtoFlat;

import java.util.ArrayList;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private ArrayList<Bookmark> bookmarks;
    private Context mContext;
    private String userid;

    public BookmarkAdapter(Context c, ArrayList<Bookmark> l , String userid) {
        this.mContext = c;
        this.bookmarks = l;
        this.userid = userid;
    }


    @NonNull
    @Override
    public BookmarkAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.list_bookmark, viewGroup, false);
        BookmarkAdapter.ViewHolder vh = new BookmarkAdapter.ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkAdapter.ViewHolder viewHolder, int i) {
        Bookmark bookmark = bookmarks.get(i);
        Flat flat = bookmark.getFlat();

        viewHolder.tvName.setText(flat.getLocation());
        if (flat instanceof SBFlat){
            viewHolder.tvDetails.setText("Flat Type: Sale of Balance Flat" + '\n' +
                            flat.getFlatSize() + "\n" +
                            "Flat Supply: " + ((SBFlat)flat).getFlatSupply() + '\n' +
                            "Ethnic Quota: " + ((SBFlat)flat).getEthnicQuota());
        }
        else if (flat instanceof PastBtoFlat){
            viewHolder.tvDetails.setText("Flat Type: Past-launch BTO Flat" + '\n' +
                            ((PastBtoFlat)flat).getPrice() + '\n' +
                    flat.getFlatSize() + "\n" + ((PastBtoFlat)flat).getRegion());
        }
        else if (flat instanceof UpcomingBtoFlat){
            viewHolder.tvDetails.setText( "Flat Type: Upcoming-launch BTO Flat" + '\n' + "Total:" + ((UpcomingBtoFlat) flat).getTotal());
        }
        else if (flat instanceof ResaleFlat){
            viewHolder.tvDetails.setText("Flat Type: Resale Flat" + '\n' +
                    flat.getFlatSize() + "\n" +
                    "Storey Range: " + ((ResaleFlat) flat).getStorey() + '\n' +
                    "Floor Area: "+((ResaleFlat) flat).getFloorArea() + " m2" + '\n' +
                    "Remaining Lease: "+((ResaleFlat) flat).getRemainingLease() + " years");
        }

        // insert fragment for image here
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    private void deleteItem(int position){
        Bookmark bookmark = bookmarks.get(position);
        Flat flat = bookmark.getFlat();
        UserController.removeUserBookmark(mContext, mContext.getFilesDir(), userid, flat);
        bookmarks.remove(bookmark);
        notifyItemRemoved(position);
        Toast.makeText(mContext,"Bookmark removed",Toast.LENGTH_SHORT).show();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvDetails;
        // other variables in the current Activity
        public View mView;
        public ImageView tvUn;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.bookmarkName);
            tvDetails = itemView.findViewById(R.id.bookmarkDetails);
            tvUn = itemView.findViewById(R.id.unbookmark);
            // casting for other variables
            mView = itemView;
            tvUn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteItem(getAdapterPosition());
                }
            });
        }
    }
}

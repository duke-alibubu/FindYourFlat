package com.example.androidhdb2.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidhdb2.R;
import com.example.androidhdb2.controllers.FlatController;
import com.example.androidhdb2.model.ResaleFlat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button click;
    public static TextView data;
    String Flat_Type;
    String Selling_Price_range;
    String Remaining_Lease_range;
    String Storey_range;
    String Floor_Area_range;
    String[] flatdetail = new String[6];
    Bundle bundle;


    // Custom class for custom spinner font
    public static class MySpinnerAdapter extends ArrayAdapter<String> {
        // Initialise custom font
        Typeface font = ResourcesCompat.getFont(getContext(),R.font.playfair_display_bold);

        public MySpinnerAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
        }

        // Affects default (closed) state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            Log.d("VIEW", String.valueOf(view.getText()));
            view.setTypeface(font);
            return view;
        }

        // Affects opened state of the spinner
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setTypeface(font);
            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resale);
        final Intent intent = getIntent();
        final String SearchType = intent.getExtras().getString("SearchType");
        String userID = intent.getStringExtra("UserID");

        // Spinners

        Spinner FlatType = (Spinner) findViewById(R.id.FlatType);
        Spinner Selling_Price_Range = (Spinner) findViewById(R.id.Selling_Price_Range);
        Spinner Remaining_Lease_Range = (Spinner) findViewById(R.id.Remaining_Lease_Range);
        final Spinner Storey_Range = (Spinner) findViewById(R.id.Storey_Range);
        Spinner Floor_Area_Range = (Spinner) findViewById(R.id.Floor_Area_Range);

        // Custom ArrayAdapter to change font
        MySpinnerAdapter FlatTypeAdapter = new MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.FlatType)));

        FlatTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FlatType.setAdapter(FlatTypeAdapter);
        FlatType.setOnItemSelectedListener(this);

        //--------------Selling Price Range DropDown---------------------
        MySpinnerAdapter SellingPriceRangeAdapter = new MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.Selling_Price_Range)));
        Log.d("SELLINGPRICE", String.valueOf(Arrays.asList(getResources().getStringArray(R.array.Selling_Price_Range))));
//        ArrayAdapter<CharSequence> SellingPriceRangeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Selling_Price_Range, android.R.layout.simple_spinner_item);
        SellingPriceRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Selling_Price_Range.setAdapter(SellingPriceRangeAdapter);
        Selling_Price_Range.setOnItemSelectedListener(this);

        //Remaining Lease Range DropDown-----------------------
        MySpinnerAdapter RemainingLeaseRangeAdapter = new MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.Remaining_Lease_Range)));
//        ArrayAdapter<CharSequence> RemainingLeaseRangeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Remaining_Lease_Range, android.R.layout.simple_spinner_item);
        RemainingLeaseRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Remaining_Lease_Range.setAdapter(RemainingLeaseRangeAdapter);
        Remaining_Lease_Range.setOnItemSelectedListener(this);

        //Storey Range DropDown-----------------------
        MySpinnerAdapter StoreyRangeAdapter = new MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.Storey_Range)));
//        ArrayAdapter<CharSequence> StoreyRangeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Storey_Range, android.R.layout.simple_spinner_item);
        StoreyRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Storey_Range.setAdapter(StoreyRangeAdapter);
        Storey_Range.setOnItemSelectedListener(this);

        //Floor Area Square DropDown-----------------------
        MySpinnerAdapter FloorAreaSquareAdapter = new MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.Floor_Area_Range)));
//        ArrayAdapter<CharSequence> FloorAreaSquareAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Floor_Area_Range, android.R.layout.simple_spinner_item);
        FloorAreaSquareAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Floor_Area_Range.setAdapter(FloorAreaSquareAdapter);
        Floor_Area_Range.setOnItemSelectedListener(this);

        //Create Button to get to next page
        click =(Button) findViewById(R.id.button);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Flat_Type.equals("Choose FlatType"))
                {
                    Toast.makeText(FilterActivity.this,"Please Fill in The Flat Type",Toast.LENGTH_LONG).show();
                }
                else{
                    flatdetail[0] = Flat_Type;
                    if(Selling_Price_range != null)
                        flatdetail[1] = Selling_Price_range;
                    if(Remaining_Lease_range != null)
                        flatdetail[2] = Remaining_Lease_range;
                    if(Storey_range != null)
                        flatdetail[3] = Storey_range;
                    if(Floor_Area_range != null)
                        flatdetail[4] = Floor_Area_range;
                    Bundle extras = new Bundle();
                    extras.putStringArray("FLAT DETAILS", flatdetail);
                    extras.putString("SearchType", SearchType);
                    extras.putString("UserID", userID);
                    //extras.putString("MORE STUFFS", MORESTUFFS); Can put multiple stuff in extra
                    Intent intent = new Intent(FilterActivity.this, MapsActivity.class);
                    intent.putExtras(extras);
                    startActivity(intent);
                }

            }

        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.d("FLAT","entered onitemselected class");
        switch (parent.getId()) {
            case R.id.FlatType:
                Flat_Type = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Flat_Type, Toast.LENGTH_LONG).show();
                Log.d("FLAT","FLAT TYPE HAPPENED"+Flat_Type);
                break;
            case R.id.Selling_Price_Range:
                Selling_Price_range = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Selling_Price_range, Toast.LENGTH_LONG).show();
                Log.d("FLAT","SPR HAPPENED");
                break;
            case R.id.Remaining_Lease_Range:
                Remaining_Lease_range = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Remaining_Lease_range, Toast.LENGTH_LONG).show();
                Log.d("FLAT","RLR HAPPENED");
                break;
            case R.id.Storey_Range:
                Storey_range = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Storey_range, Toast.LENGTH_LONG).show();
                Log.d("FLAT","SR HAPPENED");
                break;
            case R.id.Floor_Area_Range:
                Floor_Area_range = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Floor_Area_range, Toast.LENGTH_LONG).show();
                Log.d("FLAT","FAR HAPPENED");
                break;
        }
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback.
        //This is for in the event when previous selection removes the next selection. abit like if else.
            /*This means that the method is called whenever the currently selected item is removed from the list of
            available items.
            As the doc describes, this can occur under different circumstances,
            but generally if the adapter is modified such that the currently selected item is no longer available
            then the method will be called.
            This method may be used so that you can set which item will be selected given that
            the previous item is no longer available. This is instead of letting the spinner
            automatically select the next item in the list.--from google*/
    }


}

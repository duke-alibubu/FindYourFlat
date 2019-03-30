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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidhdb2.R;

import java.util.Arrays;
import java.util.List;

public class FilterActivitySBF extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button click;
    public static TextView data;
    String FlatTypeValue;
    String SellingPriceRange;
    String FlatSupplyRange;
    String EthnicGroup;
    String EthnicGroupQuota;
    String[] flatdetail = new String[6];
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_sbf);
        final Intent intent = getIntent();
        final String SearchType = intent.getExtras().getString("SearchType");
        String userID = intent.getStringExtra("UserID");

        // Spinners

        Spinner FlatType = (Spinner) findViewById(R.id.FlatType);
        Spinner Selling_Price_Range = (Spinner) findViewById(R.id.Selling_Price_Range);
        Spinner Flat_Supply_Range = (Spinner) findViewById(R.id.Flat_Supply_Range);
        Spinner Ethnic_Group = (Spinner) findViewById(R.id.Ethnic_Group);
        Spinner Ethnic_Group_Quota = (Spinner) findViewById(R.id.Ethnic_Group_Quota);

        // Custom ArrayAdapter to change font
        FilterActivity.MySpinnerAdapter FlatTypeAdapter = new FilterActivity.MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.FlatType2)));

        FlatTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FlatType.setAdapter(FlatTypeAdapter);
        FlatType.setOnItemSelectedListener(this);

        //--------------Selling Price Range DropDown---------------------
        FilterActivity.MySpinnerAdapter SellingPriceRangeAdapter = new FilterActivity.MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.Selling_Price_Range2)));
        Log.d("SELLINGPRICE", String.valueOf(Arrays.asList(getResources().getStringArray(R.array.Selling_Price_Range2))));
//        ArrayAdapter<CharSequence> SellingPriceRangeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Selling_Price_Range, android.R.layout.simple_spinner_item);
        SellingPriceRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Selling_Price_Range.setAdapter(SellingPriceRangeAdapter);
        Selling_Price_Range.setOnItemSelectedListener(this);

        //Remaining Lease Range DropDown-----------------------
        FilterActivity.MySpinnerAdapter FlatSupplyRangeAdapter = new FilterActivity.MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.Flat_Supply_Range)));
//        ArrayAdapter<CharSequence> RemainingLeaseRangeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Remaining_Lease_Range, android.R.layout.simple_spinner_item);
        FlatSupplyRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Flat_Supply_Range.setAdapter(FlatSupplyRangeAdapter);
        Flat_Supply_Range.setOnItemSelectedListener(this);

        //Storey Range DropDown-----------------------
        FilterActivity.MySpinnerAdapter EthnicGroupAdapter = new FilterActivity.MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.Ethnic_Group)));
//        ArrayAdapter<CharSequence> StoreyRangeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Storey_Range, android.R.layout.simple_spinner_item);
        EthnicGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Ethnic_Group.setAdapter(EthnicGroupAdapter);
        Ethnic_Group.setOnItemSelectedListener(this);

        //Floor Area Square DropDown-----------------------
        FilterActivity.MySpinnerAdapter EthnicGroupQuotaAdapter = new FilterActivity.MySpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.Ethnic_Group_Quota)));
//        ArrayAdapter<CharSequence> FloorAreaSquareAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Floor_Area_Range, android.R.layout.simple_spinner_item);
        EthnicGroupQuotaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Ethnic_Group_Quota.setAdapter(EthnicGroupQuotaAdapter);
        Ethnic_Group_Quota.setOnItemSelectedListener(this);

        //Create Button to get to next page
        click =(Button) findViewById(R.id.button);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FlatType.equals("Choose FlatType"))
                {
                    Toast.makeText(FilterActivitySBF.this,"Please Fill in The Flat Type",Toast.LENGTH_LONG).show();
                }
                if(EthnicGroupQuota.equals("Select Ethnic Group Quota Range") && !EthnicGroup.equals("Choose Ethnic Group") ||
                        !EthnicGroupQuota.equals("Select Ethnic Group Quota Range") && EthnicGroup.equals("Choose Ethnic Group"))
                    Toast.makeText(FilterActivitySBF.this, "Both Ethnic Group & Quota must be selected.", Toast.LENGTH_LONG).show();
                else{
                    flatdetail[0] = FlatTypeValue;
                    if(SellingPriceRange != null)
                        flatdetail[1] = SellingPriceRange;
                    if(FlatSupplyRange != null)
                        flatdetail[2] = FlatSupplyRange;
                    if(EthnicGroup != null)
                        flatdetail[3] = EthnicGroup;
                    if(EthnicGroupQuota != null)
                        flatdetail[4] = EthnicGroupQuota;
                    Bundle extras = new Bundle();
                    extras.putStringArray("FLAT DETAILS", flatdetail);
                    extras.putString("SearchType", SearchType);
                    extras.putString("UserID", userID);
                    //extras.putString("MORE STUFFS", MORESTUFFS); Can put multiple stuff in extra
                    Intent intent = new Intent(FilterActivitySBF.this, SBFDetailActivity.class);
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
                FlatTypeValue = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Flat_Type, Toast.LENGTH_LONG).show();
                Log.d("FLAT","FLAT TYPE HAPPENED"+FlatTypeValue);
                break;
            case R.id.Selling_Price_Range:
                SellingPriceRange = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Selling_Price_range, Toast.LENGTH_LONG).show();
                Log.d("FLAT","SPR HAPPENED");
                break;
            case R.id.Flat_Supply_Range:
                FlatSupplyRange = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Remaining_Lease_range, Toast.LENGTH_LONG).show();
                Log.d("FLAT","RLR HAPPENED");
                break;
            case R.id.Ethnic_Group:
                EthnicGroup = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(this, Storey_range, Toast.LENGTH_LONG).show();
                Log.d("FLAT","SR HAPPENED");
                break;
            case R.id.Ethnic_Group_Quota:
                EthnicGroupQuota = parent.getItemAtPosition(pos).toString();
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

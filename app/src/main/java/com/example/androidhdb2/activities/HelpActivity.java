package com.example.androidhdb2.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.androidhdb2.R;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

public class HelpActivity extends AppCompatActivity {
    TextView HyperLinkResale, HyperLinkBTO, HyperLinkSBF;
    Spanned TextResale, TextBTO, TextSBF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        HyperLinkBTO = (TextView)findViewById(R.id.textViewBTO);
        TextBTO = Html.fromHtml("<a href='https://www.hdb.gov.sg/cs/infoweb/residential/buying-a-flat/new/bto-sbf'>BTO Flats</a>", FROM_HTML_MODE_COMPACT);
        HyperLinkBTO.setMovementMethod(LinkMovementMethod.getInstance());
        HyperLinkBTO.setText(TextBTO);

        HyperLinkResale = (TextView)findViewById(R.id.textViewResale);
        TextResale = Html.fromHtml("<a href='https://data.gov.sg/dataset/resale-flat-prices'>Resale Flats</a>", FROM_HTML_MODE_COMPACT);
        HyperLinkResale.setMovementMethod(LinkMovementMethod.getInstance());
        HyperLinkResale.setText(TextResale);

        HyperLinkSBF = (TextView)findViewById(R.id.textViewSBF);
        TextSBF = Html.fromHtml("<a href='https://www.hdb.gov.sg/cs/infoweb/residential/buying-a-flat/new/bto-sbf'>SBF Flats</a>", FROM_HTML_MODE_COMPACT);
        HyperLinkSBF.setMovementMethod(LinkMovementMethod.getInstance());
        HyperLinkSBF.setText(TextSBF);
    }
}

package com.ejang.foodwatch.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ejang.foodwatch.Activities.BaseActivity;
import com.ejang.foodwatch.R;

/**
 * Created by eric_ on 2017-08-03.
 */

public class AboutFragment extends Fragment {
    BaseActivity mActivity;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof BaseActivity)
        {
            mActivity = (BaseActivity) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout aboutLayout = (LinearLayout) inflater.inflate(R.layout.content_about, container, false);

        // Set the main text for this page
        TextView aboutBlurb = (TextView) aboutLayout.findViewById(R.id.about_blurb);
        aboutBlurb.setMovementMethod(LinkMovementMethod.getInstance());
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M)
        {
            aboutBlurb.setText(Html.fromHtml(getString(R.string.about_blurb_text)));
        }
        else
        {
            String s = getString(R.string.about_blurb_text);
            System.err.println(s);
            aboutBlurb.setText(Html.fromHtml(getString(R.string.about_blurb_text), Html.FROM_HTML_MODE_LEGACY));
        }

        // Bring up an alert dialog with a list of open source licenses when button is clicked
        LinearLayout openSourceInfo = (LinearLayout) aboutLayout.findViewById(R.id.about_license);
        openSourceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLicensesAlertDialog();
            }
        });

        // Replace current fragment with a new fragment with disclaimer info when button is clicked
        LinearLayout disclaimer = (LinearLayout) aboutLayout.findViewById(R.id.about_disclaimer);
        disclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.getSupportActionBar().setTitle("Disclaimer");
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.container_about, new DisclaimerFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Replace current fragment with a new fragment with privacy policy info when button is clicked
        LinearLayout privacyPolicy = (LinearLayout) aboutLayout.findViewById(R.id.about_privacy_policy);
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.getSupportActionBar().setTitle("Privacy Policy");
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.container_about, new PrivacyPolicyFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return aboutLayout;
    }

    private void displayLicensesAlertDialog() {
        WebView view = (WebView) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Open Source Licenses")
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}

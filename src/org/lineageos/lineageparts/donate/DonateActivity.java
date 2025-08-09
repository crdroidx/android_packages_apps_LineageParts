/*
 * SPDX-FileCopyrightText: 2025 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.lineageparts.donate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import lineageos.providers.LineageSettings;

import org.lineageos.lineageparts.R;

public class DonateActivity extends AppCompatActivity {

    private static final String DONATE_LAST_CHECKED = "pref_donate_checked_in";

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_donate);

        Button donateNow = findViewById(R.id.crdroid_donate_button);
        Button dismiss = findViewById(R.id.crdroid_later_button);

        donateNow.setOnClickListener(v -> openDonatePage());
        dismiss.setOnClickListener(v -> onDismissClick());
    }

    private void openDonatePage() {
        setDonateChecked();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://crdroid.net/donate")));
        finish();
    }

    private void onDismissClick() {
        setDonateChecked();
        finish();
    }

    private void setDonateChecked() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putLong(DONATE_LAST_CHECKED, System.currentTimeMillis())
                .apply();
    }
}

/*
 * Copyright (C) 2019 ExtendedUI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exui.config.center.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.exui.config.center.preferences.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class UITunerFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "UITunerFragment";
    private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
    private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
    private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
    private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";
    private static final String KEY_SCREEN_OFF_FOD = "screen_off_fod";
    private static final String QS_BLUR_INTENSITY = "qs_blur_intensity";
    private static final String KEY_QS_PANEL_ALPHA = "qs_panel_alpha";

    private ContentResolver mResolver;
    private AppMultiSelectListPreference mAspectRatioAppsSelect;
    private ScrollAppsViewPreference mAspectRatioApps;
    private SwitchPreference mScreenOffFOD;
    private SystemSettingSeekBarPreference mQsBlurIntensity;
    private CustomSeekBarPreference mQsPanelAlpha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.config_center_uituner_category);

        PreferenceScreen prefScreen = getPreferenceScreen();
        Context mContext = getContext();

        mResolver = getActivity().getContentResolver();

        if (!getResources().getBoolean(com.android.internal.R.bool.config_supportsInDisplayFingerprint)) {
            prefScreen.removePreference(findPreference("fod_category"));
        }
        final PreferenceCategory aspectRatioCategory =
            (PreferenceCategory) getPreferenceScreen().findPreference(KEY_ASPECT_RATIO_CATEGORY);
        final boolean supportMaxAspectRatio =
            getResources().getBoolean(com.android.internal.R.bool.config_haveHigherAspectRatioScreen);
        if (!supportMaxAspectRatio) {
            getPreferenceScreen().removePreference(aspectRatioCategory);
        } else {
            mAspectRatioAppsSelect =
                (AppMultiSelectListPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST);
            mAspectRatioApps =
                (ScrollAppsViewPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST_SCROLLER);
            final String valuesString = Settings.System.getString(getContentResolver(),
                Settings.System.ASPECT_RATIO_APPS_LIST);
            List<String> valuesList = new ArrayList<String>();
            if (!TextUtils.isEmpty(valuesString)) {
                valuesList.addAll(Arrays.asList(valuesString.split(":")));
                mAspectRatioApps.setVisible(true);
                mAspectRatioApps.setValues(valuesList);
            } else {
                mAspectRatioApps.setVisible(false);
            }
            mAspectRatioAppsSelect.setValues(valuesList);
            mAspectRatioAppsSelect.setOnPreferenceChangeListener(this);
        }

        boolean mScreenOffFODValue = Settings.System.getInt(mResolver,
                Settings.System.SCREEN_OFF_FOD, 0) != 0;

        mScreenOffFOD = (SwitchPreference) findPreference(KEY_SCREEN_OFF_FOD);
        if (mScreenOffFOD != null) {
            mScreenOffFOD.setChecked(mScreenOffFODValue);
            mScreenOffFOD.setOnPreferenceChangeListener(this);

        mQsBlurIntensity = (SystemSettingSeekBarPreference) findPreference(QS_BLUR_INTENSITY);
        int qsBlurIntensity = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_BLUR_INTENSITY, 100);
        mQsBlurIntensity.setValue(qsBlurIntensity);
        mQsBlurIntensity.setOnPreferenceChangeListener(this);

        mQsPanelAlpha = (CustomSeekBarPreference) findPreference(KEY_QS_PANEL_ALPHA);
        int qsPanelAlpha = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_PANEL_BG_ALPHA, 221);
        mQsPanelAlpha.setValue((int)(((double) qsPanelAlpha / 255) * 100));
        mQsPanelAlpha.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAspectRatioAppsSelect) {
            Collection<String> valueList = (Collection<String>) newValue;
            mAspectRatioApps.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(getContentResolver(),
                    Settings.System.ASPECT_RATIO_APPS_LIST, TextUtils.join(":", valueList));
                mAspectRatioApps.setVisible(true);
                mAspectRatioApps.setValues(valueList);
            } else {
                Settings.System.putString(getContentResolver(),
                    Settings.System.ASPECT_RATIO_APPS_LIST, "");
            }
            return true;
        }
        if (preference == mScreenOffFOD) {
            int mScreenOffFODValue = (Boolean) newValue ? 1 : 0;
            Settings.System.putInt(mResolver, Settings.System.SCREEN_OFF_FOD, mScreenOffFODValue);
            Settings.Secure.putInt(mResolver, Settings.Secure.DOZE_ALWAYS_ON, mScreenOffFODValue);
            return true;
        }
        if (preference == mQsBlurIntensity) {
            Context mContext = getContext();
            int value = (Integer) newValue;
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.QS_BLUR_INTENSITY, value);
            return true;
        }
        if (preference == mQsPanelAlpha) {
            int bgAlpha = (Integer) newValue;
            int trueValue = (int) (((double) bgAlpha / 100) * 255);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.QS_PANEL_BG_ALPHA, trueValue);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

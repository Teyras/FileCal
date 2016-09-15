package cz.bucharjan.filecal.ui;


import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import cz.bucharjan.filecal.R;
import cz.bucharjan.filecal.config.AccountConfig;
import cz.bucharjan.filecal.config.AccountStorage;
import cz.bucharjan.filecal.config.LocalIcsConfig;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class AccountSettingsActivity extends AppCompatActivity {
    public final static String KEY_NEW_ACCOUNT = "NewAccount";
    public final static String EXTRA_NO_HEADERS = ":android:no_headers";
    public final static String EXTRA_SHOW_FRAGMENT = ":android:show_fragment";
    private static final String TAG = "FileCal.AccountSettings";
    public static final String ACCOUNT_TYPE = "cz.bucharjan.filecal";
    private AccountManager accountManager;
    private AccountStorage accountStorage;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private FragmentSwitcher switcher = new FragmentSwitcher() {
        @Override
        public void switchFragment(Fragment fragment) {
            String backStateName = ((Object) fragment).getClass().getName();

            try {
                FragmentManager manager = getFragmentManager();
                boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

                if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
                    FragmentTransaction transaction = manager.beginTransaction();

                    transaction.replace(R.id.new_account_layout, fragment, backStateName);
                    transaction.addToBackStack(backStateName);

                    transaction.commit();
                }
            } catch (IllegalStateException exception) {
                Log.w(TAG, exception.toString());
            }
        }
    };

    private SettingsFragmentResultListener resultListener = new SettingsFragmentResultListener() {
        @Override
        public void onResult(AccountConfig config) {
            Account account = new Account(config.getName(), ACCOUNT_TYPE);
            accountManager.addAccountExplicitly(account, "", null);
            accountStorage.save(account, config);
            finish();
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountManager = AccountManager.get(this);
        accountStorage = new AccountStorage(accountManager);

        setContentView(R.layout.new_account);
        setupActionBar();

        CalendarTypeFragment fragment = new CalendarTypeFragment();
        fragment.setDependencies(switcher, resultListener);

        switcher.switchFragment(fragment);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CalendarTypeFragment extends PreferenceFragment {
        private FragmentSwitcher switcher;
        private SettingsFragmentResultListener resultListener;

        public void setDependencies(FragmentSwitcher switcher, SettingsFragmentResultListener resultListener) {
            this.switcher = switcher;
            this.resultListener = resultListener;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_calendar_type);

            findPreference("local_ics").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (switcher != null) {
                        LocalIcsPreferenceFragment fragment = new LocalIcsPreferenceFragment();
                        fragment.setResultListener(resultListener);
                        switcher.switchFragment(fragment);
                    }

                    return true;
                }
            });
        }
    }

    public static class LocalIcsPreferenceFragment extends PreferenceFragment {
        private static final int FILE_SELECT_CODE = 0;
        public static final int REQUEST_CODE = 1;

        private SettingsFragmentResultListener resultListener;

        public void setResultListener(SettingsFragmentResultListener resultListener) {
            this.resultListener = resultListener;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_local_ics);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("name"));
            bindPreferenceSummaryToValue(findPreference("calendar_path"));

            findPreference("calendar_path").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("text/calendar");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    try {
                        startActivityForResult(
                                Intent.createChooser(intent, "Select a File to Upload"),
                                FILE_SELECT_CODE);
                    } catch (android.content.ActivityNotFoundException ex) {
                        // TODO
                    }

                    return true;
                }
            });
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == FILE_SELECT_CODE) {
                Uri uri = data.getData();
                SharedPreferences preferences = this.getPreferenceManager().getSharedPreferences();
                preferences.edit().putString("calendar_path", uri.toString()).apply();
                return;
            }

            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.home) {
                startActivity(new Intent(getActivity(), AccountSettingsActivity.class));
                return true;
            }

            if (id == R.id.action_save) {
                submit();
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private void submit() {
            SharedPreferences preferences = this.getPreferenceManager().getSharedPreferences();
            LocalIcsConfig config = new LocalIcsConfig(
                    preferences.getString("name", null),
                    preferences.getString("calendar_path", null),
                    preferences.getBoolean("import_alarms", false),
                    preferences.getBoolean("keep_local", true),
                    preferences.getBoolean("sync_bidirectional", true)
            );

            if (resultListener != null) {
                resultListener.onResult(config);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_calendar, menu);
        }
    }
}

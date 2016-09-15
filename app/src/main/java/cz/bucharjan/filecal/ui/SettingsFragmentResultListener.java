package cz.bucharjan.filecal.ui;

import cz.bucharjan.filecal.config.AccountConfig;

/**
 * A callback invoked when a settings fragment is submitted
 */
public interface SettingsFragmentResultListener {
    public void onResult(AccountConfig config);
}

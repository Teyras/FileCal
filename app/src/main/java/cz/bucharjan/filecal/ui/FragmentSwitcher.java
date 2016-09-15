package cz.bucharjan.filecal.ui;

import android.app.Fragment;

/**
 * A callback that replaces content of an activity with a given fragment
 */
public interface FragmentSwitcher {
    public void switchFragment(Fragment fragment);
}

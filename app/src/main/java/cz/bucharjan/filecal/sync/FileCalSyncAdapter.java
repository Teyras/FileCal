package cz.bucharjan.filecal.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import cz.bucharjan.filecal.config.AccountStorage;

/**
 * Provides access to synchronization functionality for the Android account system
 */
public class FileCalSyncAdapter extends AbstractThreadedSyncAdapter {
    private AccountManager accountManager;
    private AccountStorage accountStorage;

    public FileCalSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        accountManager = AccountManager.get(context);
        accountStorage = new AccountStorage(accountManager);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

    }
}

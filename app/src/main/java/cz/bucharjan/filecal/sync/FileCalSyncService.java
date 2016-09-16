package cz.bucharjan.filecal.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Provides access to {@link FileCalSyncAdapter} for the operating system
 */
public class FileCalSyncService extends Service {
    private static final Object syncAdapterLock = new Object();
    private static FileCalSyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new FileCalSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}

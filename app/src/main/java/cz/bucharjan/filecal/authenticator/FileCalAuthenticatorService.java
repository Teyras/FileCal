package cz.bucharjan.filecal.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FileCalAuthenticatorService extends Service {
    public IBinder onBind(Intent intent) {
        FileCalAuthenticator authenticator = new FileCalAuthenticator(this);
        return authenticator.getIBinder();
    }
}

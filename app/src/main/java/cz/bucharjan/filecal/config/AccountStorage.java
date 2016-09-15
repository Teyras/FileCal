package cz.bucharjan.filecal.config;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Saves and loads configuration objects using {@link AccountManager}
 */
public class AccountStorage {
    private static final String KEY = "config";
    private AccountManager manager;

    public AccountStorage(AccountManager manager) {
        this.manager = manager;
    }

    public void save(Account account, AccountConfig config) {
        String stringValue = "";

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(config);
            stringValue = new String(Base64.encode(bos.toByteArray(), Base64.DEFAULT));
        } catch (IOException e) {

        }

        manager.setUserData(account, KEY, stringValue);
    }

    public AccountConfig load(Account account) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(manager.getUserData(account, KEY), Base64.DEFAULT));
             ObjectInput in = new ObjectInputStream(bis)) {
            return (AccountConfig) in.readObject();
        } catch (ClassNotFoundException e) {

        } catch (IOException e) {

        }

        return null;
    }
}

package cz.bucharjan.filecal;

public class FileCalException extends Exception {
    public FileCalException(String msg) {
        super(msg);
    }

    public FileCalException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

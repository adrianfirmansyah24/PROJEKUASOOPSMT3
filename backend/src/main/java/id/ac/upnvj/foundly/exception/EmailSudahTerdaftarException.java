package id.ac.upnvj.foundly.exception;

public class EmailSudahTerdaftarException extends RuntimeException {
    public EmailSudahTerdaftarException() {
        super("Email sudah terdaftar.");
    }
}

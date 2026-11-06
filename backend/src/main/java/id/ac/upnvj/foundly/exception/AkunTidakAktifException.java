package id.ac.upnvj.foundly.exception;

public class AkunTidakAktifException extends RuntimeException {
    public AkunTidakAktifException() {
        super("Akun tidak aktif atau tidak ditemukan.");
    }
}

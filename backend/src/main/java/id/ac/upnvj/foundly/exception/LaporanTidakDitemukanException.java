package id.ac.upnvj.foundly.exception;
public class LaporanTidakDitemukanException extends RuntimeException {
    public LaporanTidakDitemukanException(Long id) {
        super("Laporan dengan ID " + id + " tidak ditemukan.");
    }
}

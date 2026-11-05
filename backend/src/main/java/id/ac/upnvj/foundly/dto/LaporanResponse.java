package id.ac.upnvj.foundly.dto;

import id.ac.upnvj.foundly.model.*;
import java.time.LocalDateTime;

/** Hanya data publik. Ciri privat tidak pernah masuk response ini. */
public record LaporanResponse(Long id, JenisLaporan jenis, StatusLaporan status,
        KategoriBarang kategori, String namaBarang, String deskripsiPublik,
        String kampus, String area, String detailLokasi, LocalDateTime waktuKejadian,
        String panduanBuktiKlaim) {
    public static LaporanResponse dari(Laporan laporan) {
        Barang barang = laporan.getBarang();
        return new LaporanResponse(laporan.getId(), laporan.getJenis(), laporan.getStatus(),
                barang.getKategori(), barang.getNama(), barang.getDeskripsiPublik(),
                laporan.getKampus(), laporan.getArea(), laporan.getDetailLokasi(),
                laporan.getWaktuKejadian(), barang.getPanduanBuktiKlaim());
    }
}

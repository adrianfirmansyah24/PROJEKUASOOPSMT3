package id.ac.upnvj.foundly.service;

import id.ac.upnvj.foundly.dto.*;
import id.ac.upnvj.foundly.exception.LaporanTidakDitemukanException;
import id.ac.upnvj.foundly.exception.AkunTidakAktifException;
import id.ac.upnvj.foundly.model.*;
import id.ac.upnvj.foundly.repository.LaporanRepository;
import id.ac.upnvj.foundly.repository.PenggunaRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaporanService {
    private final LaporanRepository repository;
    private final PenggunaRepository penggunaRepository;
    public LaporanService(LaporanRepository repository, PenggunaRepository penggunaRepository) {
        this.repository = repository;
        this.penggunaRepository = penggunaRepository;
    }

    @Transactional
    public LaporanResponse buat(BuatLaporanRequest request, Long penggunaId) {
        Pengguna pelapor = penggunaRepository.findByIdAndAktifTrue(penggunaId)
                .orElseThrow(AkunTidakAktifException::new);
        Barang barang = switch (request.kategori()) {
            case ELEKTRONIK -> new BarangElektronik(request.namaBarang(), request.deskripsiPublik(), request.ciriPrivat());
            case KARTU_IDENTITAS -> new KartuIdentitas(request.namaBarang(), request.deskripsiPublik(), request.ciriPrivat());
            case UMUM -> new BarangUmum(request.namaBarang(), request.deskripsiPublik(), request.ciriPrivat());
        };
        Laporan laporan = new Laporan(pelapor, request.jenis(), barang, request.kampus(), request.area(),
                request.detailLokasi(), request.waktuKejadian());
        return LaporanResponse.dari(repository.save(laporan));
    }
    public LaporanResponse detail(Long id) {
        return LaporanResponse.dari(repository.findById(id)
                .orElseThrow(() -> new LaporanTidakDitemukanException(id)));
    }
    public DaftarLaporanResponse cari(JenisLaporan jenis, String kataKunci, int page, int size) {
        Page<Laporan> hasil = repository.cari(jenis, kataKunci.trim(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return new DaftarLaporanResponse(hasil.map(LaporanResponse::dari).getContent(),
                page, size, hasil.getTotalElements());
    }
}

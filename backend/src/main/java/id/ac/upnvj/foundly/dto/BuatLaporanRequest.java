package id.ac.upnvj.foundly.dto;

import id.ac.upnvj.foundly.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record BuatLaporanRequest(
        @NotNull JenisLaporan jenis,
        @NotNull KategoriBarang kategori,
        @NotBlank @Size(max = 120) String namaBarang,
        @NotBlank @Size(max = 2000) String deskripsiPublik,
        @NotBlank @Size(max = 2000) String ciriPrivat,
        @NotBlank @Size(max = 120) String kampus,
        @NotBlank @Size(max = 120) String area,
        @NotBlank @Size(max = 500) String detailLokasi,
        @NotNull @PastOrPresent LocalDateTime waktuKejadian) {
}

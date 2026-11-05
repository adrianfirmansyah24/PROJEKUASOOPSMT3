package id.ac.upnvj.foundly.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "laporan")
public class Laporan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private JenisLaporan jenis;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private StatusLaporan status;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "barang_id", nullable = false, unique = true)
    private Barang barang;
    @Column(nullable = false, length = 120)
    private String kampus;
    @Column(nullable = false, length = 120)
    private String area;
    @Column(nullable = false, length = 500)
    private String detailLokasi;
    @Column(nullable = false)
    private LocalDateTime waktuKejadian;

    protected Laporan() { }

    public Laporan(JenisLaporan jenis, Barang barang, String kampus, String area,
                   String detailLokasi, LocalDateTime waktuKejadian) {
        if (jenis == null || barang == null || waktuKejadian == null
                || kampus == null || kampus.isBlank() || area == null || area.isBlank()
                || detailLokasi == null || detailLokasi.isBlank()) {
            throw new IllegalArgumentException("Data laporan wajib lengkap.");
        }
        this.jenis = jenis;
        this.barang = barang;
        this.kampus = kampus.trim();
        this.area = area.trim();
        this.detailLokasi = detailLokasi.trim();
        this.waktuKejadian = waktuKejadian;
        // Status awal ditentukan object, tidak diterima dari pemanggil API.
        this.status = jenis == JenisLaporan.KEHILANGAN
                ? StatusLaporan.AKTIF : StatusLaporan.MENUNGGU_PENITIPAN;
    }
    public Long getId() { return id; }
    public JenisLaporan getJenis() { return jenis; }
    public StatusLaporan getStatus() { return status; }
    public Barang getBarang() { return barang; }
    public String getKampus() { return kampus; }
    public String getArea() { return area; }
    public String getDetailLokasi() { return detailLokasi; }
    public LocalDateTime getWaktuKejadian() { return waktuKejadian; }
}

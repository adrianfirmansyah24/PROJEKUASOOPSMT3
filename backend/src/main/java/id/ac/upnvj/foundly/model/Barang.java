package id.ac.upnvj.foundly.model;

import jakarta.persistence.*;

@Entity
@Table(name = "barang")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kategori", length = 30)
public abstract class Barang {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nama;
    @Column(nullable = false, length = 2000)
    private String deskripsiPublik;
    @Column(nullable = false, length = 2000)
    private String ciriPrivat;

    protected Barang() { } // Diperlukan JPA untuk memuat object dari database.

    protected Barang(String nama, String deskripsiPublik, String ciriPrivat) {
        if (nama == null || nama.isBlank() || deskripsiPublik == null || deskripsiPublik.isBlank()
                || ciriPrivat == null || ciriPrivat.isBlank()) {
            throw new IllegalArgumentException("Data barang dan ciri privat wajib diisi.");
        }
        this.nama = nama.trim();
        this.deskripsiPublik = deskripsiPublik.trim();
        this.ciriPrivat = ciriPrivat.trim();
    }

    public Long getId() { return id; }
    public String getNama() { return nama; }
    public String getDeskripsiPublik() { return deskripsiPublik; }
    public abstract KategoriBarang getKategori();
    public abstract String getPanduanBuktiKlaim();
}

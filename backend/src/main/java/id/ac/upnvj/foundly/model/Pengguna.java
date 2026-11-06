package id.ac.upnvj.foundly.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@Entity
@Table(name = "pengguna")
public class Pengguna {
    private static final ZoneId ZONA_JAKARTA = ZoneId.of("Asia/Jakarta");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nama;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeranPengguna peran;

    @Column(nullable = false)
    private boolean aktif;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Pengguna() { }

    public Pengguna(String nama, String email, String passwordHash) {
        if (nama == null || nama.isBlank() || email == null || email.isBlank()
                || passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Nama, email, dan password hash wajib diisi.");
        }
        this.nama = nama.trim();
        this.email = normalisasiEmail(email);
        this.passwordHash = passwordHash;
        this.peran = PeranPengguna.PENGGUNA;
        this.aktif = true;
    }

    public static String normalisasiEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @PrePersist
    void sebelumSimpan() {
        LocalDateTime sekarang = LocalDateTime.now(ZONA_JAKARTA);
        createdAt = sekarang;
        updatedAt = sekarang;
    }

    @PreUpdate
    void sebelumPerbarui() {
        updatedAt = LocalDateTime.now(ZONA_JAKARTA);
    }

    public Long getId() { return id; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public PeranPengguna getPeran() { return peran; }
    public boolean isAktif() { return aktif; }
}

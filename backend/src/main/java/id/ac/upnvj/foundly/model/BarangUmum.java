package id.ac.upnvj.foundly.model;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("UMUM")
public class BarangUmum extends Barang {
    protected BarangUmum() { }
    public BarangUmum(String nama, String deskripsiPublik, String ciriPrivat) {
        super(nama, deskripsiPublik, ciriPrivat);
    }
    @Override public KategoriBarang getKategori() { return KategoriBarang.UMUM; }
    @Override public String getPanduanBuktiKlaim() {
        return "Jelaskan ciri tersembunyi, isi barang, atau siapkan foto kepemilikan sebelumnya.";
    }
}

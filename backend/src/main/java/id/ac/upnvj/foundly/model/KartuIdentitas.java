package id.ac.upnvj.foundly.model;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("KARTU_IDENTITAS")
public class KartuIdentitas extends Barang {
    protected KartuIdentitas() { }
    public KartuIdentitas(String nama, String deskripsiPublik, String ciriPrivat) {
        super(nama, deskripsiPublik, ciriPrivat);
    }
    @Override public KategoriBarang getKategori() { return KategoriBarang.KARTU_IDENTITAS; }
    @Override public String getPanduanBuktiKlaim() {
        return "Siapkan identitas pendukung untuk diperiksa petugas; jangan unggah nomor identitas ke deskripsi publik.";
    }
}

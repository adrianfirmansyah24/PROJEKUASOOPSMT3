package id.ac.upnvj.foundly.model;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ELEKTRONIK")
public class BarangElektronik extends Barang {
    protected BarangElektronik() { }
    public BarangElektronik(String nama, String deskripsiPublik, String ciriPrivat) {
        super(nama, deskripsiPublik, ciriPrivat);
    }
    @Override public KategoriBarang getKategori() { return KategoriBarang.ELEKTRONIK; }
    @Override public String getPanduanBuktiKlaim() {
        return "Siapkan nomor seri, bukti pembelian, atau ciri khusus perangkat.";
    }
}

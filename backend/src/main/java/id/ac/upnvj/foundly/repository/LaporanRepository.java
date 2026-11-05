package id.ac.upnvj.foundly.repository;

import id.ac.upnvj.foundly.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LaporanRepository extends JpaRepository<Laporan, Long> {
    @Query("""
        select l from Laporan l join l.barang b
        where (:jenis is null or l.jenis = :jenis)
          and (:kataKunci = '' or lower(b.nama) like lower(concat('%', :kataKunci, '%'))
               or lower(b.deskripsiPublik) like lower(concat('%', :kataKunci, '%')))
        """)
    Page<Laporan> cari(@Param("jenis") JenisLaporan jenis,
                       @Param("kataKunci") String kataKunci, Pageable pageable);
}

package id.ac.upnvj.foundly.repository;

import id.ac.upnvj.foundly.model.Pengguna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PenggunaRepository extends JpaRepository<Pengguna, Long> {
    Optional<Pengguna> findByEmail(String email);
    Optional<Pengguna> findByIdAndAktifTrue(Long id);
    boolean existsByEmail(String email);
}

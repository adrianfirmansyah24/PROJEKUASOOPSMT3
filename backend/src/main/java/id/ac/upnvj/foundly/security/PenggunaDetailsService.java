package id.ac.upnvj.foundly.security;

import id.ac.upnvj.foundly.model.Pengguna;
import id.ac.upnvj.foundly.repository.PenggunaRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class PenggunaDetailsService implements UserDetailsService {
    private final PenggunaRepository repository;

    public PenggunaDetailsService(PenggunaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailNormal = Pengguna.normalisasiEmail(email);
        return repository.findByEmail(emailNormal)
                .map(FoundlyUserPrincipal::dari)
                .orElseThrow(() -> new UsernameNotFoundException("Email atau password salah."));
    }
}

package id.ac.upnvj.foundly.security;

import id.ac.upnvj.foundly.model.Pengguna;
import id.ac.upnvj.foundly.model.PeranPengguna;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public record FoundlyUserPrincipal(Long id, String nama, String email, String passwordHash,
                                   PeranPengguna peran, boolean aktif) implements UserDetails {
    public static FoundlyUserPrincipal dari(Pengguna pengguna) {
        return new FoundlyUserPrincipal(pengguna.getId(), pengguna.getNama(), pengguna.getEmail(),
                pengguna.getPasswordHash(), pengguna.getPeran(), pengguna.isAktif());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + peran.name()));
    }

    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return aktif; }
}

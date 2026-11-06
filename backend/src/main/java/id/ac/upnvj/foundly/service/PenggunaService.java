package id.ac.upnvj.foundly.service;

import id.ac.upnvj.foundly.dto.*;
import id.ac.upnvj.foundly.exception.EmailSudahTerdaftarException;
import id.ac.upnvj.foundly.model.Pengguna;
import id.ac.upnvj.foundly.repository.PenggunaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PenggunaService {
    private final PenggunaRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PenggunaService(PenggunaRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PenggunaResponse registrasi(RegistrasiRequest request) {
        String email = Pengguna.normalisasiEmail(request.email());
        if (repository.existsByEmail(email)) {
            throw new EmailSudahTerdaftarException();
        }
        Pengguna pengguna = new Pengguna(request.nama(), email, passwordEncoder.encode(request.password()));
        try {
            return PenggunaResponse.dari(repository.saveAndFlush(pengguna));
        } catch (DataIntegrityViolationException exception) {
            throw new EmailSudahTerdaftarException();
        }
    }
}

package id.ac.upnvj.foundly.dto;

import id.ac.upnvj.foundly.model.Pengguna;
import id.ac.upnvj.foundly.model.PeranPengguna;

public record PenggunaResponse(Long id, String nama, String email, PeranPengguna peran) {
    public static PenggunaResponse dari(Pengguna pengguna) {
        return new PenggunaResponse(pengguna.getId(), pengguna.getNama(),
                pengguna.getEmail(), pengguna.getPeran());
    }
}

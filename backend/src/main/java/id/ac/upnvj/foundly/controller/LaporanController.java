package id.ac.upnvj.foundly.controller;

import id.ac.upnvj.foundly.dto.*;
import id.ac.upnvj.foundly.model.JenisLaporan;
import id.ac.upnvj.foundly.service.LaporanService;
import id.ac.upnvj.foundly.security.FoundlyUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.net.URI;

@RestController
@RequestMapping("/api/laporan")
public class LaporanController {
    private final LaporanService service;
    public LaporanController(LaporanService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<LaporanResponse> buat(@Valid @RequestBody BuatLaporanRequest request,
                                                @AuthenticationPrincipal FoundlyUserPrincipal principal) {
        LaporanResponse response = service.buat(request, principal.id());
        return ResponseEntity.created(URI.create("/api/laporan/" + response.id())).body(response);
    }
    @GetMapping("/{id}")
    public LaporanResponse detail(@PathVariable @Positive Long id) { return service.detail(id); }

    @GetMapping
    public DaftarLaporanResponse cari(@RequestParam(required = false) JenisLaporan jenis,
            @RequestParam(defaultValue = "") @Size(max = 120) String kataKunci,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.cari(jenis, kataKunci, page, size);
    }
}

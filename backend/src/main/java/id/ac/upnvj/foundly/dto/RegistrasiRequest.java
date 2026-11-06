package id.ac.upnvj.foundly.dto;

import jakarta.validation.constraints.*;

public record RegistrasiRequest(
        @NotBlank @Size(max = 120) String nama,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 64) String password) {
}

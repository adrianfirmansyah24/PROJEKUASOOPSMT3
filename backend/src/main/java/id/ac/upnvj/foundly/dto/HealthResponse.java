package id.ac.upnvj.foundly.dto;

/** DTO menentukan data yang dikirim ke pemanggil API. */
public record HealthResponse(String application, String status, String message) {
}

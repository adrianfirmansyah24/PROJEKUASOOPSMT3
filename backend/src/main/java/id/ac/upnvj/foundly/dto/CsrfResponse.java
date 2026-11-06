package id.ac.upnvj.foundly.dto;

public record CsrfResponse(String headerName, String parameterName, String token) {
}

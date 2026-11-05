package id.ac.upnvj.foundly.dto;
import java.util.List;
public record DaftarLaporanResponse(List<LaporanResponse> data, int page, int size, long totalElements) { }

package id.ac.upnvj.foundly;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import id.ac.upnvj.foundly.model.KategoriBarang;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FoundlyApplicationTests {

    @Container
    static final MySQLContainer mysql = new MySQLContainer("mysql:8.4.13")
            .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void databasePengujian(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbc;

    private final ObjectMapper json = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void healthEndpointMengembalikanStatusBackend() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/api/health"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                    contentType -> assertThat(contentType).startsWith("application/json"));
            assertThat(response.body()).contains(
                    "\"application\":\"Foundly\"",
                    "\"status\":\"UP\"",
                    "\"message\":\"Backend Foundly berjalan.\"");
        }
    }

    @ParameterizedTest
    @EnumSource(KategoriBarang.class)
    void laporanTersimpanDanDibacaDenganPanduanSesuaiSubclass(KategoriBarang kategori) throws Exception {
        String nama = "Uji " + kategori;
        HttpResponse<String> dibuat = request("POST", "/api/laporan", payload("PENEMUAN", kategori.name(), nama));
        assertThat(dibuat.statusCode()).isEqualTo(201);
        JsonNode data = json.readTree(dibuat.body());
        long id = data.get("id").asLong();
        assertThat(dibuat.headers().firstValue("Location")).contains("/api/laporan/" + id);
        assertThat(data.get("status").asText()).isEqualTo("MENUNGGU_PENITIPAN");

        // Request baru memuat ulang entity dari MySQL, termasuk subclass hasil discriminator.
        HttpResponse<String> detail = request("GET", "/api/laporan/" + id, null);
        assertThat(detail.statusCode()).isEqualTo(200);
        JsonNode tersimpan = json.readTree(detail.body());
        assertThat(tersimpan.get("namaBarang").asText()).isEqualTo(nama);
        assertThat(tersimpan.get("kategori").asText()).isEqualTo(kategori.name());
        String panduan = tersimpan.get("panduanBuktiKlaim").asText();
        switch (kategori) {
            case ELEKTRONIK -> assertThat(panduan).contains("nomor seri");
            case KARTU_IDENTITAS -> assertThat(panduan).contains("identitas pendukung");
            case UMUM -> assertThat(panduan).contains("ciri tersembunyi");
        }
        assertThat(detail.body()).doesNotContain("ciriPrivat", "RAHASIA-UJI");
        assertThat(dibuat.body()).doesNotContain("ciriPrivat", "RAHASIA-UJI");
        assertThat(jdbc.queryForObject("select b.ciri_privat from barang b join laporan l on l.barang_id=b.id where l.id=?",
                String.class, id)).isEqualTo("RAHASIA-UJI");
    }

    @Test
    void pencarianMemfilterJenisDanNamaTanpaMencariBuktiPrivat() throws Exception {
        String nama = "PencarianUnik";
        request("POST", "/api/laporan", payload("KEHILANGAN", "UMUM", nama));
        request("POST", "/api/laporan", payload("PENEMUAN", "UMUM", nama));
        HttpResponse<String> hasil = request("GET", "/api/laporan?jenis=KEHILANGAN&kataKunci=pencarianunik&size=1", null);
        assertThat(hasil.statusCode()).isEqualTo(200);
        JsonNode data = json.readTree(hasil.body());
        assertThat(data.get("totalElements").asLong()).isEqualTo(1);
        assertThat(data.get("data").size()).isEqualTo(1);
        assertThat(data.get("data").get(0).get("status").asText()).isEqualTo("AKTIF");
        assertThat(hasil.body()).doesNotContain("ciriPrivat", "RAHASIA-UJI");
        JsonNode privat = json.readTree(request("GET", "/api/laporan?kataKunci=RAHASIA-UJI", null).body());
        assertThat(privat.get("totalElements").asLong()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{invalid", "[]"})
    void inputRusakDitolakTanpaMenulisData(String payload) throws Exception {
        Long sebelum = jdbc.queryForObject("select count(*) from laporan", Long.class);
        assertThat(request("POST", "/api/laporan", payload).statusCode()).isEqualTo(400);
        assertThat(jdbc.queryForObject("select count(*) from laporan", Long.class)).isEqualTo(sebelum);
    }

    @Test
    void dataTidakValidDitolakTanpaMeninggalkanBarang() throws Exception {
        Long sebelum = jdbc.queryForObject("select count(*) from barang", Long.class);
        String valid = payload("PENEMUAN", "UMUM", "Dompet");
        for (String tidakValid : new String[] {
                valid.replace("Dompet", " "),
                valid.replace("PENEMUAN", "SALAH"),
                valid.replace("2026-01-01T10:00:00", "2099-01-01T10:00:00"),
                valid.replace("RAHASIA-UJI", "x".repeat(2001))}) {
            assertThat(request("POST", "/api/laporan", tidakValid).statusCode()).isEqualTo(400);
        }
        assertThat(jdbc.queryForObject("select count(*) from barang", Long.class)).isEqualTo(sebelum);
    }

    @ParameterizedTest
    @ValueSource(strings = {"?page=-1", "?size=0", "?size=101", "?jenis=SALAH", "/abc", "/0"})
    void parameterTidakValidMenghasilkan400(String parameter) throws Exception {
        assertThat(request("GET", "/api/laporan" + parameter, null).statusCode()).isEqualTo(400);
    }

    @Test
    void laporanTidakDitemukanMenghasilkan404() throws Exception {
        HttpResponse<String> hasil = request("GET", "/api/laporan/9223372036854775807", null);
        assertThat(hasil.statusCode()).isEqualTo(404);
        assertThat(hasil.body()).contains("tidak ditemukan").doesNotContain("stackTrace", "password");
    }

    private String payload(String jenis, String kategori, String nama) {
        return """
                {"jenis":"%s","kategori":"%s","namaBarang":"%s",
                 "deskripsiPublik":"Barang berwarna hitam","ciriPrivat":"RAHASIA-UJI",
                 "kampus":"Pondok Labu","area":"Kantin","detailLokasi":"Dekat meja depan",
                 "waktuKejadian":"2026-01-01T10:00:00"}
                """.formatted(jenis, kategori, nama);
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

}

package id.ac.upnvj.foundly;

import id.ac.upnvj.foundly.model.KategoriBarang;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.*;

import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.UUID;

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

    @LocalServerPort
    private int port;

    private final ObjectMapper json = new ObjectMapper();
    private HttpClient client;
    private String csrfToken;

    @BeforeEach
    void buatClientBaru() {
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder().cookieHandler(cookieManager).build();
        csrfToken = null;
    }

    @Test
    void endpointPublikDapatDibacaTanpaLogin() throws Exception {
        assertThat(request("GET", "/api/health", null, false).statusCode()).isEqualTo(200);
        assertThat(request("GET", "/api/laporan", null, false).statusCode()).isEqualTo(200);
    }

    @Test
    void registrasiMenyimpanPasswordHashDanRolePengguna() throws Exception {
        String email = emailUnik();
        HttpResponse<String> response = registrasi("Andi Foundly", email, "rahasia123");

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.headers().firstValue("Location")).isEmpty();
        assertThat(response.body()).contains("\"peran\":\"PENGGUNA\"")
                .doesNotContain("password", "passwordHash", "rahasia123");

        String hash = jdbc.queryForObject("select password_hash from pengguna where email=?",
                String.class, email);
        assertThat(hash).startsWith("$2").doesNotContain("rahasia123");
        assertThat(jdbc.queryForObject("select aktif from pengguna where email=?", Boolean.class, email)).isTrue();
    }

    @Test
    void emailDinormalisasiDanTidakBolehDuplikat() throws Exception {
        String email = emailUnik();
        assertThat(registrasi("Pengguna Satu", email.toUpperCase(), "rahasia123").statusCode()).isEqualTo(201);
        HttpResponse<String> duplikat = registrasi("Pengguna Dua", email.toUpperCase(), "rahasia456");

        assertThat(duplikat.statusCode()).isEqualTo(409);
        assertThat(jdbc.queryForObject("select count(*) from pengguna where email=?", Long.class, email)).isEqualTo(1L);
    }

    @Test
    void csrfWajibUntukRequestYangMengubahData() throws Exception {
        long sebelum = jdbc.queryForObject("select count(*) from pengguna", Long.class);
        HttpResponse<String> response = request("POST", "/api/auth/register",
                payloadRegistrasi("Tanpa CSRF", emailUnik(), "rahasia123"), false);

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).contains("CSRF");
        assertThat(jdbc.queryForObject("select count(*) from pengguna", Long.class)).isEqualTo(sebelum);
    }

    @Test
    void loginMenyimpanSessionDanLogoutMemutuskannya() throws Exception {
        String email = emailUnik();
        registrasi("Pengguna Session", email, "rahasia123");

        HttpResponse<String> login = login(email, "rahasia123");
        assertThat(login.statusCode()).isEqualTo(200);
        assertThat(login.body()).contains(email).doesNotContain("password", "rahasia123");
        assertThat(request("GET", "/api/auth/me", null, false).statusCode()).isEqualTo(200);

        assertThat(request("POST", "/api/auth/logout", null, true).statusCode()).isEqualTo(204);
        csrfToken = null;
        assertThat(request("GET", "/api/auth/me", null, false).statusCode()).isEqualTo(401);
    }

    @Test
    void passwordSalahMenghasilkan401TanpaMembuatSessionLogin() throws Exception {
        String email = emailUnik();
        registrasi("Pengguna Salah Password", email, "rahasia123");

        HttpResponse<String> response = login(email, "password-salah");
        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("Email atau password salah")
                .doesNotContain(email, "UsernameNotFoundException", "BadCredentialsException");
        assertThat(request("GET", "/api/auth/me", null, false).statusCode()).isEqualTo(401);
    }

    @Test
    void membuatLaporanWajibLogin() throws Exception {
        HttpResponse<String> response = request("POST", "/api/laporan",
                payloadLaporan("PENEMUAN", "UMUM", "Barang tanpa login"), true);
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @ParameterizedTest
    @EnumSource(KategoriBarang.class)
    void laporanTersimpanDenganPelaporDanPolymorphism(KategoriBarang kategori) throws Exception {
        Long penggunaId = registrasiDanLogin();
        String nama = "Uji " + kategori;
        HttpResponse<String> dibuat = request("POST", "/api/laporan",
                payloadLaporan("PENEMUAN", kategori.name(), nama), true);

        assertThat(dibuat.statusCode()).isEqualTo(201);
        JsonNode data = json.readTree(dibuat.body());
        long id = data.get("id").asLong();
        assertThat(data.get("status").asText()).isEqualTo("MENUNGGU_PENITIPAN");
        assertThat(jdbc.queryForObject("select pelapor_id from laporan where id=?", Long.class, id))
                .isEqualTo(penggunaId);

        HttpResponse<String> detail = request("GET", "/api/laporan/" + id, null, false);
        assertThat(detail.statusCode()).isEqualTo(200);
        JsonNode tersimpan = json.readTree(detail.body());
        assertThat(tersimpan.get("kategori").asText()).isEqualTo(kategori.name());
        String panduan = tersimpan.get("panduanBuktiKlaim").asText();
        switch (kategori) {
            case ELEKTRONIK -> assertThat(panduan).contains("nomor seri");
            case KARTU_IDENTITAS -> assertThat(panduan).contains("identitas pendukung");
            case UMUM -> assertThat(panduan).contains("ciri tersembunyi");
        }
        assertThat(detail.body()).doesNotContain("ciriPrivat", "RAHASIA-UJI", "pelapor");
        assertThat(jdbc.queryForObject("select b.ciri_privat from barang b join laporan l on l.barang_id=b.id where l.id=?",
                String.class, id)).isEqualTo("RAHASIA-UJI");
    }

    @Test
    void pencarianMemfilterJenisDanTidakMencariBuktiPrivat() throws Exception {
        registrasiDanLogin();
        String nama = "PencarianUnik";
        request("POST", "/api/laporan", payloadLaporan("KEHILANGAN", "UMUM", nama), true);
        request("POST", "/api/laporan", payloadLaporan("PENEMUAN", "UMUM", nama), true);

        JsonNode hasil = json.readTree(request("GET",
                "/api/laporan?jenis=KEHILANGAN&kataKunci=pencarianunik&size=1", null, false).body());
        assertThat(hasil.get("totalElements").asLong()).isEqualTo(1);
        assertThat(hasil.get("data").get(0).get("status").asText()).isEqualTo("AKTIF");
        assertThat(hasil.toString()).doesNotContain("ciriPrivat", "RAHASIA-UJI", "pelapor");
        JsonNode privat = json.readTree(request("GET", "/api/laporan?kataKunci=RAHASIA-UJI", null, false).body());
        assertThat(privat.get("totalElements").asLong()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{invalid", "[]"})
    void inputLaporanRusakDitolakTanpaMenulisData(String payload) throws Exception {
        registrasiDanLogin();
        Long sebelum = jdbc.queryForObject("select count(*) from laporan", Long.class);
        assertThat(request("POST", "/api/laporan", payload, true).statusCode()).isEqualTo(400);
        assertThat(jdbc.queryForObject("select count(*) from laporan", Long.class)).isEqualTo(sebelum);
    }

    @Test
    void dataLaporanTidakValidTidakMeninggalkanBarang() throws Exception {
        registrasiDanLogin();
        Long sebelum = jdbc.queryForObject("select count(*) from barang", Long.class);
        String valid = payloadLaporan("PENEMUAN", "UMUM", "Dompet");
        for (String tidakValid : new String[] {
                valid.replace("Dompet", " "),
                valid.replace("PENEMUAN", "SALAH"),
                valid.replace("2026-01-01T10:00:00", "2099-01-01T10:00:00"),
                valid.replace("RAHASIA-UJI", "x".repeat(2001))}) {
            assertThat(request("POST", "/api/laporan", tidakValid, true).statusCode()).isEqualTo(400);
        }
        assertThat(jdbc.queryForObject("select count(*) from barang", Long.class)).isEqualTo(sebelum);
    }

    @ParameterizedTest
    @ValueSource(strings = {"?page=-1", "?size=0", "?size=101", "?jenis=SALAH", "/abc", "/0"})
    void parameterLaporanTidakValidMenghasilkan400(String parameter) throws Exception {
        assertThat(request("GET", "/api/laporan" + parameter, null, false).statusCode()).isEqualTo(400);
    }

    @Test
    void laporanTidakDitemukanMenghasilkan404() throws Exception {
        HttpResponse<String> hasil = request("GET", "/api/laporan/9223372036854775807", null, false);
        assertThat(hasil.statusCode()).isEqualTo(404);
        assertThat(hasil.body()).contains("tidak ditemukan").doesNotContain("stackTrace", "password");
    }

    private Long registrasiDanLogin() throws Exception {
        String email = emailUnik();
        JsonNode pengguna = json.readTree(registrasi("Pengguna Laporan", email, "rahasia123").body());
        assertThat(login(email, "rahasia123").statusCode()).isEqualTo(200);
        return pengguna.get("id").asLong();
    }

    private HttpResponse<String> registrasi(String nama, String email, String password) throws Exception {
        return request("POST", "/api/auth/register", payloadRegistrasi(nama, email, password), true);
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        String payload = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
        return request("POST", "/api/auth/login", payload, true);
    }

    private String emailUnik() {
        return "uji-" + UUID.randomUUID() + "@foundly.local";
    }

    private String payloadRegistrasi(String nama, String email, String password) {
        return "{\"nama\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}"
                .formatted(nama, email, password);
    }

    private String payloadLaporan(String jenis, String kategori, String nama) {
        return """
                {"jenis":"%s","kategori":"%s","namaBarang":"%s",
                 "deskripsiPublik":"Barang berwarna hitam","ciriPrivat":"RAHASIA-UJI",
                 "kampus":"Pondok Labu","area":"Kantin","detailLokasi":"Dekat meja depan",
                 "waktuKejadian":"2026-01-01T10:00:00"}
                """.formatted(jenis, kategori, nama);
    }

    private String tokenCsrf() throws Exception {
        if (csrfToken == null) {
            HttpResponse<String> response = request("GET", "/api/auth/csrf", null, false);
            assertThat(response.statusCode()).isEqualTo(200);
            csrfToken = json.readTree(response.body()).get("token").asText();
        }
        return csrfToken;
    }

    private HttpResponse<String> request(String method, String path, String body, boolean denganCsrf) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json");
        if (denganCsrf) {
            builder.header("X-CSRF-TOKEN", tokenCsrf());
        }
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}

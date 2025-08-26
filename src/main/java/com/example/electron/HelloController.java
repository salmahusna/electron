package com.example.electron;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class HelloController {

    @FXML
    private TextField idField;

    @FXML
    private TextArea outputArea;

    @FXML
    private TextField namaField;

    @FXML
    private TextField ktpField;

    @FXML
    private TextField alamatField;

    @FXML
    private ChoiceBox<String> jkChoice;

    @FXML
    private DatePicker tglLahirPicker;

    private String id_pasien;

    private final String baseUrl = "http://127.0.0.1:8000/api.php"; // endpoint API

    /**
     * Tombol Insert
     */
    @FXML
    private void handleInsert() throws URISyntaxException, IOException, InterruptedException {
        String nama = namaField.getText();
        String ktp = ktpField.getText();
        String alamat = alamatField.getText();
        String jenisKelamin = jkChoice.getValue();
        String tglLahir = (tglLahirPicker.getValue() != null) ? tglLahirPicker.getValue().toString() : "";

        insertPasien(nama, ktp, alamat, jenisKelamin, tglLahir);

        // kalau mau, baru clear form setelah berhasil
        // clearForm();
    }


    /**
     * Tombol Update
     */
    @FXML
    private void handleUpdate() throws URISyntaxException, IOException, InterruptedException {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            outputArea.setText("⚠️ ID Pasien tidak boleh kosong untuk update!");
            return;
        }

        String nama = namaField.getText();
        String ktp = ktpField.getText();
        String alamat = alamatField.getText();
        String jenisKelamin = jkChoice.getValue();
        String tglLahir = (tglLahirPicker.getValue() != null) ? tglLahirPicker.getValue().toString() : "";

        // kirim request update ke API
        updatePasien(id, nama, ktp, alamat, jenisKelamin, tglLahir);

        // reload data dari server agar form dan outputArea terisi ulang
        loadPasienById(id);
    }


    /**
     * Tombol Batal / Clear
     */
    @FXML
    private void handleBatal() {
        clearForm();
    }

    private void clearForm() {
        idField.clear();
        namaField.clear();
        ktpField.clear();
        alamatField.clear();
        jkChoice.setValue(null);
        tglLahirPicker.setValue(null);
        outputArea.clear();
    }

    /**
     * GET pasien by id
     */
    public String getPasien(String code) {
        final String tokenUrl = baseUrl + "?id_pasien=" + code;
        String result = "";

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(tokenUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            result = response.body();

            // Parse JSON agar tidak tampil mentah
            Map<String, String> data = parseJson(result);

            StringBuilder sb = new StringBuilder();
            sb.append("=== Data Pasien Dari Database ===\n");
            sb.append("Response Code : ").append(response.statusCode()).append("\n");
//            sb.append("ID Pasien     : ").append(data.get("id_pasien")).append("\n");
            sb.append("Nama          : ").append(data.get("nama_pasien")).append("\n");
            sb.append("No. KTP       : ").append(data.get("no_ktp")).append("\n");
            sb.append("Alamat        : ").append(data.get("alamat")).append("\n");
            sb.append("Jenis Kelamin : ").append(data.get("jenis_kelamin")).append("\n");
            sb.append("Tanggal Lahir : ").append(data.get("tgl_lahir")).append("\n");

            outputArea.setText(sb.toString());

            System.out.println(sb);

        } catch (URISyntaxException e) {
            result = "URL API tidak valid: " + e.getMessage();
            outputArea.setText(result);
            System.err.println(result);

        } catch (IOException e) {
            result = "Gagal terhubung ke server API: " + e.getMessage();
            outputArea.setText(result);
            System.err.println(result);

        } catch (InterruptedException e) {
            result = "Proses request terhenti: " + e.getMessage();
            outputArea.setText(result);
            System.err.println(result);
            Thread.currentThread().interrupt();
        }

        return result;
    }


    /**
     * INSERT pasien (POST)
     */
    private void insertPasien(String nama, String ktp, String alamat, String jenisKelamin, String tglLahir)
            throws URISyntaxException, IOException, InterruptedException {

        String jsonInput = String.format(
                "{\"nama_pasien\":\"%s\",\"no_ktp\":\"%s\",\"alamat\":\"%s\",\"jenis_kelamin\":\"%s\",\"tgl_lahir\":\"%s\"}",
                nama, ktp, alamat, jenisKelamin, tglLahir
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "?action=insert"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // tampilkan hasil ke outputArea
        String resultText = "=== Data Pasien Disimpan ===\n"
                + "Nama          : " + nama + "\n"
                + "No. KTP       : " + ktp + "\n"
                + "Alamat        : " + alamat + "\n"
                + "Jenis Kelamin : " + jenisKelamin + "\n"
                + "Tanggal Lahir : " + tglLahir + "\n"
                + "Response Code : " + response.statusCode() + "\n"
                + "Result        : " + response.body();

        outputArea.setText(resultText);
        System.out.println(resultText);
    }


    /**
     * UPDATE pasien (PUT)
     */
    private void updatePasien(String idPasien, String nama, String ktp, String alamat, String jenisKelamin, String tglLahir)
            throws URISyntaxException, IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        String requestBody = String.format(
                "id_pasien=%s&nama_pasien=%s&no_ktp=%s&alamat=%s&jenis_kelamin=%s&tgl_lahir=%s",
                idPasien, nama, ktp, alamat, jenisKelamin, tglLahir
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "?action=update&id=" + idPasien))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String resultText = "=== Data Pasien Diperbarui ===\n"
                + "Response Code : " + response.statusCode() + "\n"
                + "Result : " + response.body();

        outputArea.setText(resultText);
        System.out.println(resultText);
    }


    /**
     * Alias untuk tombol Simpan
     */

    @FXML
    private void handleSimpan() throws URISyntaxException, IOException, InterruptedException {
        String nama = namaField.getText();
        String ktp = ktpField.getText();
        String alamat = alamatField.getText();
        String jenisKelamin = jkChoice.getValue();
        String tglLahir = (tglLahirPicker.getValue() != null) ? tglLahirPicker.getValue().toString() : "";

        // simpan data ke server
        insertPasien(nama, ktp, alamat, jenisKelamin, tglLahir);

        // JANGAN langsung clear form, tapi refresh data dari server
        String id = idField.getText().trim();
        if (!id.isEmpty()) {
            loadPasienById(id);
        }
    }
    private void loadPasienById(String id) {
        String json = getPasien(id); // ambil response JSON

        Map<String, String> data = parseJson(json);

        namaField.setText(data.get("nama_pasien"));
        ktpField.setText(data.get("no_ktp"));
        alamatField.setText(data.get("alamat"));
        jkChoice.setValue(data.get("jenis_kelamin"));
        if (data.get("tgl_lahir") != null && !data.get("tgl_lahir").isEmpty()) {
            tglLahirPicker.setValue(java.time.LocalDate.parse(data.get("tgl_lahir")));
        }
    }



    /**
     * Fungsi bantu parse JSON sederhana
     */
    private Map<String, String> parseJson(String json) {
        String clean = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] pairs = clean.split(",");
        Map<String, String> dataMap = new HashMap<>();
        for (String pair : pairs) {
            String[] entry = pair.split(":", 2);
            if (entry.length == 2) {
                dataMap.put(entry[0].trim(), entry[1].trim());
            }
        }
        return dataMap;
    }
}

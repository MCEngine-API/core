package io.github.mcengine.api.core.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.InetAddress;

import org.json.JSONObject;

public class MCEngineCoreApiLicense {

    private static final String LICENSE_SERVER_URL = "https://example.com/api/verify";

    public static boolean checkLicense(String license) {
        try {
            // Get local IP address
            String serverIp = InetAddress.getLocalHost().getHostAddress();

            // Prepare JSON request
            JSONObject requestBody = new JSONObject();
            requestBody.put("license", license);
            requestBody.put("server_ip", serverIp);

            // Open connection
            URL url = URI.create(LICENSE_SERVER_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            int statusCode = connection.getResponseCode();
            InputStream responseStream = (statusCode == 200)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            int status = jsonResponse.optInt("status", 404);

            return status == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

package services.Reclamation;


import okhttp3.*;

import java.io.IOException;


public class EmailJSService {
    private static final String API_URL = "https://api.emailjs.com/api/v1.0/email/send";

    public static void sendEmail(String status, String to_name, String to_email) {
        OkHttpClient client = new OkHttpClient();

        // ✅ Fixed JSON formatting
        String jsonPayload = "{"
                + "\"service_id\": \"service_i0524yq\","
                + "\"template_id\": \"template_azxmhct\","
                + "\"user_id\": \"EFBTBRVBtpK6Uelob\","
                + "\"accessToken\": \"zkOZbRev8ZVtKUa2H6ZuN\","
                + "\"template_params\": {"
                + "\"status\": \"" + status + "\","
                + "\"to_name\": \"" + to_name + "\","
                + "\"to_email\": \"" + to_email + "\""
                + "}"
                + "}";

        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .header("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("✅ Email sent successfully to " + to_email);
            } else {
                System.err.println("❌ Failed to send email. Response Code: " + response.code());
                System.err.println("🔹 Response Body: " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ An error occurred while sending the email.");
        }
    }


}
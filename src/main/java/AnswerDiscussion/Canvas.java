package AnswerDiscussion;

import java.net.URI;
import java.net.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


public class Canvas {
  String courseId;
  static HttpClient client = HttpClient.newHttpClient();
  static String canvasAPIKey = System.getenv("CANVAS_API_KEY");

  Canvas(String courseId) {
    this.courseId = courseId;
  }

  //makes the uri for seton hall canvas course
  URI makeUri(String uri){
    return URI.create("https://setonhall.instructure.com/api/v1/courses/" + this.courseId + uri);
  }

  //Makes http requests using the given uri, bodypublisher and methodtype
  public HttpRequest makeRequest(String uri, HttpRequest.BodyPublisher body, String methodType) {
    return HttpRequest.newBuilder(makeUri(uri))
      .method(methodType, body)
      .header("Authorization", "Bearer " + canvasAPIKey)
      .build();
  }

  //Makes a post request using the given uri, bodypublisher and methodtype
  public HttpRequest makePostRequest(String uri, HttpRequest.BodyPublisher body, String methodType) {
    return HttpRequest.newBuilder(makeUri(uri))
      .method(methodType, body)
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer " + canvasAPIKey)
      .build();
  }

  //gets all files from this canvas course
  public JSONArray getAllFiles() {
    HttpRequest request = makeRequest("/files?sort=created_at&order=desc",
      HttpRequest.BodyPublishers.noBody(), "GET");

    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenApply(Canvas::getJSONData)
      .join();

  }

  //gets all discussions from this canvas course
  public JSONArray getAllDiscussions() {
    HttpRequest request = makeRequest("/discussion_topics?sort=created_at&order=desc",
      HttpRequest.BodyPublishers.noBody(), "GET");
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenApply(Canvas::getJSONData)
      .join();
  }

  //gets the most recent discussion from this canvas course
  JSONObject getMostRecentDiscussion() {
    JSONArray discussions = getAllDiscussions();
    return discussions.getJSONObject(0);
  }

  //gets the most recent file from this canvas course
  JSONObject getMostRecentFile() {
    JSONArray files = getAllFiles();
    return files.getJSONObject(0);
  }

  //Reads the contents of the given file
  public String readFileContents(JSONObject newestFile) {
    String fileUrl = newestFile.getString("url");
    String fileName = newestFile.getString("filename");
    try {
      downloadPdf(fileUrl, fileName);
      return extractTextFromPdf(fileName);
    } catch (Exception e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }


  //Downloads the given pdf url under the given file name
  public void downloadPdf(String url, String outputFileName) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    HttpRequest request = HttpRequest.newBuilder(URI.create(url))
      .method("GET", HttpRequest.BodyPublishers.noBody())
      .header("Authorization", "Bearer " + canvasAPIKey)
      .build();

    HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

    if (response.statusCode() == 200) {
      try (FileOutputStream fos = new FileOutputStream(outputFileName)) {
        fos.write(response.body());
      }
    } else {
      throw new RuntimeException("Failed to download PDF. HTTP Status: " + response.statusCode());
    }
  }

  //Extract text from a PDF file
  public String extractTextFromPdf(String filePath) throws IOException {
    File file = new File(filePath);
    try (PDDocument document = PDDocument.load(file)) {
      PDFTextStripper pdfStripper = new PDFTextStripper();
      file.delete();
      return pdfStripper.getText(document); // Return extracted text as a string
    }
  }

  //puts the response body into a JSON array
  static JSONArray getJSONData(String responseBody) {
    return new JSONArray(responseBody);
  }

  //makes a discussion post to the given dicussion board using its id
  public String makeDicussionPost(String dicussionId, String response) {
    JSONObject body = new JSONObject().put("message", response);
    HttpRequest request = makePostRequest("/discussion_topics/" + dicussionId + "/entries",
      HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8), "POST");

    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .join();
  }

}

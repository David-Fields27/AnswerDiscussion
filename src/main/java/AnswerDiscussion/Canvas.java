package AnswerDiscussion;

import java.net.URI;
import java.net.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Canvas {
  String courseId;

  static HttpClient client = HttpClient.newHttpClient();
  static String canvasAPIKey = "Yours goes here";

  Canvas(String courseId) {
    this.courseId = courseId;
  }

  URI makeUri(String uri){
    return URI.create("https://setonhall.instructure.com/api/v1/courses/" + this.courseId + uri);
  }

  public HttpRequest makeRequest(String uri, HttpRequest.BodyPublisher body, String methodType) {
    return HttpRequest.newBuilder(makeUri(uri))
      .method(methodType, body)
      .header("Authorization", "Bearer " + canvasAPIKey)
      .build();
  }
  public JSONObject makeDicussionPost(String dicussionId, String reponse) {
    return null;
  }

  public JSONArray getAllDiscussions() {
    HttpRequest request = makeRequest("/discussion_topics?sort=created_at&order=desc",
      HttpRequest.BodyPublishers.noBody(), "GET");
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenApply(Canvas::getJSONData)
      .join();
  }

  JSONObject getMostRecentDiscussion() {
    JSONArray discussions = getAllDiscussions();
    return discussions.getJSONObject(0);
  }

  JSONObject getMostRecentFile() {
    JSONArray files = getAllFiles();
    return files.getJSONObject(0);
  }


  public JSONArray getAllFiles() {
    HttpRequest request = makeRequest("/files?sort=created_at&order=desc",
      HttpRequest.BodyPublishers.noBody(), "GET");

    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenApply(Canvas::getJSONData)
      .join();

  }

  static JSONArray getJSONData(String responseBody) {
    return new JSONArray(responseBody);
  }
}

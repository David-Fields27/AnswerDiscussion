package AnswerDiscussion;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.*;

public class ChatGpt {

  static HttpClient client = HttpClient.newHttpClient();
  static String chatAPIKey = "Yours goes here";
  String prompt;
  JSONObject file;

  ChatGpt(String prompt, JSONObject file) {
    this.prompt = prompt;
    this.file = file;
  }



  HttpRequest.BodyPublisher makeBody() {
    JSONObject body = new JSONObject()
      .put("model", "gpt-4o-mini")
      .put("messages", new JSONArray()
        .put(new JSONObject()
          .put("role", "user")
          .put("content", new JSONArray()
            .put(new JSONObject()
              .put("type", "text")
              .put("text", this.prompt)))));

    return HttpRequest.BodyPublishers.ofString(body.toString());
  }


  public HttpRequest makeRequest() {
    return HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
      .method("POST", makeBody())
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer " + chatAPIKey)
      .build();
  }

  public String askChat() {
    HttpRequest request = makeRequest();

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenAccept(System.out::println)
      .join();

    return "";
  }
}

package AnswerDiscussion;

import org.json.JSONObject;

public class Main {

  public static void main(String[] args) {
  Canvas canvasCourse = new Canvas("31315");

  JSONObject newestFile = canvasCourse.getMostRecentFile();
  JSONObject newestDiscussion = canvasCourse.getMostRecentDiscussion();

  ChatGpt chatGpt = new ChatGpt(newestDiscussion.get("message").toString(), newestFile);
  String response = "";
  System.out.println(newestDiscussion);
  System.out.println(newestDiscussion.get("message"));
  System.out.println(newestDiscussion.get("id"));

  chatGpt.askChat();
  //canvasCourse.makeDicussionPost(newestDiscussion.get("id").toString(), response);



  }
}

package AnswerDiscussion;

import org.json.JSONObject;

public class Main {
  public static void main(String[] args) {
  Canvas canvasCourse = new Canvas("31315");

  JSONObject newestFile = canvasCourse.getMostRecentFile();
  JSONObject newestDiscussion = canvasCourse.getMostRecentDiscussion();

  String prompt2Chat = "Document: " + canvasCourse.readFileContents(newestFile)
    + "\n" + "Prompt: " + newestDiscussion.get("message");
  ChatGpt chatGpt = new ChatGpt(prompt2Chat);
  String chatResponse = chatGpt.askChat();

  canvasCourse.makeDicussionPost(newestDiscussion.get("id").toString(), chatResponse);
  }
}
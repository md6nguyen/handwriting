package agents.recognition.writing;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.reactivex.rxjava3.core.Flowable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class WritingRecognitionAgent {

  private static final String APP_NAME = "LatexAgent";
  private static final String USER_ID = "test_user_456";
  private static final String MODEL_NAME = "gemini-2.5-flash";

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(5678), 0);
    server.createContext("/recognition/writing", new WritingRecognitionHandler());
    server.setExecutor(null);
    server.start();
  }

  private static class WritingRecognitionHandler implements HttpHandler {
    private static enum Mode {
      MATH, CHINESE;
    }

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final Splitter AMP_SPLITTER = Splitter.on("&").trimResults();
    private static final Splitter EQ_SPLITTER = Splitter.on("=").limit(2).trimResults();

    public void handle(HttpExchange t) throws IOException {
      try {
        checkArgument(t.getRequestMethod().equals("POST"), "POST request expected");
        Map<String, String> params = Maps.newHashMap();
        String body = new String(t.getRequestBody().readAllBytes());
        for (String item: AMP_SPLITTER.splitToList(body)) {
          List<String> pair = EQ_SPLITTER.splitToList(item);
          checkArgument(pair.size() == 2, "Invalid parameter %s", item);
          params.put(pair.get(0), pair.get(1));
        }

        WritingRecognitionAgent writingRecognitionAgent = new WritingRecognitionAgent();
        Mode mode = Mode.valueOf(params.get("mode").toUpperCase());
        String response;
        byte[] image = BASE64_DECODER.decode(params.get("img"));
        switch (mode) {
          case MATH -> response = writingRecognitionAgent.runMathAgent(image);
          case CHINESE -> response = writingRecognitionAgent.runChineseAgent(image);
          default ->  throw new IllegalArgumentException(String.format("Unexpected mode %s", mode.name()));
        }
        byte[] bytes = response.getBytes();
        t.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
          os.write(bytes);
        }
      } catch (IllegalArgumentException e) {
        String err = e.getMessage();
        t.sendResponseHeaders(400, err.length());
        try (OutputStream os = t.getResponseBody()) {
          os.write(err.getBytes());
        }
      } catch (Exception e) {
        String err = e.getMessage();
        t.sendResponseHeaders(500, err.length());
        try (OutputStream os = t.getResponseBody()) {
          os.write(err.getBytes());
        }
      }
    }
  }

  public String runMathAgent(byte[] image) {
    LlmAgent mathAgent =
        LlmAgent.builder()
            .model(MODEL_NAME)
            .name("MathAgent")
            .description("Convert image to Latex math expression.")
            .instruction(
                """
                You are an expert in analyzing math expression embedded in an image.
                Based *only* on the user's request containing a png image, convert the image to the equivalent Latex format.
                Output *only* the Latex expression in a raw string and no more or no less. Do not surround with $ signs.
                Do not try to evaluate the math expression.
                """)
            .outputKey("latex_expr")
            .build();

    // Create an InMemoryRunner
    InMemoryRunner runner = new InMemoryRunner(mathAgent, APP_NAME);
    // InMemoryRunner automatically creates a session service. Create a session using the service
    Session session = runner.sessionService().createSession(APP_NAME, USER_ID).blockingGet();
    Content userImage = Content.fromParts(Part.fromBytes(image, "image/png"));

    // Run the agent
    Flowable<Event> eventStream = runner.runAsync(USER_ID, session.id(), userImage);

    // Stream event response
    ArrayList<String> res = new ArrayList<>();
    eventStream.blockingForEach(
        event -> {
          if (event.finalResponse()) {
            res.add(event.stringifyContent());
          }
        });
    return Iterables.getOnlyElement(res);
  }

  public String runChineseAgent(byte[] image) {
    LlmAgent chineseAgent =
            LlmAgent.builder()
                    .model(MODEL_NAME)
                    .name("ChineseAgent")
                    .description("Convert image to Chinese phrase.")
                    .instruction(
                            """
                            You are a Chinese teacher familiar with both simplified and traditional characters.
                            Based *only* on the user's request containing a png image, convert the image to the equivalent Chinese phrase.
                            Also grade the quality of the writing by giving a score from 1 to 10 where 1 is the worst and 10 is excellent.
                            Please give explanation of the given score.
                            Provide response as valid json string, no more no less, with 4 fields: phrase, meaning, score and explanation.
                            Do not make new lines. Do not surround with ```json and ```
                            """)
                    .outputKey("chinese_phrase")
                    .build();

    // Create an InMemoryRunner
    InMemoryRunner runner = new InMemoryRunner(chineseAgent, APP_NAME);
    // InMemoryRunner automatically creates a session service. Create a session using the service
    Session session = runner.sessionService().createSession(APP_NAME, USER_ID).blockingGet();
    Content userImage = Content.fromParts(Part.fromBytes(image, "image/png"));

    // Run the agent
    Flowable<Event> eventStream = runner.runAsync(USER_ID, session.id(), userImage);

    // Stream event response
    ArrayList<String> res = new ArrayList<>();
    eventStream.blockingForEach(
            event -> {
              if (event.finalResponse()) {
                res.add(event.stringifyContent());
              }
            });
    return Iterables.getOnlyElement(res);
  }
}
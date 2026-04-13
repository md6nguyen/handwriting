package recognition.writing.controller;

import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.form.tex.TeXFormFactory;
import org.matheclipse.core.form.tex.TeXParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
@RequestMapping(path = "/recognition/writing")
final class EvaluationApi {
  private Logger logger = LoggerFactory.getLogger(EvaluationApi.class);

  private static final TeXParser TEX_PARSER = new TeXParser();

  private static final TeXFormFactory TEX_FORM_FACTORY = new TeXFormFactory();
  @PostMapping(path = "eval")
  private ResponseEntity<String> eval(@RequestBody String request) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:5678/recognition/writing"))
            .header("Content-type", "text/plain")
            .POST(HttpRequest.BodyPublishers.ofString(request))
            .build();
    HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
    String expr = response.body();
    logger.info(String.format("Gemini recognition ===> %s", expr));
    int respCode = response.statusCode();
    switch (respCode) {
      case 200:
        if (request.contains("mode=math")) {
          String evalExpr = evaluateMathExpr(expr);
          logger.info(String.format("Symja evaluation ===> %s", evalExpr));
          return ResponseEntity.ok().body(String.format("%s = %s", expr, evalExpr));
        } else {
          return ResponseEntity.ok().body(expr);
        }
      case 400:
        return ResponseEntity.badRequest().body(expr);
      default:
        return ResponseEntity.status(500).body(expr);
    }
  }

  private static String evaluateMathExpr(String expression) {
    EvalEngine engine = EvalEngine.get();
    engine.setRelaxedSyntax(true);

    StringBuilder res = new StringBuilder();
    TEX_FORM_FACTORY.convert(res, engine.evaluate(TEX_PARSER.parse(expression)));
    return res.toString();
  }

}
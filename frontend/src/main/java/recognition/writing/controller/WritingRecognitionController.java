package recognition.writing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WritingRecognitionController {

  @RequestMapping("/")
  public String root() {
    return "index.html";
  }

  @RequestMapping("/home.html")
  public String home() {
    return "index.html";
  }

}

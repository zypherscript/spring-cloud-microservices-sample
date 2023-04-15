package com.example.downstream;

import com.netflix.discovery.EurekaClient;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@RefreshScope
public class DownstreamApplication {

  public static void main(String[] args) {
    SpringApplication.run(DownstreamApplication.class, args);
  }

}

@Controller
@ResponseBody
class RestController {

  private final ObservationRegistry observationRegistry;

  @Autowired
  @Lazy
  private EurekaClient eurekaClient;

  @Value("${spring.application.name}")
  private String appName;

  @Value("${user.password:}")
  private String password;

  RestController(ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
  }

  @GetMapping(value = "/info/{username}", produces = MediaType.TEXT_PLAIN_VALUE)
  public String getInfo(@PathVariable("username") String username) {
    var app = eurekaClient.getApplication(appName);
    Assert.notNull(app, "application from eureka client mustn't be null");
    return Observation
        .createNotStarted("getInfo", this.observationRegistry)
        .observe(() -> "%s :: Hi %s, your pwd is %s".formatted(app.getName(), username, password));
  }
}

@ControllerAdvice
class ErrorHandlingControllerAdvice {

  @ExceptionHandler
  ProblemDetail handleIllegalArgumentException(
      IllegalArgumentException illegalArgumentException) {
    var pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
    pd.setDetail("application from eureka client is not ready yet :(");
    return pd;
  }
}

package com.example.downstream;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RefreshScope
@RestController
public class DownstreamApplication {

  public static void main(String[] args) {
    SpringApplication.run(DownstreamApplication.class, args);
  }

  @Autowired
  @Lazy
  private EurekaClient eurekaClient;

  @Value("${spring.application.name}")
  private String appName;

  @Value("${user.password:}")
  private String password;

  @GetMapping(value = "/info/{username}", produces = MediaType.TEXT_PLAIN_VALUE)
  public String getInfo(@PathVariable("username") String username) {
    var from = eurekaClient.getApplication(appName).getName(); //proof with ProblemDetail
    return "%s :: Hi %s, your pwd is %s".formatted(from, username, password);
  }

}

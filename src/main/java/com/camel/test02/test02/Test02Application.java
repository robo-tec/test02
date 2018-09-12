package com.camel.test02.test02;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan(basePackages="com.camel")
public class Test02Application {

  @Value("${robo.api.path}")
  String contextPath;

  @Value("${server.port}")
  String serverPort;

  public static void main(String[] args) {
    SpringApplication.run(Test02Application.class, args);
  }


  @Bean
  ServletRegistrationBean servletRegistrationBean() {
    ServletRegistrationBean servlet = new ServletRegistrationBean
        (new CamelHttpTransportServlet(), contextPath+"/*");
    servlet.setName("CamelServlet");
    return servlet;
  }

  @Component
  class RestApi extends RouteBuilder {
    @Override
    public void configure() {
      CamelContext context = new DefaultCamelContext();

      restConfiguration()
          .contextPath(contextPath)
          .port(serverPort)
          .enableCORS(true)
          .apiContextPath("/api-doc")
          .apiProperty("api.title", "Test REST API")
          .apiProperty("api.version", "v1")
          .apiContextRouteId("doc-api")
          .component("servlet")
          .bindingMode(RestBindingMode.json);

      rest("/api/")
          .id("api-route")
          .consumes("application/json")
          .post("/bean")
          .bindingMode(RestBindingMode.json_xml)
          .type(Person.class)
          .to("direct:remoteService");

      from("direct:remoteService")
          .routeId("direct-route")
          .tracing()
          .log(">>> ${body.id}")
          .log(">>> ${body.name}")
          .transform().simple("Hello ${in.body.name}")
          .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }
  }
}

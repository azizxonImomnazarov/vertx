package com.safenetpay.firstproject.firstproject;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {


  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  void getEmployeeById(Vertx vertx,VertxTestContext testContext) throws Exception {
    HttpClient client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 8080, "localhost", "/api/employees/3")
      .compose(req -> {
        return req.send().compose(HttpClientResponse::body);     
      })
      .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
        assertEquals(buffer.toJsonObject(), new JsonObject()
        .put("id",2)
        .put("passport", "AB384729834")
        .put("country", "uzbekistan")
        .put("is_married", false)
        .put("employeeId",2)
        .put("name", "lazizxon")
        .put("surName","ibragimov")
        .put("department", "safenetpay")
        .put("salary",400));
        testContext.completeNow();
      })));
   }

   @Test
   @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
   void getEmployees(Vertx vertx,VertxTestContext testContext) throws Exception {
     HttpClient client = vertx.createHttpClient();
     client.request(HttpMethod.GET, 8080, "localhost", "/api/employees")
       .compose(req -> {
         return req.send().compose(HttpClientResponse::body);     
       })
       .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
         assertTrue(buffer.toJsonArray().size() > 0);
         testContext.completeNow();
       })));
    }

    
   @Test
   @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
   void saveEmployee(Vertx vertx,VertxTestContext testContext) throws Exception {
     HttpClient client = vertx.createHttpClient();
     client.request(HttpMethod.POST, 8080, "localhost", "/api/employees")
       .compose(req -> {
         return req.send(new JsonObject()
         .put("name", "lazizxon")
         .put("surName","ibragimov")
         .put("department", "safenetpay")
         .put("salary",400).toBuffer().toJsonObject().toString()).compose(HttpClientResponse::body);     
       })
       .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
         assertEquals(buffer.toJsonObject(), new JsonObject()
         .put("success", true));
         testContext.completeNow();
       })));
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void updateEmployee(Vertx vertx,VertxTestContext testContext) throws Exception {
      HttpClient client = vertx.createHttpClient();
      client.request(HttpMethod.PUT, 8080, "localhost", "/api/employees/5")
        .compose(req -> {
          return req.send(new JsonObject()
          .put("name", "lazizxon")
          .put("surName","ibragimov")
          .put("department", "safenetpay")
          .put("salary",5000).toBuffer().toJsonObject().toString()).compose(HttpClientResponse::body);     
        })
        .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
          assertEquals(buffer.toJsonObject(), new JsonObject()
          .put("success", true));
          testContext.completeNow();
        })));
     }

     @Test
     @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
     void deleteEmployee(Vertx vertx,VertxTestContext testContext) throws Exception {
       HttpClient client = vertx.createHttpClient();
       client.request(HttpMethod.DELETE, 8080, "localhost", "/api/employees/10")
         .compose(req -> {
           return req.send(new JsonObject().toString()).compose(HttpClientResponse::body);     
         })
         .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
           assertEquals(buffer.toJsonObject(), new JsonObject()
           .put("success", true));
           testContext.completeNow();
         })));
      }
    
}

package com.safenetpay.firstproject.firstproject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.netty.handler.codec.http.HttpHeaderValues;
import org.apache.log4j.Logger;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.LinkedList;
import java.util.List;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = Logger.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    LOGGER.debug("application is started .....");

    DataBase dataBase = new DataBase(vertx);
    Router router = Router.router(vertx);

    router.options("/api/employees").handler(rc -> {
      rc.response()
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,"GET, POST, DELETE, PUT, PATCH, OPTIONS")
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,"Content-Type, api_key, Authorization")
        .putHeader(HttpHeaders.CONTENT_TYPE,HttpHeaderValues.APPLICATION_JSON)
        .end();
    });

    router.options("/api/employees/:id").handler(rc -> {
      rc.response()
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,"GET, POST, DELETE, PUT, PATCH, OPTIONS")
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,"Content-Type, api_key, Authorization")
        .putHeader(HttpHeaders.CONTENT_TYPE,HttpHeaderValues.APPLICATION_JSON)
        .end();
    });

    router.get("/api/employees/function").handler(rc -> {
      Future<JsonArray> futureData = dataBase.getDataWithFunc();
      futureData.onComplete(rc1 -> {
        rc.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .end(rc1.result().toString());
      });
    });

    router.get("/api/employees").handler(rc -> {
      Future<JsonArray> futureData = dataBase.getData();
      futureData.onComplete(rc1 -> {
        rc.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
        .end(rc1.result().encode());
      });
    });

    router.post("/api/employees/all").handler(rc -> {
      rc.request().body().onComplete(rc1 -> {
        List<Future> futures = new LinkedList<>();
        Gson gson = new Gson();
        List<Employee> employees = gson.fromJson(rc1.result().toString(), new TypeToken<List<Employee>>() {
        }.getType());
        for (Employee employee : employees) {
          futures.add(dataBase.saveData(new JsonObject(gson.toJson(employee))));
        }
        CompositeFuture.all(futures).onComplete(rc2 -> {
          rc.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
            .end(futures.get(0).result().toString());
        });
      });
    });

    router.post("/api/employees").handler(rc -> {
      rc.request().body().onComplete(rc1 -> {
        Future<JsonObject> answer = dataBase.saveData(rc1.result().toJsonObject());
        answer.onComplete(rc2 -> {
          rc.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
            .end(rc2.result().encode());
        });
      });
    });

    router.put("/api/employees/:id").handler(rc -> {
      rc.request().body().onComplete(rc1 -> {
        Future<JsonObject> answer = dataBase.updateData(rc1.result().toJsonObject(),
            Integer.valueOf(rc.request().getParam("id")));
        answer.onComplete(rc2 -> {
          rc.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
            .end(rc2.result().encode());
        });
      });
    });

    router.delete("/api/employees/:id").handler(rc -> {
      rc.request().body().onComplete(rc1 -> {
        Future<JsonObject> answer = dataBase.deleteData(rc1.result().toJsonObject(),
            Integer.valueOf(rc.request().getParam("id")));
        answer.onComplete(rc2 -> {
          rc.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
            .end(rc2.result().encode());
        });
      });
    });

    router.get("/api/employees/info1").handler(rc -> {
      Future<JsonArray> answer = dataBase.getAllDataWithInfo();
      answer.onComplete(rc1 -> {
        JsonArray jsonArray = rc1.result();
        Future<JsonArray> ans = dataBase.getData(jsonArray);
        ans.onComplete(rc2 -> {
          rc.response().end(rc2.result().encode());
        });
      });
    });

    router.get("/api/employees/info2").handler(rc -> {
      Future<JsonArray> employeesAnswer = dataBase.getData();
      Future<JsonArray> employeeInfosAnswer = dataBase.getEmployeeInfo();
      CompositeFuture all = CompositeFuture.all(employeesAnswer, employeeInfosAnswer);
      all.onComplete(cf -> {
        if (all.succeeded()) {
          JsonArray employees = employeesAnswer.result().toBuffer().toJsonArray();
          JsonArray employeesInfo = employeeInfosAnswer.result().toBuffer().toJsonArray();
          for (int i = 0; i < employees.size(); i++) {
            JsonObject employee = employees.getJsonObject(i);
            for (int j = 0; j < employeesInfo.size(); j++) {
              JsonObject employeeInfo = employeesInfo.getJsonObject(j);
              if (employee.getInteger("id").equals(employeeInfo.getInteger("employeeId"))) {
                employee.put("passport", employeeInfo.getString("passport"))
                    .put("country", employeeInfo.getString("country"))
                    .put("isMarried", employeeInfo.getString("isMarried"));
              }
            }
          }
          rc.response().putHeader("content-type", "application/json").end(employees.toString());
        } else {
          all.cause().printStackTrace();
          ;
        }
      });
    });

    router.get("/api/employees/:id").handler(rc -> {
      Integer id = Integer.parseInt(rc.request().getParam("id"));
      Future<JsonObject> answer = dataBase.getDataWithInfo(id);
      answer.onComplete(rc1 -> {
        JsonObject jsonObject = rc1.result();
        Integer employeeId = Integer.parseInt(jsonObject.getString("employeeId"));
        Future<JsonObject> ans = dataBase.getEmployeeById(jsonObject, employeeId);
        ans.onComplete(rc2 -> {
          rc.response().end(rc2.result().encode());
        });
      });
    });


    ConfigStoreOptions fileStore = new ConfigStoreOptions()
    .setType("file")
    .setFormat("json")
    .setConfig(new JsonObject().put("path", "config.json"));

    ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions().addStore(fileStore);

    ConfigRetriever conf = ConfigRetriever.create(vertx, retrieverOptions);

    conf.getConfig(ar -> {
      if (ar.failed()) {
        LOGGER.error("Failed , {}",ar.cause());
      } else {
        JsonObject config = ar.result();
        JsonObject http = config.getJsonObject("http");
        int port = http.getInteger("port");
        String host = http.getString("host");
        vertx.createHttpServer().requestHandler(router).listen(port,host,
         h -> {
              if (h.succeeded()) {
                startPromise.complete();
                LOGGER.info("HTTP server started on port " + port);
              } else {
                startPromise.fail(h.cause());
              }
            });
      }
    });
  }
}

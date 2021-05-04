package com.safenetpay.firstproject.firstproject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.safenetpay.firstproject.testvertx.FirstVerticale;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle(), new DeploymentOptions().setWorker(false));
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(new FirstVerticale());
    vertx.eventBus().consumer("vertx.api.hello", ms -> {
      System.out.println(ms.address() + " " + ms.body());
      System.out.println(ms.isSend());
      System.out.println(ms.replyAddress());
      ms.reply("is resived message " + ms.body());
      ms.replyAndRequest("getname", ar -> {
        if (ar.succeeded()) {
          System.out.println(ar.result().body());
        } else {
          System.out.println(ar.cause().getMessage());
        }
      });
    });
    DataBase dataBase = new DataBase(vertx);
    Router router = Router.router(vertx);
    router.get("/api/employees").handler(rc -> {
      long before = System.currentTimeMillis();
      Future<JsonArray> futureData = dataBase.getData();
      futureData.onComplete(rc1 -> {
        rc.response().putHeader("content-type", "application/json").end(rc1.result().toString());
      });
      long after = System.currentTimeMillis();
      System.out.println(after - before);
    });

    router.post("/api/employees/all").handler(rc -> {
      rc.request().body().onComplete(rc1 -> {
        List<Future> futures = new LinkedList<>();
        Gson gson = new Gson();
        List<Employee> employees = gson.fromJson(rc1.result().toString(), new TypeToken<List<Employee>>(){}.getType());
        for (Employee employee : employees) {
          futures.add(dataBase.saveData(new JsonObject(gson.toJson(employee))));
        }
        CompositeFuture.all(futures)
          .onComplete(rc2 -> {
          rc.response().putHeader("content-type", "application/json").end(rc2.result().toString());
        });
      });
    });

    router.post("/api/employees").handler(rc -> {
      rc.request().body().onComplete(rc1 -> {
        Future<JsonObject> answer = dataBase.saveData(rc1.result().toJsonObject());
        answer.onComplete(rc2 -> {
          rc.response().putHeader("content-type", "application/json").end(rc2.result().toString());
        });
      });
    });

    router.put("/api/employees/:id").handler(rc -> {
      rc.request().body().onComplete(rc1 -> {
        Future<JsonObject> answer = dataBase.updateData(rc1.result().toJsonObject(),
          Integer.valueOf(rc.request().getParam("id")));
        answer.onComplete(rc2 -> {
          rc.response().putHeader("content-type", "application/json").end(rc2.result().toString());
        });
      });
    });

    router.delete("/api/employees/:id").handler(rc -> {
      rc.request().body().onComplete(rc1 -> {
        Future<JsonObject> answer = dataBase.deleteData(rc1.result().toJsonObject(),
          Integer.valueOf(rc.request().getParam("id")));
        answer.onComplete(rc2 -> {
          rc.response().putHeader("content-type", "application/json").end(rc2.result().toString());
        });
      });
    });

    router.get("/api/employees/info1").handler(rc -> {
      Future<JsonArray> answer = dataBase.getAllDataWithInfo();
      answer.onComplete(rc1 -> {
        JsonArray jsonArray = rc1.result();
        Future<JsonArray> ans = dataBase.getData(jsonArray);
        ans.onComplete(rc2 -> {
          rc.response().end(rc2.result().toString());
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
                employee
                  .put("passport", employeeInfo.getString("passport"))
                  .put("country", employeeInfo.getString("country"))
                  .put("isMarried", employeeInfo.getString("isMarried"));
              }
            }
          }
          rc.response()
            .putHeader("content-type", "application/json")
            .end(employees.toString());
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
          rc.response().end(rc2.result().toString());
        });
      });
    });

    vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8080");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}

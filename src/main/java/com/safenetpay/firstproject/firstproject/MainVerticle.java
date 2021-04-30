package com.safenetpay.firstproject.firstproject;

import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    DataBase dataBase = new DataBase(vertx);
    Router router = Router.router(vertx);
    router.get("/api/employees").handler(rc -> {
      Future<JsonArray> futureData = dataBase.getData();
      futureData.onComplete(rc1 -> {
        rc.response().putHeader("content-type", "application/json").end(rc1.result().toString());
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
                  .put("passport",employeeInfo.getString("passport"))
                  .put("country",employeeInfo.getString("country"))
                  .put("isMarried",employeeInfo.getString("isMarried"));
              }
            }
          }
          rc.response()
            .putHeader("content-type","application/json")
            .end(employees.toString());
        } else {
          all.cause().printStackTrace();;
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

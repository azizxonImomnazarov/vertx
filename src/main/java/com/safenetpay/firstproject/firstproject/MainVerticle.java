package com.safenetpay.firstproject.firstproject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.List;

public class MainVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    DataBase dataBase = new DataBase(vertx);
    Router router = Router.router(vertx);
    router.get("/api/employees")
      .handler(rc -> {
        Future<JsonArray> futureData = dataBase.getData();
        futureData.onComplete(rc1 -> {
          rc.response()
            .putHeader("content-type", "application/json")
            .end(rc1.result().toString());
        });
      });

    router.post("/api/employees")
      .handler(rc -> {
        rc.request().body().onComplete(rc1 -> {
          Future<JsonObject> answer = dataBase.saveData(rc1.result().toJsonObject());
          answer.onComplete(rc2 -> {
            rc.response()
              .putHeader("content-type", "application/json")
              .end(rc2.result().toString());
          });
        });
      });

    router.put("/api/employees/:id")
      .handler(rc -> {
        rc.request().body().onComplete(rc1 -> {
          Future<JsonObject> answer = dataBase.updateData(rc1.result().toJsonObject(),
            Integer.valueOf(rc.request().getParam("id")));
          answer.onComplete(rc2 -> {
            rc.response()
              .putHeader("content-type", "application/json")
              .end(rc2.result().toString());
          });
        });
      });

    router.delete("/api/employees/:id")
      .handler(rc -> {
        rc.request().body().onComplete(rc1 -> {
          Future<JsonObject> answer = dataBase.deleteData(rc1.result().toJsonObject(),
            Integer.valueOf(rc.request().getParam("id")));
          answer.onComplete(rc2 -> {
            rc.response()
              .putHeader("content-type", "application/json")
              .end(rc2.result().toString());
          });
        });
      });

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8888, http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println("HTTP server started on port 8888");
        } else {
          startPromise.fail(http.cause());
        }
      });
  }
}

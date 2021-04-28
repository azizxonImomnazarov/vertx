package com.safenetpay.firstproject.firstproject;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class DataBase {

  PgConnectOptions connectOptions;
  PoolOptions poolOptions;
  PgPool client;

  public DataBase(Vertx vertx) {
    connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("127.0.0.1")
      .setDatabase("employee")
      .setUser("postgres")
      .setPassword("postgres");

    poolOptions = new PoolOptions()
      .setMaxSize(5);

    client = PgPool.pool(vertx, connectOptions, poolOptions);
  }

  public Future<JsonArray> getData() {
    Promise<JsonArray> promise = Promise.promise();
    client
      .preparedQuery("SELECT * FROM employee")
      .execute(ar -> {
        if (ar.succeeded()) {
          JsonArray jsonArray = new JsonArray();
          RowSet<Row> rows = ar.result();
          for (Row row : rows) {
            JsonObject jsonObject = new JsonObject()
              .put("id", row.getInteger("id"))
              .put("name", row.getString("name"))
              .put("surName", row.getString("sur_name"))
              .put("salary", row.getDouble("salary"));
            jsonArray.add(jsonObject);
          }
          promise.complete(jsonArray);
        } else {
          System.out.println(ar.cause());
        }
      });
    return promise.future();
  }

  public Future<JsonObject> saveData(JsonObject employee) {
    Promise<JsonObject> answer = Promise.promise();
    client
      .preparedQuery("INSERT INTO employee(name, sur_name, department, salary) VALUES($1, $2, $3, $4)")
      .execute(Tuple.of(
        employee.getString("name"),
        employee.getString("surName"),
        employee.getString("department"),
        employee.getDouble("salary")),
        rc -> {
          JsonObject jsonObject = new JsonObject();
          if (rc.succeeded()) {
            jsonObject.put("success", true);
          } else {
            jsonObject
              .put("success", false)
              .put("cause", rc.cause().getMessage());
          }
          answer.complete(jsonObject);
        });
    return answer.future();
  }

  public Future<JsonObject> updateData(JsonObject employee, Integer id) {
    Promise<JsonObject> answer = Promise.promise();
    client
      .preparedQuery("UPDATE employee SET name = $1,sur_name = $2,department = $3,salary = $4 where id = $5")
      .execute(Tuple.of(
        employee.getString("name"),
        employee.getString("surName"),
        employee.getString("department"),
        employee.getDouble("salary"),
        employee.getInteger("id") == null ? id : employee.getInteger("id")),
        rc -> {
          JsonObject jsonObject = new JsonObject();
          if (rc.succeeded()) {
            jsonObject.put("success", true);
          } else {
            jsonObject
              .put("success", false)
              .put("cause", rc.cause().getMessage());
          }
          answer.complete(jsonObject);
        });
    return answer.future();
  }

  public Future<JsonObject> deleteData(JsonObject employee, Integer id) {
    Promise<JsonObject> answer = Promise.promise();
    client
      .preparedQuery("DELETE FROM employee WHERE id = $1")
      .execute(Tuple.of(id == null ? employee.getInteger("id") : id),
        rc -> {
          JsonObject jsonObject = new JsonObject();
          if (rc.succeeded()) {
            jsonObject.put("success", true);
          } else {
            jsonObject
              .put("success", false)
              .put("cause", rc.cause().getMessage());
          }
          answer.complete(jsonObject);
        });
    return answer.future();
  }
}

package com.safenetpay.firstproject.firstproject;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;

import java.util.Iterator;

public class DataBase {

  PgConnectOptions connectOptions;
  PoolOptions poolOptions;
  PgPool client;

  public DataBase(Vertx vertx) {
    connectOptions = new PgConnectOptions()
      .setPort(5433)
      .setHost("localhost")
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
          RowSet<Row> rowSet = rc.result();
          if (rc.succeeded()) {

            if (rowSet.rowCount() > 0) {
              jsonObject.put("success", true);
            } else {
              jsonObject.put("success", false);
            }
            
          } else {
            jsonObject
              .put("success", false)
              .put("cause", rc.cause().getMessage());
          }
          answer.complete(jsonObject);
        });
    return answer.future();
  }

  public Future<JsonObject> getDataWithInfo(Integer id) {
    Promise<JsonObject> promise = Promise.promise();
    client
    .preparedQuery("select * from employee_info where id = $1")
    .execute(Tuple.of(id),rc -> {
      JsonObject jsonObject = new JsonObject();
      if(rc.succeeded()) {
        RowSet<Row> rowSet = rc.result();
        Iterator<Row> iterator = rowSet.iterator();
        if(iterator.hasNext()) {
          Row row = iterator.next();
          jsonObject
          .put("id", row.getInteger("id"))
          .put("passport", row.getString("passport"))
          .put("country", row.getString("country"))
          .put("is_married" , row.getBoolean("is_married"))
          .put("employeeId", row.getInteger("employeeid"));
        }
      } else {
        jsonObject
        .put("success", false)
        .put("cause", rc.cause().getMessage());
      }
      promise.complete(jsonObject);
    });
    return promise.future();
  }

  public Future<JsonObject> getEmployeeById(JsonObject jsonObject, Integer employeeId) {
    Promise<JsonObject> promise = Promise.promise();
    client
    .preparedQuery("select * from employee where id = $1")
    .execute(Tuple.of(employeeId),rc -> {
      if(rc.succeeded()) {
        RowSet<Row> rowSet = rc.result();
        Iterator<Row> iterator = rowSet.iterator();
        if(iterator.hasNext()) {
          Row row = iterator.next();
          jsonObject
          .put("id", row.getInteger("id"))
          .put("name", row.getString("name"))
          .put("surName", row.getString("sur_name"))
          .put("department" , row.getString("department"))
          .put("salary", row.getDouble("salary"));
        }
      } else {
        jsonObject
        .put("success", false)
        .put("cause", rc.cause().getMessage());
      }
      promise.complete(jsonObject);
    });
    return promise.future();
  }

public Future<JsonArray> getAllDataWithInfo() {
  Promise<JsonArray> promise = Promise.promise();
  client
    .preparedQuery("SELECT * FROM employee_info")
    .execute(ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = new JsonArray();
        RowSet<Row> rows = ar.result();
        for (Row row : rows) {
          JsonObject jsonObject = new JsonObject()
            .put("id", row.getInteger("id"))
            .put("passport", row.getString("passport"))
            .put("country", row.getString("country"))
            .put("isMarried", row.getBoolean("is_married"))
            .put("employeeId", row.getInteger("employeeid"));
          jsonArray.add(jsonObject);
        }
        promise.complete(jsonArray);
      } else {
        System.out.println(ar.cause());
      }
    });
  return promise.future();
}
public Future<JsonArray> getData(JsonArray jsonArray) {
  Promise<JsonArray> promise = Promise.promise();
  JsonArray jsonArray2 = new JsonArray();
  for (int i = 0; i < jsonArray.size(); i++) {
    JsonObject jsonObject = jsonArray.getJsonObject(i);
    client
    .preparedQuery("SELECT * FROM employee WHERE id = $1")
    .execute(Tuple.of(jsonObject.getInteger("employeeId")),
    rc -> {
      if (rc.succeeded()) {
        RowSet<Row> rowSet = rc.result();
        Iterator<Row> iterator = rowSet.iterator();
        Row row = iterator.next();
        jsonObject
        .put("id", row.getInteger("id"))
        .put("name", row.getString("name"))
        .put("surName", row.getString("sur_name"))
        .put("department", row.getString("department"))
        .put("salary", row.getDouble("salary"));
        jsonArray.add(jsonObject);
        promise.complete(jsonArray);
      } else {
        System.out.println(rc.cause());
      }
    });
  }
  return promise.future();
}

  public Future<JsonArray> getEmployeeInfo() {
    Promise<JsonArray> promise = Promise.promise();
    client
      .preparedQuery("SELECT * FROM employee_info")
      .execute(ar -> {
        if (ar.succeeded()) {
          JsonArray jsonArray = new JsonArray();
          RowSet<Row> rows = ar.result();
          for (Row row : rows) {
            JsonObject jsonObject = new JsonObject()
              .put("id", row.getInteger("id"))
              .put("passport", row.getString("passport"))
              .put("country", row.getString("country"))
              .put("isMarried", row.getBoolean("is_married"))
              .put("employeeId", row.getDouble("employeeid"));
            jsonArray.add(jsonObject);
          }
          promise.complete(jsonArray);
        } else {
          ar.cause().printStackTrace();
        }
      });
    return promise.future();
  }

  public Future<JsonArray> getDataWithFunc() {
    Promise<JsonArray> promise = Promise.promise();
    client
      .preparedQuery("select * from get_employees()")
      .execute(ar -> {
        if (ar.succeeded()) {
          JsonArray jsonArray = new JsonArray();
          RowSet<Row> rows = ar.result();
          for (Row row : rows) {
            JsonObject jsonObject = new JsonObject()
              // .put("id", row.getInteger("id"))
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
}

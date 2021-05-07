package com.safenetpay.firstproject.testvertx;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

import java.util.concurrent.CompletionStage;

public class Main {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
//
//    Vertx vertx1 = Vertx.vertx(new VertxOptions().setWorkerPoolSize(16));
//
//    Future<Vertx> vertxFuture = Vertx.clusteredVertx(new VertxOptions().setWorkerPoolSize(16));
//    vertxFuture
//      .onComplete(ar -> {
//        if (ar.succeeded()) {
//          ar.result().createHttpServer().requestHandler(sr -> sr.response().end("hello world"));
//        } else {
//          System.out.println(ar.cause().getMessage());
//        }
//      });
//
//    Vertx.clusteredVertx(new VertxOptions().setWorkerPoolSize(16),
//      ar -> {
//      if (ar.succeeded()) {
//        ar.result().createHttpServer().requestHandler(sr -> sr.response().end("hello world"));
//      } else {
//        System.out.println(ar.cause().getMessage());
//      }
//    });

//    vertx.setPeriodic(5000,aLong -> {
//      System.out.println("hello after 5 sec");
//    });
    FileSystem fs = vertx.fileSystem();

    Future<Void> future = fs
      .createFile("index.txt")
      .compose(v -> {
        // When the file is created (fut1), execute this:
        return fs.writeFile("/foo", Buffer.buffer());
      })
      .compose(v -> {
        // When the file is written (fut2), execute this:
        return fs.move("/foo", "/bar");
      }).onComplete(ar -> {
        if (ar.succeeded()) {
          System.out.println("file good");
        } else {
          System.out.println("file bad");
        }
      });
    Future<Void> future1 = fs.createFile("index.txt").onComplete(ar -> {
      if (ar.succeeded()) {
        System.out.println("goooood");
      } else {
        System.out.println("baaad");
      }
    });
    
  }
}

// package com.safenetpay.firstproject.firstproject;

// import java.math.BigInteger;

// import io.vertx.core.AbstractVerticle;
// import io.vertx.core.Promise;
// import io.vertx.core.Vertx;
// import io.vertx.core.http.HttpServer;
// import io.vertx.core.http.HttpServerOptions;
// import io.vertx.ext.web.Router;
// import io.vertx.ext.web.openapi.RouterBuilder;
// import io.vertx.ext.web.openapi.RouterBuilderOptions;
// import io.vertx.ext.web.validation.RequestParameter;
// import io.vertx.ext.web.validation.RequestParameters;
// import io.vertx.ext.web.validation.ValidationHandler;

// public class OpenAPIExample extends AbstractVerticle {

//     public static void main(String[] args) {
//         Vertx vertx = Vertx.vertx();
//         vertx.deployVerticle(new OpenAPIExample());
//     }

//     @Override
//     public void start(Promise<Void> startPromise) {
//         // Load the api spec. This operation is asynchronous
//         RouterBuilder.create(this.vertx, "petstore.yaml").onFailure(Throwable::printStackTrace) // In case the contract
//                                                                                                 // loading failed print
//                                                                                                 // the stacktrace

//                                                                                                 new BigInteger()
//                 .onSuccess(routerBuilder -> {
//                     // Before router creation you can enable/disable various router factory
//                     // behaviours
//                     RouterBuilderOptions factoryOptions = new RouterBuilderOptions()
//                             .setMountResponseContentTypeHandler(true); // Mount ResponseContentTypeHandler automatically
//                     routerBuilder.setOptions(factoryOptions);

//                     // Setup an handler for listPets
//                     routerBuilder.operation("listEmployees").handler(routingContext -> {
//                         // Load the parsed parameters
//                         RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
//                         // Handle listPets operation
//                         RequestParameter limitParameter = params.queryParameter(/* Parameter name */ "limit");
//                         if (limitParameter != null) {
//                             // limit parameter exists, use it!
//                             Integer limit = limitParameter.getInteger();
//                         } else {
//                             // limit parameter doesn't exist (it's not required).
//                             // If it's required you don't have to check if it's null!
//                         }
//                         routingContext.response().setStatusCode(200).end();
//                     });

//                     // Create the router
//                     Router router = routerBuilder.createRouter();

//                     // Now you can use your Router instance
//                     HttpServer server = vertx
//                             .createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"))
//                             .requestHandler(router);
//                     server.listen().onSuccess(s -> System.out.println("Server started on port " + s.actualPort()))
//                             .onFailure(Throwable::printStackTrace);
//                 });
//     }
// }

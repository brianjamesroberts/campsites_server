package com.unfairtools;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by brianroberts on 10/28/16.
 */
public class RESTService {

    Vertx vertx;

    public RESTService(Vertx vert){
        vertx = vert;
        init();
    }


    public void getMarkers(RoutingContext routingContext){

        System.out.println("getMarkers received");

//        System.out.println(routingContext.getBodyAsJson());
//        System.out.println(routingContext.getBodyAsString());
//        routingContext.request().bodyHandler(r -> {
//            System.out.println(r.toJsonObject().getString("name"));
//        });


        final InfoObject infoObject = Json.decodeValue(routingContext.getBodyAsString(),
                InfoObject.class);

        System.out.println(infoObject.name + "OK WE GOOD?");



        InfoObject inf = new InfoObject();
        inf.name = "oh hey";
        routingContext.response().end(Json.encodePrettily(inf));
            //netSocket.write(Json.encode(inf) + "\n");

    }

    public void init(){
            Router router = Router.router(vertx);

            router.route("/campsites/api/*").handler(BodyHandler.create());

            router.post("/campsites/api/boundsformarkers").handler(this::getMarkers);
//            router.post("/pongonline/api/login").handler(this::getLogin);
//            router.post("/pongonline/api/invites").handler(this::getInvites);
//            router.post("/pongonline/api/invite_user").handler(this::inviteUser);
//            router.post("/pongonline/api/new_account").handler(this::newAccount);
//            router.post("/pongonline/api/validate_invite").handler(this::validateInvite);
            vertx
                    .createHttpServer()
                    .requestHandler(router::accept)
                    .listen(InitServer.restfulPort, result -> {
                        if (result.succeeded()) {
                            System.out.println("Success deploying REST to " + InitServer.restfulPort);
                            //fut.complete();
                        } else {
                            System.out.println("Failure deploying REST to " + InitServer.restfulPort);
                            //fut.fail(result.cause());
                        }

                    });

    }

}

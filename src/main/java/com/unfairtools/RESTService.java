package com.unfairtools;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brianroberts on 10/28/16.
 */
public class RESTService {

    private Vertx vertx;
    private AsyncSQLClient postgreSQLClient;


    public RESTService(Vertx vert){
        vertx = vert;
        init();

        JsonObject postgreSQLClientConfig = new JsonObject()
                .put("database", "mapsites")
                .put("username", InitServer.dbOwner)
                .put("password", InitServer.dbPassword);
        postgreSQLClient = PostgreSQLClient.createShared(vertx, postgreSQLClientConfig);

    }



    public void getMarkers(RoutingContext routingContext){

        System.out.println("getMarkers received");





        final InfoObject infoObject = Json.decodeValue(routingContext.getBodyAsString(),
                InfoObject.class);

        System.out.println(infoObject.latNorth);
        vertx.executeBlocking(future -> {
            postgreSQLClient.getConnection(res -> {
                if (res.succeeded()) {
                    SQLConnection connection = res.result();
                    InfoObject returnResult = new InfoObject();
                    try {
                        connection.query("SELECT * FROM " +
                                Constants.LOCATIONS_TABLE_NAME
                                + " WHERE " + Constants.LocationsTable.latitude + " > " + infoObject.latSouth
                                + " AND " + Constants.LocationsTable.longitude + " > " + infoObject.longWest
                                + " AND " + Constants.LocationsTable.latitude + " < " + infoObject.latNorth
                                + " AND " + Constants.LocationsTable.longitude + " < " + infoObject.longEast
                                + ";", res2 -> {
                            if (res2.succeeded()) {
                                List<JsonObject> results = res2.result().getRows();
                                int sz = res2.result().getNumRows();
                                returnResult.ids = new int[sz];
                                returnResult.latitudes = new double[sz];
                                returnResult.longitudes = new double[sz];
                                returnResult.types = new int[sz];
                                infoObject.names = new String[sz];

                                for(int i = 0; i < sz; i++){
                                    JsonObject tmp = results.get(i);
                                    returnResult.ids[i] = tmp.getInteger("id");
                                    returnResult.longitudes[i] = tmp.getDouble("longitude");
                                    returnResult.latitudes[i] = tmp.getDouble("latitude");
                                    returnResult.names[i] = tmp.getString("name");
                                    returnResult.types[i] = tmp.getInteger("type");
                                }
                                future.complete("sending routingContextBack");
                            } else {
                               future.fail("failed to select points");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        connection.close();
                        routingContext.response().end(Json.encodePrettily(returnResult));
                    }
                }else{
                }
            });
        },res-> {
            if (!res.succeeded()) {
                routingContext.response().end(Json.encodePrettily(new InfoObject()));
            }

        });

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

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
                .put("database", "campsites")
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

            postgreSQLClient.getConnection(res5 -> {
                if (res5.succeeded()) {
                    System.out.println("res succeeded");
                    SQLConnection connection = res5.result();
                    try {
                        connection.query("SELECT * FROM " +
                                Constants.LOCATIONS_TABLE_NAME
                                + " WHERE " + Constants.LocationsTable.latitude + " > " + infoObject.latSouth
                                + " AND " + Constants.LocationsTable.longitude + " > " + infoObject.longWest
                                + " AND " + Constants.LocationsTable.latitude + " < " + infoObject.latNorth
                                + " AND " + Constants.LocationsTable.longitude + " < " + infoObject.longEast
                                + ";", res33 -> {
                            if (!res33.failed()) {
                                System.out.println("res33 succeeded");
                                List<JsonObject> results = res33.result().getRows();
                                int sz = res33.result().getNumRows();
                                System.out.println("Size is " + sz);

                                InfoObject returnResult = new InfoObject();

                                returnResult.ids = new int[sz];
                                returnResult.latitudes = new double[sz];
                                returnResult.longitudes = new double[sz];
                                returnResult.types = new int[sz];
                                returnResult.names = new String[sz];

                                for(int i = 0; i < sz; i++){
                                    JsonObject tmp = results.get(i);
                                    returnResult.longitudes[i] = tmp.getDouble("longitude");
                                    System.out.println("Sending " + tmp.getDouble("longitude") + ":LONGITUDE");
                                    returnResult.latitudes[i] = tmp.getDouble("latitude");
                                    returnResult.names[i] = tmp.getString("name");
                                    returnResult.types[i] = tmp.getInteger("type");
                                    returnResult.ids[i] = Integer.parseInt(tmp.getString("id"));
                                    System.out.println("Sending " + tmp.getString("id") + ":ID");

                                }
                                //routingContext.response().end(Json.encodePrettily(returnResult));
                                System.out.println("RESPONSE SENT");
                                future.complete(returnResult);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        connection.close();
                    }
                }else{
                    System.out.println("res failed");
                }
            });
        },res6-> {
            if (res6.succeeded()) {
                System.out.println("res6 succeeded");
                System.out.println(Json.encodePrettily(res6.result()));
                routingContext.response().end(Json.encodePrettily((InfoObject)res6.result()));

            }else{

                System.out.println("res6 failed");
                routingContext.response().end(Json.encodePrettily(new InfoObject()));
            }
        });

        //routingContext.response().end(Json.encodePrettily(new InfoObject()));

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

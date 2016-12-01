package com.unfairtools;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by brianroberts on 10/28/16.
 */
public class RESTService {

    private Vertx vertx;
    private AsyncSQLClient postgreSQLClient;

    private AuthBank authBank;


    public RESTService(Vertx vert){
        vertx = vert;
        init();

        authBank = new AuthBank();

        JsonObject postgreSQLClientConfig = new JsonObject()
                .put("database", "campsites")
                .put("username", InitServer.dbOwner)
                .put("password", InitServer.dbPassword);
        postgreSQLClient = PostgreSQLClient.createShared(vertx, postgreSQLClientConfig);

    }

    public void getMarkerDetailedInfo(RoutingContext routingContext){

        System.out.println("getMarkerDetailedInfo received");

        final InfoObject infoObject = Json.decodeValue(routingContext.getBodyAsString(),
                InfoObject.class);

        System.out.println("attempting to fetch marker info for: " + infoObject.ids[0]);


        vertx.executeBlocking(future -> {
            postgreSQLClient.getConnection(res2 -> {
                if (res2.succeeded()) {
                    SQLConnection connection = res2.result();
                    try {
                        String query = "SELECT * FROM " +
                                Constants.LOCATIONS_INFO_TABLE_NAME
                                + " WHERE " + Constants.LocationsInfoTable.id_primary_key + " = " + infoObject.ids[0]
                                + ";";
                        System.out.println(query);
                        connection.query(query, res3 -> {
                            connection.close();
                            if (res3.succeeded()) {
                                System.out.println("res3 succeeded");
                                JsonObject results = res3.result().getRows().get(0);
                                System.out.println("JSon acheieved");
                                MarkerInfoObject returnResult = new MarkerInfoObject();
                                returnResult.id_primary_key = results.getInteger(Constants.LocationsInfoTable.id_primary_key);
                                returnResult.description = results.getString(Constants.LocationsInfoTable.description);
                                returnResult.website = results.getString(Constants.LocationsInfoTable.website);
                                returnResult.phone = results.getString(Constants.LocationsInfoTable.phone);
                                returnResult.google_url = results.getString(Constants.LocationsInfoTable.google_url);
                                returnResult.season = results.getString(Constants.LocationsInfoTable.season);
                                returnResult.facilities = results.getString(Constants.LocationsInfoTable.facilities);
                                System.out.println("RESPONSE SENT ( marker info ) " + Json.encodePrettily(returnResult));
                                future.complete(returnResult);
                                return;
                            } else {
                                future.fail("db query failed (res3)");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        future.fail("Unknown fail for getMarkerInfo");
                    } finally {
//                        connection.close();
                    }

                } else {
                    future.fail("Can't connect to db (res2)");
                    //return;
                }
            });
        },true, res1 ->{
                if(res1.succeeded()){
                    routingContext.response().end(Json.encodePrettily(res1.result()));
                }else{
                    routingContext.response().end("nullll");
                    System.out.println(res1.cause());
                }
        });
    }



    public void getMarkers_get(RoutingContext routingContext){

        System.out.println("getMarkers_get received");



        final InfoObject infoObject = new InfoObject();

        System.out.println(routingContext.request().getHeader("lat_ne"));

        String latNE = routingContext.request().absoluteURI();
        System.out.println(latNE);

        infoObject.latNorth = Double.parseDouble(routingContext.request().getParam("lat_ne"));
        infoObject.longEast = Double.parseDouble(routingContext.request().getParam("lng_ne"));

        infoObject.latSouth = Double.parseDouble(routingContext.request().getParam("lat_sw"));
        infoObject.longWest = Double.parseDouble(routingContext.request().getParam("lng_sw"));

        System.out.println(infoObject.latNorth + "latNorthEast");
        System.out.println(infoObject.longEast + "lngNorthEast");
        System.out.println(infoObject.latSouth + "latSouthWest");
        System.out.println(infoObject.longWest + "lngSouthWest");


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
                            connection.close();
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

//                        connection.close();
                    }
                }else{
                    System.out.println("res failed");
                }
            });
        },true,res6-> {
            if (res6.succeeded()) {
                System.out.println("res6 succeeded");
                System.out.println(Json.encodePrettily(res6.result()));
                routingContext.response().end(Json.encodePrettily((InfoObject)res6.result()));

            }else{

                System.out.println("res6 failed");
                routingContext.response().end(Json.encodePrettily(new InfoObject()));
            }
        });

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
                            connection.close();
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

//                        connection.close();
                    }
                }else{
                    System.out.println("res failed");
                }
            });
        },true,res6-> {
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


    public void getSearchInfo(RoutingContext routingContext){

        final InfoObject infoObject = Json.decodeValue(routingContext.getBodyAsString(),
                InfoObject.class);


        vertx.executeBlocking(future -> {

            postgreSQLClient.getConnection(res5 -> {
                if (res5.succeeded()) {
                    System.out.println("res succeeded");
                    SQLConnection connection = res5.result();
                    try {


                        //"SELECT * FROM LOCATIONS_TABLE WHERE name ILIKE '%el%';";
                        String query = "SELECT * FROM " +
                                Constants.LOCATIONS_TABLE_NAME +
                                " WHERE " +
                                Constants.LocationsTable.name + " ILIKE " + "'%" + infoObject.name.trim() + "%'"
                                + ";";
                        System.out.println("querying " + query);
                        connection.query(query, res33 -> {
                            connection.close();
                            if (!res33.failed()) {
                                InfoObject returnInfo = new InfoObject();
                                int numRows = res33.result().getNumRows();
                                System.out.println("Num of results " + numRows);
                                returnInfo.names = new String[numRows];
                                returnInfo.latitudes = new double[numRows];
                                returnInfo.longitudes = new double[numRows];
                                returnInfo.ids = new int[numRows];
                                List<JsonArray> results = res33.result().getResults();
                                for(int i = 0;  i < 5 &&i < numRows; i ++) {
                                    System.out.println("Appending " + results.get(i).getString(3));
                                    returnInfo.latitudes[i] = results.get(i).getDouble(1);
                                    returnInfo.longitudes[i] = results.get(i).getDouble(2);
                                    returnInfo.names[i] = results.get(i).getString(3);
                                    returnInfo.ids[i] = Integer.parseInt(results.get(i).getString(0));
                                }
                                future.complete(returnInfo);
                            } else {

                            }
                        });
                    } catch (Exception e) {
                        future.fail(e.toString());
                    }
                    finally{

//                        connection.close();

                    }
                }
            });
                    },true,res->{
            if(res.succeeded()){
                System.out.println("Result succeeded for get search results : " + infoObject.name);
                routingContext.response().end(Json.encodePrettily(res.result()));
            }else{
                System.out.println("Result FAILED for get search results : " + infoObject.name);
                routingContext.response().end(Json.encodePrettily(new InfoObject()));
            }

            });


    }

    public void getLogin(RoutingContext routingContext){

        System.out.println("login post received");
        String user = routingContext.request().getHeader("username");
        String pass = routingContext.request().getHeader("password");
        System.out.println("REST:" + routingContext.request().getHeader("username"));
        System.out.println("REST:" + routingContext.request().getHeader("password"));
        if(!user.matches("^[a-zA-Z0-9]*$")||!pass.matches("^[a-zA-Z0-9]*$")){
            InfoObject inf = new InfoObject();
            inf.name = "Must use a-z 1-9";
            inf.ids = new int[]{0};
            routingContext.response().end(Json.encodePrettily(inf));
            //netSocket.write(Json.encode(inf) + "\n");
        }else {
            vertx.executeBlocking(future -> {
                // Call some blocking API that takes a significant amount of time to return
                Login(authBank, vertx, user, pass, "campsites", future, postgreSQLClient);
            }, true,res -> {
                if (res.succeeded()) {
                    System.out.println("Login achieved\n");
                    InfoObject ret = new InfoObject();
                    ret.names = new String[]{user};
                    ret.name= new String(user);
                    ret.ids = new int[]{1};
                    ret.authKey = (String)res.result();
                    System.out.println("AUTH KEY: " + ret.authKey);
                    routingContext.response().end(Json.encode(ret));
                } else {
                    System.out.println("Login Failed" + res.cause());
                    InfoObject ret = new InfoObject();
                    ret.ids = new int[]{0};
                    ret.name = new String("login_denied");
                    routingContext.response().end(Json.encodePrettily(ret));

                }
            });
        }
    }

    public static String generateAuth(String username){
        return username+"$$$$$$$";
    }

    public static void Login(AuthBank authBank, io.vertx.core.Vertx vertx, String user, String pass, String appName, Future future, AsyncSQLClient postgreSQLClient){

        postgreSQLClient.getConnection(res -> {

            if (res.succeeded()) {
                SQLConnection connection = res.result();

                try {

                    connection.query("SELECT COUNT(*) from " + Constants.LOGIN_TABLE_NAME + " WHERE username = '" + user
                            + "' AND password = '" + pass + "';", res2 -> {
                        connection.close();
                        if (res2.succeeded()) {
                            System.out.println("Count for " + user + " : " + pass + " resulted in " +
                                    res2.result().getResults().get(0).getInteger(0));
                            if (res2.result().getResults().get(0).getInteger(0) > 0) {
                                String auth = authBank.putUser(user);
                                if(auth!=null) {
                                    future.complete(auth);
                                    return;
                                }else{
                                    future.fail("Failed to create auth for " + user + " auth was " + auth );
                                    return;
                                }
                                //future.complete();
                            } else {
                                future.fail("Incorrect Username or Password");
                                return;
                            }
                        } else {
                            future.fail("Internal failure 9009");
                            return;
                        }

                    });
                }catch(Exception e){

                }finally{

//                    connection.close();

                }
            } else {
                System.out.println("Failed to connect to postgres" + res.cause());
            }
        });
    }

    public void init(){
            Router router = Router.router(vertx);

            router.route("/campsites/api/*").handler(BodyHandler.create());

            router.get("/campsites/api/boundsformarkers_get").handler(this::getMarkers_get);

            router.post("/campsites/api/boundsformarkers").handler(this::getMarkers);

            router.post("/campsites/api/idformarkerinfo").handler(this::getMarkerDetailedInfo);

            router.post("/campsites/api/login").handler(this::getLogin);


            router.post("/campsites/api/postforsearchinfo").handler(this::getSearchInfo);
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

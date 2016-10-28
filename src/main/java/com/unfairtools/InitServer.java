package com.unfairtools;

import io.vertx.core.Vertx;

/**
 * Created by brianroberts on 10/28/16.
 */
public class InitServer {


    public static int restfulPort = 8999;
    public static String dbOwner = "brianroberts";
    public static String dbPassword = "password1";
    public static String ServerIP = "158.69.207.153";


    public InitServer(){

        Vertx vertx;
        vertx = Vertx.vertx();

        //com.unfairtools.RESTService rest =
                new RESTService(vertx);

    }

    public static void main(String[] args){
        new InitServer();
    }

}

package com.unfairtools;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by brianroberts on 11/30/16.
 */
public class AuthBank {


    public AuthBank(){
        map = new ConcurrentHashMap<String,String>();
    }
    private ConcurrentHashMap<String,String> map;

    public String getUser(String auth){
        if(map.containsKey(auth))
            return map.get(auth);
        else
            return null;
    }


    public String putUser(String username){
        StringBuilder generatedAuth = new StringBuilder();
        for(int i = 0; i < 10; i ++){
            //adds chars ! through _
           generatedAuth.append((char) (Math.random()*(90 - 33) + 33));

        }
        System.out.println("AUTHBANK: generated: " + generatedAuth.toString() );
        if(!map.containsKey(generatedAuth.toString())) {
            map.put(generatedAuth.toString(), username);
            System.out.println("Returning " + generatedAuth.toString());
            return generatedAuth.toString();
        }else{
            return null;
        }
    }

}

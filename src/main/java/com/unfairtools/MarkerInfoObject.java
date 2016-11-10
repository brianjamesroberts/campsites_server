package com.unfairtools;

/**
 * Created by newuser on 11/10/16.
 */
public class MarkerInfoObject {
        public int id_primary_key;
        public String description;
        public String website;
        public String phone;
        public String google_url;
        public String season;
        public String facilities;

        public MarkerInfoObject(){

        }

        public MarkerInfoObject(int id, String des, String web, String ph, String goog, String seas, String facil){
            this.id_primary_key = id;
            this.description = des;
            this.website = web;
            this.phone = ph;
            this.google_url = goog;
            this.season = seas;
            this.facilities = facil;
        }

}

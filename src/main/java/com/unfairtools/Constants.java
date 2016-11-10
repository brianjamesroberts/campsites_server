package com.unfairtools;

/**
 * Created by brianroberts on 10/28/16.
 */
public class Constants{
    public final static String LOCATIONS_TABLE_NAME = "LOCATIONS_TABLE";
    public class LocationsTable{
        public final static String id_primary_key = "id";
        public final static String latitude = "latitude";
        public final static String longitude = "longitude";
        public final static String name = "name";
        public final static String type = "type";

    }

    public final static String MAP_PREFERENCES_TABLE_NAME = "MAP_PREFERENCES";
    public class MapPreferencesTable{
        public final static String longitude = "longitude";
        public final static String latitude = "latitude";
        public final static String zoom = "zoom";
    }

    public final static String LOCATIONS_INFO_TABLE_NAME = "LOCATION_INFO_TABLE";
    public class LocationsInfoTable{
        public final static String id_primary_key = "id";
        public final static String description = "description";
        public final static String website = "website";
        public final static String phone = "phone";
        public final static String google_url = "google_url";
        public final static String season = "season";
        public final static String facilities = "facilities";
    }

}

package pt.isel.cn.landmarks.domain;

public class Config {
    public static final String PROJECT_ID = "cn2425-t1-g06";
    public static final String PHOTOS_BUCKET = "landmarks-photos";
    public static final String METADATA_COLLECTION = "landmarks-metadata";
    public static final String SUBSCRIPTION_NAME = "landmarks-subscription";
    public static final String API_KEY = System.getenv("GOOGLE_MAPS_API_KEY");
}

package omero.gateway;

public class BuildConfig {
    public static String getBuildVersion(){
        return BuildConfig.class.getPackage().getImplementationVersion();
    }
}

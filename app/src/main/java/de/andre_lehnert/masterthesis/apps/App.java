package de.andre_lehnert.masterthesis.apps;

/**
 * Created by alehnert on 09.08.2016.
 */
public class App {

    private String id;
    private String name;
    private String appPackage;

    public App() {

    }

    public App(String id, String name, String appPackage) {
        this.id = id;
        this.name = name;
        this.appPackage = appPackage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }
}

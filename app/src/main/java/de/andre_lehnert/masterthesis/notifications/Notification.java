package de.andre_lehnert.masterthesis.notifications;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alehnert on 09.08.2016.
 */
public class Notification {

    private String text;
    private String title;
    private String id;
    private String appName;
    private String appId;
    private String appPackage;

    private Date date;

    Notification() {

    }

    public Notification(String title, String text) {
        this.title = title;
        this.text = text;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date start) {
        this.date = start;
    }

    public String getDateFormated() {
        // 2016-07-31T14:21:00.000+0000
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ").format(date);
    }
}

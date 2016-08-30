package de.andre_lehnert.masterthesis.invocations;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alehnert on 09.08.2016.
 */
public class Invocation {

    private String appName;
    private String appId;
    private String appPackage;
    private Date startDate;
    private Date endDate;

    public Invocation() {

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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date start) {
        this.startDate = start;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStartDateFormated() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ").format(startDate);
    }

    public String getEndDateFormated() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ").format(endDate);
    }
}

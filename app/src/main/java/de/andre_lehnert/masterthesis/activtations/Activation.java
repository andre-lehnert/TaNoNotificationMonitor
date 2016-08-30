package de.andre_lehnert.masterthesis.activtations;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alehnert on 09.08.2016.
 */
public class Activation {

    private Date startDate;
    private Date endDate;
    private String smartphoneId;

    public Activation() {

    }

    public Activation(String smartphoneId, Date startDate, Date endDate) {
        this.smartphoneId = smartphoneId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getSmartphoneId() {
        return smartphoneId;
    }

    public void setSmartphoneId(String smartphoneId) {
        this.smartphoneId = smartphoneId;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getStartDateFormated() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ").format(startDate);
    }

    public String getEndDateFormated() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ").format(endDate);
    }
}

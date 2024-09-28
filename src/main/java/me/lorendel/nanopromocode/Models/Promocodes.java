package me.lorendel.nanopromocode.Models;

import java.util.Date;

public class Promocodes {

    private String promocodes;
    private Date dateCreated;
    private int countUsed;

    public Promocodes(String promocodes, Date dateCreated, int countUsed) {
        this.promocodes = promocodes;
        this.dateCreated = dateCreated;
        this.countUsed = countUsed;
    }

    public int getCountUsed() {
        return countUsed;
    }

    public void setCountUsed(int countUsed) {
        this.countUsed = countUsed;
    }

    public String getPromocodes() {
        return promocodes;
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}

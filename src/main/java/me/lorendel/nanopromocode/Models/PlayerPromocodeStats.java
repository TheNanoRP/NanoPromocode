package me.lorendel.nanopromocode.Models;

import java.util.Date;

public class PlayerPromocodeStats {

    private String uuid;
    private Date dateUsed;
    private String promocode;

    public PlayerPromocodeStats(String uuid, Date dateUsed, String promocode) {
        this.uuid = uuid;
        this.dateUsed = dateUsed;
        this.promocode = promocode;
    }

    public Date getDateUsed() {
        return dateUsed;
    }

    public void setDateUsed(Date dateUsed) {
        this.dateUsed = dateUsed;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPromocode() {
        return promocode;
    }

    public void setPromocode(String promocode) {
        this.promocode = promocode;
    }
}

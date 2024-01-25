package org.alist.hub.bo;

public class GuestBO implements Persistent {
    @Override
    public String getId() {
        return "guestlogin.txt";
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getFileValue() {
        return "";
    }
}

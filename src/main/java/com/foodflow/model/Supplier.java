package com.foodflow.model;

public class Supplier extends Person {
    private String companyName;
    private String city;
    private String status;

    public Supplier() {}
    public Supplier(long id, String companyName, String contactName, String email, String phone, String city, String status) {
        super(id, contactName, email, phone);
        this.companyName = companyName; this.city = city; this.status = status;
    }
    @Override public String getRoleLabel() { return "Food supplier"; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

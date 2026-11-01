package com.foodflow.model;

public abstract class Person {
    private long id;
    private String contactName;
    private String email;
    private String phone;

    protected Person() {}
    protected Person(long id, String contactName, String email, String phone) {
        this.id = id; this.contactName = contactName; this.email = email; this.phone = phone;
    }
    public abstract String getRoleLabel();
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

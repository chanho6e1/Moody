package com.example.Moody.Model;

import java.util.Map;

public class UserModel {
    //사용자 정보
    private String email;
    private String name;
    private String password;
    private String birth;
    private String uID;
    private Boolean check;
    private String profile;
    private Map<String, Object> liked;
    private String range;
    private Boolean connection;
    //new
    private Boolean ostate;
    private Boolean lstate;

    public UserModel() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setUID(String uID) {
        this.uID = uID;
    }

    public String getUID() {
        return uID;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public void setLiked(Map<String, Object> liked) {
        this.liked = liked;
    }

    public Map<String, Object> getLiked() {
        return liked;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public void setConnection(Boolean connection) {
        this.connection = connection;
    }

    public Boolean getConnection() {
        return connection;
    }

    public Boolean getOstate() { return ostate; }
    public void setOstate(Boolean ostate) { this.ostate = ostate; }

    public Boolean getLstate() { return lstate; }
    public void setLstate(Boolean lstate){ this.lstate = lstate; }
}

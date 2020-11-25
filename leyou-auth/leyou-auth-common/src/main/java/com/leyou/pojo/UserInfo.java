package com.leyou.pojo;


public class UserInfo {

    private Long id;

    private String username;

    public UserInfo(Long toLong, String toString) {
    }

    public UserInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
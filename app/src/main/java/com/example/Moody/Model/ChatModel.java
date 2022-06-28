package com.example.Moody.Model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {
    //채팅 정보
    private String userName; //유저 이름
    private String msg; //메세지 내용
    private String uID; //유저 아이디
    private Object timestamp; //작성 시간
    private String msgType; //메세지 타입
    private Map<String,Object> readUsers = new HashMap<String,Object>(); //읽었는지 안읽었는지


    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setUID(String userUID) {
        this.uID = userUID;
    }

    public String getUID() {
        return uID;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setReadUsers(Map<String, Object> readUsers) {
        this.readUsers = readUsers;
    }

    public Map<String, Object> getReadUsers() {
        return readUsers;
    }
}
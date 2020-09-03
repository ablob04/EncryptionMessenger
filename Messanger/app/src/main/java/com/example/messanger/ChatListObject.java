package com.example.messanger;

public class ChatListObject {
    private String ChatID;
    private String EncryptionChoice;
    private int Shift;
    private String Key;

    public ChatListObject(String chatID) {
        this.ChatID = chatID;
        this.EncryptionChoice = "";
        this.Shift = 0;
        this.Key = "";
    }

    public String getChatID() {
        return ChatID;
    }

    public void setEncryptionChoice(String EncryptionType){
        EncryptionChoice = EncryptionType;
    }

    public void setShift(int shift){
        Shift = shift;
    }

    public void setKey(String key){
        Key = key;
    }
    public String getKey(){
        return Key;
    }
    public String getEncryptionChoice(){
        return EncryptionChoice;
    }
    public int getShift(){
        return Shift;
    }
}

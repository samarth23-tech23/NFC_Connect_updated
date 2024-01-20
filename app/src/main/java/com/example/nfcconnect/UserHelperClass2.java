
package com.example.nfcconnect;


public class UserHelperClass2 {


    String name, encNFCpass, aesKey;

    public UserHelperClass2() {
    }

    public UserHelperClass2(String name, String encNFCpass, String aesKey) {
        this.name = name;
        this.encNFCpass = encNFCpass;
        this.aesKey = aesKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncNFCpass() {
        return encNFCpass;
    }

    public void setEncNFCpass(String encNFCpass) {
        this.encNFCpass = encNFCpass;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }
}

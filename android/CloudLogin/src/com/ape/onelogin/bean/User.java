package com.ape.onelogin.bean;

import com.cloud.client.CloudObject;

public class User extends CloudObject{
    
    private String userName;
    private String password;
    private String expiresIn;
    private String mobile;
    private String email;
    private String imei;
    
    private String registerDate;

    private String nickName;
    private String gender;
    private String birthday;
    private String address;
    private String avatar;
    
    private String homeTown;
    private String country;
    private String province;
    private String city;
    private String profession;
    
    private String accessId;
    private String secretKey;
    private String bucketName;
    private String ossType;
    private String ossLocal;
    private String sdkType;
    
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;
    
    public static final String MALE = "1";
    public static final String FEMALE = "0";
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getImei() {
        return imei;
    }
    public void setImei(String imei) {
        this.imei = imei;
    }
    
    public String getRegisterDate() {
        return registerDate;
    }
    
    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }
    
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getBirthday() {
        return birthday;
    }
    
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getHomeTown() {
        return homeTown;
    }
    
    public void setHomeTown(String homeTown) {
        this.homeTown = homeTown;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getProfession() {
        return profession;
    }
    
    public void setProfession(String profession) {
        this.profession = profession;
    }
    
    public String getAccessId() {
        return accessId;
    }
    
    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public String getOssType() {
        return ossType;
    }
    
    public void setOssType(String ossType) {
        this.ossType = ossType;
    }
    
    public String getOssLocal() {
        return ossLocal;
    }
    
    public void setOssLocal(String ossLocal) {
        this.ossLocal = ossLocal;
    }
    
    public String getSdkType() {
        return sdkType;
    }
    
    public void setSdkType(String sdkType) {
        this.sdkType = sdkType;
    }
    
    public String getField1() {
        return field1;
    }
    
    public void setField1(String field1) {
        this.field1 = field1;
    }
    
    public String getField2() {
        return field2;
    }
    
    public void setField2(String field2) {
        this.field2 = field2;
    }
    
    public String getField3() {
        return field3;
    }
    
    public void setField3(String field3) {
        this.field3 = field3;
    }
    public String getField4() {
        return field4;
    }
    
    public void setField4(String field4) {
        this.field4 = field4;
    }
    
    public String getField5() {
        return field5;
    }
    
    public void setField5(String field5) {
        this.field5 = field5;
    }
}

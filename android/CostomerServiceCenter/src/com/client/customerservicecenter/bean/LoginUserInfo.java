package com.client.customerservicecenter.bean;

/**
 * 保存用户基本信息
 * @author jianwen.zhu
 * @since 2014/9/28
 */
public class LoginUserInfo {
	private Integer id;
	private String nickName;
	private String headImg;
	
	private String accessId;
	private String secretKey;
	private String bucketName;
	private String ossType;
	private String ossLocal;
	
	public LoginUserInfo() {
		super();
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getHeadImg() {
		return headImg;
	}
	public void setHeadImg(String headImg) {
		this.headImg = headImg;
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
}

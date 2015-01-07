package com.client.customerservicecenter.bean;

/**
 * 城市信息
 * @author jianwen.zhu
 * @since 2014/10/22
 */
public class CityInfo {
	private Integer id;
	private String code;
	private String city;
	private String provinceCode;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getProvinceCode() {
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}
}

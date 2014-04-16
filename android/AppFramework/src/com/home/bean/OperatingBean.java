package com.home.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author jianwen.zhu
 * 2013/12/10
 */
public class OperatingBean implements Parcelable {

	private String odrid;
	/**
	 * 操作集指令
	 * 
	 * @see MSG 短信
	 * @see IMG 显示图片 需要下载URI
	 * @see AUD 音频播放
	 */
	private String operatOrder;

	/** 操作集指令描述 */
	private String operatDesc;

	/** 操作包含信息 */
	private String operatMsg;

	/** 资源url */
	private String operatURL;
	
	/** 网络链接 */
	private String operateLink;
	
	private String dateCreation; // the creation time of the record

	private static final String ID = "odrid";
	private static final String OPERATE_ORDER = "operatorder";
	private static final String OPERATE_DESC = "operatdesc";
	private static final String OPERATE_MSG = "operatmsg";
	private static final String OPERATE_URL = "operaturl";
	private static final String OPERATE_LINK = "operatelink";
	private static final String DATE_CREATION= "datecreation";
	private static final String TABLE_NAME = "tinnooperate";

	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "(" + ID + " INTEGER PRIMARY KEY,"
			+ OPERATE_ORDER + " VARCHAR(20),"
			+ OPERATE_DESC + " VARCHAR(100),"
			+ OPERATE_MSG + " VARCHAR(1000),"
			+ OPERATE_URL + " VARCHAR(20),"
			+ OPERATE_LINK + " VARCHAR(100),"
			+ DATE_CREATION + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

	public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME 
			+ " (" + ID + ","
			+ OPERATE_ORDER + ","
			+ OPERATE_DESC + ","
			+ OPERATE_MSG + ","
			+ OPERATE_URL + ","
			+ OPERATE_LINK + ","
			+ DATE_CREATION+")" 
			+ " VALUES (#,'#','#','#','#','#','#')";
	
	public static final String SQL_QUERY_ALL = "SELECT * FROM " + TABLE_NAME
			+ " ORDER BY " + DATE_CREATION + " DESC ";
	
	public static final String SQL_UPDATE  ="UPDATE " + TABLE_NAME + " SET " 
			+ OPERATE_ORDER + "='#'," 
			+ OPERATE_DESC + "='#'," 
			+ OPERATE_MSG + "='#'," 
			+ OPERATE_URL + "='#',"
			+ OPERATE_LINK+"='#',"
			+ DATE_CREATION+"='#'" 
			+ " WHERE " + ID + "='#'";
	public static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	public static final String SQL_QUERY_BY_PAGINATION  = "SELECT * FROM " + TABLE_NAME 
			+ " ORDER BY " + DATE_CREATION + " DESC "
			+ " LIMIT # OFFSET #";
	public static final String SQL_QUERY_COUNT = "SELECT count(*) FROM " + TABLE_NAME;
	public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	public static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;

	public String getOperateLink() {
		return operateLink;
	}

	public void setOperateLink(String operateLink) {
		this.operateLink = operateLink;
	}

	public String getOperatOrder() {
		return operatOrder;
	}

	public void setOperatOrder(String operatOrder) {
		this.operatOrder = operatOrder;
	}

	public String getOperatDesc() {
		return operatDesc;
	}

	public void setOperatDesc(String operatDesc) {
		this.operatDesc = operatDesc;
	}

	public String getOperatMsg() {
		return operatMsg;
	}

	public void setOperatMsg(String operatMsg) {
		this.operatMsg = operatMsg;
	}

	public String getOperatURL() {
		return operatURL;
	}

	public void setOperatURL(String operatURL) {
		this.operatURL = operatURL;
	}
	public String getOdrid() {
		return odrid;
	}

	public void setOdrid(String odrid) {
		this.odrid = odrid;
	}
	
	public String getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(String dateCreation) {
		this.dateCreation = dateCreation;
	}

	public static final Parcelable.Creator<OperatingBean> CREATOR = new Creator<OperatingBean>() {
		public OperatingBean createFromParcel(Parcel source) {
			OperatingBean mOperatingBean = new OperatingBean();
			mOperatingBean.odrid = source.readString();
			mOperatingBean.operatOrder = source.readString();
			mOperatingBean.operatDesc = source.readString();
			mOperatingBean.operatMsg = source.readString();
			mOperatingBean.operatURL = source.readString();
			mOperatingBean.operateLink = source.readString();
			mOperatingBean.dateCreation = source.readString();
			return mOperatingBean;
		}

		public OperatingBean[] newArray(int size) {
			return new OperatingBean[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(odrid);
		parcel.writeString(operatOrder);
		parcel.writeString(operatDesc);
		parcel.writeString(operatMsg);
		parcel.writeString(operatURL);
		parcel.writeString(operateLink);
		parcel.writeString(dateCreation);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this.getClass() == o.getClass())
		{
			OperatingBean op = (OperatingBean) o;
			return this.getOdrid().equals(op.getOdrid());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int type = this.getClass().hashCode();
		int code = type*31 + Integer.valueOf(odrid);
		return code;
	}

}
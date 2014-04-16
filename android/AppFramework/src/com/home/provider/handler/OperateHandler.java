/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.home.provider.handler;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.home.bean.OperatingBean;

/**
 * 
 * @author jianwen.zhu
 * 2013/6/14
 */
public class OperateHandler extends IHandler<OperatingBean> {

	private OperateHandler() {
	}

	private static OperateHandler instance = new OperateHandler();

	public static OperateHandler getInstance() {
		return instance;
	}
	
	@Override
	public boolean add(OperatingBean obj) {
		if (dbHelper == null || obj == null) {
			return false;
		}
		
		OperatingBean bean = query(obj);
		if(bean == null)
		{
			List<String> params = new ArrayList<String>();
 			params.add(obj.getOdrid());
 			params.add(obj.getOperatOrder());
 			params.add(obj.getOperatDesc());
 			params.add(obj.getOperatMsg());
 			params.add(obj.getOperatURL());
 			params.add(obj.getOperateLink());
 			params.add(obj.getDateCreation());
 			try {
				dbHelper.executeSQL(OperatingBean.SQL_INSERT, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else
		{
			update(obj);
		}
		return true;
	}

	/**
	 * 
	 */
	public  void clear() {
		if(dbHelper != null) {
			dbHelper.executeSQL(OperatingBean.SQL_DELETE_ALL);
		}
	}

	@Override
	public void delete(OperatingBean obj) {
		if(dbHelper ==null || obj == null){
 			return;
 		}
		OperatingBean tmp = query(obj);
		if(tmp != null)
		{
			List<String> params = new ArrayList<String>();
			params.add(tmp.getOdrid());
			dbHelper.executeSQL(OperatingBean.SQL_DELETE, params);
		}
	}

	@Override
	public void update(OperatingBean obj) {
		if(dbHelper ==null || obj == null){
			return;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getOperatOrder());
		params.add(obj.getOperatDesc());
		params.add(obj.getOperatMsg());
		params.add(obj.getOperatURL());
		params.add(obj.getOperateLink());
		params.add(obj.getDateCreation());
		params.add(obj.getOdrid());
		dbHelper.executeSQL(OperatingBean.SQL_UPDATE, params);
	}

	@Override
	public OperatingBean query(OperatingBean obj) {
		if (dbHelper == null) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getOdrid());
		Cursor result = dbHelper.query(OperatingBean.SQL_QUERY, params);
		try {
			if (result != null) {
				if (result.getCount() == 0) {
					result.close();
					return null;
				}
				result.moveToFirst();
				OperatingBean msg = new OperatingBean();
				msg.setOdrid(result.getString(result.getColumnIndex("odrid")));
				msg.setOperatDesc(result.getString(result.getColumnIndex("operatdesc")));
				msg.setOperateLink(result.getString(result.getColumnIndex("operatelink")));
				msg.setOperatOrder(result.getString(result.getColumnIndex("operatorder")));
				msg.setOperatURL(result.getString(result.getColumnIndex("operaturl")));
				msg.setOperatMsg(result.getString(result.getColumnIndex("operatmsg")));
				msg.setDateCreation(result.getString(result.getColumnIndex("datecreation")));
				return msg;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(result != null) {
				result.close();
			}
		}
		return null;
	}

	@Override
	public void batch(List<OperatingBean> addObjs,
			List<OperatingBean> updateObjs, List<OperatingBean> deleteObjs) {
		
	}

	@Override
	public List<OperatingBean> queryAll() {
		if(dbHelper == null){
			return null;
		}
		List<OperatingBean> beans = new ArrayList<OperatingBean>();
		Cursor result = dbHelper.query(OperatingBean.SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			OperatingBean msg = new OperatingBean();
			msg.setOdrid(result.getString(result.getColumnIndex("odrid")));
			msg.setOperatDesc(result.getString(result.getColumnIndex("operatdesc")));
			msg.setOperateLink(result.getString(result.getColumnIndex("operatelink")));
			msg.setOperatOrder(result.getString(result.getColumnIndex("operatorder")));
			msg.setOperatURL(result.getString(result.getColumnIndex("operaturl")));
			msg.setOperatMsg(result.getString(result.getColumnIndex("operatmsg")));
			msg.setDateCreation(result.getString(result.getColumnIndex("datecreation")));
			beans.add(msg);
		}
		result.close(); 
		return beans;
	}
	
	public List<OperatingBean> queryByPagination(int pagesize,int offset) {
		if(dbHelper == null){
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(pagesize + "");
		params.add(offset + "");
		List<OperatingBean> beans = new ArrayList<OperatingBean>();
		Cursor result = dbHelper.query(OperatingBean.SQL_QUERY_BY_PAGINATION,params);
		while(result != null && result.moveToNext()){
			OperatingBean msg = new OperatingBean();
			msg.setOdrid(result.getString(result.getColumnIndex("odrid")));
			msg.setOperatDesc(result.getString(result.getColumnIndex("operatdesc")));
			msg.setOperateLink(result.getString(result.getColumnIndex("operatelink")));
			msg.setOperatOrder(result.getString(result.getColumnIndex("operatorder")));
			msg.setOperatURL(result.getString(result.getColumnIndex("operaturl")));
			msg.setOperatMsg(result.getString(result.getColumnIndex("operatmsg")));
			msg.setDateCreation(result.getString(result.getColumnIndex("datecreation")));
			beans.add(msg);
		}
		result.close(); 
		return beans;
	}
 
}

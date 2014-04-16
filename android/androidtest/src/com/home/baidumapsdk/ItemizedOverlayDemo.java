package com.home.baidumapsdk;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.home.R;
/**
 *  在一个圆周上添加自定义overlay. 
 */
public class ItemizedOverlayDemo extends Activity {

	final static String TAG = "MainActivty";
	static MapView mMapView = null;
	
	private MapController mMapController = null;

	public MKMapViewListener mMapListener = null;
	FrameLayout mMapViewContainer = null;
	
	Button testItemButton = null;
	Button removeItemButton = null;
	Button removeAllItemButton = null;
	EditText indexText = null;
	int index =0;
	
	/**
	 *  圆心经纬度坐标 
	 */
	int cLat = 39909230 ;
	int cLon = 116397428 ;
	// 存放overlayitem 
	public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
	// 存放overlay图片
	public List<Drawable>  res = new ArrayList<Drawable>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemizedoverlay);
        mMapView = (MapView)findViewById(R.id.bmapView);
        mMapController = mMapView.getController();
        initMapView();
        mMapView.getController().setZoom(13);
        mMapView.getController().enableClick(true);
        mMapView.displayZoomControls(true);
        testItemButton = (Button)findViewById(R.id.button1);
        removeItemButton = (Button)findViewById(R.id.button2);
        removeAllItemButton = (Button)findViewById(R.id.button3);
       
        OnClickListener clickListener = new OnClickListener(){
			public void onClick(View v) {
				testItemClick();
			}
        };
        OnClickListener removeListener = new OnClickListener(){
        	public void onClick(View v){
        		testRemoveItemClick();
        	}
        };
        OnClickListener removeAllListener = new OnClickListener(){
            public void onClick(View v){
                testRemoveAllItemClick();
            }
        };
        
        testItemButton.setOnClickListener(clickListener);
        removeItemButton.setOnClickListener(removeListener);
        removeAllItemButton.setOnClickListener(removeAllListener);
        mMapListener = new MKMapViewListener() {
			
			@Override
			public void onMapMoveFinish() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				// TODO Auto-generated method stub
				String title = "";
				if (mapPoiInfo != null){
					title = mapPoiInfo.strText;
					Toast.makeText(ItemizedOverlayDemo.this,title,Toast.LENGTH_SHORT).show();
				}
			}
		};
		mMapView.regMapViewListener(DemoApplication.getInstance().mBMapManager, mMapListener);
		
		res.add(getResources().getDrawable(R.drawable.icon_marka));
		res.add(getResources().getDrawable(R.drawable.icon_markb));
		res.add(getResources().getDrawable(R.drawable.icon_markc));
		res.add(getResources().getDrawable(R.drawable.icon_markd));
		res.add(getResources().getDrawable(R.drawable.icon_marke));
		res.add(getResources().getDrawable(R.drawable.icon_markf));
		res.add(getResources().getDrawable(R.drawable.icon_markg));
		res.add(getResources().getDrawable(R.drawable.icon_markh));
		res.add(getResources().getDrawable(R.drawable.icon_marki));
		
		// overlay 数量 
		int iSize = 9;
		double pi = 3.1415926 ;
		// overlay半径
		int r = 50000;
		// 准备overlay 数据
		for (int i=0; i<iSize ; i++){
		   	int lat = (int) (cLat + r*Math.cos(2*i*pi/iSize));
		   	int lon = (int) (cLon + r*Math.sin(2*i*pi/iSize));
		   	OverlayItem item= new OverlayItem(new GeoPoint(lat,lon),"item"+i,"item"+i);
		   	item.setMarker(res.get(i%(res.size())));
		   	mGeoList.add(item);
		}
    }
    
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	mMapView.onSaveInstanceState(outState);
    	
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	mMapView.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    protected void onDestroy() {
        mMapView.destroy();
        super.onDestroy();
    }
    
    private void initMapView() {
        mMapView.setLongClickable(true);
        //mMapController.setMapClickEnable(true);
        //mMapView.setSatellite(false);
    }
    
    public void testRemoveAllItemClick(){
        mMapView.getOverlays().clear();
        mMapView.refresh();
    }
    public void testRemoveItemClick(){
    	int n = (int) ( Math.random()*( mGeoList.size() - 1 ));
    	Drawable marker = ItemizedOverlayDemo.this.getResources().getDrawable(R.drawable.icon_marka);
	    mMapView.getOverlays().clear();
	    
	    OverlayTest ov = new OverlayTest(marker, this);
	    for(int i=0 ;i<mGeoList.size();i++){
	    	if ( i != n )
	    	   ov.addItem(mGeoList.get(i));
	    }
	    mMapView.getOverlays().add(ov);
	    
	    mMapView.refresh();
	    mMapView.getController().setCenter(new GeoPoint(cLat,cLon));
    }
    
    public void testItemClick() {
    	Drawable marker = ItemizedOverlayDemo.this.getResources().getDrawable(R.drawable.icon_marka);
	    mMapView.getOverlays().clear();
	    OverlayTest ov = new OverlayTest(marker, this);
	    for(OverlayItem item : mGeoList){
	    	ov.addItem(item);
	    }
	    mMapView.getOverlays().add(ov);
	    mMapView.refresh();
	    mMapView.getController().setCenter(new GeoPoint(cLat,cLon));
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}

class OverlayTest extends ItemizedOverlay<OverlayItem> {
    public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
	private Context mContext = null;
    static PopupOverlay pop = null;

	public OverlayTest(Drawable marker,Context context){
		super(marker);
		this.mContext = context;
        pop = new PopupOverlay( ItemizedOverlayDemo.mMapView,new PopupClickListener() {
			
			@Override
			public void onClickedPopup() {
				 Log.d("hjtest  ", "clickpop");
			}
		});
	    populate();
		
	}
	protected boolean onTap(int index){
		Drawable marker = this.mContext.getResources().getDrawable(R.drawable.pop);  //得到需要标在地图上的资源
		BitmapDrawable bd = (BitmapDrawable) marker;
        Bitmap popbitmap = bd.getBitmap();
	    pop.showPopup(popbitmap, mGeoList.get(index).getPoint(), 32);
		// int latspan = this.getLatSpanE6();
		// int lonspan = this.getLonSpanE6();
		Toast.makeText(this.mContext, mGeoList.get(index).getTitle(), Toast.LENGTH_SHORT).show();
		super.onTap(index);
		return false;
	}
	public boolean onTap(GeoPoint pt, MapView mapView){
		if (pop != null){
			pop.hidePop();
		}
		super.onTap(pt,mapView);
		return false;
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return mGeoList.get(i);
	}
	
	@Override
	public int size() {
		return mGeoList.size();
	}
	public void addItem(OverlayItem item){
		mGeoList.add(item);
		populate();
	}
	public void removeItem(int index){
		mGeoList.remove(index);
		populate();
	}

	
}

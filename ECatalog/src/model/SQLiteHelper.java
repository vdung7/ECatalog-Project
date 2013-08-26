package model;

import java.util.ArrayList;

import android.Manifest.permission;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Picture;

public class SQLiteHelper extends SQLiteOpenHelper{
	private static final String DATABASE_PATH = "/data/data/com.example.ecatalog/databases/";
	private static final String DATABASE_NAME = "ecatalog.sqlite";
	private static final int DATABASE_VERSION = 1;
	
	// Products table
	public static final String P_TABLE_NAME = "Products";
	public static final String PID = "_pid";
	public static final String NAME = "name";
	public static final String CODENAME = "codeName"; 
	public static final String PRICE = "price";
	public static final String QUANTITY = "quantity";
	public static final String ISHOT = "isHot";
	public static final String DETAIL = "detail";
	
	// Contents table
	public static final String C_TABLE_NAME = "Contents";
	public static final String CID = "_cid";
	public static final String CNAME = "content";
	public static final String CIMGPATH = "cimgPath";
	
	// ImagePaths table
	public static final String I_TABLE_NAME = "ImagePaths";
	public static final String IMGPATH = "imgPath";
	
	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// create products table	
		db.execSQL("CREATE TABLE " + P_TABLE_NAME + " ("
				+ PID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ CID + " INTEGER," 
				+ CODENAME + " TEXT," 
				+ NAME + " TEXT,"
				+ DETAIL + " TEXT," 
				+ QUANTITY + " INTEGER," 
				+ ISHOT + " TEXT," 
				+ PRICE + " DOUBLE);");
		
		// create contents table	
		db.execSQL("CREATE TABLE " + C_TABLE_NAME + " (" 
				+ CID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ CNAME + " TEXT,"
				+ CIMGPATH + " TEXT);");
		
		// create contents table	
		db.execSQL("CREATE TABLE " + I_TABLE_NAME + " (" 
				+ PID + " INTEGER," 
				+ IMGPATH + " TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("Upgrading db from " + oldVersion + " to new version: " + newVersion);
	    db.execSQL("DROP TABLE IF EXISTS " + P_TABLE_NAME);
	    db.execSQL("DROP TABLE IF EXISTS " + C_TABLE_NAME);
	    db.execSQL("DROP TABLE IF EXISTS " + I_TABLE_NAME);
	    onCreate(db);
	}
	
	public Product getProduct(int pid){
		SQLiteDatabase sd = getWritableDatabase();
		
		Cursor c  = sd.rawQuery("select * from "+P_TABLE_NAME+" where "+PID+" = "+String.valueOf(pid), null);
		Cursor c2 = sd.rawQuery("select "+IMGPATH+" from "+I_TABLE_NAME+" where "+PID+" = "+String.valueOf(pid), null);
		c.moveToNext();
		
		Product result = new Product();
		result.setName(c.getString(c.getColumnIndex(NAME)));
		result.setCodeName(c.getString(c.getColumnIndex(CODENAME)));
		result.setDetail(c.getString(c.getColumnIndex(DETAIL)));
		result.setQuantity(c.getInt(c.getColumnIndex(QUANTITY)));
		result.setPrice(c.getDouble(c.getColumnIndex(PRICE)));
		result.setHot(c.getString(c.getColumnIndex(ISHOT)).equalsIgnoreCase("hot")?true:false);
		result.setPid(c.getInt(c.getColumnIndex(PID)));
		result.setCid(c.getInt(c.getColumnIndex(CID)));
		
		// add all image from ImagePaths Table
		while(c2.moveToNext()){
			result.addImagePath(c2.getString(c2.getColumnIndex(IMGPATH)));
		}
		return result;
	}
	
	public ArrayList<Product> getProductsByContent(long cid){
		SQLiteDatabase sd = getWritableDatabase();
		
		ArrayList<Product> list = new ArrayList<Product>();
		Cursor c = sd.rawQuery("select * from "+P_TABLE_NAME
								+" where "+CID+" = "+cid, null);
		c.moveToLast();
		c.moveToNext();
		while(c.moveToPrevious()){
			Product result = new Product();
			result.setName(c.getString(c.getColumnIndex(NAME)));
			result.setPrice(c.getDouble(c.getColumnIndex(PRICE)));
			result.setHot(c.getString(c.getColumnIndex(ISHOT)).equalsIgnoreCase("hot")?true:false);
			result.setPid(c.getInt(c.getColumnIndex(PID)));
			result.setCid(c.getInt(c.getColumnIndex(CID)));
			
			int pid = result.getPid();
			Cursor c2 = sd.rawQuery("select "+IMGPATH+" from "+I_TABLE_NAME+" where "+PID+" = "+String.valueOf(pid), null);
			c2.moveToNext();
			// just add 1 image
			result.addImagePath(c2.getString(c2.getColumnIndex(IMGPATH)));
			
			list.add(result);
		}
		return list;
	}
	
	public int addProduct(Product product, long nextPID) {
		ContentValues values = new ContentValues();
		values.put(NAME, product.getName());
		values.put(CID, product.getCid());
		values.put(CODENAME, product.getCodeName());
		values.put(DETAIL, product.getDetail());
		values.put(QUANTITY, product.getQuantity());
		values.put(PRICE, product.getPrice());
		values.put(ISHOT, product.isHot()?"hot":"");
		
		int result = (int) getWritableDatabase().insert(P_TABLE_NAME, NAME, values);
		
		// add image paths list of new product to ImagePaths table
		ArrayList<String> list = product.getImagePathsList();
		for(int i=0; i<list.size(); i++) {
			this.addImagePath( nextPID, list.get(i) );
		}
		
		return result;
	}
	
	public void removeProduct(long pid){
		// if remove a product , imagePath have pid similar will be remove too
		SQLiteDatabase sd = getWritableDatabase();
		sd.execSQL("delete from "+P_TABLE_NAME+" where "+PID+" = "+pid);	
		sd.execSQL("delete from "+I_TABLE_NAME+" where "+PID+" = "+pid);
	}
	
 	public void updateProduct(Product product){
 		SQLiteDatabase sd = getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
		values.put(NAME, product.getName());
		values.put(CODENAME, product.getCodeName());
		values.put(CID, product.getCid());
		values.put(DETAIL, product.getDetail());
		values.put(QUANTITY, product.getQuantity());
		values.put(PRICE, product.getPrice());
		values.put(ISHOT, product.isHot()?"hot":"");
		
		sd.update(P_TABLE_NAME, values, PID+" = "+product.getPid(), null);
 	}
 	
 	public long maxIdProducts(){
 		SQLiteDatabase sd = getWritableDatabase();
 		
 		Cursor c = sd.rawQuery("select max("+PID+") from "+P_TABLE_NAME, null);
 		c.moveToFirst();
 		if(!c.isNull(0)){
 			return c.getLong(0);
 		}
 		return 0;
 		
 	}
 	
 	public ArrayList<Content> getAllContents() {
 		SQLiteDatabase sd = getWritableDatabase();
 		
 		ArrayList<Content> list = new ArrayList<Content>();
		Cursor c = sd.rawQuery("select * from "+C_TABLE_NAME, null);
		
		while(c.moveToNext()){
			Content result = new Content();
			result.setName(c.getString(c.getColumnIndex(CNAME)));
			result.setCid(c.getInt(c.getColumnIndex(CID)));
			result.setImgPath(c.getString(c.getColumnIndex(CIMGPATH)));
			
			list.add(result);
		}
		
		return list;
 	}
 	
 	public Content getContent(int cid){
 		SQLiteDatabase sd = getWritableDatabase();
		
		Cursor c = sd.rawQuery("select * from "+C_TABLE_NAME+" where "+CID+" = "+String.valueOf(cid), null);
		c.moveToNext();
		
		Content result = new Content();
		result.setName(c.getString(c.getColumnIndex(CNAME)));
		result.setImgPath(c.getString(c.getColumnIndex(CIMGPATH)));
		result.setCid(c.getInt(c.getColumnIndex(CID)));
		
		return result;
 	}
 	
 	public int addContent(Content content) {
 		ContentValues values = new ContentValues();
		values.put(CNAME, content.getName());
		values.put(CIMGPATH, content.getImgPath());
		
		int result = (int) getWritableDatabase().insert(C_TABLE_NAME, CNAME, values);
		
		return result;
 	}
 	
 	public void removeContent(long cid) {
 		// if remove a content , products have cid similar will be remove too
 		SQLiteDatabase sd = getWritableDatabase();
 		sd.execSQL("delete from "+C_TABLE_NAME+" where "+CID+" = "+cid);
 		sd.execSQL("delete from "+P_TABLE_NAME+" where "+CID+" = "+cid);
 	}
 	
 	public void updateContent(Content content){
 		SQLiteDatabase sd = getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
		values.put(CNAME, content.getName());
		values.put(CIMGPATH, content.getImgPath());
		
		sd.update(C_TABLE_NAME, values, CID+" = "+content.getCid(), null);
		
 	} 
 	
 	public ArrayList<Product> filterProducts(int from,int to,int cid){
 		SQLiteDatabase sd=getWritableDatabase();
 		ArrayList<Product> list=new ArrayList<Product>();
 		
 		Cursor c=sd.rawQuery("select * from "+P_TABLE_NAME+
 				" where "+PRICE+" >= "+from+" and "+PRICE+" <= "+to
 				+" and "+CID+" = "+cid,null);
 		
 		c.moveToLast();
 		c.moveToNext();
 		
 		while(c.moveToPrevious()){
 			Product result = new Product();
			result.setName(c.getString(c.getColumnIndex(NAME)));
			result.setPrice(c.getDouble(c.getColumnIndex(PRICE)));
			result.setHot(c.getString(c.getColumnIndex(ISHOT)).equalsIgnoreCase("hot")?true:false);
			result.setPid(c.getInt(c.getColumnIndex(PID)));
			result.setCid(c.getInt(c.getColumnIndex(CID)));
			
			int pid = result.getPid();
			Cursor c2 = sd.rawQuery("select "+IMGPATH+" from "+I_TABLE_NAME+" where "+PID+" = "+String.valueOf(pid), null);
			c2.moveToNext();
			// just add 1 image
			result.addImagePath(c2.getString(c2.getColumnIndex(IMGPATH)));
			
			list.add(result);
 		}
 		return list;
 	}
 	
 	public void addImagePath (long pid, String imagePath) {
 		ContentValues values = new ContentValues();
		values.put(PID, pid);
		values.put(IMGPATH, imagePath);
		
		getWritableDatabase().insert(I_TABLE_NAME, imagePath, values);
 	}
 	
 	public void removeImagePath (int pid, String imagePath) {
 		SQLiteDatabase sd = getWritableDatabase();
 		sd.execSQL("delete from "+I_TABLE_NAME
 				+" where "+PID+" = "+pid+" and "+IMGPATH+" = \""+imagePath+"\"");
 	}
}

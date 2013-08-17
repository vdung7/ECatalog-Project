package model;

import java.util.ArrayList;

public class Product {
	private int pid;
	private int cid;
	private String codeName, imagePath, name, detail;
	private int quantity;
	private ArrayList<String> imagePaths;
	private double price;
	private boolean isHot;
	
	public Product(String codename, ArrayList<String> imagePaths, String name, int quantity,
			String detail, float price, boolean isHot) {
		super();
		this.codeName = codename;
		this.imagePaths = imagePaths;
		this.name = name;
		this.quantity = quantity;
		this.detail = detail;
		this.price = price;
		this.isHot = isHot;
	}
	
	public Product() {
		this("",new ArrayList<String>(), "", 0, "", 0f, false);
	}
	
	@Override
	public String toString() {
		return "Product [pid=" + pid + ", cid=" + cid + ", codeName="
				+ codeName + ", imagePath=" + imagePath + ", name=" + name
				+ ", detail=" + detail + ", quantity=" + quantity + ", price="
				+ price + ", isHot=" + isHot + "]";
	}

	
	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public ArrayList<String> getImagePathsList() {
		return imagePaths;
	}
	
	public void setImagePathsList(ArrayList<String> imagePaths) {
		this.imagePaths = imagePaths;  
	}
	
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	public void addImagePath(String imagePath) {
		imagePaths.add(imagePath);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public boolean isHot() {
		return isHot;
	}
	public void setHot(boolean isHot) {
		this.isHot = isHot;
	}
	
	
}

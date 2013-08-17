package model;

public class Content {
	private int cid=0;
	private String imgPath, name;
	public Content(String imgPath, String name) {
		super();
		this.imgPath = imgPath;
		this.name = name;
	}
	
	public Content() {
		this("","");
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}

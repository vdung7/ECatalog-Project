package view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import model.Content;
import model.Product;
import model.SQLiteHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecatalog.R;

public class DetailViewActivity extends Activity{
	private int pid;	// for we know this Activity for New or View
	private int cid;	// for we can interact with Content Table
	private long nextId;	//  to define nextId for new Product

	private String imgFilePath = "";
	private ArrayList<String> imgFilePathsList = new ArrayList<String>();

	private boolean camera;
	private static final int PICK_IMG = 4;
	private static final int TAKE_PIC = 3;

	private Product p=new Product();
	private SQLiteHelper sh;

	private TextView pIdTxt;
	private EditText pCodeNameTxt;
	private EditText pNameTxt;
	private EditText pPriceTxt;
	private EditText pQuanTxt;
	private EditText pDetailTxt;
	private CheckBox isHotChk;
	private Button pEditBtn,pSaveBtn;

	private OnKeyListener mKeyListener = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			switch (v.getId()) {
			case R.id.pNameTxt:
			case R.id.pPriceTxt:
				pSaveBtn.setEnabled(
						pNameTxt.getText().length() > 0 &&
						pPriceTxt.getText().length() > 0);
				break;
			}
			return false;
		}
	};

	private BaseAdapter imgListAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return imgFilePathsList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// i won't use convertView parameter because 
		// this class just inner class
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewitem, null);

			ImageView iv = (ImageView) retval.findViewById(R.id.imagePathView);

			if (imgFilePathsList.size() > 0) {
				String imgPath = imgFilePathsList.get(position);
				File imageFile = new File(imgPath);
				Bitmap bitmap = BitmapFactory.decodeFile(imageFile
						.getAbsolutePath());
				iv.setImageBitmap(bitmap);
			}
			return retval;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		// take signal from parent Activity 
		nextId=getIntent().getExtras().getLong("nextId");
		pid=getIntent().getExtras().getInt("pid");
		cid=getIntent().getExtras().getInt("cid");
		camera=getIntent().getExtras().getBoolean("camera");

		// connect to Database
		sh=new SQLiteHelper(this);

		// configure ListView of Images
		// used HorizontialListView class
		HorizontialListView listview = (HorizontialListView) findViewById(R.id.listImgPathView);
		listview.setAdapter(imgListAdapter);
		// listener for remove 1 image from product
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			AlertDialog alertDialog;
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				// a product always have an image, so if user try for delete last image, 
				// block this action and alarm to user with Toast
				if (imgFilePathsList.size()>1) {
					alertDialog = new AlertDialog.Builder(DetailViewActivity.this)
					.setTitle(R.string.titleDeleteDialog)
					.setMessage(R.string.messageDeleteImgDialog)
					.setPositiveButton("OK", new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (pid > 0)
								sh.removeImagePath(pid,
										imgFilePathsList.get(position));
							imgFilePathsList.remove(position);
							imgListAdapter.notifyDataSetChanged();
						}
					})
					.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							alertDialog.cancel();
						}
					}).create();
					alertDialog.show();
				}else {
					Toast.makeText(getApplicationContext(), 
							"You cannot remove all images of this product!", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		//connect to layout file
		pIdTxt = (TextView) findViewById(R.id.idTxt);
		pCodeNameTxt = (EditText) findViewById(R.id.pCodeName);
		pNameTxt =(EditText)findViewById(R.id.pNameTxt);
		pPriceTxt =(EditText)findViewById(R.id.pPriceTxt);
		pQuanTxt =(EditText)findViewById(R.id.pQuanTxt);
		pDetailTxt =(EditText)findViewById(R.id.pDetailTxt);
		isHotChk=(CheckBox)findViewById(R.id.isHotChk);

		pEditBtn=(Button)findViewById(R.id.pEditBtn);
		pSaveBtn=(Button)findViewById(R.id.pSaveBtn);

		// add event listener
		pNameTxt.setOnKeyListener(mKeyListener);
		pPriceTxt.setOnKeyListener(mKeyListener);
		pEditBtn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setEditable();
				pSaveBtn.setEnabled(true);
			}
		});
		pSaveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//get fields into temporary product object
				p.setCodeName(pCodeNameTxt.getText().toString());
				p.setCid(cid);
				p.setName(pNameTxt.getText().toString());
				p.setDetail(pDetailTxt.getText().toString());
				p.setHot(isHotChk.isChecked());
				p.setPrice(Double.parseDouble(pPriceTxt.getText().toString()));
				p.setQuantity(Integer.parseInt(pQuanTxt.getText().toString()));

				if(pid == 0){
					//insert into database
					p.setImagePathsList(imgFilePathsList);
					p.setPid(sh.addProduct(p, nextId));
					pid=p.getPid();

					// update imgPath for Content of this Product
					Content content = sh.getContent(cid);
					content.setImgPath( imgFilePathsList.get(0) );
					sh.updateContent(content);
				}else
					//update into database
					sh.updateProduct(p);

				Toast.makeText(getApplicationContext(),
						"Your product has been saved", Toast.LENGTH_LONG).show();

			}
		});

		// if this Activity for add new product
		if(pid ==0 ){
			if(camera)
				takePic();
			else
				pickPic();
			eraseDat();
		}
		// if this Activity for update or view product
		else{
			p=sh.getProduct(pid);

			// display image of this product
			imgFilePathsList = p.getImagePathsList();
			imgListAdapter.notifyDataSetChanged();

			// fill data of this product
			fillData();

			pEditBtn.setEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detail_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.addImageCamera: {
			takePic();
			break;
		}
		case R.id.addImageGallery: {
			pickPic();
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private void takePic() {
		//start intent for camera (to take an image)
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, TAKE_PIC);
	}

	private void pickPic(){
		//start intent for gallery (to pick an image)
		Intent galleryIntent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(galleryIntent, PICK_IMG);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// result from camera intent
		if(requestCode == TAKE_PIC && resultCode==RESULT_OK && null != data) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");

			// store image has been taken by camera intent
			try{
				// image filename pattern : [demopic+PID+'order in ImgPathsList'.jpg] 
				imgFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() 
						+"/ecatPics/demopic"+nextId+imgFilePathsList.size()+".jpg";
				FileOutputStream outstream = new FileOutputStream(imgFilePath);
				photo.compress(Bitmap.CompressFormat.JPEG, 90, outstream);

				// add imgPath to imgPathsList and notify to ListView
				imgFilePathsList.add(imgFilePath);
				imgListAdapter.notifyDataSetChanged();

				// if this products not new, update imagePaths table (database)
				if(pid>0){
					sh.addImagePath(pid, imgFilePath);
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// result from gallery (pick img) intent
		else if(requestCode == PICK_IMG && resultCode==RESULT_OK && null != data) {
			Uri selectIMG=data.getData();
			String[] file={MediaStore.Images.Media.DATA};
			Cursor c=getContentResolver().query(selectIMG,file,null,null,null);

			c.moveToFirst();
			int columnIdex=c.getColumnIndex(file[0]);
			imgFilePath = c.getString(columnIdex);
			c.close();

			// add imgPath to imgPathsList and notify to ListView
			imgFilePathsList.add(imgFilePath);
			imgListAdapter.notifyDataSetChanged();

			// if this products not new, update imagePaths table (database)
			if(pid>0){
				sh.addImagePath(pid, imgFilePath);
			}

		}
		// if user choose CANCEL option, back to ProductsListActivity
		else if(resultCode == RESULT_CANCELED && pid==0) {
			finish();
		}
	}

	private void fillData() {
		pIdTxt.setText("ID: "+p.getPid());
		pCodeNameTxt.setText(p.getCodeName());
		pNameTxt.setText(p.getName());
		pPriceTxt.setText(Double.toString(p.getPrice()));
		pQuanTxt.setText(""+p.getQuantity());
		pDetailTxt.setText(p.getDetail());
		isHotChk.setChecked(p.isHot());
	}

	private void eraseDat() {
		setEditable();

		pSaveBtn.setEnabled(false);

		pIdTxt.setText("ID: ");
		pCodeNameTxt.setText("");
		pNameTxt.setText("");
		pPriceTxt.setText("");
		pQuanTxt.setText("0");
		pDetailTxt.setText("");
		isHotChk.setChecked(true);
	}

	private void setEditable() {
		pCodeNameTxt.setEnabled(true);
		pNameTxt.setEnabled(true);
		pPriceTxt.setEnabled(true);
		pQuanTxt.setEnabled(true);
		pDetailTxt.setEnabled(true);
		isHotChk.setClickable(true);
	}

}

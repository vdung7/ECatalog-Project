package view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cyber.app.ecatalog.R;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

public class DetailViewActivity extends Activity{
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private final String PENDING_ACTION_BUNDLE_KEY = "com.example.ecatalog:PendingAction";
	private enum PendingAction {
		NONE,
		POST_PHOTO,
	}
	private PendingAction pendingAction = PendingAction.NONE;

	private int pid;	// for we know this Activity for New or View
	private int cid;	// for we can interact with Content Table
	private long nextId;	//  to define nextId for new Product
	private boolean shareable = false;

	// use to process image paths
	private String imgFilePath = "";
	private ArrayList<String> imgFilePathsList = new ArrayList<String>();

	private boolean camera;
	public static final int PICK_IMG = 4;
	public static final int TAKE_PIC = 3;

	private Product p=new Product();
	private SQLiteHelper sh;
	private UiLifecycleHelper uiHelper;

	private TextView pIdTxt;
	private EditText pCodeNameTxt;
	private EditText pNameTxt;
	private EditText pPriceTxt;
	private EditText pQuanTxt;
	private EditText pDetailTxt;
	private CheckBox isHotChk;

	private Button pEditBtn,pSaveBtn,pShareBtn;


	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
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
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		// get signal from parent Activity 
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
							R.string.toastIllegalImage, Toast.LENGTH_LONG)
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
		pShareBtn=(Button)findViewById(R.id.pShareBtn);

		// add event listener
		pEditBtn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setEditable();
			}
		});
		pSaveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onSave();
			}
		});
		pShareBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onShare();
			}
		});

		// if this Activity for add new product
		if(pid == 0 ){
			if(camera)
				takePic();
			else
				pickPic();
			eraseData();
		}
		// if this Activity for update or view product
		else{
			p=sh.getProduct(pid);
			shareable = true;

			// display image of this product
			imgFilePathsList = p.getImagePathsList();
			imgListAdapter.notifyDataSetChanged();

			// fill data of this product
			fillData();

			pEditBtn.setEnabled(true);
		}
	}

	private void onSave() {
		//check values of pNameTxt and pPriceTxt
		if(pNameTxt.getText().length() > 0 &&
				pPriceTxt.getText().length() > 0){
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
				shareable = true;
				updateUI();

				// update imgPath for Content of this Product
				Content content = sh.getContent(cid);
				content.setImgPath( imgFilePathsList.get(0) );
				sh.updateContent(content);
			}else
				//update into database
				sh.updateProduct(p);

			Toast.makeText(getApplicationContext(),
					R.string.toastSaveProduct, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(),
					R.string.toastIllegalValues, Toast.LENGTH_LONG).show();
		}

	}

	private void onShare() {
		onSave();
		performPublish(PendingAction.POST_PHOTO);
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
		// event when user want to be helped
		// this code create a help dialog in ProductsList
		case R.id.helpDetail: {
			AlertDialog alertDialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(R.string.help);
			final FrameLayout frameView = new FrameLayout(this);
			builder.setView(frameView);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			alertDialog = builder.create();
			LayoutInflater inflater = alertDialog.getLayoutInflater();
			View dialoglayout = inflater.inflate(R.layout.detail_helper, frameView);
			alertDialog.show();			
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
		uiHelper.onActivityResult(requestCode, resultCode, data);
		// result from camera intent
		if(requestCode == TAKE_PIC && resultCode==RESULT_OK && null != data) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");

			// store image has been taken by camera intent
			try{
				long idPath = nextId==0 ? pid : nextId;
				// image filename pattern : [product+PID+'order in ImgPathsList'.jpg] 
				imgFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() 
						+"/ecatPics/product"+idPath+imgFilePathsList.size()+".jpg";
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
			// after check duplicated images
			if (!imgFilePathsList.contains(imgFilePath)) {
				imgFilePathsList.add(imgFilePath);
				imgListAdapter.notifyDataSetChanged();
				
				// if this products not new, update imagePaths table (database)
				if(pid>0){
					sh.addImagePath(pid, imgFilePath);
				}
			}else{
				Toast.makeText(getApplicationContext(),
						R.string.toastDuplicateImage, Toast.LENGTH_LONG).show();
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

	private void eraseData() {
		setEditable();

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

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean logedIn = (session != null && session.isOpened());

		pShareBtn.setEnabled(logedIn && shareable);
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null && session.getPermissions().contains("publish_actions");
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE &&
				(exception instanceof FacebookOperationCanceledException ||
						exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(DetailViewActivity.this)
			.setTitle(R.string.cancelled)
			.setMessage(R.string.permission_not_granted)
			.setPositiveButton("OK", null)
			.show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}
		updateUI();
	}

	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		// These actions may re-set pendingAction if they are still pending, but we assume they
		// will succeed.
		pendingAction = PendingAction.NONE;

		switch (previouslyPendingAction) {
		case POST_PHOTO:
			postPhoto();
			break;
		}
	}

	private String toMessage() {
		return "ID: "+p.getPid()+"\n"
				+getString(R.string.pName)+" "+p.getName()+"\n"
				+getString(R.string.pPrice)+" "+p.getPrice()+"\n"
				+getString(R.string.pDetail)+" "+p.getDetail()+"\n";
	}

	private void postPhoto() {
		if (hasPublishPermission()) {
			String imgPath="";
			File imageFile;
			Bitmap bitmap;
			for (int i = 0; i < imgFilePathsList.size(); i++) {
				try {
					// get Image 
					imgPath = imgFilePathsList.get(i);
					imageFile = new File(imgPath);
					bitmap = BitmapFactory.decodeFile(imageFile
							.getAbsolutePath());
					// construct a request
					Request request = Request.newUploadPhotoRequest(
							Session.getActiveSession(), bitmap,
							new Request.Callback() {
								@Override
								public void onCompleted(Response response) {
									showPublishResult(
											getString(R.string.photo_post),
											response.getGraphObject(),
											response.getError());
								}
							});
					// put information of this product to message
					Bundle params = request.getParameters();
					params.putString("message", toMessage());
					// specify privacy setting to EVERYONE
					JSONObject privacy = new JSONObject();
					try {
						privacy.put("value", "EVERYONE");
					} catch (JSONException e) {
					}
					params.putString("privacy", privacy.toString());
					// okay, post now
					request.executeAsync();
					
				} catch (OutOfMemoryError e) {
					new AlertDialog.Builder(this)
					.setTitle(getString(R.string.error))
					.setMessage(e.getMessage())
					.setPositiveButton("OK", null)
					.show();
				}
			}
		} else {
			pendingAction = PendingAction.POST_PHOTO;
		}
	}

	private void performPublish(PendingAction action) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction();
			} else {
				// We need to get new permissions, then complete the action when we get called back.
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS)
				.setDefaultAudience(SessionDefaultAudience.EVERYONE);
				session.requestNewPublishPermissions(newPermissionsRequest);
			}
		}
	}

	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}

	private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
		String title = null;
		String alertMessage = null;
		if (error == null) {
			title = getString(R.string.success);
			String id = result.cast(GraphObjectWithId.class).getId();
			alertMessage = getString(R.string.successfully_posted_post, message, id);
		} else {
			title = getString(R.string.error);
			alertMessage = error.getErrorMessage();
		}

		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(alertMessage)
		.setPositiveButton("OK", null)
		.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
		updateUI();
	}

	@Override
	protected void onPause() {
		super.onPause();
		uiHelper.onPause();
		sh.close();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
		sh.close();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

}

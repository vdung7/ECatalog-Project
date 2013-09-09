package view;

import java.io.File;
import java.util.ArrayList;

import model.Product;
import model.ProductsListAdapter;
import model.SQLiteHelper;

import com.example.ecatalog.R;
import android.os.Bundle;
import android.os.Environment;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

public class ProductsListActivity extends ListActivity {
	private ArrayList<Product> productList = new ArrayList<Product>();
	private ProductsListAdapter productListAdapter;

	private int cid = 0;
	private EditText fromTxt ;
	private EditText toTxt ;
	private SQLiteHelper sh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_products_list);

		//make picture folder if it not exists;
		makePictureDirectory();

		//take signal from MainActivity
		cid = getIntent().getExtras().getInt("cid");



		// make ActionBar in bottom and top
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.filter);

		fromTxt = (EditText)findViewById(R.id.from);
		toTxt = (EditText)findViewById(R.id.to);

		// button in ActionBar (top)
		ImageButton filterBtn = (ImageButton)findViewById(R.id.filterBtn);
		Button allBtn = (Button)findViewById(R.id.allBtn);

		// view when from-to filter
		filterBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					int from = Integer.parseInt(fromTxt.getText().toString());
					int to = Integer.parseInt(toTxt.getText().toString());
					productList = sh.filterProducts(from, to, cid);	
					onFilter(productList);
				} catch (NumberFormatException e) {}
			}
		});

		// view all products in current content
		allBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toTxt.setText("");
				fromTxt.setText("");
				productList =  sh.getProductsByContent(cid);
				onFilter(productList);
			}
		});
	}

	private void onFilter(ArrayList<Product> list) {
		productListAdapter = new ProductsListAdapter(ProductsListActivity.this, getApplicationContext(),
				R.layout.list_row, list);
		setListAdapter(productListAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.browser:
		case R.id.camera: {
			// determine next ID for new Product
			SQLiteHelper sh = new SQLiteHelper(this);
			long nextId = sh.maxIdProducts()+1;

			// open DetailViewActivity with "adding" signal
			Intent screen2intent = new Intent(ProductsListActivity.this, DetailViewActivity.class);
			screen2intent.putExtra("cid", cid);
			screen2intent.putExtra("pid", 0);
			screen2intent.putExtra("nextId", nextId);
			screen2intent.putExtra("camera", item.getItemId()==R.id.camera);

			startActivity(screen2intent);

			break;
		}
		case R.id.move: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle(getString(R.string.findByID));
			final EditText input = new EditText(this);
			input.setHint("ID");
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			builder.setView(input);

			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					try {
						int id = Integer.parseInt(input.getText().toString());
						onResume();
						((ProductsListAdapter)getListAdapter()).moveTo(id);
					} catch (NumberFormatException e) {
						Toast.makeText(getApplicationContext(),
								getString(R.string.illegalValue), Toast.LENGTH_LONG).show();
					}
				}
			});

			builder.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.show();
			break;
		}
		// event when user want to be helped
		// this code create a help dialog in ProductsList
		case R.id.helpProduct: {
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
			View dialoglayout = inflater.inflate(R.layout.product_helper, frameView);
			alertDialog.show();			
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean makePictureDirectory(){
		File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+
				"/ecatPics");
		if(!folder.exists())
			if(folder.mkdir())
				return true;
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		//connect activity with database and adapter
		sh = new SQLiteHelper(this);
		productList = sh.getProductsByContent(cid);
		productListAdapter = new ProductsListAdapter(ProductsListActivity.this, getApplicationContext(),
				R.layout.list_row, productList);
		setListAdapter(productListAdapter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		sh.close();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		sh.close();
	}

}

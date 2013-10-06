package model;

import java.io.File;
import java.util.ArrayList;

import view.DetailViewActivity;
import view.ProductsListActivity;

import cyber.app.ecatalog.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProductsListAdapter extends ArrayAdapter<String>{
	private LayoutInflater mInflater;
	private int mViewResId;
	private ArrayList<Product> productsList;
	private ProductsListActivity mainAct; 
	
	public ProductsListAdapter(ProductsListActivity act, Context context, int viewResourceId,
			ArrayList<Product> products) {
		super(context, viewResourceId);
		
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainAct = act;
		mViewResId = viewResourceId;

		this.productsList = products;
		
	}
	
	@Override
	public int getCount() {
		return productsList.size();
	}
	
	@Override
	public String getItem(int position) {
		return productsList.get(position).getName();
	}
	
	@Override
	public long getItemId(int position) {
		return productsList.get(position).getPid();
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		convertView = mInflater.inflate(mViewResId, null);
		
		ImageView iv = (ImageView) convertView.findViewById(R.id.imgProduct);
		
		File imageFile = 
				new File(productsList.get(position).getImagePathsList().get(0));
		Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
		iv.setImageBitmap(bitmap);
		
		iv.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent(mainAct, DetailViewActivity.class);
				mIntent.putExtra("pid", productsList.get(position).getPid());
				mIntent.putExtra("cid", productsList.get(position).getCid());
				mainAct.startActivity(mIntent);
			}
		});
		
		iv.setOnLongClickListener( new View.OnLongClickListener() {
			AlertDialog alertDialog;
			@Override
			public boolean onLongClick(View arg0) {
				alertDialog = new AlertDialog.Builder(mainAct)
				.setTitle(R.string.titleDeleteDialog)
				.setMessage(R.string.messageDeleteProductDialog)
				.setPositiveButton("OK", new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						SQLiteHelper sh = new SQLiteHelper(mainAct);
						sh.removeProduct(getItemId(position));
						productsList.remove(position);
						ProductsListAdapter.this.notifyDataSetChanged();
					}
				})
				.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
						alertDialog.cancel();
					}
				})
				.create();
				alertDialog.show();
				return false;
			}
		});
		
		ImageView ivHot = (ImageView) convertView.findViewById(R.id.isHotView);
		if(productsList.get(position).isHot())
			ivHot.setImageResource(R.drawable.rating_important);
		
		TextView tv = (TextView) convertView.findViewById(R.id.textProduct);
		tv.setText( "ID: "+productsList.get(position).getPid()+
					"\n"+
					mainAct.getString(R.string.pName)+" "+productsList.get(position).getName()+
					"\n"+
					mainAct.getString(R.string.pPrice)+" "+productsList.get(position).getPrice());
		
		
		return convertView;
	}

	public void moveTo(int pid){
		for(int i=0; i<productsList.size(); i++)
			if(productsList.get(i).getPid() == pid){
				mainAct.getListView().setSelection(i );
				return;
			}
		Toast.makeText(mainAct, mainAct.getString(R.string.pNotFound), Toast.LENGTH_LONG).show();		
	}
}

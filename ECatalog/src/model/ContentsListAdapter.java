package model;

import java.io.File;
import java.util.ArrayList;

import view.DetailViewActivity;
import view.MainActivity;
import view.ProductsListActivity;

import cyber.app.ecatalog.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContentsListAdapter extends ArrayAdapter<String>{

	private LayoutInflater mInflater;
	private int mViewResId;
	private ArrayList<Content> contentsList;
	private MainActivity mainAct; 
	
	public ContentsListAdapter(MainActivity act, Context context, int viewResourceId,
			ArrayList<Content> contents) {
		super(context, viewResourceId);
		
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainAct = act;
		mViewResId = viewResourceId;

		this.contentsList = contents;
		
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return contentsList.size();
	}
	
	@Override
	public String getItem(int position) {
		return contentsList.get(position).getName();
	}
	
	@Override
	public long getItemId(int position) {
		return contentsList.get(position).getCid();
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		convertView = mInflater.inflate(mViewResId, null);
		
		ImageView iv = (ImageView) convertView.findViewById(R.id.imgContent);
		
		String imgPath = contentsList.get(position).getImgPath();
		if(imgPath == null || imgPath.equals("")){
			iv.setImageResource(R.drawable.empty);
		}else{
			File imageFile = new File(imgPath);
			Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
			iv.setImageBitmap(bitmap);
		}
		
		iv.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent(mainAct, ProductsListActivity.class);
				mIntent.putExtra("cid", contentsList.get(position).getCid());
				System.out.println("--- "+contentsList.get(position).getCid());
				mainAct.startActivity(mIntent);
			}
		});
		
		iv.setOnLongClickListener( new View.OnLongClickListener() {
			AlertDialog alertDialog;
			@Override
			public boolean onLongClick(View arg0) {
				alertDialog = new AlertDialog.Builder(mainAct)
				.setTitle(R.string.titleDeleteDialog)
				.setMessage(R.string.messageDeleteContentDialog)
				.setPositiveButton("OK", new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						SQLiteHelper sh = new SQLiteHelper(mainAct);
						sh.removeContent(getItemId(position));
						contentsList.remove(position);
						ContentsListAdapter.this.notifyDataSetChanged();
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
		
		TextView tv = (TextView) convertView.findViewById(R.id.txtContent);
		tv.setText(contentsList.get(position).getName());
		
		return convertView;
	}

	
}

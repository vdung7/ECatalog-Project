package view;

import java.io.File;

import model.Content;
import model.ContentsListAdapter;
import model.ProductsListAdapter;
import model.SQLiteHelper;

import com.example.ecatalog.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends Activity{
	private GridView gridContent;
	private String new_content = "";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contents_list);
		
		//connect activity with database and adapter
		SQLiteHelper sh = new SQLiteHelper(this);
		gridContent = (GridView) findViewById(R.id.ContentsGridView);
		gridContent.setAdapter(new ContentsListAdapter(MainActivity.this, 
				getApplicationContext(), R.layout.grid_cell, sh.getAllContents()));
	}

	@Override
	public void onResume() {
		super.onResume();
		//connect activity with database and adapter
		SQLiteHelper sh = new SQLiteHelper(this);
		gridContent.setAdapter(new ContentsListAdapter(MainActivity.this, 
		getApplicationContext(), R.layout.grid_cell, sh.getAllContents()));
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.content_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.addContent: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle("Input new Content");
				final EditText input = new EditText(this);
				input.setHint("Name of new Content");
				input.setInputType(InputType.TYPE_CLASS_TEXT);
				builder.setView(input);
				
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new_content = input.getText().toString();
						if(!new_content.equals("")){
							SQLiteHelper sh = new SQLiteHelper(getApplicationContext());
							sh.addContent(new Content("", new_content));
							Toast.makeText(getApplicationContext(), 
									"New Content has been created", Toast.LENGTH_LONG).show();
							onResume();
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
		}
		this.onResume();
		return super.onOptionsItemSelected(item);
	}
	
}

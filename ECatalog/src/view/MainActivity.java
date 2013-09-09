package view;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Content;
import model.ContentsListAdapter;
import model.SQLiteHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.apperhand.device.android.AndroidSDKProvider;
import com.example.ecatalog.R;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
public class MainActivity extends Activity{
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");

	private GridView gridContent;
	private TextView greeting; 
	private LoginButton loginBtn;
	private ContentsListAdapter contentListAdapter;
	private ArrayList<Content> contentList = new ArrayList<Content>();
	private String new_content = "";
	private GraphUser user;
	private SQLiteHelper sh;
	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contents_list);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		AndroidSDKProvider.initSDK(this);

		gridContent = (GridView) findViewById(R.id.ContentsGridView);
		greeting = (TextView) findViewById(R.id.txtWelcome);
		loginBtn = (LoginButton) findViewById(R.id.login_button);

		loginBtn.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
				MainActivity.this.user = user;
				updateUI();
			}
		});
		loginBtn.setPublishPermissions(PERMISSIONS);
		loginBtn.setDefaultAudience(SessionDefaultAudience.EVERYONE);

		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);

		// Add code to print out the key hash
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"com.example.ecatalog", 
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			System.out.println("NameNotFoundException");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
		updateUI();
		///connect activity with database and adapter
		sh = new SQLiteHelper(this);

		contentList =  sh.getAllContents();
		contentListAdapter = new ContentsListAdapter(MainActivity.this, 
				getApplicationContext(), R.layout.grid_cell, contentList);
		gridContent.setAdapter(contentListAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.content_menu, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog alertDialog;
		switch(item.getItemId()){
		// event : user want to add 1 more content
		case R.id.addContent: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle(getString(R.string.addContentDiaTitle));
			final EditText input = new EditText(this);
			input.setHint(getString(R.string.hintNewContent));
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			builder.setView(input);

			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					new_content = input.getText().toString();
					if(!new_content.equals("")){
						sh = new SQLiteHelper(getApplicationContext());

						Content newContent = new Content("", new_content); 
						sh.addContent(newContent);
						contentList.add(newContent);
						contentListAdapter.notifyDataSetChanged();

						Toast.makeText(getApplicationContext(), 
								getString(R.string.addContentSuccess), Toast.LENGTH_LONG).show();
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
		// event when user want to be helped
		// this code create a help dialog for adding a New Content
		case R.id.helpContent:
		case R.id.aboutDev:   {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(item.getItemId()==R.id.helpContent?R.string.help:R.string.about);
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
			View dialoglayout;
			if(item.getItemId() == R.id.helpContent)
				dialoglayout = inflater.inflate(R.layout.content_helper, frameView);
			else
				dialoglayout = inflater.inflate(R.layout.about_message, frameView);
			alertDialog.show();			
			break;
		}
		}
		this.onResume();
		return super.onOptionsItemSelected(item);
	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean logedIn = (session != null && session.isOpened());

		if (logedIn && user != null) {
			greeting.setText(getString(R.string.hello_user)+" "+ user.getName());
		} else {
			greeting.setText(null);
		}
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		updateUI();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
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
}


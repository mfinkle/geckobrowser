package com.starkravingfinkle.geckobrowser;

import org.mozilla.gecko.GeckoView;
import org.mozilla.gecko.GeckoView.Browser;
import org.mozilla.gecko.GeckoViewChrome;
import org.mozilla.gecko.GeckoViewContent;
import org.mozilla.gecko.PrefsHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;

public class MainActivity extends Activity {
	private static final String LOGTAG = "GeckoBrowser";

    GeckoView mGeckoView;
    TextView mPageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGeckoView = (GeckoView) findViewById(R.id.gecko_view);

        Button goButton = (Button) findViewById(R.id.go_button);
        goButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText text = (EditText) findViewById(R.id.url_bar);

                GeckoView.Browser browser = mGeckoView.getSelected();
                if (browser == null) {
                    browser = mGeckoView.add(text.getText().toString());
                } else {
                    browser.loadUrl(text.getText().toString());
                }
            }
        });
        
        mGeckoView.setChromeCallback(new MyGeckoViewChrome());
        mGeckoView.setContentCallback(new MyGeckoViewContent());

        mPageTitle = (TextView) findViewById(R.id.page_title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
    	Browser selected = mGeckoView.getSelected();
    	if (selected != null && selected.canGoBack()) {
    	    selected.goBack();
    	} else {
    	    moveTaskToBack(true);
    	}
    }

    private class MyGeckoViewChrome extends GeckoViewChrome {
        @Override
        public void onReady(GeckoView view) {
            Log.i(LOGTAG, "Gecko is ready");

            PrefsHelper.setPref("devtools.debugger.remote-enabled", true);

            // The Gecko libraries have finished loading and we can use the rendering engine.
            // Let's add a browser (required) and load a page into it.
            mGeckoView.add("http://starkravingfinkle.org");
        }

        @Override
        public void onAlert(GeckoView view, GeckoView.Browser browser, String message, GeckoView.PromptResult result) {
            Log.i(LOGTAG, "Alert!");
            result.confirm();
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onConfirm(GeckoView view, GeckoView.Browser browser, String message, final GeckoView.PromptResult result) {
            Log.i(LOGTAG, "Confirm!");
	        new AlertDialog.Builder(MainActivity.this)
	        .setTitle("javaScript dialog")
	        .setMessage(message)
	        .setPositiveButton(android.R.string.ok,
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                        result.confirm();
	                    }
	                })
	        .setNegativeButton(android.R.string.cancel,
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                        result.cancel();
	                    }
	                })
	        .create()
	        .show();
        }

        @Override
        public void onDebugRequest(GeckoView view, GeckoView.PromptResult result) {
            Log.i(LOGTAG, "Remote Debug!");
            result.confirm();
        }
    }

    private class MyGeckoViewContent extends GeckoViewContent {
        @Override
        public void onReceivedTitle(GeckoView view, GeckoView.Browser browser, String title) {
            Log.i(LOGTAG, "Received a title");
			
            // Use the title returned from Gecko to update the UI
            // TODO: Only if the browser is the selected browser
            mPageTitle.setText(title);
        }
    }
}

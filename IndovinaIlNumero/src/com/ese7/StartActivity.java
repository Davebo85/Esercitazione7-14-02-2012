package com.ese7;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        
        final EditText user = (EditText) findViewById(R.id.editText1);
        final EditText oppo = (EditText) findViewById(R.id.editText2);
        
        Button gioca = (Button) findViewById(R.id.button1);
        gioca.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
			Intent intent = new Intent(StartActivity.this, Main.class);
			String username = user.getText().toString();
			String opponent = oppo.getText().toString();
			
			intent.putExtra("username", username);
			intent.putExtra("opponent", opponent);
			startActivity(intent);
			}
		});
    }
}
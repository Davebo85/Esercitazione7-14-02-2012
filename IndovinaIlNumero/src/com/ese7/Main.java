package com.ese7;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements MesssageReceiver {
	private enum Stato {
		WAIT_FOR_START, WAIT_FOR_START_ACK, WAIT_FOR_SELECT, WAIT_FOR_BET, WAIT_FOR_NUMBER_SELECTION, USER_SELECTING, USER_BETTING
	}

	protected static final int SHOW_TOAST = 0;;
	private String selectedNumber;
	private String TAG = "ESERCITAZIONE";
	Stato statoCorrente;
	ConnectionManager connection;

	final Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Main.SHOW_TOAST:
				Toast.makeText(Main.this, msg.getData().getString("toast"),
						Toast.LENGTH_LONG).show();
				break;
			default:
				super.handleMessage(msg);
				break;
			}

		};
	};

	Timer timer = new Timer();
	TimerTask sendStart = new TimerTask() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (statoCorrente == Stato.WAIT_FOR_START_ACK) {
				connection.send("START");
			} else {
				Log.d(TAG, "Sending START but the state is " + statoCorrente);
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView player1 = (TextView) findViewById(R.id.textView1);
		TextView player2 = (TextView) findViewById(R.id.textView2);

		String nomeProprio = getIntent().getExtras().getString("username");
		String nomeAvversario = getIntent().getExtras().getString("opponent");
		player1.setText(nomeProprio);
		player2.setText(nomeAvversario);

		Button b1 = (Button) findViewById(R.id.button1);
		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				numberSelected(v);
			}
		});
		Button b2 = (Button) findViewById(R.id.button2);
		b2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				numberSelected(v);
			}
		});
		Button b3 = (Button) findViewById(R.id.button3);
		b3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				numberSelected(v);
			}
		});

		statoCorrente = Stato.WAIT_FOR_START;

		connection = new ConnectionManager(nomeProprio, nomeAvversario, this);

		if (nomeAvversario.hashCode() < nomeProprio.hashCode()) {
			// Inizio io
			timer.schedule(sendStart, 1000, 5000);
			statoCorrente = Stato.WAIT_FOR_START_ACK;
		} else {
			// Inizia avversario
			statoCorrente = Stato.WAIT_FOR_START;
		}
	}

	@Override
	public void receiveMessage(String msg) {
		// TODO Auto-generated method stub
		String body = msg;
		if (body.equals("START")) {
			// Mando l'ack indietro
			connection.send("STARTACK");
			Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
			Bundle b = new Bundle();
			b.putString("toast", "Scegli un numero");
			osmsg.setData(b);
			handler.sendMessage(osmsg);
			statoCorrente = Stato.USER_SELECTING;
		} else if (body.equals("STARTACK")) {
			if (statoCorrente == Stato.WAIT_FOR_START_ACK) {
				statoCorrente = Stato.WAIT_FOR_NUMBER_SELECTION;
			} else {
				Log.e(TAG, "Ricevuto STARTACK ma lo stato è " + statoCorrente);
			}
		} else if (body.startsWith("SELECTED")) {
			if (statoCorrente == Stato.WAIT_FOR_NUMBER_SELECTION) {
				selectedNumber = body.split(":")[1];
				Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				b.putString("toast", "Indovina il numero");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente = Stato.USER_BETTING;
			} else {
				Log.e(TAG, "Ricevuto SELECTED ma lo stato è " + statoCorrente);
			}
		} else if (body.startsWith("BET")) {
			if (statoCorrente == Stato.WAIT_FOR_BET) {
				String result = body.split(":")[1];
				Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				if (result.equals("Y"))
					b.putString("toast",
							"Hai perso, il tuo avversario ha indovinato");
				else
					b.putString("toast",
							"hai vinto, il tuo avversario ha sbagliato");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente = Stato.WAIT_FOR_NUMBER_SELECTION;
			} else {
				Log.e(TAG, "Ricevuto SELECTED ma lo stato è " + statoCorrente);
			}
		} else {
			Log.e(TAG, "Ricevuto START ma lo stato è " + statoCorrente);
		}
	}

	public void numberSelected(View v) {
		Button b = (Button) v;
		String choice = b.getText().toString();
		if (statoCorrente == Stato.USER_SELECTING) {
			connection.send("SELECTED:" + choice);
			statoCorrente = Stato.WAIT_FOR_BET;
		} else if (statoCorrente == Stato.USER_BETTING) {
			String bet = b.getText().toString();
			
			if (bet.equals(selectedNumber)) {
				connection.send("BET:Y" );
				Toast.makeText(Main.this,
						"Bravo hai indovinato, ora tocca a te",
						Toast.LENGTH_LONG).show();
			} else {
				connection.send("BET:N" );
				Toast.makeText(Main.this,
						"Peccato non hai indovinato, ora tocca a te",
						Toast.LENGTH_LONG).show();
			}
			statoCorrente = Stato.USER_SELECTING;
		}
	}
}

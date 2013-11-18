package com.leybzon.glassheart;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

/**
 * @author gleybzon
 *
 */
public class MainActivity extends Activity {

	private boolean isSimulation = false;
	private static Timer timerSimualtion;

	ImageView viewHeart = null;
	Animation anumationPulse = null;
	TextView heartRate = null;

    //TODO: replace with your PubNub keys!
	Pubnub pubnub = new Pubnub("pub-replace-with-your-id",
			"sub-replace-with-your-id",
			"sec-replace-with-your-id", false);

	public static final String channelData = "gene";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (isSimulation) {
			timerSimualtion = new Timer();
			timerSimualtion.scheduleAtFixedRate(new simulateTask(), 0, 1000); // 1 [sec]
		}

		viewHeart = (ImageView) findViewById(R.id.imageHeart);
		anumationPulse = AnimationUtils.loadAnimation(this, R.anim.pulse);

		anumationPulse.setRepeatCount(1);
		viewHeart.startAnimation(anumationPulse);

		heartRate = (TextView) findViewById(R.id.heartRate);

		this.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				pubnub.disconnectAndResubscribe();
			}
		}, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		subscribe(channelData);

	}

	private void subscribe(String channel) {
		Hashtable<String, String> args = new Hashtable<String, String>(1);

		args.put("channel", channel);

		try {
			pubnub.subscribe(args, new Callback() {
				@Override
				public void successCallback(String channel, Object message) {
					Log.w("PubNub", channel + " " + message.toString());
					showBeatRate(message.toString());
				}

				@Override
				public void errorCallback(String channel, Object message) {
					Log.e("PubNub", channel + " " + message.toString());
				}
			});
		} catch (PubnubException e) {
			Log.e("PubNub", e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	public void showBeatRate(final String rate) {
		Log.i(this.toString(), "Heart Rate: " + rate);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (heartRate == null || viewHeart == null) {
					Log.e(this.toString(), "Can not show the beat rate");
					return;
				}
				
				try {
					if (Integer.parseInt(rate) > 0) {
						heartRate.setText(rate);
						viewHeart.startAnimation(anumationPulse);
						heartRate.startAnimation(anumationPulse);
					} else {
						heartRate.setText("?");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class simulateTask extends TimerTask {
		@Override
		public void run() {
			showBeatRate(String.valueOf(Math.round(Math.random()*20+60)));
		}
	}

}

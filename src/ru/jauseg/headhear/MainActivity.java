package ru.jauseg.headhear;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnBufferReadyListener
{
	private AudioStreamReader audioStreamReader;
	private AudioStreamPlayer audioStreamPlayer;

	private int scale = 1;

	private Handler bufferReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == 2839)
			{
				onBufferReady((short[]) msg.obj);
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		audioStreamReader = new AudioStreamReader(bufferReadyHandler);
		audioStreamPlayer = new AudioStreamPlayer();

		setContentView(R.layout.activity_main);
		
		findViewById(R.id.button_dec).setOnClickListener(onClickListener);
		findViewById(R.id.button_inc).setOnClickListener(onClickListener);

		updateInfo();
	}

	@Override
	protected void onResume()
	{
		audioStreamReader.start();
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		audioStreamReader.stop();
		bufferReadyHandler.removeMessages(2839);
		audioStreamPlayer.stop();
		super.onPause();
	}

	private static short calculateAverage(short[] data)
	{
		short average = 0;
		long sum = 0;
		int bufferSize = data.length;

		int min = data[0];
		int max = data[0];

		for (int i = 0; i < bufferSize; i++)
		{
			short value = data[i];
			sum += value > 0 ? +value : -value;

			min = value < min ? value : min;
			max = value > max ? value : max;
		}

		average = (short) (sum / bufferSize);

		// Log.v("hh", String.format("hh avg = %d, min = %d, max = %d", average,
		// min, max));

		return average;
	}

	private static void scale(int value, short[] data)
	{
		int length = data.length;

		for (int i = 0; i < length; i++)
		{
			// data[i] = (short) ((((int) data[i]) * value) / 256);
			data[i] = (short) (value * data[i]);
		}
	}

	@Override
	public void onBufferReady(short[] data)
	{
		// Log.v("hh", "onBufferReady: " + data);
		scale(scale, data);
		audioStreamPlayer.play(data);

		// int t = calculateAverage(data);
	}

	private void updateInfo()
	{
		TextView textInfoScale = (TextView) findViewById(R.id.text_scale);
		textInfoScale.setText(String.format("Scale = %d", scale));
	}

	private OnClickListener onClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.button_dec:
				if (scale > 1)
				{
					scale--;
				}
				break;

			case R.id.button_inc:
				if (scale < 32)
				{
					scale++;
				}
				break;
			}
			updateInfo();
		}
	};
}

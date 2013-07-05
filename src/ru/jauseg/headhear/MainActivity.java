package ru.jauseg.headhear;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class MainActivity extends Activity implements OnBufferReadyListener
{
	private Random rnd;

	private AudioStreamReader audioStreamReader;
	private AudioStreamPlayer audioStreamPlayer;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		rnd = new Random(SystemClock.elapsedRealtime());
		audioStreamReader = new AudioStreamReader(this);
		audioStreamPlayer = new AudioStreamPlayer();

		setContentView(R.layout.activity_main);

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
		audioStreamPlayer.stop();
		super.onPause();
	}

	private static short calculateAverage(short[] data)
	{
		short average = 0;
		long sum = 0;
		int bufferSize = data.length;

		for (int i = 0; i < bufferSize; i++)
		{
			short value = data[i];
			sum += value > 0 ? +value : -value;
		}

		average = (short) (sum / bufferSize);

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

	int count = 0;

	@Override
	public void onBufferReady(short[] data)
	{
		scale(64, data);
		audioStreamPlayer.play(data);

		if (count > 5)
		{
			finish();
		}

	//	count++;
		// int t = calculateAverage(data);
		// Log.v("hh", String.format("hh avg = %d", calculateAverage(data)));
	}
}

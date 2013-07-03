package ru.jauseg.headhear;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		startListenToMicrophone();
		super.onResume();
	}

	@Override
	protected void onPause() {
		stopListenToMicrophone();
		super.onPause();
	}

	private Thread recordingThread;
	private int bufferSize = 1024 * 4;
	private short[][] buffers = new short[16][bufferSize];
	private int[] averages = new int[16];
	private int lastBuffer = 0;

	private AudioRecord recorder;
	private boolean recorderStarted = false;

	protected void startListenToMicrophone() {
		if (!recorderStarted) {

			recordingThread = new Thread() {
				@Override
				public void run() {
					int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					recorder = new AudioRecord(AudioSource.MIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 10);
					recorder.setPositionNotificationPeriod(bufferSize);
					recorder.setRecordPositionUpdateListener(new OnRecordPositionUpdateListener() {
						@Override
						public void onPeriodicNotification(AudioRecord recorder) {

							short[] buffer = buffers[++lastBuffer % buffers.length];
							recorder.read(buffer, 0, bufferSize);
							
							// Log.v("hh", "onPeriodicNotification");

							// long sum = 0;
							// for (int i = 0; i < bufferSize; ++i) {
							// sum += Math.abs(buffer[i]);
							// }
							 averages[lastBuffer % buffers.length] = calculateAverage(buffer);
							lastBuffer = lastBuffer % buffers.length;
							Log.v("hh", "onPeriodicNotification = " + averages[lastBuffer % buffers.length]);

						}

						@Override
						public void onMarkerReached(AudioRecord recorder) {
							Log.v("hh", "onMarkerReached");
						}
					});
					recorder.startRecording();
					short[] buffer = buffers[lastBuffer % buffers.length];
					recorder.read(buffer, 0, bufferSize);

					while (true) {
						if (isInterrupted()) {
							recorder.stop();
							recorder.release();
							break;
						}
						SystemClock.sleep(333);
					}
				}
			};
			recordingThread.start();

			recorderStarted = true;
		}
	}

	private static short calculateAverage(short[] data) {

		short average = 0;
		long sum = 0;
		int bufferSize = data.length;

		for (int i = 0; i < bufferSize; i++) {
			short value = data[i];
			sum += value > 0 ? +value : -value;
		}

		average = (short) (sum / bufferSize);

		return average;
	}

	private void stopListenToMicrophone() {
		if (recorderStarted) {
			if (recordingThread != null && recordingThread.isAlive() && !recordingThread.isInterrupted()) {
				recordingThread.interrupt();
			}
			recorderStarted = false;
		}
	}
}

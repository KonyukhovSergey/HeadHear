package ru.jauseg.headhear;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class AudioStreamReader
{
	protected static final String TAG = "AudioStreamReader";

	private Thread recordingThread;

	private short[][] buffers;
	private int currentBufferIndex;
	// private short[] prevBuffer;

	private AudioRecord recorder;
	private boolean recorderStarted = false;

	private int sampleRateInHz;

	private Handler bufferReadyHandler;

	private void init(int buffersCount, int bufferSize)
	{
		buffers = new short[buffersCount][bufferSize];
		currentBufferIndex = 0;
		recorderStarted = false;
	}

	public AudioStreamReader(int buffersCount, int bufferSize, int sampleRateInHz, Handler bufferReadyHandler)
	{
		this.sampleRateInHz = sampleRateInHz;
		this.bufferReadyHandler = bufferReadyHandler;
		init(buffersCount, bufferSize);
	}

	public AudioStreamReader(Handler bufferReadyHandler)
	{
		this(AudioConfig.BUFFERS_COUNT, AudioConfig.BUFFER_SIZE, AudioConfig.SAMPLE_RATE_IN_HZ, bufferReadyHandler);
	}

	public void start()
	{
		if (!recorderStarted)
		{
			recordingThread = new Thread()
			{
				@Override
				public void run()
				{
					recorder = new AudioRecord(AudioSource.MIC, sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT, AudioConfig.BUFFER_SIZE * AudioConfig.BUFFERS_COUNT * 2);

					recorder.startRecording();

					short[] buffer = buffers[currentBufferIndex];

					while (!isInterrupted())
					{
						recorder.read(buffer, 0, buffer.length);

						// Log.v(TAG, "read ready");

						if (bufferReadyHandler != null)
						{
							bufferReadyHandler.sendMessage(bufferReadyHandler.obtainMessage(2839, buffer));
							// Log.v(TAG, "message send");
						}

						currentBufferIndex = (currentBufferIndex + 1) % buffers.length;
						buffer = buffers[currentBufferIndex];
					}

					recorder.stop();
					recorder.release();
				}
			};

			recordingThread.start();

			recorderStarted = true;
		}
	}

	public void stop()
	{
		if (recorderStarted)
		{
			if (recordingThread != null && recordingThread.isAlive() && !recordingThread.isInterrupted())
			{
				recordingThread.interrupt();
				// Log.v("hh", "interrupt");
			}

			recorderStarted = false;
		}
	}
}

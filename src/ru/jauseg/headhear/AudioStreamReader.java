package ru.jauseg.headhear;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.os.Process;
import android.util.Log;

public class AudioStreamReader
{
	protected static final String TAG = "AudioStreamReader";

	private OnBufferReadyListener onBufferReadyListener;
	private Thread recordingThread;

	private short[][] buffers;
	private int currentBufferIndex;
	private short[] prevBuffer;

	private AudioRecord recorder;
	private boolean recorderStarted = false;

	private int sampleRateInHz;

	private void init(int buffersCount, int bufferSize)
	{
		buffers = new short[buffersCount][bufferSize];
		currentBufferIndex = 0;
		prevBuffer = buffers[0];
		recorderStarted = false;
	}

	public AudioStreamReader(int buffersCount, int bufferSize, int sampleRateInHz,
			OnBufferReadyListener onBufferReadyListener)
	{
		this.sampleRateInHz = sampleRateInHz;
		this.onBufferReadyListener = onBufferReadyListener;
		init(buffersCount, bufferSize);
	}

	public AudioStreamReader(OnBufferReadyListener onBufferReadyListener)
	{
		this(AudioConfig.BUFFERS_COUNT, AudioConfig.BUFFER_SIZE, AudioConfig.SAMPLE_RATE_IN_HZ, onBufferReadyListener);
	}

	public void onBufferReadyListener(OnBufferReadyListener onBufferReadyListener)
	{
		this.onBufferReadyListener = onBufferReadyListener;
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
					int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);

					recorder = new AudioRecord(AudioSource.MIC, sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT, minBufferSize );

					recorder.setPositionNotificationPeriod(buffers[0].length);

					recorder.setRecordPositionUpdateListener(new OnRecordPositionUpdateListener()
					{
						@Override
						public void onPeriodicNotification(AudioRecord recorder)
						{
							if (onBufferReadyListener != null)
							{
								//Log.v(TAG, "onBufferReady");
								onBufferReadyListener.onBufferReady(prevBuffer);
							}
						}

						@Override
						public void onMarkerReached(AudioRecord recorder)
						{
						}
					});

					//Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

					recorder.startRecording();

					short[] buffer = buffers[currentBufferIndex];

					while (!isInterrupted())
					{
						prevBuffer = buffer;
						//Log.v(TAG, "buffer Read Begin index = " + currentBufferIndex);
						recorder.read(buffer, 0, buffer.length);
						//Log.v(TAG, "buffer Read End index = " + currentBufferIndex);
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
				//Log.v("hh", "interrupt");
			}
			recorderStarted = false;
		}
	}

}

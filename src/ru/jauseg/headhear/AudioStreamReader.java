package ru.jauseg.headhear;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.os.Process;

public class AudioStreamReader
{
	private OnBufferReadyListener onBufferReadyListener;
	private Thread recordingThread;

	private short[][] buffers;
	private int lastBufferIndex;
	private short[] prevBuffer;

	private AudioRecord recorder;
	private boolean recorderStarted = false;

	private int sampleRateInHz;
	
	private void init(int buffersCount, int bufferSize)
	{
		buffers = new short[buffersCount][bufferSize];
		lastBufferIndex = 0;
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
		this(1024, 8, 44100, onBufferReadyListener);
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
							AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 10);

					recorder.setPositionNotificationPeriod(buffers[0].length);

					recorder.setRecordPositionUpdateListener(new OnRecordPositionUpdateListener()
					{
						@Override
						public void onPeriodicNotification(AudioRecord recorder)
						{
							if (onBufferReadyListener != null)
							{
								onBufferReadyListener.onBufferReady(prevBuffer);
							}
						}

						@Override
						public void onMarkerReached(AudioRecord recorder)
						{
						}
					});

					Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

					recorder.startRecording();

					short[] buffer = buffers[lastBufferIndex % buffers.length];

					while (!isInterrupted())
					{
						prevBuffer = buffer;
						recorder.read(buffer, 0, buffer.length);
						buffer = buffers[++lastBufferIndex % buffers.length];
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
			}
			recorderStarted = false;
		}
	}

}

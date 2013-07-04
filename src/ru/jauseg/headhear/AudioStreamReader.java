package ru.jauseg.headhear;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;

public class AudioStreamReader
{
	private OnBufferReadyListener onBufferReadyListener;
	private Thread recordingThread;

	private int bufferSize;
	private int buffersCount;
	private short[][] buffers;
	private int lastBufferIndex;
	private short[] prevBuffer;

	private AudioRecord recorder;
	private boolean recorderStarted = false;

	private int sampleRateInHz;

	public AudioStreamReader(int bufferSize, int buffersCount, int sampleRateInHz,
			OnBufferReadyListener onBufferReadyListener)
	{
		this.bufferSize = bufferSize;
		this.buffersCount = buffersCount;
		this.sampleRateInHz = sampleRateInHz;
		this.onBufferReadyListener = onBufferReadyListener;
		init();
	}

	public AudioStreamReader(OnBufferReadyListener onBufferReadyListener)
	{
		this(1024, 16, 44100, onBufferReadyListener);
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
					
					recorder.setPositionNotificationPeriod(bufferSize);
					
					recorder.setRecordPositionUpdateListener(new OnRecordPositionUpdateListener()
					{
						@Override
						public void onPeriodicNotification(AudioRecord recorder)
						{
							if(onBufferReadyListener!=null)
							{
								onBufferReadyListener.onBufferReady(prevBuffer);
							}
						}

						@Override
						public void onMarkerReached(AudioRecord recorder)
						{
						}
					});

					recorder.startRecording();
					
					short[] buffer = buffers[lastBufferIndex % buffers.length];

					while (true)
					{
						prevBuffer = buffer;
						recorder.read(buffer, 0, bufferSize);
						buffer = buffers[++lastBufferIndex % buffers.length];

						if (isInterrupted())
						{
							recorder.stop();
							recorder.release();
							break;
						}
					}
				}
			};
			recordingThread.start();

			recorderStarted = true;
		}
	}

	public void stop()
	{
		if (recorderStarted) {
			if (recordingThread != null && recordingThread.isAlive() && !recordingThread.isInterrupted()) {
				recordingThread.interrupt();
			}
			recorderStarted = false;
		}
	}

	private void init()
	{
		buffers = new short[buffersCount][bufferSize];
		lastBufferIndex = 0;
		prevBuffer = buffers[0];
		recorderStarted = false;
	}
}

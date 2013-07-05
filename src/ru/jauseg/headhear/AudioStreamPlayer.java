package ru.jauseg.headhear;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

public class AudioStreamPlayer
{
	protected static final String TAG = "AudioStreamPlayer";
	private Thread playerThread;
	private boolean isPlay = false;

	private short buffers[][];
	private int playIndex;
	private int addedIndex;

	private AudioTrack audioTrack;
	private int sampleRateInHz;

	private int indexPlay = 0;
	private int indexAdded = 0;
	private int deltaPrev = 0;

	private void startPlayThread()
	{
		if (!isPlay)
		{
			playerThread = new Thread()
			{
				@Override
				public void run()
				{
					int mAudioPlayBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
							AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

					audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
							AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, mAudioPlayBufferSize,
							AudioTrack.MODE_STREAM);

					// Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

					audioTrack.play();

					while (!isInterrupted())
					{
						// Log.v(TAG, "thread: addedIndex = " + addedIndex +
						// " playIndex = " + playIndex);

						short[] buffer = buffers[playIndex];

						if (playIndex != addedIndex)
						{
							audioTrack.write(buffer, 0, buffer.length);
							indexPlay++;
							playIndex = (playIndex + 1) % buffers.length;

						}
						else
						{
							SystemClock.sleep(50);
							Log.v(TAG, "XPEHb = " + playIndex);
						}

						int deltaNew = indexAdded - indexPlay;
						if (deltaNew != deltaPrev)
						{
							deltaPrev = deltaNew;
							Log.v(TAG, "deltaUpdate: delta = " + deltaNew);

						}

					}

					audioTrack.stop();
					isPlay = false;
					Log.v("hh", "interrupted");
				}
			};

			playerThread.start();
			isPlay = true;
		}
	}

	private void init(int buffersCount)
	{
		playIndex = 0;
		addedIndex = 0;
		buffers = new short[buffersCount][];
	}

	public AudioStreamPlayer(int buffersCount, int sampleRateInHz)
	{
		this.sampleRateInHz = sampleRateInHz;
		init(buffersCount);
	}

	public AudioStreamPlayer()
	{
		this(AudioConfig.BUFFERS_COUNT, AudioConfig.SAMPLE_RATE_IN_HZ);
	}

	public void play(short[] buffer)
	{
		// Log.v(TAG, "play: addedIndex = " + addedIndex + " playIndex = " +
		// playIndex);
		buffers[addedIndex] = buffer;
		addedIndex = (addedIndex + 1) % buffers.length;
		indexAdded++;

		startPlayThread();
	}

	public void stop()
	{
		if (isPlay)
		{
			if (playerThread != null && playerThread.isAlive() && !playerThread.isInterrupted())
			{
				Log.v("hh", "interrupting");
				playerThread.interrupt();
			}
			isPlay = false;
		}
	}
}

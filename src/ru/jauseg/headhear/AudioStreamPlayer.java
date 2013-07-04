package ru.jauseg.headhear;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;

public class AudioStreamPlayer
{
	private Thread playerThread;
	private boolean isPlay = false;

	private short buffers[][];
	private int currentBufferIndex;
	private int lastBufferIndex;

	private AudioTrack audioTrack;
	private int sampleRateInHz;

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

					Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

					audioTrack.play();

					while (!isInterrupted())
					{
						audioTrack.write(buffers[currentBufferIndex], 0, buffers[currentBufferIndex].length);
						currentBufferIndex = (currentBufferIndex + 1) % buffers.length;
					}

					audioTrack.stop();
				}
			};

			playerThread.start();
			isPlay = true;
		}
	}

	private void init(int buffersCount)
	{
		currentBufferIndex = 0;
		lastBufferIndex = 0;
		buffers = new short[buffersCount][];
	}

	public AudioStreamPlayer(int buffersCount, int sampleRateInHz)
	{
		this.sampleRateInHz = sampleRateInHz;
		init(buffersCount);
	}

	public AudioStreamPlayer()
	{
		this(8, 44100);
	}

	public void play(short[] buffer)
	{
		buffers[lastBufferIndex] = buffer;
		lastBufferIndex = (lastBufferIndex + 1) % buffers.length;
		startPlayThread();
	}

	public void stop()
	{
		if (isPlay)
		{
			if (playerThread != null && playerThread.isAlive() && !playerThread.isInterrupted())
			{
				playerThread.interrupt();
			}
			isPlay = false;
		}
	}
}

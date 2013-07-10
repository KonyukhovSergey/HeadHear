package ru.jauseg.headhear;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.SystemClock;

public class AudioBuffer
{
	public short[] buffer;

	public long readTimeStart;
	public long readTimeEnd;
	public long playTimeStart;
	public long playTimeEnd;

	public long playTime()
	{
		return playTimeEnd - playTimeStart;
	}

	public long readTime()
	{
		return readTimeEnd - readTimeStart;
	}

	public void read(AudioRecord record)
	{
		readTimeStart = SystemClock.elapsedRealtime();
		record.read(buffer, 0, buffer.length);
		readTimeEnd = SystemClock.elapsedRealtime();
	}

	public void play(AudioTrack track)
	{
		playTimeStart = SystemClock.elapsedRealtime();
		track.write(buffer, 0, buffer.length);
		playTimeEnd = SystemClock.elapsedRealtime();
	}
}

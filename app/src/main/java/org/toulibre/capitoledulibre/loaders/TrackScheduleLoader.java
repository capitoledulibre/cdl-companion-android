package org.toulibre.capitoledulibre.loaders;

import org.toulibre.capitoledulibre.db.DatabaseManager;
import org.toulibre.capitoledulibre.model.Day;
import org.toulibre.capitoledulibre.model.Track;

import android.content.Context;
import android.database.Cursor;

public class TrackScheduleLoader extends SimpleCursorLoader {

	private final Day day;
	private final Track track;

	public TrackScheduleLoader(Context context, Day day, Track track) {
		super(context);
		this.day = day;
		this.track = track;
	}

	@Override
	protected Cursor getCursor() {
		return DatabaseManager.getInstance().getEvents(day, track);
	}
}

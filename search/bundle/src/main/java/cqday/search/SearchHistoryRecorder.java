package cqday.search;

import java.util.Date;
import java.util.List;


public interface SearchHistoryRecorder {

	public static interface Entry {
	
		public enum Status {
			FAILED, SUCCESS, INPROGRESS
		}
		
		Date getStart();
		
		Date getEnd();
		
		Entry.Status getStatus();
		
		String getError();
	}
	
	void startIndex();

	void successful();

	void fail(Throwable e);

	List<Entry> getHistory();
}

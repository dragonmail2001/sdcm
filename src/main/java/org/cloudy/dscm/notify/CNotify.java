package org.cloudy.dscm.notify;

public interface CNotify<T> {
	public boolean delivery(T message) throws Exception;
}

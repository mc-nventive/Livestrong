package com.livestrong.myplate.back;

import java.lang.reflect.Method;

public interface DataHelperDelegate {

	/**
	 * Callback method used when the requested data is ready.
	 *  
	 * @param methodCalled See DataHelper.METHOD_* constants.
	 * @param data The requested data.
	 * 
	 * Example implementation:
	 * 
	 *	public void dataReceived(Method methodCalled, Object data) {
	 *		if (methodCalled.equals(DataHelper.METHOD_GET_FOOD)) {
	 *			if (data instanceof Food) {
	 *				Food food = (Food) data;
	 *				System.out.println("Loaded Food object from API: " + food.getItemTitle());
	 *			}
	 *		}
	 *	}
	 */
	public void dataReceivedThreaded(final Method methodCalled, final Object data);

	/**
	 * Call method called when an error occurs while trying to fetch data from the local database or remote API.
	 * 
	 * @param methodCalled See DataHelper.METHOD_* constants.
	 * @param error The exception that was thrown, if any. null otherwise.
	 * @param errorMessage The human-readable error message, if any. Can also be null.
	 * @return True if your implementation handled the error, false otherwise. If false is returned, the DataHelper will try to display a generic error message dialog. Return true if you don't want that.
	 */
	public boolean errorOccurredThreaded(final Method methodCalled, final Exception error, final String errorMessage);

}

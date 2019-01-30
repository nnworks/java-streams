package streams;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class containing the result value and a possible throwable of a map operation
 * @param <V> the value type of the result
 */
public class Result<V> {
	V value;
	Throwable throwable;
	
	public Result(V value, Throwable throwable) {
	  this.value = value;
	  this.throwable = throwable;
	}

	public Result(V value) {
	  this.value = value;
	  this.throwable = null;
	}
	
	public V getValue() {
		return value;
	}
	
	public Throwable getThrowable() {
		return throwable;
	}
	
	public boolean hasValue() {
		return value != null;
	}
	
	public boolean hasThrowable() {
		return throwable != null;
	}
	
	public String toString() {
		return (hasValue() ? value.toString() : "") + ((hasThrowable()) ? " [throwable message: " + throwable.getMessage() + "]": "");
	}

	/**
	 * Methods for filtering and handling
	 */
	
	public static enum Filter {
		SUCCESS,
		EXCEPTIONALS
	}
	
	/**
	 * Creates a filter to remove the success or the exceptional results.
	 * @param filterExceptional the result to filter: when true, the exceptional results are filtered out, else the success results.
	 * @return true or false depending filterExceptional given.
	 */
	public static <V> Predicate<Result<V>> createResultFilter(Filter filter) {
		switch (filter) {
		    case SUCCESS:
				return (result) -> {
					if (!result.hasThrowable()) {
						return false;
					} else {
						return true;
					}
				};
		    case EXCEPTIONALS:
				return (result) -> {
					if (result.hasThrowable()) {
						return false;
					} else {
						return true;
					}
				};
			default:
				throw new IllegalArgumentException("Did not expect value " + filter + " for the filter");
		}
	}
	
	/**
	 * creates a lambda function that calls the success function when the result is not exceptional, or the exceptional function when it is. 
	 * @param success function called on success, with the result as parameter
	 * @param exceptional function called in case the result has a throwable attached, with the result as parameter
	 * @return the result of the success or exceptional call
	 */
	public static <V> Function<Result<V>, Result<V>> createHandler(Function<Result<V>, Result<V>> success, Function<Result<V>, Result<V>> exceptional) {
		return (p) -> {
			if (!p.hasThrowable()) {
				return success.apply(p);
			} else {
				return exceptional.apply(p);
			}
		};
	}
	
	
	
	
}
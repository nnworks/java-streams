package streams;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class containing the immutable result value and a possible throwable of a map operation
 * @param <V> the value type of the result
 */
public class Result<V> {
    private final V value;
    private final Throwable throwable;

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

    /**
     * Enumeration with predefined filters.
     * The SUCCESS filter will test true for results that have no throwable.
     * The EXCEPTIONALS filter will test true for results that have a throwable.
     */
    public static enum Filter {
        SUCCESS(false),
        EXCEPTIONALS(true);

        boolean filterExceptionals;

        Filter(boolean filterExceptionals) {
            this.filterExceptionals = filterExceptionals;
        }

        public boolean test(Result<?> result) {
            if (filterExceptionals == result.hasThrowable()) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Creates a stream filter (Predicate) to remove the success or the exceptional results.
     * @param filter the result to let through: when Filter.SUCCESS, the exceptional results are removed, when Filter.EXCEPTIONALS the success results are removed from the result.
     * @return true or false depending filter given.
     */
    public static <V> Predicate<Result<V>> createResultFilter(Filter filter) {
        return (result) -> {
            return filter.test(result);
        };
    }

    /**
     * creates a lambda function that calls the success function when the result is not exceptional, or the exceptional function when it is.
     * @param successhandler function called on success, with the result as parameter
     * @param exceptionalHandler function called in case the result has a throwable attached, with the result as parameter
     * @return the result of the success or exceptional call
     */
    public static <V> Function<Result<V>, Result<V>> createHandler(Function<Result<V>, Result<V>> successhandler, Function<Result<V>, Result<V>> exceptionalHandler) {
        return (p) -> {
            if (!p.hasThrowable()) {
                return successhandler.apply(p);
            } else {
                return exceptionalHandler.apply(p);
            }
        };
    }

    /**
     * pulls out the value from the result.
     * @param filter the result to let through: when Filter.SUCCESS, the exceptional results are removed, when Filter.EXCEPTIONALS the success results are removed from the result.
     * @return true or false depending filter given.
     */
    public static <V> V extractValue(Result<V> result) {
      return result.getValue();
    }

}
package streams.toolkit;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import streams.toolkit.Result.Filter;


class ResultTest {

  List<String> list;
  List<String> expectedFilteredResult;
  List<String> expectedHandledResult;

  String v = "value";
  Throwable t = new Throwable();
  Result<String> resultWithThrowable = new Result<String>(v, t);
  Result<String> resultWithoutThrowable = new Result<String>(v);

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
  }

  @AfterAll
  static void tearDownAfterClass() throws Exception {
  }

  @BeforeEach
  void setUp() throws Exception {
    list = List.of("1", "2", "3", "4", "5", "6");
    expectedFilteredResult = List.of("fwithNoExc(f(1))",
                                     "fwithNoExc(f(2))",
                                     "fwithNoExc(f(4))",
                                     "fwithNoExc(f(5))");
    expectedHandledResult = List.of("handledNormally(fwithNoExc(f(1)))",
                                     "handledNormally(fwithNoExc(f(2)))",
                                     "handledExceptionally(fwithExc(f(3)))",
                                     "handledNormally(fwithNoExc(f(4)))",
                                     "handledNormally(fwithNoExc(f(5)))",
                                     "handledExceptionally(fwithExc(f(6)))");
  }

  @AfterEach
  void tearDown() throws Exception {
  }

  @Test
  void testResult() {
    Assertions.assertEquals(v, resultWithThrowable.getValue());
    Assertions.assertEquals(t, resultWithThrowable.getThrowable());
    Assertions.assertEquals(v, resultWithoutThrowable.getValue());
    Assertions.assertEquals(null, resultWithoutThrowable.getThrowable());
  }

  @Test
  void testCreateResultFilter() {

    Predicate<Result<String>> keepExceptionals = Result.createResultFilter(Result.Filter.EXCEPTIONALS);

    Assertions.assertTrue(keepExceptionals.test(resultWithThrowable));
    Assertions.assertFalse(keepExceptionals.test(resultWithoutThrowable));

    Predicate<Result<String>> removeExceptionals = Result.createResultFilter(Result.Filter.SUCCESS);

    Assertions.assertFalse(removeExceptionals.test(resultWithThrowable));
    Assertions.assertTrue(removeExceptionals.test(resultWithoutThrowable));
  }

  @Test
  void testCreateResultFilterInStream() {
     // Returning a list of Results containing the processed value and possibly an
     // exception
    List<String> result = list.stream()
        .map(ResultTest::toResult)     										 // String in, Result out
        .map(ResultTest::setSomeExceptions)         										 // Result in, Result with sometimes an exception out
        .filter(Result.createResultFilter(Filter.SUCCESS)) // Result in, Result out
        .map(Result::extractValue)     										 // Result in, Value (String) out
        .collect(Collectors.toList()); 										 // reduce to list with results

    Assertions.assertArrayEquals(expectedFilteredResult.toArray(new String[expectedFilteredResult.size()]), result.toArray(new String[result.size()]));
  }

  @Test
  void testResultHandlerInStream() {
     // Returning a list of Results containing the processed value and possibly an
     // exception
    List<String> result = list.stream()
        .map(ResultTest::toResult)     										 // String in, Result out
        .map(ResultTest::setSomeExceptions)         										 // Result in, Result with sometimes an exception out
        .map(Result.createResultHandler(                         // Result in, Result out
            (p)-> {
              return new Result<String>("handledNormally(" + p.getValue() + ")");
            },
            (p) -> {
              return new Result<String>("handledExceptionally(" + p.getValue() + ")");
            }))
        .map(Result::extractValue)     										 // Result in, Value (String) out
        .collect(Collectors.toList()); 										 // reduce to list with results

    Assertions.assertArrayEquals(expectedHandledResult.toArray(new String[expectedHandledResult.size()]), result.toArray(new String[result.size()]));
  }

  /**
   * Just for the fun of playing with ForkJoinPool :-)
   */
  @Test
  void testResultHandlerInParallelStream() {
    ForkJoinPool pool = new ForkJoinPool(4);
    try {
      List<String> result = pool.submit(() -> {
        return list.stream().parallel()
            .map(ResultTest::toResult)
            .map(ResultTest::setSomeExceptions)
            .map(Result.createResultHandler(                         // Result in, Result out
                (p)-> {
                  return new Result<String>("handledNormally(" + p.getValue() + ")");
                },
                (p) -> {
                  return new Result<String>("handledExceptionally(" + p.getValue() + ")");
                }))
            .map(Result::extractValue)
            .collect(Collectors.toList());
      }).get();


      Assertions.assertArrayEquals(expectedHandledResult.toArray(new String[expectedHandledResult.size()]), result.toArray(new String[result.size()]));

    } catch (InterruptedException | ExecutionException e) {
      Assertions.fail("no exception should have been raised");
    }
  }

  /**
   * Helpers
   */

  private static Result<String> toResult(String in) {
    return new Result<String>("f(" + in + ")");
  }

  private static Result<String> setSomeExceptions(Result<String> result) {
      if (result.getValue().contains("(3)") || result.getValue().contains("(6)")) {
        return new Result<String>("fwithExc(" + result.getValue() + ")",
            new IllegalArgumentException("Exception: " + result.getValue()));
      } else {
        return new Result<String>("fwithNoExc(" + result.getValue() + ")");
      }
  }
}

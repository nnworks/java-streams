package streams;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

import streams.Result;

public class TestStreams {

	List<String> list = List.of("1", "2", "3", "4", "5", "6");

	public void start() {
		serialProcessing();
		parallelProcessingDefaultPool();
		parallelProcessingCustomPool();
	}

	private void serialProcessing() {
		/** 
		 * shows that one item is processed through the whole stream before the next one is processed, instead of composing lists between the mappings.
		 */  
		List<String> result1 = list.stream()
				.map(TestStreams::function1)   // String in, String out
				.map(creatFunctionWithoutEx()) // String in, String out
				.map(TestStreams::function2)   // String in, String out
				.collect(Collectors.toList()); // reduce to List with Strings
		
		System.out.println("\n" + result1.toString() + "\n");
		
		/**
		 * Returning a list of Pairs containing the processed value and possibly an exception 
		 */
		List<Result<String>> result2 = list.stream()
				.map(TestStreams::function1WithResult) // String in, Result out 
				.map(createFunctionWithResultEx())     // String in, Result out, with exception for value 3 and 6 
				.map(TestStreams::function2WithResult) // Result in, Result out
				.map(Result.createHandler((p)-> { return p; }, (p) -> { System.out.print(p.getThrowable().getMessage()); return p; })) 
				.filter(Result.createResultFilter(Result.Filter.EXCEPTIONALS)) // filter out the results with an exception
				.collect(Collectors.toList()); // reduce to list
		
		System.out.println("\n" + result2.toString() + "\n");
	}
	
	/** 
	 *  Using parallel processing with default thread pool.
	 */  
	private void parallelProcessingDefaultPool() {
		
		List<String> result2 = list.parallelStream().map(TestStreams::function1).map(TestStreams::function2).collect(Collectors.toList());
		System.out.println("\n" + result2.toString() + "\n");
	}
	
	/** 
	 *  Using parallel processing with custom thread pool with 2 threads.
	 *  The lambda expression in the pool.submit() is executed in the custom thread pool context.
	 */  
	private void parallelProcessingCustomPool() {
		ForkJoinPool pool = new ForkJoinPool(2);
		try {
			List<String> result3 = pool.submit(() -> list.stream().parallel().map(TestStreams::function1).map(TestStreams::function2).collect(Collectors.toList())).get();
			System.out.println("\n" + result3.toString() + "\n");
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	static String function1(String in) {
		System.out.print("f1 ");
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		
		return "function1(" + in + ")";
	}

	static String function2(String in) {
		System.out.print("f2 ");
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		
		return "function2(" + in + ")";
	}

	static Result<String> function1WithResult(String in) {
		System.out.print("f1 ");
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		
		return new Result<String>("function1(" + in + ")");
	}
	
	static Result<String> function2WithResult(Result<String> in) {
		System.out.print("f2 ");
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		
		return new Result<String>("function2(" + in.getValue() + ")", in.getThrowable());
	}
	
	private Function<String, String> creatFunctionWithoutEx() {
		return (String in) -> {
			System.out.print("fwithEx");
			return "fwithNoEx(" + in + ")";
		};
	}

	private Function<Result<String>, Result<String>> createFunctionWithResultEx() {
		return (in) -> {
			
			if (in.getValue().contains("(3)") || in.getValue().contains("(6)")) {
				System.out.print("fwithEx");
				return new Result<String>("fwithNoEx(" + in.getValue() + ")", new IllegalArgumentException("Exception: " + in.getValue()));
			} else {
				System.out.print("fwithNoEx");
				return new Result<String>("fwithNoEx(" + in.getValue() + ")");
			}
		};
	}
}

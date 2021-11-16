import java.util.Random;

public class SortingAlgorithms{
	static int comparisons = 0;

	public static void main(String[] args) {
		int[] mergel2g = new int[32];
		int[] heapl2g = new int[32];
		int[] quickl2g = new int[32];
		int[] mergeg2l = new int[32];
		int[] heapg2l = new int[32];
		int[] quickg2l = new int[32];
		
		//generate random integers for n#
		int[] merge1 = new int[32];
		int[] merge2 = new int[1024];
		int[] merge3 = new int[32768];
		int[] merge4 = new int[1048576];
		int[] heap1 = new int[32];
		int[] heap2 = new int[1024];
		int[] heap3 = new int[32768];
		int[] heap4 = new int[1048576];
		int[] quick1 = new int[32];
		int[] quick2 = new int[1024];
		int[] quick3 = new int[32768];
		int[] quick4 = new int[1048576];
		
		//populate best and worst case data
		for(int i = 0; i < 32; i++) {
			mergel2g[i] = i;
			heapl2g[i] = i;
			quickl2g[i] = i;
		}
		for(int i = 0; i < 32; i++) {
			mergeg2l[i] = 31 - i;
			heapg2l[i] = 31 - i;
			quickg2l[i] = 31 - i;
		}
		//populate random data input
		populateIntegerArray(merge1);
		populateIntegerArray(merge2);
		populateIntegerArray(merge3);
		populateIntegerArray(merge4);
		heap1 = merge1.clone();
		heap2 = merge2.clone();
		heap3 = merge3.clone();
		heap4 = merge4.clone();
		quick1 = merge1.clone();
		quick2 = merge2.clone();
		quick3 = merge3.clone();
		quick4 = merge4.clone();
		
		System.out.println("MergeSort Algorithm");
		System.out.println("n = 32");
		System.out.println("Least-to-Greatest Sorted Input");
		printArray(mergel2g);
		long start = System.currentTimeMillis();
		mergeSort(mergel2g, 0, mergel2g.length - 1);
		long finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Output");
		printArray(mergel2g);
		System.out.println();

		System.out.println("Greatest-to-Least Sorted Input");
		printArray(mergeg2l);
		comparisons = 0;
		start = System.currentTimeMillis();
		mergeSort(mergeg2l, 0, mergeg2l.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Output");
		printArray(mergeg2l);
		System.out.println();
		
		System.out.println("Average-Case Input");
		printArray(merge1);
		comparisons = 0;
		start = System.currentTimeMillis();
		mergeSort(merge1, 0, merge1.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Average-Case Output");
		printArray(merge1);
		System.out.println();
		
		System.out.println("n = 1024");
		comparisons = 0;
		start = System.currentTimeMillis();
		mergeSort(merge2, 0, merge2.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();
	
		System.out.println("n = 32768");
		comparisons = 0;
		start = System.currentTimeMillis();
		mergeSort(merge3, 0, merge3.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();
		
		System.out.println("n = 1048576");
		comparisons = 0;
		start = System.currentTimeMillis();
		mergeSort(merge4, 0, merge4.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();

	
		System.out.println("HeapSort Algorithm");
		System.out.println("n = 32");
		System.out.println("Least-to-Greatest Sorted Input");
		printArray(heapl2g);
		comparisons = 0;
		start = System.currentTimeMillis();
		heapSort(heapl2g);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Output");
		printArray(heapl2g);
		System.out.println();

		System.out.println("Greatest-to-Least Sorted Input");
		printArray(heapg2l);
		comparisons = 0;
		start = System.currentTimeMillis();
		heapSort(heapg2l);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Output");
		printArray(heapg2l);
		System.out.println();
		
		System.out.println("Average-Case Input");
		printArray(heap1);
		comparisons = 0;
		start = System.currentTimeMillis();
		heapSort(heap1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Average-Case Output");
		printArray(heap1);
		System.out.println();
		
		System.out.println("n = 1024");
		comparisons = 0;
		start = System.currentTimeMillis();
		heapSort(heap2);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();
	
		System.out.println("n = 32768");
		comparisons = 0;
		start = System.currentTimeMillis();
		heapSort(heap3);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();
		
		System.out.println("n = 1048576");
		comparisons = 0;
		start = System.currentTimeMillis();
		heapSort(heap4);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();

		System.out.println("QuickSort Algorithm");
		System.out.println("n = 32");
		System.out.println("Least-to-Greatest Sorted Input");
		printArray(quickl2g);
		comparisons = 0;
		start = System.currentTimeMillis();
		quickSort(quickl2g, 0, quickl2g.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Output");
		printArray(quickl2g);
		System.out.println();

		System.out.println("Greatest-to-Least Sorted Input");
		printArray(quickg2l);
		comparisons = 0;
		start = System.currentTimeMillis();
		quickSort(quickg2l, 0, quickg2l.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Output");
		printArray(quickg2l);
		System.out.println();
		
		System.out.println("Average-Case Input");
		printArray(quick1);
		comparisons = 0;
		start = System.currentTimeMillis();
		quickSort(quick1, 0, quick1.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println("Average-Case Output");
		printArray(quick1);
		System.out.println();
		
		System.out.println("n = 1024");
		comparisons = 0;
		start = System.currentTimeMillis();
		quickSort(quick2, 0, quick2.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();
	
		System.out.println("n = 32768");
		comparisons = 0;
		start = System.currentTimeMillis();
		quickSort(quick3, 0, quick3.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();
		
		System.out.println("n = 1048576");
		comparisons = 0;
		start = System.currentTimeMillis();
		quickSort(quick4, 0, quick4.length - 1);
		finish = System.currentTimeMillis();
		System.out.println("Actual Comparisons = " + comparisons);
		System.out.println("Time Spent = " + (finish - start)+ " milliseconds");
		System.out.println();
	}
	static void populateIntegerArray(int[] arr) {
		int len = arr.length;
		Random rand = new Random();
		for(int i = 0; i < len; i++) {
			arr[i] = rand.nextInt(1000);
		}
	}
	static void mergeSort(int[] arr, int start, int end) {
		if (start < end) {
			int mid = (start + end) / 2;
			mergeSort(arr, start, mid);
			mergeSort(arr, mid + 1, end);
			merge(arr, start, mid, end);
		}
	}
	static void merge(int[] arr, int start, int mid, int end) {
		int[] temp = new int[end - start + 1];
		int i = start;
		int j = mid + 1;
		int k = 0;
		for(; i <= mid && j <= end;){
			if(isSmaller(arr[i], arr[j])) {
				temp[k] = arr[i];
				k++;
				i++;
			} else {
				temp[k] = arr[j];
				k++;
				j++;
			}
		}
		for(;i <= mid; i++, k++) {
			temp[k] = arr[i];
		}
		for(;j <= end; j++, k++) {
			temp[k] = arr[j];
		}
		for(i = start; i <= end; i++) {
			arr[i] = temp[i - start];
		}
	}
	
	static void heapSort(int[] arr) {
		buildHeap(arr, 0, arr.length - 1);
		int[] temp = arr.clone();
		for(int i = 0, n = arr.length - 1; i < 32; i++, n--) {
			arr[i] = deleteMin(temp, n);
		}
	}
	static void buildHeap(int[] arr, int r, int n) {
		if(2*r + 1 > n) {
			return;
		}
		buildHeap(arr, 2*r + 1, n);
		buildHeap(arr, 2*r + 2, n);
		pushDown(arr, r, n);
	}

	static void pushDown(int[] arr, int r, int n) {
		if(2*r + 1 > n) {
			return;
		}
		int s = 0;
		if(2*r + 1 == n || !isSmaller(arr[2*r + 2], arr[2*r + 1])) {
			s = 2*r + 1;
		} else {
			s = 2*r + 2;
		}
		if(isSmaller(arr[s], arr[r])) {
			int temp = arr[s];
			arr[s] = arr[r];
			arr[r] = temp;
			pushDown(arr, s, n);
		}
	}
	static int deleteMin(int[] arr, int n) {
		int temp = arr[0];
		arr[0] = arr[n];
		n = n - 1;
		pushDown(arr, 0, n);
		return(temp);
	}

	static void quickSort(int[] arr, int left, int right) {
		if(left >= right) {
			return;
		}
		Random rand = new Random();
		int k = rand.nextInt(right - left + 1) + left;
		int pivot = arr[k];
		int temp = arr[left];
		arr[left] = pivot;
		arr[k] = temp;
		int l = left + 1;
		int r = right;
		while(l <= r) {
			while(l <= r && !isSmaller(pivot, arr[l])) {
				l++;
			}
			while(l <= r && isSmaller(pivot, arr[r])) {
				r--;
			}
			if(l < r) {
				temp = arr[l];
				arr[l] = arr[r];
				arr[r] = temp;
				l++;
				r--;
			}
		}
		temp = arr[left];
		arr[left] = arr[r];
		arr[r] = temp;
		quickSort(arr, left, r - 1);
		quickSort(arr, r + 1, right);
	}


	static boolean isSmaller(int a, int b) {
		comparisons++;
		if(a < b) {
			return true;
		} else {
			return false;
		}
	}
	static boolean isSorted(int[] arr) {
		for(int i = 0; i < arr.length - 1; i++) {
			if(arr[i] > arr[i + 1]){
				return false;
			}
		}
		return true;
	}

	static void printArray(int[] arr){
		System.out.print("{");
		for(int i = 0; i < arr.length - 1; i++) {
			System.out.print(arr[i] + ", ");
		}
		System.out.print(arr[arr.length - 1]);
		System.out.println("}");
	}
}

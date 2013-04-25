package jvm.sample;

public class ArraySample {
	private int [] numbers = new int [10];
	
	public static int main(String [] args) {
//		return new TestArray().test1();
//		return new TestArray().test2();
		return new ArraySample().test3();
	}
	
	private int vectAsInt(int [] vect){
		return vect[0] + vect[1] * 10 + vect[2] * 100 + vect[3] * 1000;
	}
	
	// multidimensional array 
	private int test3(){
		int lg = 3;
		int [][] m = new int[lg][lg];
		for (int i = 0; i < lg; i++){
			for (int j = 0; j < lg; j++){
				m[i][j] = i * j;
			}
		}
		
		int suma = 0;
		for (int i = 0; i < lg; i++){
			for (int j = 0; j < lg; j++){
				suma += m[i][j];
			}
		}
		return suma;
	}
	
	@SuppressWarnings("unused")
	private int test2(){
		ArraySample ta = new ArraySample();
		ta.sort(ta.numbers2);
		return ta.vectAsInt(ta.numbers2);
	}

	@SuppressWarnings("unused")
	private int test1(){
		ArraySample t = new ArraySample();

		t.numbers[4] = 5;
		
		for (int i = 0; i < t.numbers.length - 1; i++){
			t.numbers[i+1] += t.numbers[i];
		}

		return t.numbers[t.numbers.length-1];	
	}
	
	private int numbers2 [] = {1,4,5,2};
	private int number;

	public void sort(int[] values) {
		this.numbers = values;
		number = values.length;

		mergesort(0, number - 1);
	
	}

	private void mergesort(int low, int high) {
		// Check if low is smaller then high, if not then the array is sorted
		if (low < high) {
			// Get the index of the element which is in the middle
			int middle = (low + high) / 2;
			// Sort the left side of the array
			mergesort(low, middle);
			// Sort the right side of the array
			mergesort(middle + 1, high);
			// Combine them both
			merge(low, middle, high);
		}
	}

	private void merge(int low, int middle, int high) {

		// Helperarray
		int[] helper = new int[number];

		// Copy both parts into the helper array
		for (int i = low; i <= high; i++) {
			helper[i] = numbers[i];
		}

		int i = low;
		int j = middle + 1;
		int k = low;
		// Copy the smallest values from either the left or the right side back
		// to the original array
		while (i <= middle && j <= high) {
			if (helper[i] <= helper[j]) {
				numbers[k] = helper[i];
				i++;
			} else {
				numbers[k] = helper[j];
				j++;
			}
			k++;
		}
		// Copy the rest of the left side of the array into the target array
		while (i <= middle) {
			numbers[k] = helper[i];
			k++;
			i++;
		}
		helper = null;

	}
}

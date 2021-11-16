import java.util.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class RadixSort {
	public static void main(String[] args) {
		/* Command Line Arguments
		String file1 = "f.txt";
		String file2 = "g.txt";
		if (args.length == 1) {
			file1 = args[0];
		} else if (args.length == 2){
			file1 = args[0];
			file2 = args[1];
		}*/
		Scanner scan = new Scanner(System.in);
		String input1, input2;
		System.out.println("Please specify the input file (default = f.txt)");
		input1 = scan.nextLine();
		System.out.println("Please specify the output file (default = g.txt)");
		input2 = scan.nextLine();

		if(input1.length() == 0) {
			input1 = "f.txt";
		}
		if(input2.length() == 0) {
			input2 = "g.txt";
		}
		radixSortLSD(input1, input2);
	}
	
	public static void radixSortLSD(String in, String out) {
		File iFile = new File(in);
		File oFile = new File(out);
		int[][] stringList = new int[1000][21];
		int row = 0;
		int col = 0;
		try{
			FileReader input = new FileReader(iFile);
			FileWriter output = new FileWriter(oFile);
			
			int c;
			while(true) {
				c = input.read();
				if (c == 10 || c == 32) {
					//keep reading whitespace
					while(true) {
						c = input.read();
						if(c == 10 || c == 32) {
							continue;
						}
						row++;
						col = 0;
						break;
					}
				}
				if(c == -1) {
					break;
				}
				stringList[row][col] = c;
				col++;
			}
		//sort the array
		//holds the sorted indices of the array;
		int numStrings = row;
		int numStringsCopy = numStrings;
		int[] sorted = new int[numStrings];
		for(int i = 1; i < numStrings; i++) {
			sorted[i] = i;
		}
		//bucket index 0 represents "A"
		int[][] bucket;
		//used to count how many is in a bucket, index 0 contains how many has padding
		//index 1 counts how many has "A"
		//bucket
		int[] bucketCount;
		int charac, counter, sortedIndex;
		col = 20;
		while(col != -1) {
			bucketCount = new int[27];
			bucket = new int[27][1000];
			numStringsCopy = numStrings;
			counter = 0;
			while(numStringsCopy != 0) {
				//hold the index the character is located in " " = 0, "A" = 1, "B" = 2 ..
				charac = stringList[sorted[counter]][col] - 64;
				if(charac == -64) {
					charac = 0;
				}
				//put the index + 1 of the string in the original array to the bucket
				bucket[charac][bucketCount[charac]] = sorted[counter] + 1;
				//increase the count of strings in a certain bucket
				bucketCount[charac]++;
				numStringsCopy--;
				counter++;
			}
			//get the new indices of the sorted array from the bucket
			for(int i = 0, j = 0; i < 27;) {
				sortedIndex = bucket[i][j];
				//if no more indexes, go to the next letter
				if(sortedIndex == 0){
					j = 0;
					i++;
					continue;
				} else {
					//check each index for current letter
					sorted[numStringsCopy] = sortedIndex - 1;
					numStringsCopy++;
					j++;
					if(numStringsCopy == numStrings){
						break;
					}
				}
			}
			col--;
		}
		//write the sorted array to file
		for(numStringsCopy = 0, col = 0; numStringsCopy < numStrings; col++) {
			if(col > 20) {
				col = -1;
				numStringsCopy++;
				output.write("\n");
				continue;
			}
			if(stringList[sorted[numStringsCopy]][0] == 0) {
				break;
			}
			if(stringList[sorted[numStringsCopy]][col] != 0) {
				output.write(stringList[sorted[numStringsCopy]][col]);
			}

		}
		output.close();
		} catch(Exception e){}
	}
}

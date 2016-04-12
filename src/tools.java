import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

public class tools {
	public static String[] DISTRIBUTIONS_NAMES = new String[] {"uniform_discrete", "uniform_continuous", "normal_discrete", "normal_continuous", "poisson_discrete"};
	public static int[] DISTRIBUTIONS_IDS = new int[] {1, 2, 3, 4, 5};
	public static String[][] FILENAME_DELIMITER_PAIRS = new String[][] {{".txt", "\t"}, {".csv", ","}};
	public static String[] LOGIC_OPERATORS = new String[] {"==", "!=", ">=", "<=", ">", "<"};
	public static String[] CRITERIA_ARRAY_STRUCTURE = new String[] {"Operator", "Variable_Name", "Value"};
	public static int CRITERION_ARRAY_OPERATOR_INDEX = 0;
	public static int CRITERION_ARRAY_VARIABLENAME_INDEX = 1;
	public static int CRITERION_ARRAY_VALUE_INDEX = 2;
	public static String[][] OPERATOR_ARRAY_STRUCTURE = new String[][] {{"-", "+"}, {"*", "/"}, {"^"}};
	public static int OPERATOR_ARRAY_INDEX_MINUS = 0;
	public static int OPERATOR_ARRAY_INDEX_PLUS = 1;
	public static int OPERATOR_ARRAY_INDEX_MULTIPLICATION = 2;
	public static int OPERATOR_ARRAY_INDEX_DIVISION = 3;
	public static String[] CUMSUM_PRESENTVALUE_ARRAY_STRUCTURE = new String[] {"Original", "Present Value", "Cumulative Sum", "Cumulative Sum of Present Value"};
	public static int CUMSUM_PRESENTVALUE_ARRAY_INDEX_ORIGINAL = 0;
	public static int CUMSUM_PRESENTVALUE_ARRAY_INDEX_PRESENTVALUE = 1;
	public static int CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUM = 2;
	public static int CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE = 3;
	public static int MAXIMUM_NUMBER_OF_VALS = 200;
	public static int TIMES_OF_RESAMPLING_FOR_SEED = 10;

	public static void rename (String fullPathTemplate, String oldStr, String newStr, boolean renameFileNameOnly) {
		String filepath;
		File[] files;
		int counter;

		files = getDirectory(fullPathTemplate);
		counter = 0;
		for (File file : files) {
			filepath = file.getPath();
			if (filepath.contains(oldStr)) {
				if (renameFileNameOnly) {
					file.renameTo(new File(file.getParent() + "/" + file.getName().replace(oldStr, newStr)));
				} else {
					file.renameTo(new File(filepath.replace(oldStr, newStr)));
				}
				counter ++;
				if (counter == 20) {
					System.out.print("Processing folder:" + file.getParent() + "\nNumber of files finished: ");
				} else if (counter % 20 == 0) {
					System.out.print("..." + counter);
				}
			}
		}
		System.out.println("\ndone.");
	}

	public static File[] getDirectory (String fullPathTemplate) {
		String directory;
		int index;
		final ArrayList<String> templates;
		FilenameFilter filter;
		int[] indices;

		index = fullPathTemplate.lastIndexOf("/") + 1;
		directory = fullPathTemplate.substring(0, index);
		templates = parseTemplate(fullPathTemplate.substring(index));
		filter = new FilenameFilter() {
			@Override
			public boolean accept(File directory, String filename) {
				boolean isToAccept;
				int length;

				isToAccept = true;
				filename = filename.toLowerCase();
				for (String template : templates) {
					if (! template.startsWith("<<<")) {
						if (! filename.startsWith(template)) {
							isToAccept = false;
							break;
						} else {
							filename = filename.substring(template.length());
						}
					} else {
						length = Integer.parseInt(template.substring(3, template.length() - 3));
						try {
							int a = Integer.parseInt(filename.substring(0, length));
							filename = filename.substring(length);
						} catch (NumberFormatException nfe) {
							isToAccept = false;
							break;
						}
					}
				}

				return isToAccept;
			}
		};

		return new File(directory).listFiles(filter);
	}

	public static ArrayList<String> parseTemplate (String template) {
		int a, b;
		ArrayList<String> result;

		result = new ArrayList<String>();
		a = template.indexOf("<<<");
		while (a > 0) {
			result.add(template.substring(0, a).toLowerCase());
			b = template.indexOf(">>>") + 3;
			result.add(template.substring(a, b).toLowerCase());
			template = template.substring(b);
			a = template.indexOf("<<<");
		}
		result.add(template.toLowerCase());

		return result;
	}

	public static void changeHeader (String directory, String newHeader) {
		String filepath;
		BufferedReader reader;
		PrintWriter writer;
		File[] files = new File(directory).listFiles();

		for (File file : files) {
			filepath = file.getPath();
			file.renameTo(new File(filepath + ".bak"));
			System.out.print(filepath);
			try {
				reader = new BufferedReader(new FileReader(filepath + ".bak"));
				writer = new PrintWriter(new BufferedWriter(new FileWriter(filepath + "", true)));
				reader.readLine();
				writer.println(newHeader);
				while (reader.ready()) {
					writer.println(reader.readLine());
				}
				reader.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(" ... done.");
		}
		System.out.println("\nProgream completed.");
	}

	public static double[][] initializeAgents_bak (String[] keys, int[] keysDataRange, String[] vals, String[] distributions, String outFile) throws Exception {
		double[][] valsArray;
		String delimiter;
		Object[][] distributionParsed;
		int numRows;

		distributionParsed = new Object[distributions.length][];
		for (int i = 0; i < distributionParsed.length; i++) {
			distributionParsed[i] = parseDistributionFunction( distributions[i] );
		}
		numRows = 1;
		for (int i = 0; i < keysDataRange.length; i++) {
			numRows *= keysDataRange[i];
		}
		valsArray = new double[numRows][vals.length];
		for (int i = 0; i < numRows; i ++) {
			for (int j = 0; j < valsArray[i].length; j ++) {
				valsArray[i][j] = (double) getRandomNumber(distributionParsed[j]);
			}
		}

		if (outFile.endsWith(".csv")) {
			delimiter = ",";
		} else if (outFile.endsWith(".txt")) {
			delimiter = "\t";
		} else {
			throw new Exception("Unrecognized file extension in:\n" + outFile);
		}
		try {
//			writeToFile(keys, keysDataRange, valsArray, vals, delimiter, outFile);
			writeToFile(keys, null, valsArray, vals, delimiter, outFile);
			System.out.println("Initialization of agents is available at:\n" + outFile);
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
		return valsArray;
	}

	public static void initializeAgents (String[][] keys, String[][] vals, long randomNumberGeneratorSeed, String outFile) throws Exception {
		double[][] valsArray;
		String delimiter;
		String[] tmp;
		Object[][] distributionParsed;
		int columnPointer;
		int[] keysPointers;
		int[][] keysRanges;
		long[] seedPointers, seedSteps;
		long seedPointer;
		boolean isDone;
		BufferedWriter writer;

		distributionParsed = new Object[vals.length][];
		for (int i = 0; i < vals.length; i++) {
			distributionParsed[i] = parseDistributionFunction (vals[i][1] + "; resamp=" + vals[i][2]);
		}
		seedSteps = new long[keys.length];
		keysPointers = new int[keys.length];
		keysRanges = new int[keys.length][2];
		for (int i = 0; i < keysRanges.length; i++) {
			tmp = keys[i][1].split(":");
			keysRanges[i][0] = Integer.parseInt(tmp[0]);
			keysRanges[i][1] = Integer.parseInt(tmp[1]);
			keysPointers[i] = keysRanges[i][0];
			if (i > 0) {
				seedSteps[i] = Long.parseLong(keys[i][2]);
			}
		}

		seedSteps[seedSteps.length - 2] = seedSteps[seedSteps.length - 1] * MAXIMUM_NUMBER_OF_VALS;
		for (int i = seedSteps.length - 3; i >= 0; i--) {
			seedSteps[i] = seedSteps[i] * seedSteps[i + 1];
		}
		seedPointers = new long[keys.length];
		for (int i = 0; i < seedPointers.length; i++) {
			seedPointers[i] = randomNumberGeneratorSeed;
		}

//		valsArray = new double[numRows][vals.length];
//		keysPointers[keysPointers.length - 1] = keysRanges[keysPointers.length - 1][0] - 1;
		isDone = false;
		seedPointer = seedPointers[seedPointers.length - 1];
		delimiter = determineDelimiter(outFile);
		writer = new BufferedWriter(new FileWriter(outFile));
		writeToFile(writer, (String[]) getOneColumnOfAMultiDemensionalArray(keys, 0), null, null, null, (String[]) getOneColumnOfAMultiDemensionalArray(vals, 0), delimiter);
		while(! isDone) {
			valsArray = getRandomNumber(distributionParsed, seedPointer, keysRanges[keysPointers.length - 1][1] - keysRanges[keysPointers.length - 1][0] + 1, seedSteps[keysPointers.length - 1]);
			writeToFile(writer, null, keysRanges[keysRanges.length - 1], keysPointers, valsArray, null, delimiter);
			isDone = true;
			columnPointer = keys.length - 1;
			for (int j = keys.length - 2; j >= 0 ; j --) {
				seedPointers[j] += seedSteps[j];
				if (keysRanges[j][1] > keysRanges[j][0]) {
					if (keysPointers[j] >= keysRanges[j][1]) {
						keysPointers[j] = keysRanges[j][0];
//						seedPointers[j] += seedSteps[j];
					} else {
						keysPointers[j] ++;
						columnPointer = j;
						isDone = false;
						break;
					}
				} else {
					if (keysPointers[j] <= keysRanges[j][1]) {
						keysPointers[j] = keysRanges[j][0];
//						seedPointers[j] += seedSteps[j];
					} else {
						keysPointers[j] --;
						columnPointer = j;
						isDone = false;
						break;
					}
				}
			}
			for (int i = columnPointer + 1; i < seedPointers.length; i++) {
				seedPointers[i] = seedPointers[columnPointer];
			}
			seedPointer = seedPointers[columnPointer];
		}

		writer.close();
		System.out.println("Initialization of agents is available at:\n" + outFile);
	}

	public static Object[] parseDistributionFunction (String distributionText) throws Exception {
		Object[] distributionID;
		String[] tmp;
		double i, j;

		distributionID = new Object[4];
		if (distributionText.startsWith(DISTRIBUTIONS_NAMES[0])) {
			// distributionID = new double[] {distributionID, range, start};
			distributionID[0] = DISTRIBUTIONS_IDS[0];
			tmp = distributionText.substring(distributionText.indexOf("(") + 1, distributionText.lastIndexOf(")")).split(":");
			i = Integer.parseInt(tmp[0]);
			j = Integer.parseInt(tmp[1]);
			distributionID[1] = (int) (j - i + 1);
			distributionID[2] = (int) i;

		} else if (distributionText.startsWith(DISTRIBUTIONS_NAMES[1])) {
			// distributionID = new double[] {distributionID, range, start};
			distributionID[0] = DISTRIBUTIONS_IDS[1];
			tmp = distributionText.substring(distributionText.indexOf("(") + 1, distributionText.lastIndexOf(")")).split(":");
			i = Double.parseDouble(tmp[0]);
			j = Double.parseDouble(tmp[1]);
			distributionID[1] = j - i;
			distributionID[2] = i;

		} else if (distributionText.startsWith(DISTRIBUTIONS_NAMES[3])) {
			// distributionID = new double[] {distributionID, range, start};
			distributionID[0] = DISTRIBUTIONS_IDS[3];
			tmp = distributionText.substring(distributionText.indexOf("(") + 1, distributionText.lastIndexOf(")")).split(",");
			distributionID[1] = Double.parseDouble(tmp[0]);
			distributionID[2] = Double.parseDouble(tmp[1]);

		} else if (distributionText.startsWith(DISTRIBUTIONS_NAMES[4])) {
			// distributionID = new double[] {distributionID, range, start};
			distributionID[0] = DISTRIBUTIONS_IDS[4];
			distributionID[1] = Integer.parseInt(distributionText.substring(distributionText.indexOf("(") + 1, distributionText.lastIndexOf(")")));

		} else {
			throw new Exception ("Unrecognized distribution function name: " + distributionText);
		}
		distributionID[3] = Integer.parseInt(distributionText.split("; resamp=")[1]);

		return distributionID;
	}

	public static Object getRandomNumber (Object[] distributionParameters) throws Exception {
		int distributionID;
		Object random;

		distributionID = (int) distributionParameters[0];
		if (distributionID == DISTRIBUTIONS_IDS[0]) {
			random = (int) ((int) (Math.random() * (int) distributionParameters[1]) + (int) distributionParameters[2]);
		} else if (distributionID == DISTRIBUTIONS_IDS[1]) {
			random = (double) (Math.random() * (double) distributionParameters[1] + (double) distributionParameters[2]);
		} else {
			throw new Exception ("Unrecognized distribution");
		}

		return random;
	}

	public static Object getRandomNumber (Object[] distributionParameters, long seed) throws Exception {
		int distributionID;
		Random generator;
		Object random;

		generator = new Random(seed);
		distributionID = (int) distributionParameters[0];
		if (distributionID == DISTRIBUTIONS_IDS[0]) {
			random = (double) generator.nextInt((int) distributionParameters[1]) + (int) distributionParameters[2];
		} else if (distributionID == DISTRIBUTIONS_IDS[1]) {
			random = generator.nextDouble();
			if (! ((double) distributionParameters[1] == 0 && (double) distributionParameters[2] == 1)) {
				random = (double) random * (double) distributionParameters[1] + (double) distributionParameters[2];
			}
		} else {
			throw new Exception ("Unrecognized distribution");
		}

		return random;
	}

	public static double[][] getRandomNumber (Object[][] distributionParameters, long seed, int numberOfRandoms, long seedStep) throws Exception {
		int distributionID, k;
		Random generator;
		double[][] random;
		double expLambda, p;

		random = new double[numberOfRandoms][distributionParameters.length];
		for (int j = 0; j < distributionParameters.length; j++) {
			generator = new Random(seed + j * seedStep);
			for (int i = 0; i < TIMES_OF_RESAMPLING_FOR_SEED; i++) {
				generator.setSeed(generator.nextLong());
			}
			distributionID = (int) distributionParameters[j][0];
			if (distributionID == DISTRIBUTIONS_IDS[0]) {
				if ((int) distributionParameters[j][3] == 0) {
					random[0][j] = (double) generator.nextInt((int) distributionParameters[j][1]) + (int) distributionParameters[j][2];
					for (int i = 1; i < random.length; i++) {
						random[i][j] = random[0][j];
					}
				} else {
					for (int i = 0; i < random.length; i++) {
						random[i][j] = (double) generator.nextInt((int) distributionParameters[j][1]) + (int) distributionParameters[j][2];
					}
				}

			} else if (distributionID == DISTRIBUTIONS_IDS[1]) {
				for (int i = 0; i < random.length; i++) {
					random[i][j] = generator.nextDouble();
					if (! ((double) distributionParameters[j][1] == 0 && (double) distributionParameters[j][2] == 1)) {
						random[i][j] = random[i][j] * (double) distributionParameters[j][1] + (double) distributionParameters[j][2];
					}
				}

			} else if (distributionID == DISTRIBUTIONS_IDS[3]) {
				for (int i = 0; i < random.length; i++) {
					random[i][j] = generator.nextGaussian();
					if (! ((double) distributionParameters[j][1] == 0 && (double) distributionParameters[j][2] == 1)) {
						random[i][j] = random[i][j] * (double) distributionParameters[j][2] + (double) distributionParameters[j][1];
					}
				}

			} else if (distributionID == DISTRIBUTIONS_IDS[4]) {
				expLambda = Math.exp(- (int) distributionParameters[j][1]);
				for (int i = 0; i < random.length; i++) {
					p = 1;
					k = 0;
					while (p > expLambda) {
						k ++;
						p *= generator.nextDouble();
					}
					random[i][j] = k - 1;
				}
			} else {
				throw new Exception ("Unrecognized distribution");
			}
		}

		return random;
	}

	public static Object getOneColumnOfAMultiDemensionalArray (Object[][] array, int colNumber) throws Exception {
		int numRowsInTheColumn;
		String[] aColumnOfTheArray;

		numRowsInTheColumn = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i].length > colNumber) {
				numRowsInTheColumn = i;
			}
		}
		if (numRowsInTheColumn > -1) {
			aColumnOfTheArray = new String[numRowsInTheColumn + 1];
			for (int i = 0; i < aColumnOfTheArray.length; i++) {
				aColumnOfTheArray[i] = (String )array[i][colNumber];
			}
			return aColumnOfTheArray;
		} else {
			return null;
		}
	}

	public static int[] convertStringArrayToIntArray (String[] strArray) throws Exception {
		int[] intArray;

		intArray = new int[strArray.length];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = Integer.parseInt(strArray[i]);
		}
		return intArray;
	}

	public static String determineDelimiter (String filename) throws Exception {
		String delimiter;

		if (filename.endsWith(".csv")) {
			delimiter = ",";
		} else if (filename.endsWith(".txt")) {
			delimiter = "\t";
		} else {
			throw new Exception("Unrecognized file extension in:\n" + filename);
		}

		return delimiter;
	}

	public static void writeToFile (String[] keysHeader, int[][] keysRanges, double[][] valsData, String[] valsHeader, String delimiter, String outFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		int[] keysPointers;

		writer.write(keysHeader[0]);
		for (int j = 1; j < keysHeader.length; j ++) {
			writer.write(delimiter + keysHeader[j]);
		}
		for (int j = 0; j < valsHeader.length; j ++) {
			writer.write(delimiter + valsHeader[j]);
		}
		writer.newLine();
		keysPointers = new int[keysHeader.length];
		keysPointers[keysPointers.length - 1] = keysRanges[keysPointers.length - 1][0] - 1;
		for (int i = 0; i < keysPointers.length - 1; i++) {
			keysPointers[i] = keysRanges[i][0];
		}
		for (int i = 0; i < valsData.length; i ++) {
			for (int j = keysPointers.length - 1; j >= 0 ; j --) {
				if (keysRanges[j][1] >= keysRanges[j][0]) {
					if (keysPointers[j] >= keysRanges[j][1]) {
						keysPointers[j] = keysRanges[j][0];
					} else {
						keysPointers[j] ++;
						break;
					}
				} else {
					if (keysPointers[j] <= keysRanges[j][1]) {
						keysPointers[j] = keysRanges[j][0];
					} else {
						keysPointers[j] --;
						break;
					}
				}
			}
			writer.write(keysPointers[0] + "");
			for (int j = 1; j < keysPointers.length; j++) {
				writer.write(delimiter + keysPointers[j]);
			}
			for (int j = 0; j < valsData[i].length; j ++) {
				writer.write(delimiter + valsData[i][j]);
			}
			writer.newLine();
		}

		writer.close();
	}

	public static void writeToFile (BufferedWriter writer, String[] keysHeader, int[] keyRange, int[] keysPointers, double[][] valsData, String[] valsHeader, String delimiter) throws IOException {
		int keyPointer;

		if (keysHeader != null && valsHeader != null) {
			writer.write(keysHeader[0]);
			for (int j = 1; j < keysHeader.length; j ++) {
				writer.write(delimiter + keysHeader[j]);
			}
			for (int j = 0; j < valsHeader.length; j ++) {
				writer.write(delimiter + valsHeader[j]);
			}
			writer.newLine();
		}

		if (valsData != null) {
			keyPointer = keyRange[0];
			for (int i = 0; i < valsData.length; i ++) {
				for (int j = 0; j < keysPointers.length - 1; j++) {
					writer.write(keysPointers[j] + delimiter);
				}
				writer.write(keyPointer + "");
				for (int j = 0; j < valsData[i].length; j ++) {
					writer.write(delimiter + valsData[i][j]);
				}
				writer.newLine();
				if (keyRange[1] >= keyRange[0]) {
					keyPointer ++;
				} else {
					keyPointer --;
				}
			}
		}
	}

	public static void consolidateSimulationData (String infileDirAndPrefix, String[] infileSuffix, int[] ageRange, int numSkip, String outfile) {
		String fileNameId;
		boolean isToAppend;
		int outFileRowNumberCounter;

		isToAppend = false;
		outFileRowNumberCounter = 1;
		System.out.print("Processing age");
		for (int i = ageRange[0]; i <= ageRange[1]; i++) {
			fileNameId = getFileNameId(i);
			try {
				displayStatusBar(i);
				outFileRowNumberCounter = combineOutputFiles(infileDirAndPrefix + fileNameId, infileSuffix, numSkip, outfile, isToAppend, outFileRowNumberCounter);
			} catch (Exception e) {
				e.printStackTrace();
			}
			isToAppend = true;
		}
//		rowCounter = insertLines(outfile, ",", rowCounter);
		System.out.println("\nResult is ready at:\n" + outfile);
	}

	public static int insertLines (String outfile, String delimiter, int rowCounter) {
		PrintWriter writer;
		int counter;

		counter = rowCounter;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(outfile, true)));
			for (int initial_age = -29; initial_age <= -21; initial_age++) {
				for (int sex = 1; sex <= 2; sex++) {
					for (int ethnicity = 1; ethnicity <= 4; ethnicity++) {
						for (int education = 1; education <= 3; education++) {
							for (int income = 1; income <= 1; income ++) {
								for (int initial_tobacco_status = 1; initial_tobacco_status <= 3; initial_tobacco_status++) {
									for (int tobacco_initiation_band = 1; tobacco_initiation_band <= 10; tobacco_initiation_band++) {
										for (int time = 0; time <= 30; time ++) {
											writer.print(counter + delimiter + initial_age + delimiter + sex + delimiter + ethnicity + delimiter + education + delimiter + income + delimiter + initial_tobacco_status + delimiter + tobacco_initiation_band + delimiter + time);
											for (int i = 10; i < 105; i++) {
												writer.print(delimiter + 0.0);
											}
											writer.println(delimiter + 0.0);
											counter ++;
										}
									}
								}
							}
						}
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return counter;
	}

	public static void combineOutputFiles (String infileDirAndPrefix, String[] infileSuffix, int numSkip, String outfile) throws Exception {
		combineOutputFiles(infileDirAndPrefix, infileSuffix, numSkip, outfile, false, -1);
	}

	public static int combineOutputFiles (String infileDirAndPrefix, String[] infileSuffix, int numSkip, String outfile, boolean isToAppend, int outFileRowNumberPointer) throws Exception {
		BufferedReader[] readers;
		String[] inDelimiters, tmp1;
		int[][] keyColumnsIndices;
		int[] numColumns;
		int totalNumColumns;
		String outDelimiter;
		String[][] tmp;
		BufferedWriter writer;
		double[] cumsum = null;
		String currentKey, nextKey;
		boolean isToSkip;

		inDelimiters = new String[infileSuffix.length];
		if (outfile == null) {
			outfile = infileDirAndPrefix + ".csv";
			outDelimiter = ",";
		} else {
			outDelimiter = getDelimiterFromFilename(outfile);
		}

		for (int i = 0; i < infileSuffix.length; i++) {
			inDelimiters[i] = getDelimiterFromFilename(infileSuffix[i]);
		}

		//Number of columns
		readers = new BufferedReader[infileSuffix.length];
		tmp = new String[infileSuffix.length][];
		numColumns = new int[tmp.length];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = new BufferedReader(new FileReader(infileDirAndPrefix + infileSuffix[i]));
			for (int j = 0; j < numSkip; j++) {
				readers[i].readLine();
			}
			tmp[i] = replaceStrings(readers[i].readLine().split(inDelimiters[i]), " ", "_");
			numColumns[i] = tmp[i].length;
		}
//		keyColumnsIndices = consolidateColumns(tmp);
		keyColumnsIndices = new int[][] {{0,1,2,3,4,5,6,7}, {0,1,2,3,4,5,6,7}, {0,1,2,3,4,5,6,7}};
		totalNumColumns = 0;
		for (int i = 0; i < numColumns.length; i++) {
			totalNumColumns += (numColumns[i] - keyColumnsIndices[i].length);
		}

		writer = new BufferedWriter(new FileWriter(outfile, isToAppend));
		if (! isToAppend) {
			if (outFileRowNumberPointer > -1) {
				writer.write("RowNumber" + outDelimiter + getOneRowOfConsolidatedData(tmp, keyColumnsIndices, outDelimiter, null) + outDelimiter + addSuffix(tmp, keyColumnsIndices, outDelimiter, "_accumulated") + "\n");
			} else {
				writer.write(getOneRowOfConsolidatedData(tmp, keyColumnsIndices, outDelimiter, null) + outDelimiter + addSuffix(tmp, keyColumnsIndices, outDelimiter, "_accumulated") + "\n");
			}
		}

		currentKey = "";
		isToSkip = false;
		while (readers[0].ready()) {
			for (int i = 0; i < readers.length; i++) {
				tmp1 = readers[i].readLine().split(inDelimiters[i]);
				if (tmp1.length < 2) {
					isToSkip = true;
					break;
				}
				// Filling in missing columns
				if (tmp1.length < numColumns[i]) {
					tmp[i] = new String[numColumns[i]];
					for (int j = 0; j < tmp1.length; j++) {
						tmp[i][j] = tmp1[j];
					}
				} else {
					tmp[i] = tmp1;
				}
				
				if (i==0) {
					isToSkip = true;
					for (int j = 1; j < keyColumnsIndices[i].length - 1; j++) {
						if (Double.parseDouble(tmp[i][keyColumnsIndices[i][j]]) != 0) {
							isToSkip = false;
							break;
						}
					}
					nextKey = tmp[i][keyColumnsIndices[i][0]];
					for (int j = 1; j < keyColumnsIndices[i].length - 1; j++) {
						nextKey += "\t" + tmp[i][keyColumnsIndices[i][j]];
					}
					if (! nextKey.equals(currentKey)) {
						cumsum = new double[totalNumColumns];
						currentKey = nextKey;
					}
				}
			}
			if (! isToSkip) {
				if (outFileRowNumberPointer > -1) {
					writer.write(outFileRowNumberPointer + outDelimiter + getOneRowOfConsolidatedData(tmp, keyColumnsIndices, outDelimiter, cumsum) + "\n");
					outFileRowNumberPointer ++;
				} else {
					writer.write(getOneRowOfConsolidatedData(tmp, keyColumnsIndices, outDelimiter, cumsum) + "\n");
				}
			}
		}
		
		writer.close();
		for (int i = 0; i < readers.length; i++) {
			readers[i].close();
		}

		return outFileRowNumberPointer;
	}

	public static int[][] consolidateColumns (String[][] headers) {
		ArrayList<Integer>[] columnsToExclude;
		int[][] result;

		columnsToExclude = new ArrayList[headers.length - 1];
		for (int i = 1; i < headers.length; i++) {
			columnsToExclude[i - 1] = new ArrayList<Integer>();
			for (int j = 0; j < i; j++) {
				for (int k = 0; k < headers[i].length; k++) {
					for (int l = 0; l < headers[j].length; l++) {
						if (headers[i][k].equalsIgnoreCase(headers[j][l]) && !columnsToExclude[i - 1].contains(k)) {
							columnsToExclude[i - 1].add(k);
							break;
						}
					}
				}
			}
		}

		result = new int[columnsToExclude.length][];
		for (int i = 0; i < result.length; i++) {
			result[i] = new int[columnsToExclude[i].size()];
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] = columnsToExclude[i].get(j);
			}
		}
		return result;
	}

	public static String addSuffix (String[][] data, int[][] columnsToExclude, String outDelimiter, String suffix) {
		String out;

		out = "";
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (! isContains(j, columnsToExclude[i])) {
					if (out.equals("")) {
						out = data[i][j] + suffix;
					} else {
						out += (outDelimiter + data[i][j] + suffix);
					}
				}
			}
		}

		return out;
	}

	//TODO reset cumsum;
	public static String getOneRowOfConsolidatedData (String[][] data, int[][] columnsToExclude, String outDelimiter, double[] cumsum) {
		String out;
		int pointer;

		out = data[0][0];
		pointer = 0;
		if (cumsum != null && ! isContains(0, columnsToExclude[0])) {
			cumsum[pointer] += Double.parseDouble(data[0][0]);
			pointer ++;
		}
		for (int j = 1; j < data[0].length; j++) {
			if (data[0][j] == null) {
				out += (outDelimiter + 0);
			} else {
				out += (outDelimiter + data[0][j]);
			}
			if (cumsum != null && ! isContains(j, columnsToExclude[0])) {
				if (data[0][j] != null) {
					cumsum[pointer] += Double.parseDouble(data[0][j]);
				}
				pointer ++;
			}
		}

		for (int i = 1; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (! isContains(j, columnsToExclude[i])) {
					if (data[i][j] == null) {
						out += (outDelimiter + 0);
					} else {
						out += (outDelimiter + data[i][j]);
					}
					if (cumsum != null) {
						if (data[i][j] != null) {
							cumsum[pointer] += Double.parseDouble(data[i][j]);
						}
						pointer ++;
					}
				}
			}
		}

		for (int i = 0; cumsum != null && i < cumsum.length; i++) {
			out += (outDelimiter + cumsum[i]);
		}

		return out;
	}

	public static String getOneRowOfConsolidatedData_working (String[][] data, int[][] columnsToExclude, String outDelimiter) {
		String out;

		out = "";
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (i == 0 || ! isContains(j, columnsToExclude[i - 1])) {
					if (i == 0 && j == 0) {
						out += data[i][j];
					} else {
						out += (outDelimiter + data[i][j]);
					}
				}
			}
		}

		return out;
	}

	public static String getFileNameId(int i) {
		String fileNameId;

		if (i <= -10) {
			fileNameId = "9" + (-i);
		} else if (i <= 0) {
			fileNameId = "90" + (-i);
		} else if (i <= 9) {
			fileNameId = "10" + i;
		} else {
			fileNameId = "1" + i;
		}

		return fileNameId;
	}

	public static String[] replaceStrings(String[] data, String strToBeReplaced, String replacementStr) {
		String[] result;
		
		result = new String[data.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = data[i].replaceAll(strToBeReplaced, replacementStr);
		}

		return result;
	}

	public static void displayStatusBar(int i) {
		if (i%5 == 0) {
			System.out.print(" ..." + i);
		}
	}
	/**
	 * Calculate Clinical Preventable Burden and Cost Effective by comparing baseline and intervention
	 * @param baseline
	 * @param intervention
	 * @param columnIndex int[] {QALY_column_index, costs_column_index}
	 * @return
	 */
	public static double[][] getCpbCe (double[][][] baseline, double[][][] intervention, int[] columnIndex) {
		double[][] cpbCe;

		cpbCe = new double[baseline.length][3];
		for (int i = 0; i < baseline.length; i++) {
			cpbCe[i][0] = intervention[i][columnIndex[0]][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUM] - baseline[i][columnIndex[0]][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUM];
			cpbCe[i][1] = intervention[i][columnIndex[0]][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE] - baseline[i][columnIndex[0]][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE];
			cpbCe[i][2] = (intervention[i][columnIndex[1]][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE] - baseline[i][columnIndex[1]][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE]) / (intervention[i][columnIndex[0]][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE] - baseline[i][columnIndex[0]][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE]);
		}

		return cpbCe;
	}

	public static double[][][] getCumSumAndPresentValue (HashMap<String, double[]> data, double discountRate, int discountBasekYear) {
		String [] keys;
		double[][][] output;
		double[] tmp;
		int discountYears;

		// keys: multi variable sorting
		keys = data.keySet().toArray(new String[0]);
//		keys = sortByNumericalOrder(keys);
		tmp = data.get(keys[0]);
		output = new double[keys.length][tmp.length][CUMSUM_PRESENTVALUE_ARRAY_STRUCTURE.length];
		for (int j = 0; j < tmp.length; j++) {
			output[discountBasekYear][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_ORIGINAL] = tmp[j];	//Original
			output[discountBasekYear][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_PRESENTVALUE] = tmp[j] / Math.pow((1 + discountRate), 1);	//Present Value
			output[discountBasekYear][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUM] = tmp[j];	//Cumulative Sum
			output[discountBasekYear][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE] = output[0][j][1];	//Cumulative Sum of Present Value
		}
		for (int i = discountBasekYear + 1; i < keys.length; i++) {
			tmp = data.get(keys[i]);
			discountYears = i - discountBasekYear;
			for (int j = 0; j < tmp.length; j++) {
				output[i][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_ORIGINAL] = tmp[j];	//Original
				output[i][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_PRESENTVALUE] = tmp[j] / Math.pow((1 + discountRate), discountYears);	//Present Value
				output[i][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUM] = output[i-1][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUM] + output[i][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_ORIGINAL];	//Cumulative Sum
				output[i][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE] = output[i-1][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_CUMSUMOFPRESENTVALUE] + output[i][j][CUMSUM_PRESENTVALUE_ARRAY_INDEX_PRESENTVALUE];	//Cumulative Sum of Present Value
			}
		}

		return output;
	}

	public static String[] sortByNumericalOrder (String[] keys) {
		float[] tmp;
		String[] keysSorted;

		tmp = new float[keys.length];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = Float.parseFloat(keys[i]);
		}
		Arrays.sort(tmp);
		keysSorted = new String[keys.length];
		for (int i = 0; i < keysSorted.length; i++) {
			keysSorted[i] = tmp[i] + "";
		}

		return keys;
	}

	public static double[][] getDiscountedValue (double[][] data, int[] columns, double discountRate) {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < columns.length; j++) {
				data[i][columns[j]] = data[i][columns[j]] * Math.pow((1 + discountRate), i);
			}
		}
		return data;
	}

	@SuppressWarnings("resource")
	public static LinkedHashMap<String, double[]> loadAndAggregateData (String filename, int headerRowNumber, String[] columnsTheKey, String[][] columnsTheValues, String filters, String aggregateFunction, double[][] weights, String[] weightMatchingColumns) throws CustomException {
		BufferedReader reader;
		String delimiter, key;
		String[] tmp = null;
		int[] indicesColumnsTheKey = null;
		int[][][] indicesColumnsTheValues = null;
		Object[] tmp1;
		int[][][] indicesOfWeightsMatchingColumns;
		double[] dataTmp = null, weightConstants;
		LinkedHashMap<String, double[]> data = new LinkedHashMap<String, double[]>();
		int totalNumOfColumns;
		double weight;
		int[][][] filtersParsed;
		double tmp2;
		int x, y;

		try {
			delimiter = getDelimiterFromFilename(filename);
			reader = new BufferedReader(new FileReader(filename));

			for (int i = 0; i < headerRowNumber; i++) {
				tmp = reader.readLine().split(delimiter);
			}
			if (columnsTheKey != null) {
				indicesColumnsTheKey = indexOf(columnsTheKey, tmp, null, null);
				for (int i = 0; i < indicesColumnsTheKey.length; i++) {
					if (indicesColumnsTheKey[i] == -1) {
						throw new CustomException("key column specified does no match file header: " + columnsTheKey[i]);
					}
				}
			} else {
				throw new CustomException("key columns cannot be null");
			}
//			indicesOfWeightsMatchingColumns = parseColumnGroups(weightMatchingColumns, tmp);
			tmp1 = parseColumnGroups2(weightMatchingColumns, tmp);
			indicesOfWeightsMatchingColumns = (int[][][]) tmp1[0];
			weightConstants = (double[]) tmp1[1];

			totalNumOfColumns = 0;

			if (columnsTheValues != null) {
//				indicesColumnsTheValues = (int[][][]) parseColumnGroups2(columnsTheValues[1], tmp)[0];
				indicesColumnsTheValues = (int[][][]) parseColumnGroups3(columnsTheValues[1], tmp)[0];
				HashMap test = parseNew(columnsTheValues[1][0], OPERATOR_ARRAY_STRUCTURE, 0);
				totalNumOfColumns += indicesColumnsTheValues.length;
			}

			filtersParsed = parseFilter(filters, tmp);

			while (reader.ready()) {
				tmp = reader.readLine().split(delimiter);
				if (runFilter(tmp, filtersParsed)) {
					key = tmp[indicesColumnsTheKey[0]];
					for (int i = 1; i < indicesColumnsTheKey.length; i++) {
						key += ("\t" + tmp[indicesColumnsTheKey[i]]);
					}
					if (! data.containsKey(key)) {
						dataTmp = new double[totalNumOfColumns];
						data.put(key, dataTmp);
					} else {
						dataTmp = data.get(key);
					}

					weight = getWeight(weights, indicesOfWeightsMatchingColumns, weightConstants, tmp);

					if (columnsTheValues != null) {
						for (int i = 0; i < indicesColumnsTheValues.length; i++) {
							tmp2 = Double.parseDouble(tmp[indicesColumnsTheValues[i][0][1]]) * weight;
							for (int j = 1; j < indicesColumnsTheValues[i].length; j++) {
								x = indicesColumnsTheValues[i][j][0] / 10;
								y = indicesColumnsTheValues[i][j][0] % 10;
								if (OPERATOR_ARRAY_STRUCTURE[x][y].equals("+")) {
									if (i == 1) {
										if (j == 1 || j == 3) {
											tmp2 += .62895 * (Double.parseDouble(tmp[indicesColumnsTheValues[i][j][1]]) * weight);
										} else if (j==2) {
											tmp2 += .67748 * (Double.parseDouble(tmp[indicesColumnsTheValues[i][j][1]]) * weight);
										} else if (j==4 || j == 5) {
											tmp2 += .74946 * (Double.parseDouble(tmp[indicesColumnsTheValues[i][j][1]]) * weight);
										} else {
											tmp2 += (Double.parseDouble(tmp[indicesColumnsTheValues[i][j][1]]) * weight);
										}
									} else {
										tmp2 += (Double.parseDouble(tmp[indicesColumnsTheValues[i][j][1]]) * weight);
									}
								} else if (OPERATOR_ARRAY_STRUCTURE[x][y].equals("-")) {
									tmp2 -= (Double.parseDouble(tmp[indicesColumnsTheValues[i][j][1]]) * weight);
								} else {
									throw new CustomException("Unsupported operator: " + OPERATOR_ARRAY_STRUCTURE[indicesColumnsTheValues[i][j][0]]);
								}
							}
							if (aggregateFunction.equalsIgnoreCase("sum")) {
								dataTmp[i] += tmp2;
							} else {
								throw new CustomException("Unrecognized aggregate function specified: " + aggregateFunction);
							}
						}
					}
				}
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	@SuppressWarnings("resource")
	public static double[][] loadData_old (String filename, int headerRowNumber, String delimiter, String[] columnsToLoad, String[][] filters) throws CustomException {
		BufferedReader reader;
		String[] tmp = null;
		int[] indices;
		double[] dataTmp;
		ArrayList<double[]> data = new ArrayList<double[]>();

		try {
			if (delimiter == null) {
				delimiter = getDelimiterFromFilename(filename);
			}
			reader = new BufferedReader(new FileReader(filename));
			for (int i = 0; i < headerRowNumber; i++) {
				tmp = reader.readLine().split(delimiter);
			}
			indices = indexOf(columnsToLoad, tmp, null, null);
			for (int i = 0; i < indices.length; i++) {
				if (indices[i] == -1) {
					throw new CustomException("Columns to load specified do no match file header: " + columnsToLoad[i]);
				}
			}

			while (reader.ready()) {
				tmp = reader.readLine().split(delimiter);
				dataTmp = new double[indices.length];
				for (int i = 0; i < indices.length; i++) {
					dataTmp[i] = Double.parseDouble(tmp[indices[i]]);
				}
				data.add(dataTmp);
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data.toArray(new double[0][]);
	}

	public static double[][] loadData (String filename, int headerRowNumber, String delimiter, String[] columnsToLoad, String filters) throws CustomException {
		return (double[][]) loadData(filename, headerRowNumber, delimiter, columnsToLoad, null, filters)[0];
	}

	@SuppressWarnings("resource")
	public static Object[] loadData (String filename, int headerRowNumber, String delimiter, String[] numericColumnsToLoad, String[] stringColumnsToLoad, String filters) throws CustomException {
		BufferedReader reader;
		String[] tmp = null;
		int[] numericColumnsIndices = null, stringColumnsIndices = null;
		double[] dataTmpNumeric;
		ArrayList<double[]> dataNumeric = new ArrayList<double[]>();
		String[] dataTmpString;
		ArrayList<String[]> dataString = null;
		int[][][] filtersParsed;

		try {
			if (delimiter == null) {
				delimiter = getDelimiterFromFilename(filename);
			}
			reader = new BufferedReader(new FileReader(filename));
			for (int i = 0; i < headerRowNumber; i++) {
				tmp = reader.readLine().split(delimiter);
			}
			if (numericColumnsToLoad != null) {
				numericColumnsIndices = indexOf(numericColumnsToLoad, tmp, null, null);
				for (int i = 0; i < numericColumnsIndices.length; i++) {
					if (numericColumnsIndices[i] == -1) {
						throw new CustomException("Columns to load specified do no match file header: " + numericColumnsToLoad[i]);
					}
				}
				dataNumeric = new ArrayList<double[]>();
			}
			if (stringColumnsToLoad != null) {
				stringColumnsIndices = indexOf(stringColumnsToLoad, tmp, null, null);
				for (int i = 0; i < stringColumnsIndices.length; i++) {
					if (stringColumnsIndices[i] == -1) {
						throw new CustomException("Columns to load specified do no match file header: " + stringColumnsToLoad[i]);
					}
				}
				dataString = new ArrayList<String[]>();
			}

			filtersParsed = parseFilter(filters, tmp);

			while (reader.ready()) {
				tmp = reader.readLine().split(delimiter);

				if (runFilter(tmp, filtersParsed)) {
					if (numericColumnsToLoad != null) {
						dataTmpNumeric = new double[numericColumnsIndices.length];
						for (int i = 0; i < numericColumnsIndices.length; i++) {
							dataTmpNumeric[i] = Double.parseDouble(tmp[numericColumnsIndices[i]]);
						}
						dataNumeric.add(dataTmpNumeric);
					}

					if (stringColumnsToLoad != null) {
						dataTmpString = new String[stringColumnsIndices.length];
						for (int i = 0; i < stringColumnsIndices.length; i++) {
							dataTmpString[i] = tmp[stringColumnsIndices[i]];
						}
						dataString.add(dataTmpString);
					}
				}
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (stringColumnsToLoad == null) {
			return new Object[] {dataNumeric.toArray(new double[0][]), null};
		} else if (numericColumnsToLoad == null) {
			return new Object[] {null, dataString.toArray(new String[0][])};
		} else {
			return new Object[] {dataNumeric.toArray(new double[0][]), dataString.toArray(new String[0][])};
		}
	}

	@SuppressWarnings("resource")
	public static HashMap<String, String> loadDataToHash (String filename, int headerRowNumber, String delimiter, String[] columnsTheKey, String[] columnsTheValue, String prefixOfCommentRow, String[] prefixOfDirectColumnIndices, String lineEndSuffix, String filters) throws CustomException {
		BufferedReader reader;
		String[] tmp = null;
		int[] columnsTheKeyIndices = null, columnsTheValueIndices = null;
		String key, value;
		HashMap<String, String> data = null;
		int[][][] parsedFilters;

		try {
			if (delimiter == null) {
				delimiter = getDelimiterFromFilename(filename);
			}
			reader = new BufferedReader(new FileReader(filename));
			for (int i = 0; i < headerRowNumber; i++) {
				tmp = reader.readLine().split(delimiter);
			}
			if (columnsTheKey != null) {
				columnsTheKeyIndices = indexOf(columnsTheKey, tmp, null, prefixOfDirectColumnIndices);
				for (int i = 0; i < columnsTheKeyIndices.length; i++) {
					if (columnsTheKeyIndices[i] == -1) {
						throw new CustomException("Columns to load specified do no match file header: " + columnsTheKey[i]);
					}
				}
			}
			if (columnsTheValue != null) {
				columnsTheValueIndices = indexOf(columnsTheValue, tmp, null, prefixOfDirectColumnIndices);
				for (int i = 0; i < columnsTheValueIndices.length; i++) {
					if (columnsTheValueIndices[i] == -1) {
						throw new CustomException("Columns to load specified do no match file header: " + columnsTheValue[i]);
					}
				}
			}

			parsedFilters = parseFilter(filters, tmp);
			data = new HashMap<String, String>();
			while (reader.ready()) {
				tmp = reader.readLine().split(delimiter);
				for (int i = 0; i < tmp.length; i++) {
					tmp[i] = tmp[i].trim();
				}
				if (tmp[tmp.length - 1].endsWith(lineEndSuffix)) {
					tmp[tmp.length - 1] = tmp[tmp.length - 1].substring(0, tmp[tmp.length - 1].length() - lineEndSuffix.length());
				}
				if (! tmp[0].startsWith(prefixOfCommentRow)) {
					if (runFilter(tmp, parsedFilters)) {
						key = "";
						for (int i = 0; i < columnsTheKeyIndices.length; i++) {
							key += tmp[columnsTheKeyIndices[i]];
						}
						value = "";
						for (int i = 0; i < columnsTheValueIndices.length; i++) {
							value += tmp[columnsTheValueIndices[i]];
						}
						data.put(key, value);
					}
				}
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static String getDelimiterFromFilename (String filename) throws CustomException {
		String delimiter = null;
		boolean found;

		found = false;
		for (int i = 0; i < FILENAME_DELIMITER_PAIRS.length; i++) {
			if (filename.endsWith(FILENAME_DELIMITER_PAIRS[i][0])) {
				delimiter = FILENAME_DELIMITER_PAIRS[i][1];
				found = true;
				break;
			}
		}

		if (! found) {
			throw new CustomException("While determining delimiter from file name extension, found unrecognized file extension in file name: " + filename);
		}

		return delimiter;
	}

	/**
	 * Indices of elements of array1 in array2
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static int[] indexOf (String[] array1, String[] array2, String[] prefixesToIgnore, String[] prefixesForDirectColumnIndices) {
		int[] indices;
		String tmp;

		indices = new int[array1.length];
		if ((prefixesToIgnore == null || prefixesToIgnore.length < 1) && (prefixesForDirectColumnIndices == null || prefixesForDirectColumnIndices.length < 1)) {
			for (int i = 0; i < array1.length; i++) {
				indices[i] = -1;
				for (int j = 0; j < array2.length; j++) {
					if (array1[i].trim().equalsIgnoreCase(array2[j].trim())) {
						indices[i] = j;
						break;
					}
				}
			}
		} else if (prefixesToIgnore != null && prefixesToIgnore.length >= 1) {
			for (int i = 0; i < array1.length; i++) {
				indices[i] = -1;
				tmp = array1[i];
				for (int j = 0; j < prefixesToIgnore.length; j++) {
					if (array1[i].startsWith(prefixesToIgnore[j])) {
						tmp = array1[i].substring(array1[i].indexOf(prefixesToIgnore[j]) + prefixesToIgnore[j].length(), array1[i].length());
						break;
					}
				}
				for (int j = 0; j < array2.length; j++) {
					if (tmp.equalsIgnoreCase(array2[j])) {
						indices[i] = j;
						break;
					}
				}
			}
		} else if (prefixesForDirectColumnIndices != null && prefixesForDirectColumnIndices.length >= 1) {
			for (int i = 0; i < indices.length; i++) {
				indices[i] = -1;
				for (int j = 0; j < prefixesForDirectColumnIndices.length; j++) {
					if (array1[i].startsWith(prefixesForDirectColumnIndices[j])) {
						indices[i] = Integer.parseInt(array1[i].substring(array1[i].indexOf(prefixesForDirectColumnIndices[j]) + prefixesForDirectColumnIndices[j].length(), array1[i].length())) - 1;
					}
				}
			}
		}

		return indices;
	}

	// TODO integer value; grouping of multiple layers of AND and OR
	public static int[][][] parseFilter(String filters, String[] columnNames) throws CustomException {
		int[][][] parsedFilters;
		String[] tmp1, tmp2, tmp;

		if (filters == null) {
			return null;
		}

		if (filters.contains("(") || filters.contains(")")) {
			throw new CustomException("Contains logic operand currently not supported: " + filters);
		}

		tmp1 = filters.split("\\|");
		parsedFilters = new int[tmp1.length][][];
		for (int i = 0; i < tmp1.length; i++) {
			tmp1[i] = tmp1[i].trim();
			tmp2 = tmp1[i].split("&");
			parsedFilters[i] = new int[tmp2.length][CRITERIA_ARRAY_STRUCTURE.length];
			for (int j = 0; j < tmp2.length; j++) {
				tmp2[j] = tmp2[j].trim();
				for (int k = 0; k < LOGIC_OPERATORS.length; k++) {
					if (tmp2[j].contains(LOGIC_OPERATORS[k])) {
						tmp = tmp2[j].split(LOGIC_OPERATORS[k]);
						parsedFilters[i][j][CRITERION_ARRAY_OPERATOR_INDEX] = k;
						parsedFilters[i][j][CRITERION_ARRAY_VARIABLENAME_INDEX] = indexOf(new String[] {tmp[0].trim()}, columnNames, null, null)[0];
						if (parsedFilters[i][j][1] < 0) {
							throw new CustomException ("Column name not found: " + tmp1[j]);
						}
						parsedFilters[i][j][CRITERION_ARRAY_VALUE_INDEX] = Integer.parseInt(tmp[1].trim());
						break;
					}
				}
			}
		}

		return parsedFilters;
	}

	public static boolean runFilter(String[] data, int[][][] filters) {
		boolean isPass;

		if (filters == null) {
			isPass = true;
		} else {
			if (data != null && data.length > 1) {
				try {
//						if (Double.parseDouble(data[0]) > -99 && Double.parseDouble(data[0]) != 0 && Double.parseDouble(data[2]) > 0 && Double.parseDouble(data[3]) > 0 && Double.parseDouble(data[4]) > 0) {
//							isPassFilter = true;
//						} else {
//							isPassFilter = false;
//						}

					isPass = false;
					// OR relationships
					for (int i = 0; i < filters.length; i++) {
		
						// AND relationships
						for (int j = 0; j < filters[i].length; j++) {
							switch (filters[i][j][CRITERION_ARRAY_OPERATOR_INDEX]) {
								case 0:
									if (Double.parseDouble(data[filters[i][j][CRITERION_ARRAY_VARIABLENAME_INDEX]]) == filters[i][j][CRITERION_ARRAY_VALUE_INDEX]) {
										isPass = true;
									}
									break;
								case 1:
									if (Double.parseDouble(data[filters[i][j][CRITERION_ARRAY_VARIABLENAME_INDEX]]) != filters[i][j][CRITERION_ARRAY_VALUE_INDEX]) {
										isPass = true;
									}
									break;
								case 2:
									if (Double.parseDouble(data[filters[i][j][CRITERION_ARRAY_VARIABLENAME_INDEX]]) >= filters[i][j][CRITERION_ARRAY_VALUE_INDEX]) {
										isPass = true;
									}
									break;
								case 3:
									if (Double.parseDouble(data[filters[i][j][CRITERION_ARRAY_VARIABLENAME_INDEX]]) <= filters[i][j][CRITERION_ARRAY_VALUE_INDEX]) {
										isPass = true;
									}
									break;
								case 4:
									if (Double.parseDouble(data[filters[i][j][CRITERION_ARRAY_VARIABLENAME_INDEX]]) > filters[i][j][CRITERION_ARRAY_VALUE_INDEX]) {
										isPass = true;
									}
									break;
								case 5:
									if (Double.parseDouble(data[filters[i][j][CRITERION_ARRAY_VARIABLENAME_INDEX]]) < filters[i][j][CRITERION_ARRAY_VALUE_INDEX]) {
										isPass = true;
									}
									break;
								default:
									isPass = false;
									break;
							}
		
							if (isPass == false) {
								break;
							}
						}
		
						if (isPass == true) {
							break;
						}
					}
				} catch (NumberFormatException nfe) {
					isPass = true;
				}
			} else {
				isPass = false;
			}
		}

		return isPass;
	}

	public static void saveData (double[][] data, String[] columnLabels, String[] rowLabels, String filename, boolean isToTranspose) {
		PrintWriter writer;
		String delimiter;

		try {
			delimiter = getDelimiterFromFilename(filename);
			writer = new PrintWriter(new FileWriter(filename));
			if (columnLabels != null) {
				if (rowLabels != null) {
					writer.print(delimiter);
				}
				writer.print(columnLabels[0]);
				for (int i = 1; i < columnLabels.length; i++) {
					writer.print(delimiter + columnLabels[i]);
				}
				writer.println("");
			}

			if (isToTranspose) {
				for (int i = 0; i < data[0].length; i++) {
					if (rowLabels != null) {
						writer.print(rowLabels[i] + delimiter);
					}
					writer.print(data[0][i]);
					for (int j = 1; j < data.length; j++) {
						writer.print(delimiter + data[j][i]);
					}
					writer.println("");
				}
			} else {
				for (int i = 0; i < data.length; i++) {
					if (rowLabels != null) {
						writer.print(rowLabels[i] + delimiter);
					}
					writer.print(data[i][0]);
					for (int j = 1; j < data[i].length; j++) {
						writer.print(delimiter + data[i][j]);
					}
					writer.println("");
				}
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CustomException e) {
			e.printStackTrace();
		}
	}

	public static void save (double[][][] data, String[] columnLabels, String[] rowLabels, String filename, double[][] filters, boolean isToTranspose, String numberFormat) {
		PrintWriter writer;
		String delimiter;
		int[] maxDimensions;
		DecimalFormat df;

		df = new DecimalFormat(numberFormat);	//"#,###,##0.0", "0.0", "0"
		try {
			delimiter = getDelimiterFromFilename(filename);
			writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			if (columnLabels != null) {
				if (rowLabels != null) {
					writer.print("Years_Of_Intervention" + delimiter + "Metrics" + delimiter);
				}
				writer.print(columnLabels[0]);
				for (int i = 1; i < columnLabels.length; i++) {
					writer.print(delimiter + columnLabels[i]);
				}
				writer.println("");
			}

			maxDimensions = getMaximumDimensions(data);
			if (isToTranspose) {
//				for (int i = 0; i < maxDimensions[0]; i++) {
//					if (filters == null || isContains(i, filters[1])) {
				for (double i : filters[1]) {
					if (i >= 0 && i < maxDimensions[0]) {
						for (int j = 0; j < maxDimensions[1]; j++) {
							writer.print(i);
							if (rowLabels != null) {
								writer.print(delimiter + rowLabels[j]);
							}
							for (int k = 0; k < data.length; k++) {
								if (i < data[k].length | j < data[k][(int) i].length) {
									writer.print(delimiter + df.format(data[k][(int) i][j]));
								} else {
									writer.print(delimiter);
								}
							}
							writer.println("");
						}
					}
				}
			} else {
				for (int i = 0; i < maxDimensions[0]; i++) {
					for (int j = 0; j < data.length; j++) {
						writer.print(i);
						if (rowLabels != null) {
							writer.print(delimiter + rowLabels[j]);
						}
						if (i < data[j].length) {
							for (int k = 0; k < data[j][i].length; k++) {
								writer.print(delimiter + df.format(data[j][i][k]));
							}
						} else {
							for (int k = 0; k < maxDimensions[1]; k++) {
								writer.print(delimiter);
							}
						}
						writer.println("");
					}
				}
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CustomException e) {
			e.printStackTrace();
		}
	}

	public static boolean isContains (double[] valuesToBeFound, double[][][] array) {
		boolean isContained = false;

		for (int i = 0; !isContained && i < array.length; i++) {
			for (int j = 0; !isContained && j < array[i].length; j++) {
				for (int k = 0; k < valuesToBeFound.length; k++) {
					if (valuesToBeFound[k] == array[i][j][0]) {
						isContained = true;
						break;
					}
				}
			}
		}

		return isContained;
	}

	public static boolean isContains (double valueToBeFound, double[] array) {
		boolean isContained = false;

		for (int i = 0; i < array.length; i++) {
			if (valueToBeFound == array[i]) {
				isContained = true;
				break;
			}
		}

		return isContained;
	}

	public static boolean isContains (int valueToBeFound, int[] array) {
		boolean isContained = false;

		for (int i = 0; i < array.length; i++) {
			if (valueToBeFound == array[i]) {
				isContained = true;
				break;
			}
		}

		return isContained;
	}

	public static int[][][] parseColumnGroups (String[] columnNames, String[] header) {
		int[][][] indices;
		int counter;
		String[] tmp;
		String operator;

		indices = new int[columnNames.length][][];
		for (int i = 0; i < columnNames.length; i++) {
			tmp = columnNames[i].split("[-+*/]+");
			indices[i] = new int[tmp.length][2];
			indices[i][0][0] = -1;
			indices[i][0][1] = indexOf(new String[] {tmp[0]}, header, null, null)[0];
			counter = 0;
			for (int j = 1; j < tmp.length; j++) {
				counter += tmp[j - 1].length() + 1;
				operator = columnNames[i].substring(counter - 1, counter);
				indices[i][j][0] = -1;
				for (int k = 0; k < OPERATOR_ARRAY_STRUCTURE.length; k++) {
					if (operator.equals(OPERATOR_ARRAY_STRUCTURE[k])) {
						indices[i][j][0] = k;
						break;
					}
				}
				tmp[j] = tmp[j].trim();
				indices[i][j][1] = indexOf(new String[] {tmp[j]}, header, null, null)[0];
				if (indices[i][j][1] == -1) {
					
				}
			}
		}

		return indices;
	}

	public static Object[] parseColumnGroups2 (String[] columnNames, String[] header) throws CustomException {
		int[][][] indices;
		int counter;
		double value;
		String[] tmp;
		String operator;
		ArrayList<Double> constants;
		double[] tmp2;

		indices = new int[columnNames.length][][];
		constants = new ArrayList<Double>();
		for (int i = 0; i < columnNames.length; i++) {
			tmp = columnNames[i].split(" [-+*/] ");
			indices[i] = new int[tmp.length][2];
			indices[i][0][0] = -1;
			counter = tmp[0].length() + 1;
			tmp[0] = tmp[0].trim();
			indices[i][0][1] = indexOf(new String[] {tmp[0]}, header, null, null)[0];
			for (int j = 1; j < tmp.length; j++) {
				operator = columnNames[i].substring(counter, counter + 1);
				indices[i][j][0] = getOperatorIndex(operator, OPERATOR_ARRAY_STRUCTURE);
				counter += tmp[j].length() + 3;
				tmp[j] = tmp[j].trim();
				indices[i][j][1] = indexOf(new String[] {tmp[j]}, header, null, null)[0];
				if (indices[i][j][1] == -1) {
					try {
						value = Double.parseDouble(tmp[j]);
						constants.add(value);
						indices[i][j][1] = - constants.size();
					}  catch(NumberFormatException nfe) {
						throw new CustomException ("Unrecognized column name or value: " + tmp[j]);
					}

				}
			}
		}

		if (constants.size() < 1) {
			tmp2 = null;
		} else {
			tmp2 = new double[constants.size()];
			for (int i = 0; i < tmp2.length; i++) {
				tmp2[i] = constants.get(i).doubleValue();
			}
		}

		return new Object[] {indices, tmp2};
	}

	public static int getOperatorIndex(String operator, String[][] arrayOfAllOperators) {
		int result;

		result = -1;
		for (int i = 0; i < arrayOfAllOperators.length; i++) {
			for (int j = 0; j < arrayOfAllOperators[i].length; j++) {
				if (operator.equals(arrayOfAllOperators[i][j])) {
					result = i * 10 + j;
					break;
				}
			}
		}

		return result;
	}

	public static Object[] parseColumnGroups3 (String[] columnNames, String[] header) throws CustomException {
		int[][][] indices;
		int counter;
		double value;
		String[] tmp;
		String operator;
		ArrayList<Double> constants;
		double[] tmp2;

		indices = new int[columnNames.length][][];
		constants = new ArrayList<Double>();
		for (int i = 0; i < columnNames.length; i++) {
			indices[i] = parseColumnGroup(columnNames[i], header, " [+-] ");
			for (int j = 1; j < indices[i].length; j++) {
				if (indices[i][j][1] == -1) {
//					try {
//						value = Double.parseDouble(tmp[j]);
//						constants.add(value);
//						indices[i][j][1] = - constants.size();
//					}  catch(NumberFormatException nfe) {
//						throw new CustomException ("Unrecognized column name or value: " + tmp[j]);
//					}
				}
			}
		}

		if (constants.size() < 1) {
			tmp2 = null;
		} else {
			tmp2 = new double[constants.size()];
			for (int i = 0; i < tmp2.length; i++) {
				tmp2[i] = constants.get(i).doubleValue();
			}
		}

		return new Object[] {indices, tmp2};
	}

	public static int[][] parseColumnGroup (String columnName, String[] header, String operators) throws CustomException {
		int[][] indices;
		int counter;
		String[] tmp;
		String operator;

		tmp = columnName.split(operators);
		indices = new int[tmp.length][2];
		indices[0][0] = -1;
		counter = tmp[0].length() + 1;
		tmp[0] = tmp[0].trim();
		indices[0][1] = indexOf(new String[] {tmp[0]}, header, null, null)[0];
		for (int i = 1; i < tmp.length; i++) {
			operator = columnName.substring(counter, counter + 1);
			indices[i][0] = getOperatorIndex(operator, OPERATOR_ARRAY_STRUCTURE);
			counter += tmp[i].length() + 3;
			tmp[i] = tmp[i].trim();

			indices[i][1] = indexOf(new String[] {tmp[i]}, header, null, null)[0];
		}

		return indices;
	}

//	public static int[][] parse (String columnName, String[] header, String[][] operators) {
//		int[][] indices = null;
//		int[] sizes;
//		int counter;
//		String[] tmp, tmp2;
//		String operator;
//		Object test;
//
//		sizes = new int[operators.length];
//		test = Array.newInstance(String.class, operators.length);
//		for (int i = 0; i < operators.length; i++) {
//			tmp = columnName.split(" [-+] ");
//			indices = new int[tmp.length][2];
//			indices[0][0] = -1;
//			counter = tmp[0].length() + 1;
//			tmp[0] = tmp[0].trim();
//			indices[0][1] = indexOf(new String[] {tmp[0]}, header, null, null)[0];
//			for (int j = 1; j < tmp.length; j++) {
//				operator = columnName.substring(counter, counter + 1);
//				indices[j][0] = -1;
//				for (int k = 0; k < OPERATOR_ARRAY_STRUCTURE.length; k++) {
//					if (operator.equals(OPERATOR_ARRAY_STRUCTURE[k])) {
//						indices[j][0] = k;
//						break;
//					}
//				}
//				counter += tmp[j].length() + 3;
//				tmp[j] = tmp[j].trim();
//
//				indices[j][1] = indexOf(new String[] {tmp[j]}, header, null, null)[0];
//			}
//		}
//
//		return indices;
//	}

	public static HashMap parseNew (String textToParse, String[][] operators, int index) {
		HashMap results;
		HashMap<String, HashMap> tmp1;
		ArrayList<String> tmp2;
		String[] tmp;
		String operatorRegex, tmp3, operator;
		int counter;

		if (index == operators.length - 1) {
			results = new HashMap<String, ArrayList<String>>();
			tmp = textToParse.split(" " + getRegex(operators[index]) + " ");
			counter = tmp[0].length() + 1;
			for (int i = 0; i < tmp.length; i++) {
				if (tmp.length == 1) {
					operator = " ";
				} else {
					operator = textToParse.substring(counter, counter + 1);
				}
				if (results.containsKey(operator)) {
					tmp2 = (ArrayList<String>) results.get(operator);
				} else {
					tmp2 = new ArrayList<String>();
					results.put(operator, tmp2);
				}
				tmp2.add(tmp[i]);
			}
		} else {
			results = new HashMap<String, HashMap>();
			tmp = textToParse.split(" " + getRegex(operators[index]) + " ");
			counter = tmp[0].length() + 1;
			for (int i = 0; i < tmp.length; i++) {
				if (tmp.length == 1) {
					operator = " ";
				} else {
					operator = textToParse.substring(counter, counter + 1);
				}
				if (results.containsKey(operator)) {
					tmp1 = (HashMap<String, HashMap>) results.get(operator);
				} else {
					tmp1 = new HashMap<String, HashMap>();
					results.put(operator, tmp1);
				}
				tmp1.put(operator, parseNew(tmp[i], operators, index + 1));
			}
		}

		return results;
	}

	public static String getRegex (String[] operators) {
		String tmp;

		tmp = "[";
		for (int i = 0; i < operators.length; i++) {
			if (operators[i].equals("-") || operators[i].equals("+") || operators[i].equals("*") || operators[i].equals("^") || operators[i].equals(".") || operators[i].equals("&") || operators[i].equals("{") || operators[i].equals("}") || operators[i].equals("[") || operators[i].equals("]") || operators[i].equals("(") || operators[i].equals(")") || operators[i].equals(":") || operators[i].equals("$") || operators[i].equals("\\")) {
				tmp += "\\" + operators[i];
			} else {
				tmp += operators[i];
			}
		}
		tmp += "]";

		return tmp;
	}

	public static String[][] parseWithTwoLayerDelimieters (String columnNames, String firstLayerDelimiter, String secondLayerDelimiter) {
		String[] tmp;
		String[][] parsedResults;

		tmp = columnNames.split(firstLayerDelimiter);
		parsedResults = new String[tmp.length][];
		for (int i = 0; i < tmp.length; i++) {
			parsedResults[i] = tmp[i].trim().split(secondLayerDelimiter);
			for (int j = 0; j < parsedResults[i].length; j++) {
				parsedResults[i][j] = parsedResults[i][j].trim();
			}
		}

		return parsedResults;
	}

	// Assuming array size uneven
	public static String[][] transpose (String[][] array) {
		String[][] transposed;
		int[] sizes;
		int maxNumColumns;

		maxNumColumns = getMaximumColumns(array);

		sizes = new int[maxNumColumns];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				sizes[j] = i + 1;
			}
		}

		transposed = new String[maxNumColumns][];
		for (int i = 0; i < sizes.length; i++) {
			transposed[i] = new String[sizes[i]];
		}

		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				transposed[j][i] = array[i][j];
			}
		}

		return transposed;
	}

	public static int[] getMaximumDimensions (double[][][] array) {
		int[] max;

		max = new int[] {-1, -1};
		for (int i = 0; i < array.length; i++) {
			if (array[i].length > max[0]) {
				max[0] = array[i].length;
			}
			for (int j = 0; j < array[i].length; j++) {
				if (array[i][j].length > max[1]) {
					max[1] = array[i][j].length;
				}
			}
		}
		return max;
	}

	public static int[] getMaximumDimensions (Object[][][] array) {
		int[] max;
		int tmp;

		max = new int[] {-1, -1};
		for (int i = 0; i < array.length; i++) {
			if (array[i].length > max[0]) {
				max[0] = array[i].length;
			}
			tmp = getMaximumColumns(array[i]);
			if (tmp > max[1]) {
				max[1] = tmp;
			}
		}
		return max;
	}

	public static int getMaximumColumns (Object[][] array) {
		int maxNumColumns;

		maxNumColumns = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i].length > maxNumColumns) {
				maxNumColumns = array[i].length;
			}
		}

		return maxNumColumns;
	}

	public static double getWeight (double[][] weights, int[][][] keyColumnIndices, String[] oneRowOfData) throws CustomException {
		double[] oneRowOfDataNumeric;

		oneRowOfDataNumeric = new double[oneRowOfData.length];
		for (int i = 0; i < oneRowOfDataNumeric.length; i++) {
			oneRowOfDataNumeric[i] = Double.parseDouble(oneRowOfData[i]);
		}

		return getWeight(weights, keyColumnIndices, oneRowOfDataNumeric);
	}

	public static double getWeight (double[][] weights, int[][][] keyColumnIndices, double[] oneRowOfData) throws CustomException {
		double[] key;
		boolean isFound = false;
		int rowNumberFound;

		rowNumberFound = -1;
		key = new double[keyColumnIndices.length];
		for (int i = 0; i < keyColumnIndices.length; i++) {
			key[i] = oneRowOfData[keyColumnIndices[i][0][1]];
			for (int j = 1; j < keyColumnIndices[i].length; j++) {
				if (OPERATOR_ARRAY_STRUCTURE[keyColumnIndices[i][j][0]].equals("+")) {
					key[i] += oneRowOfData[keyColumnIndices[i][j][1]];
				} else if (OPERATOR_ARRAY_STRUCTURE[keyColumnIndices[i][j][0]].equals("-")) {
					key[i] -= oneRowOfData[keyColumnIndices[i][j][1]];
				} else if (OPERATOR_ARRAY_STRUCTURE[keyColumnIndices[i][j][0]].equals("*")) {
					key[i] *= oneRowOfData[keyColumnIndices[i][j][1]];
				} else if (OPERATOR_ARRAY_STRUCTURE[keyColumnIndices[i][j][0]].equals("/")) {
					key[i] /= oneRowOfData[keyColumnIndices[i][j][1]];
				} else {
					throw new CustomException ("Unrecogenized operator index: " + keyColumnIndices[i][j][0]);
				}
			}
		}
		for (int i = 0; i < weights.length; i++) {
			isFound = true;
			for (int j = 0; j < keyColumnIndices.length; j++) {
				if (key[j] != weights[i][j]) {
					isFound = false;
					break;
				}
			}
			if (isFound) {
				rowNumberFound = i;
				break;
			}
		}
		if (isFound) {
			return weights[rowNumberFound][weights[0].length - 1];
		} else {
			return 1;
		}
	}

//*	Backup of the working version
//	public static double getWeight (double[][] weights, int[][] keyColumnIndices, double[] oneRowOfData) {
//		String[] tmp;
//		double[] key;
//		boolean isFound = false;
//		int rowNumberFound;
//
//		rowNumberFound = -1;
//		key = new double[keyColumnIndices.length];
//		for (int i = 0; i < keyColumnIndices.length; i++) {
//			for (int j = 0; j < keyColumnIndices[i].length; j++) {
//				key[i] += oneRowOfData[keyColumnIndices[i][j]];
//			}
//		}
//		for (int i = 0; i < weights.length; i++) {
//			isFound = true;
//			for (int j = 0; j < keyColumnIndices.length; j++) {
//				if (key[j] != weights[i][j]) {
//					isFound = false;
//					break;
//				}
//			}
//			if (isFound) {
//				rowNumberFound = i;
//				break;
//			}
//		}
//		if (isFound) {
//			return weights[rowNumberFound][weights[0].length - 1];
//		} else {
//			return 1;
//		}
//	}

	public static double getWeight (double[][] weights, int[][][] keyColumnIndices, double[] keyColumnConstants, String[] oneRowOfData) throws CustomException {
		double[] oneRowOfDataNumeric;

		oneRowOfDataNumeric = new double[oneRowOfData.length];
		for (int i = 0; i < oneRowOfDataNumeric.length; i++) {
			oneRowOfDataNumeric[i] = Double.parseDouble(oneRowOfData[i]);
		}

		return getWeight(weights, keyColumnIndices, keyColumnConstants, oneRowOfDataNumeric);
	}

	public static double getWeight (double[][] weights, int[][][] keyColumnIndices, double[] keyConstants, double[] oneRowOfData) throws CustomException {
		double[] key;
		boolean isFound = false;
		int rowNumberFound;
		int x, y;

		rowNumberFound = -1;
		key = new double[keyColumnIndices.length];
		for (int i = 0; i < keyColumnIndices.length; i++) {
			key[i] = oneRowOfData[keyColumnIndices[i][0][1]];
			for (int j = 1; j < keyColumnIndices[i].length; j++) {
				x = keyColumnIndices[i][j][0] / 10;
				y = keyColumnIndices[i][j][0] % 10;
				if (OPERATOR_ARRAY_STRUCTURE[x][y].equals("+")) {
					if (keyColumnIndices[i][j][1] >= 0) {
						key[i] += oneRowOfData[keyColumnIndices[i][j][1]];
					} else {
						key[i] += keyConstants[-1 - keyColumnIndices[i][j][1]];
					}
				} else if (OPERATOR_ARRAY_STRUCTURE[x][y].equals("-")) {
					if (keyColumnIndices[i][j][1] >= 0) {
						key[i] -= oneRowOfData[keyColumnIndices[i][j][1]];
					} else {
						key[i] -= keyConstants[-1 - keyColumnIndices[i][j][1]];
					}
//				} else if (OPERATOR_ARRAY_STRUCTURE[keyColumnIndices[i][j][0]].equals("*")) {
//					if (keyColumnIndices[i][j][1] >= 0) {
//						key[i] *= oneRowOfData[keyColumnIndices[i][j][1]];
//					} else {
//						key[i] *= keyConstants[-1 - keyColumnIndices[i][j][1]];
//					}
//				} else if (OPERATOR_ARRAY_STRUCTURE[keyColumnIndices[i][j][0]].equals("/")) {
//					if (keyColumnIndices[i][j][1] >= 0) {
//						key[i] /= oneRowOfData[keyColumnIndices[i][j][1]];
//					} else {
//						key[i] /= keyConstants[-1 - keyColumnIndices[i][j][1]];
//					}
				} else {
					throw new CustomException ("Unsupported operator: " + keyColumnIndices[i][j][0]);
				}
			}
		}
		for (int i = 0; i < weights.length; i++) {
			isFound = true;
			for (int j = 0; j < keyColumnIndices.length; j++) {
				if (key[j] != weights[i][j]) {
					isFound = false;
					break;
				}
			}
			if (isFound) {
				rowNumberFound = i;
				break;
			}
		}
		if (isFound) {
			return weights[rowNumberFound][weights[0].length - 1];
		} else {
			return 1;
		}
	}

	public static double[][] scaleWeight (double[][] weights, double scale) {
		double[][] newWeights;
		double sum;
		int pointer;

		pointer = weights[0].length - 1;
		sum = 0;
		for (int i = 0; i < weights.length; i++) {
			sum += weights[i][pointer];
		}
		newWeights = new double[weights.length][weights[0].length];
		for (int i = 0; i < newWeights.length; i++) {
			for (int j = newWeights[i].length - 2; j >= 0; j--) {
				newWeights[i][j] = weights[i][j];
			}
			newWeights[i][pointer] = (weights[i][pointer] / sum) * scale;
		}

		return newWeights;
	}

	public static double[][] convert(String[][] data) {
		double[][] dataConverted;

		dataConverted = new double[data.length][];
		for (int i = 0; i < dataConverted.length; i++) {
			dataConverted[i] = new double[data[i].length];
			for (int j = 0; j < dataConverted[i].length; j++) {
				dataConverted[i][j] = Double.parseDouble(data[i][j].trim());
			}
		}

		return dataConverted;
	}

	public static double[] convert(String[] data) {
		double[] dataConverted;

		dataConverted = new double[data.length];
		for (int i = 0; i < dataConverted.length; i++) {
			dataConverted[i] = Double.parseDouble(data[i].trim());
		}

		return dataConverted;
	}

//	Backup of a working copy
//	public static void parseAndRunConfigureFile (String configFile) throws CustomException {
//		double[][] weights;
//		double[][][] dataBaseline, dataIntervention, cpbCe;
//		double discountRate;
//		double[] yearsOfInterventionForSummaryReport;
//		String[][] scenarios, columnsToCombine, weightKeyColumns;
//		int baselineId = -1;
//		int weightFileHeaderRowNumber = 1, adj;
//		String[] columnLabels, columnsTheKey, weightFileColumnsToLoad, weightMatchingColumns, tmp;
//		String fileName, fileNameTemplate, simulationDataLoadingFilters, aggregateFunction, weightFile, weightFileLoadingCriteria;
//		String outfile, outFileNumberFormat;
//		HashMap <String, String> parameters;
//		double populationAdjScale;
//		int[] columnIndicesOfQalyAndCosts = new int[] {0, 1};
//		int numColumnsCpbCe = 3;
//
//		parameters = loadDataToHash(configFile, 0, ":\t", new String[] {"Col#1"}, new String[] {"Col#2"}, "#", new String[] {"Col#"}, ";", null);
//		fileNameTemplate = parameters.get("FileNameTemplate");
//		scenarios = parse2(parameters.get("Scenarios"), ",", "---");
//		baselineId = Integer.parseInt(parameters.get("BaselineId"));
//		simulationDataLoadingFilters = parameters.get("SimulationDataLoadingFilters");
//		populationAdjScale = Double.parseDouble(parameters.get("PopulationAdjScale"));
//		outfile = parameters.get("OutFile");
//		outFileNumberFormat = parameters.get("OutFileNumberFormat");
//		if (outFileNumberFormat == null) {outFileNumberFormat = "0";};
//		weightFile = parameters.get("WeightFile");
//		weightKeyColumns = transpose(parse2(parameters.get("WeightKeyColumns"), ",", "---"));
//		weightFileColumnsToLoad = weightKeyColumns[0];
//		weightMatchingColumns = weightKeyColumns[1];
//		weightFileLoadingCriteria = parameters.get("WeightFileLoadingCriteria");
//		discountRate = Double.parseDouble(parameters.get("DiscountRate"));
//		yearsOfInterventionForSummaryReport = convert(parameters.get("YearsOfInterventionForSummaryReport").split(", "));
//
////		fileNameTemplate = "C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~64_Obesity-Adults/2015 Analysis/physicalactivitymodel_pp_20151012_GlobalN_<scenario>12.txt";
////		scenarios = new String[][] {{"1", "baseline"},	{"2", "Clincial_BaseCase"},	{"3", "Clinical_HighAdherence"},	{"4", "Clinical_LowAdherence"},	{"5", "Clinical_NoMaintanence"},	{"6", "Clinical_HighMaintanence"},	{"7", "Clinical_LowMaintanence"}, {"8", "Clincial_HighEffect"}, {"9", "Clincial_LowEffect"}, {"10", "Clincial_HighEffect_LowReferral"}, {"11", "Clincial_HighEffect_HighReferral"}, {"12", "Clincial_LowEffect__LowReferral_LowAdherence"}, {"13", "Clincial_LowEffect_HighReferral_HighAdherence"}, {"14", "Clincial_HighEffect_HighReferral_HighAdherence"}, {"15", "Clincial_HighEffect_HighReferral_HighMaintanence"}, {"16", "Clincial_LowEffect_LowReferral_LowMaintanence"}, {"17", "Clincial_LowEffect_LowReferral_HighMaintanence"}};
////		baselineId = 1;
////		outfile = "C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~64_Obesity-Adults/2015 Analysis/physicalactivitymodel_pp_20151012_CpbCe.csv";
//
//		if (baselineId < 0) {
//			for (int i = 0; i < scenarios.length; i++) {
//				if (scenarios[i][1].equalsIgnoreCase("baseline")) {
//					baselineId = i;
//					break;
//				}
//			}
//		}
//
//		try {
////			loadData(configFile, 0, " = ", null, stringColumnsToLoad, null);
//
////			double[][] test = loadData("C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~65_Obesity-Childhood/tests_physicalactivities_obesity/100000agents/physicalactivitymodel_pp_20150919_GlobalN_112.txt", 4, new String[] {"Initial_Age", "Time", "Sex", "Ethnicity", "Region", "CVD_Disease_costs", "Obesity_Attr_Costs_Overall", "Direct_MedicalIntervention_Costs", "Patient_Time_Costs_Intervention", "Direct_MedicalMaintainence_Costs", "Patient_Time_Costs_ClinicalMaintainence", "Direct_Non-Medical_Intervention_Costs", "Direct_Non-Medical_Maintainence_Costs"}, new String[][]{{"Initial_Age >= 0", "Sex > 0"}});
//			weights = loadData(weightFile, weightFileHeaderRowNumber, null, weightFileColumnsToLoad, weightFileLoadingCriteria);
////			saveData(weights, weightFileColumnsToLoad, null, "C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~64_Obesity-Adults/weights.csv", false);
//			weights = scaleWeight(weights, populationAdjScale);
////			saveData(weights, weightFileColumnsToLoad, null, "C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~64_Obesity-Adults/weights.csv", false);
//
//			fileName = fileNameTemplate.replaceAll("<scenario>", baselineId + "");
//
//			columnsToCombine = transpose(parse2(parameters.get("ColumnsToCombine"), ",", "---"));	//[-+*/]
////			tmp = parameters.get("ColumnsToCombine").split("\\}, \\{");
////			tmp[0] = tmp[0].substring(tmp[0].indexOf("{") + 1,  tmp[0].length());
////			tmp[tmp.length - 1] = tmp[tmp.length - 1].substring(0, tmp[tmp.length - 1].indexOf("}"));
////			columnsToCombine = new String[tmp.length][];
////			for (int i = 0; i < tmp.length; i++) {
////				columnsToCombine[i] = tmp[i].split(", ");
////			}
//
//			columnsTheKey = parameters.get("ColumnsTheKey").split(", ");
//			aggregateFunction = parameters.get("AggregateFunction");
//			dataBaseline = getCumSumAndPresentValue(loadAndAggregateData(fileName, 4, null, columnsToCombine, columnsTheKey, simulationDataLoadingFilters, aggregateFunction, weights, weightMatchingColumns), discountRate);	//"Initial_Age", "Time", "Sex", "Ethnicity", "Region"
//			adj = 0;
//			cpbCe = new double[scenarios.length - 1][dataBaseline.length][numColumnsCpbCe];
//			for (int i = 0; i < scenarios.length; i++) {
//				if (! scenarios[i][0].equalsIgnoreCase(baselineId + "")) {
//					fileName = fileNameTemplate.replaceAll("<scenario>", scenarios[i][0] + "");
//					dataIntervention = getCumSumAndPresentValue(loadAndAggregateData(fileName, 4, null, columnsToCombine, columnsTheKey, simulationDataLoadingFilters, aggregateFunction, weights, weightMatchingColumns), discountRate);
//					cpbCe[i - adj] = getCpbCe(dataBaseline, dataIntervention, columnIndicesOfQalyAndCosts);
//				} else {
//					adj = 1;
//				}
//			}
//
//			columnLabels = new String[scenarios.length - 1];
//			adj = 0;
//			for (int i = 0; i < scenarios.length; i++) {
//				if (! scenarios[i][0].equalsIgnoreCase(baselineId + "")) {
//					columnLabels[i - adj] = scenarios[i][1];
//				} else {
//					adj = 1;
//				}
//			}
//			save(cpbCe, columnLabels, new String[] {"CPB_Accumulated", "CPB_Accumulated_Discount", "CE"}, outfile, new double[][] {{}, yearsOfInterventionForSummaryReport}, true, outFileNumberFormat);
//			System.out.println("CPB and CE results are ready at:\n" + outfile);
//		} catch (CustomException e) {
//			e.printStackTrace();
//		}
//	}

	public static void parseAndRunConfigureFile (String configFile) throws CustomException {
		double[][] weights;
		double[][][] dataBaseline, dataIntervention, cpbCe, npv;
		double discountRate;
		double[] yearsOfInterventionForSummaryReport;
		String[][] scenarios, columnsTheValues, weightKeyColumns;
		int baselineId = -1;
		int weightFileHeaderRowNumber = 1, adj, discountBaseYear;
		String[] columnLabels, columnsTheKey, weightFileColumnsToLoad, weightMatchingColumns;
		String fileName, fileNameTemplate, simulationDataLoadingFilters, aggregateFunction, weightFile, weightFileLoadingCriteria;
		String outfile, outFileNumberFormat;
		HashMap <String, String> parameters;
		double populationAdjScale;
		int[] columnIndicesOfQalyAndCosts = new int[] {0, 1};
		int numColumnsCpbCe = 3;

		parameters = loadDataToHash(configFile, 0, ":\t", new String[] {"Col#1"}, new String[] {"Col#2"}, "#", new String[] {"Col#"}, ";", null);
		fileNameTemplate = parameters.get("FileNameTemplate");
		scenarios = parseWithTwoLayerDelimieters(parameters.get("Scenarios"), ",", "---");
		baselineId = Integer.parseInt(parameters.get("BaselineId"));
		simulationDataLoadingFilters = parameters.get("SimulationDataLoadingFilters");
		populationAdjScale = Double.parseDouble(parameters.get("PopulationAdjScale"));
		outfile = parameters.get("OutFile");
		outFileNumberFormat = parameters.get("OutFileNumberFormat");
		if (outFileNumberFormat == null) {outFileNumberFormat = "0";};
		weightFile = parameters.get("WeightFile");
		weightKeyColumns = transpose(parseWithTwoLayerDelimieters(parameters.get("WeightKeyColumns"), ",", "---"));
		weightFileColumnsToLoad = weightKeyColumns[0];
		weightMatchingColumns = weightKeyColumns[1];
		weightFileLoadingCriteria = parameters.get("WeightFileLoadingCriteria");
		discountRate = Double.parseDouble(parameters.get("DiscountRate"));
		discountBaseYear = Integer.parseInt(parameters.get("DiscountBaseYear"));
		yearsOfInterventionForSummaryReport = convert(parameters.get("YearsOfInterventionForSummaryReport").split(", "));

		if (baselineId < 0) {
			for (int i = 0; i < scenarios.length; i++) {
				if (scenarios[i][1].equalsIgnoreCase("baseline")) {
					baselineId = Integer.parseInt(scenarios[i][0]);
					break;
				}
			}
		}

		try {
			weights = loadData(weightFile, weightFileHeaderRowNumber, null, weightFileColumnsToLoad, weightFileLoadingCriteria);
//			saveData(weights, weightFileColumnsToLoad, null, "C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~64_Obesity-Adults/weights.csv", false);
			weights = scaleWeight(weights, populationAdjScale);
//			saveData(weights, weightFileColumnsToLoad, null, "C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~64_Obesity-Adults/weights.csv", false);

			fileName = fileNameTemplate.replaceAll("<scenario>", baselineId + "");

			columnsTheValues = transpose(parseWithTwoLayerDelimieters(parameters.get("ColumnsTheValues"), ",", "---"));	//[-+*/]
			columnsTheKey = parameters.get("ColumnsTheKey").split(", ");
			aggregateFunction = parameters.get("AggregateFunction");
			dataBaseline = getCumSumAndPresentValue(loadAndAggregateData(fileName, 4, columnsTheKey, columnsTheValues, simulationDataLoadingFilters, aggregateFunction, weights, weightMatchingColumns), discountRate, discountBaseYear);	//"Initial_Age", "Time", "Sex", "Ethnicity", "Region"
			adj = 0;
			cpbCe = new double[scenarios.length - 1][dataBaseline.length][numColumnsCpbCe];
//			npv = new double[scenarios.length][][];
			for (int i = 0; i < scenarios.length; i++) {
				if (! scenarios[i][0].equalsIgnoreCase(baselineId + "")) {
					fileName = fileNameTemplate.replaceAll("<scenario>", scenarios[i][0] + "");
					dataIntervention = getCumSumAndPresentValue(loadAndAggregateData(fileName, 4, columnsTheKey, columnsTheValues, simulationDataLoadingFilters, aggregateFunction, weights, weightMatchingColumns), discountRate, discountBaseYear);
					cpbCe[i - adj] = getCpbCe(dataBaseline, dataIntervention, columnIndicesOfQalyAndCosts);
//					npv[i] = getNpv(dataIntervention, new int[][] {{98}, {0, 1, 2, 3, 4, 5, 6, 7, 8}});
				} else {
					adj = 1;
				}
			}
//			npv[0] = getNpv(dataBaseline, new int[][] {{98}, {0, 1, 2, 3, 4, 5, 6, 7, 8}});

			columnLabels = new String[scenarios.length - 1];
			adj = 0;
			for (int i = 0; i < scenarios.length; i++) {
				if (! scenarios[i][0].equalsIgnoreCase(baselineId + "")) {
					columnLabels[i - adj] = scenarios[i][1];
				} else {
					adj = 1;
				}
			}
			save(cpbCe, columnLabels, new String[] {"CPB_Accumulated", "CPB_Accumulated_Discount", "CE"}, outfile, new double[][] {{}, yearsOfInterventionForSummaryReport}, true, outFileNumberFormat);

//			String[] rowHeaders = new String[columnsTheValues[0].length];
//			for (int i = 0; i < rowHeaders.length; i++) {
//				if (columnsTheValues[0][i] == null || columnsTheValues[0][i].equals("")) {
//					rowHeaders[i] = columnsTheValues[1][i];
//				} else{
//					rowHeaders[i] = columnsTheValues[0][i];
//				}
//			}
//			columnLabels = new String[scenarios.length];
//			for (int i = 0; i < scenarios.length; i++) {
//				columnLabels[i] = scenarios[i][1];
//			}
//			save(npv, columnLabels, rowHeaders, outfile, new double[][] {{}, yearsOfInterventionForSummaryReport}, true, outFileNumberFormat);
			System.out.println("CPB and CE results are ready at:\n" + outfile);
		} catch (CustomException e) {
			e.printStackTrace();
		}
	}

	public static void main (String[] args) {
		String help;

		help = "java tools --generatedistribution";

		for (String command : args) {
			if (command.equalsIgnoreCase("--generatedistribution")) {
				try {
					parseDistributionFunction("uniform_discrete(-20:85)");
					System.out.println(getRandomNumber(new Object[] {(int) 0, (int) 2, (int) 0}));
					System.out.println(getRandomNumber(parseDistributionFunction("uniform_discrete(0:1)")));
				} catch (Exception e) {
					System.out.println("When getting random number,");
					e.printStackTrace();
				}

			} else if (command.equalsIgnoreCase("--test")) {
//				rename("K:/Data_tmp/adultmedia_2014_forward/tobacco_cohort_estimation_massmedia_GlobalN_<<<1>>>900<<<1>>>.txt", "tobacco_cohort_estimation_massmedia_GlobalN_", "tobacco_cohort_estimation_massmedia_GlobalN_90", true);
//				rename("K:/Data_tmp/adultmedia_2014_forward/tobacco_cohort_estimation_massmedia_GlobalN_<<<3>>>.txt", "tobacco_cohort_estimation_massmedia_GlobalN_", "tobacco_cohort_estimation_massmedia_GlobalN_9", true);
//				rename("K:/Data_tmp/whatif_adultcessation_25pct/", "cessation+10pct", "cessation+25pct", true);
//				changeHeader("P:/Community_Prevent_II_12-007/Modeling/Tobacco/Microsimulation_Individual/", "SOL, _trial, thread, _stage, S6_Starting_Age, S4_Sex, S3_Ethnicity, S2_Education, S1_Income, S6_Age, S5_CurrentState, S8_Tsmoke, S7_Tquit, S7_z_Tquit_RiskAdjustment, S9_I_Rx, P_FormerSmoker, P_NeverSmoker, P_Smoker, HS3_ARSM_IN, HS3_ASCL_IN, _global100_col20_, _global100_col21_, _global100_col22_, _global100_col23_, HS3_BLDR_IN, HS3_BREM_IN, HS3_CRVX_IN, HS3_ESO_IN, HS3_ISHD_IN, HS3_LEUK_IN, HS3_LOCP_IN, HS3_LRNX_IN, HS3_OARD_IN, HS3_OTHD_IN, HS3_PANC_IN, HS3_PFLU_IN, HS3_RENL_IN, HS3_STOM_IN, HS3_STRK_IN, HS3_TLBR_IN, HS1_ARSM_ENDAGE, HS1_ASCL_ENDAGE, HS1_BLDR_ENDAGE, HS1_BREM_ENDAGE, HS1_CRVX_ENDAGE, HS1_ISHD_ENDAGE, HS1_ESO_ENDAGE, HS1_LEUK_ENDAGE, HS1_LOCP_ENDAGE, HS1_LRNX_ENDAGE, HS1_ARSM_QALY, HS1_ASCL_QALY, HS1_BLDR_QALY, HS1_BREM_QALY, HS1_CRVX_QALY, HS1_ISHD_QALY, HS1_ESO_QALY, HS1_LEUK_QALY, HS1_LOCP_QALY, HS1_LRNX_QALY, HS1_OARD_ENDAGE, HS1_OTHD_ENDAGE, HS1_PANC_ENDAGE, HS1_PFLU_ENDAGE, HS1_RENL_ENDAGE, HS1_STOM_ENDAGE, HS1_STRK_ENDAGE, HS1_TLBR_ENDAGE, HS1_OARD_QALY, HS1_OTHD_QALY, HS1_PANC_QALY, HS1_PFLU_QALY, HS1_RENL_QALY, HS1_STOM_QALY, HS1_STRK_QALY, HS1_TLBR_QALY, HS2_ARSM_Death, HS2_ASCL_Death, HS2_BLDR_Death, HS2_BREM_Death, HS2_CRVX_Death, HS2_ISHD_Death, HS2_ESO_Death, HS2_LEUK_Death, HS2_LOCP_Death, HS2_LRNX_Death, HS2_OARD_Death, HS2_OTHD_Death, HS2_PANC_Death, HS2_PFLU_Death, HS2_RENL_Death, HS2_STOM_Death, HS2_STRK_Death, HS2_TLBR_Death, HS_QALYDEC, I_Rx_NRT_Status, Tob_Attrib_Costs, NRT_Inv_Costs, EOL");

				Random gen1 = new Random(1234);
				Random gen2 = new Random(1244);
				Random gen3 = new Random(1254);
				for (int i = 0; i < 10; i++) {
					System.out.print(gen1.nextDouble() + "\t");
//					gen2.setSeed(1234 + i);
					System.out.println(gen2.nextDouble() + "\t" + gen3.nextDouble());
//					System.out.println(i%2==0? gen2.nextDouble() : gen2.nextInt());
				}
			} else if (command.equalsIgnoreCase("--initializeagents")) {
				try {
//					initializeAgents(new String[] {"trial", "stage"}, new int[] {100000, 30}, new String[] {"InitialAge", "S_z_99_Cohort", "Ethnicity"}, new String[] {"uniform_discrete(-20:85)", "uniform_discrete(1:2)", "uniform_discrete(1:4)"}, "C:/Users/b1744/Documents/Tmp/test_agents.csv");
					initializeAgents(new String[][] {{"trial", "1:50000", "1000000"}, {"stage", "0:30", "600"}}
									, new String[][] {{"InitialAge", "uniform_discrete(-20:85)", "0"}	//uniform_discrete(lower_band: higher_band), normal_continuous(mean, sd), poisson_discrete(lambda)
													, {"S_z_99_Cohort", "uniform_discrete(1:20)", "0"}
													, {"Ethnicity", "uniform_discrete(1:40)", "0"}
													, {"z_rnd_transition", "uniform_continuous(0:1)", "1"}
													, {"z_rnd__CigsPerDay_OSH_Multiplier", "poisson_discrete(1)", "1"}
													, {"z_rnd_I_QuitType_Rx_coverage", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_QuitType_Amount", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_QuitType_FailedAttempt", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_QuitType_RxCoverage", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_QuitType_Status", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_smokechange", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_IS1", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_IS2", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_IS3", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_IS4", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_IS5", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_ARSM_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_ARSM_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_ASCL_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_ASCL_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_BLDR_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_BLDR_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_BREM_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_BREM_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_CRVX_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_CRVX_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_ESO_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_ESO_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_ISHD_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_ISHD_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_LEUK_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_LEUK_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_LOCP_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_LOCP_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_LRNX_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_LRNX_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_OARD", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_OARD_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_OARD_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_OTHD_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_OTHD_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_PANC_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_PANC_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_PFLU_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_PFLU_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_RENL_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_RENL_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_STOM_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_STOM_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_STRK_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_STRK_IN", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_TLBR_Death", "uniform_continuous(0:1)", "1"}
													, {"z_rnd_TLBR_IN", "uniform_continuous(0:1)", "1"}
													}
									, 1234
									, "C:/Users/b1744/Documents/OSH_Tobacco=Tobacco_Tax_Revenue_Allocation_1225600/Tobacco_OSH_randomnumbers.csv");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Program stopped");
				}

			} else if (command.equalsIgnoreCase("--consolidatesimulationdata")) {
				try {
//					combineOutputFiles("C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~65_Obesity-Childhood/tests_physicalactivities_obesity/100000agents/physicalactivitymodel_pp_20150919_GlobalN_" + i, new String[] {"12.txt", "17.txt", "18.txt"}, 3, null);
//					consolidateSimulationData("P:/Community_Prevent_II_12-007/Modeling/Tobacco/what-if-analysis/tax_10pct/tobacco_rwj_20150818_micro3rndfixed_medcost_tax_10pct_GlobalN_", new String[] {"2.txt", "5.txt", "6.txt"}, new int[] {-30, 85}, 3, "P:/Community_Prevent_II_12-007/Modeling/DATA_TO_IMPORT_TO_CHA_TOOL/20160222_whatif/10pct_tax.csv");
//					consolidateSimulationData("K:/Data_tmp/whatif_adultcessation_10pct/tobacco_rwj_20150818_micro3rndfixed_medcost_whatif_adult_cessation+10pct_GlobalN_", new String[] {"2.txt", "5.txt", "6.txt"}, new int[] {-30, 85}, 3, "K:/Data_tmp/whatif_adultcessation+10pct.csv");
					consolidateSimulationData("C:/Users/b1744/Documents/CHA_Tobacco=Community_Prevent_II_12-007~Modeling~Tobacco/what-if-analysis/whatif_adultcessation_50pct/tobacco_rwj_20150818_micro3rndfixed_medcost_whatif_adult_cessation+50pct_GlobalN_", new String[] {"2.txt", "5.txt", "6.txt"}, new int[] {-30, 85}, 3, "C:/Users/b1744/Documents/CHA_Tobacco=Community_Prevent_II_12-007~Modeling~Tobacco/what-if-analysis/whatif_adultcessation+50pct.csv");
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (command.equalsIgnoreCase("--calculatecpbce")) {
				try {
		//			parseAndRunConfigureFile("C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~65_Obesity-Childhood/PubHealthSim_config.txt");
					parseAndRunConfigureFile("C:/Users/b1744/Documents/ClinicalRanking=Prevention_Priorities_III_06-116~64_Obesity-Adults/PubHealthSim_config_adult.txt");
				} catch (CustomException e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("Unrecognized command: " + command + "\nHelp:\n");
			}
		}
	}

}

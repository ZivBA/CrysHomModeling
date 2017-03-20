package alignment;

import ModellingTool.RunParameters;
import meshi.util.crossLinking.MySequence;
import meshi.util.crossLinking.MySequenceList;

import javax.swing.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RvalAlignerCluster extends SwingWorker<Void, Integer> {
	
	//	private final CustomOutputStream customOut;
//	private String row;
//	private final logObj log;
	private RvalSequence sourceSeq;
	private final String profileFilePath;
	private final String fastaSeqPath;
	private final boolean fullFasta;
	private final String swissProtPath;
	private JProgressBar progressBar;
	private ExecutorService executor;

	/**
	 * This ctor creates a worker thread that wraps around the NW aligner method by Nir Kalisman and provides input/output arguments to allow output
	 * other than STD.
	 *
	 * @param profileFilePath path to profile for threading
	 * @param params          params object
	 */
	public RvalAlignerCluster(String profileFilePath, RunParameters params) {
		this.profileFilePath = profileFilePath;
		this.fastaSeqPath = params.getFastaFile();
		this.fullFasta = params.getFullFasta();
		this.swissProtPath = params.getSWISSProt().getAbsolutePath();
		//		customOut = params.getCustomOut();
//		log = new logObj();

//		row = "Prot+Chain, Input seq length, *True* length, *True* score,Entries with higher score, Best result, \n";

	}

	private static double singleRvalAlignerRun(String seq, RvalSequence Rseq) {
		AminoAcidSequence AAseq = new AminoAcidSequence(seq);
		NeedlemanWunchSolver NW = new NeedlemanWunchSolver(Rseq, AAseq, new RvalScoringScheme());
		//		if (toPrintAlignment) {
		//			NW.printAlignment();
		//
		//			log.logString += NW.logAlignment();
		//		}
		NW.printAlignment();
		return NW.alignmentScore();
	}


	@Override
	protected Void doInBackground() throws Exception {
		System.out.flush();
		MySequenceList swissProt = new MySequenceList(swissProtPath);

		System.out.println("Total num of SwissProt seqs: " + swissProt.size() + "\n");
		MySequenceList mySeqList = new MySequenceList(fastaSeqPath);
		System.out.flush();
		sourceSeq = new RvalSequence(profileFilePath);    // txt version of CSV "allWithSeq"

//		log.logString += "working on profile file: " + profileFilePath + "\n";
		System.out.println("working on profile file: " + profileFilePath + "\n");
//		log.logString += mySeqList.fileName().substring(mySeqList.fileName().lastIndexOf("/") + 1, mySeqList.fileName()
//				.lastIndexOf(".fasta")) + ":   ";
//		log.logString += "Structure positions: " + sourceSeq.size() + "   Length of *true* seq: " + mySeqList.get(0).seq().length() +
//				"\n---------------------------------------------------------------------------------------\n";
		System.out.println(mySeqList.fileName().substring(mySeqList.fileName().lastIndexOf("/") + 1, mySeqList.fileName().lastIndexOf(
				".fasta")) + ":   Structure positions: " + sourceSeq.size() + "   Length of *true* seq: " + mySeqList
				.get(0).seq().length() + "\n---------------------------------------------------------------------------------------\n");
		
		AminoAcidSequence AAseq = new AminoAcidSequence(mySeqList.get(0).seq());
		NeedlemanWunchSolver NWTrueSeq = new NeedlemanWunchSolver(sourceSeq, AAseq, new RvalScoringScheme());
		NWTrueSeq.printAlignment();
//		double sameSeqScore = singleRvalAlignerRun(mySeqList.get(0).seq(), sourceSeq);
		double sameSeqScore = NWTrueSeq.alignmentScore();
//		row += fastaSeqPath.substring(0, fastaSeqPath.indexOf(".fasta")) + ",";
//		row += sourceSeq.size() + ", " + mySeqList.get(0).seq().length() + ", " + sameSeqScore + ", ";
		System.out.flush();

		if (fullFasta) {
			
//			log.logString += "\nProcessing SwissProt sequences:\n";
			System.out.println("\nProcessing SwissProt sequences:\n");
			ExecutorCompletionService<Double[]> executorThreads = new ExecutorCompletionService<>(executor);
			List<Future<Double[]>> futures = new LinkedList<>();
			int validSwissProt = 0;

			for (int ccc = 0; ccc < swissProt.size(); ccc++) {
//								if (ccc % 65536 == 0) {
//									setProgress(Math.round((float)ccc / swissProt.size() * 100f));
//									System.out.print(ccc + ".. ");
//									publish();
//								}
				//				if (ccc % 8192 == 0) {
				//					setProgress(Math.round((float)ccc / swissProt.size() * 100f));
				////					System.out.print(ccc + ".. ");
				//					publish();
				//				}
				MySequence mySeq = swissProt.get(ccc);
				if (!(mySeq.seq().indexOf('X') > -1) &&
						!(mySeq.seq().indexOf('B') > -1) &&
						!(mySeq.seq().indexOf('J') > -1) &&
						!(mySeq.seq().indexOf('Z') > -1) &&
						!(mySeq.seq().indexOf('O') > -1) &&
						!(mySeq.seq().indexOf('U') > -1)) {
					if (mySeq.seq().length() >= sourceSeq.size()) {
						final int finalCcc = ccc;
						validSwissProt++;
						if (validSwissProt % 512 == 0) {
							progressBar.setString(validSwissProt + " valid sequences found");
						}
						futures.add(executorThreads.submit(() -> {
							AminoAcidSequence targetSeq = new AminoAcidSequence(mySeq.seq());
							NeedlemanWunchSolver NW = new NeedlemanWunchSolver(sourceSeq, targetSeq, new RvalScoringScheme());
							
							return new Double[]{NW.alignmentScore(), (double) finalCcc, NW.getQualityIndex()};
						}));
					}
				}

			}
			progressBar.setString(validSwissProt + " valid sequences found");
			progressBar.setMaximum(validSwissProt);
			int counter = 0;
			Future<Double[]> completedThread;
			List<Double[]> resultsList = new LinkedList<>();
			while (counter < validSwissProt) {
				completedThread = executorThreads.take();
				counter++;
				
				resultsList.add(completedThread.get());
				if (counter % 512 == 0) {
					setProgress(Math.round((float)(counter) / validSwissProt * 100f));
//					System.out.print(counter + ".. ");
					publish(counter);
				}
//				if (counter % 65536 == 0) {
//					setProgress(Math.round((float)(counter) / validSwissProt * 100f));
//					System.out.println(counter + ".. ");
//					publish();
//				}
			}
			setProgress(Math.round((float)(counter) / validSwissProt * 100f));
			publish(counter);
			
			Collections.sort(resultsList,new ResultsComparator());
			List<Double[]> validList = resultsList.stream().filter(p->p[0]>-1000).collect(Collectors.toList());
			List<Double[]> betterList = validList.stream().filter(FilterPredicates.largerThan(sameSeqScore)).collect(Collectors.toList());
			double largest = validList.get(0)[0];
			int validSeqCounter = validList.size();
			int worseScore = betterList.size();

			
//			row += worseScore + ", " + largest + ", ";
//			System.out.println();
//			log.logString += worseScore + " entries out of valid " + validSeqCounter + " are with better score than the *true*. The " +
//					"best score that is not the *true* sequence is: " + largest + "\n";
			System.out.println(worseScore + " entries out of valid " + validSeqCounter + " are with better score than the *true*. The " +
					"best score that is not the *true* sequence is: " + largest);
			
			System.out.println("The top 10 best scores are:");
			for (int i=0; i<10; i++){
				MySequence mySeq = swissProt.get(validList.get(i)[1].intValue());
				System.out.println("Number " + (i+1) +" is: " + mySeq.title().substring(0, Math.min(mySeq.title().length() - 1, 60))
									+ "\nWith Quality Index of: " + validList.get(i)[2]);
				singleRvalAlignerRun(mySeq.seq(), sourceSeq);
				System.out.println();
				System.out.flush();
			}
		}

//		row += "\n";
//		File resultCSV = new File(profileFilePath.replace(".txt", "_ThreadResults.csv"));
//		File logFile = new File(profileFilePath.replace(".txt", "_ThreadLog.txt"));
//
//		FileWriter FW;
//		try {
//			FW = new FileWriter(resultCSV);
//			FW.write(row);
//			FW.close();
//
//			FW = new FileWriter(logFile);
//			FW.write(log.logString);
//			FW.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		publish(100);
		setProgress(100);
		System.out.flush();
		return null;
	}

	@Override
	protected void process(List<Integer> s) {
//		for (String line : s) {
//			System.out.print(line);
//		}
		progressBar.setValue(s.get(s.size()-1));
		progressBar.setString(progressBar.getValue() +" of " +progressBar.getMaximum() + " done");
		if (progressBar.getValue() == progressBar.getMaximum()){
			System.out.println("Finished threading all results.");
			System.out.flush();
		}
	}

	@Override
	protected void done() {

	}

	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

//	private static class logObj {
//		public String logString = "";
//
//		public logObj() {
//		}
//
//	}
//
	
	
	
}

class ResultsComparator implements Comparator<Double[]>
{
	
	@Override
	public int compare(Double[] o1, Double[] o2) {
		return o2[0].compareTo(o1[0]);
	}
}

class FilterPredicates {
	public static Predicate<Double[]> largerThan(Double trueRes){
		return p -> p[0] > trueRes;
	}
}
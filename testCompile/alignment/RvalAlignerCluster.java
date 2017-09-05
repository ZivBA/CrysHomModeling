package alignment;

import ModellingTool.RunParameters;
import meshi.util.crossLinking.MySequence;
import meshi.util.crossLinking.MySequenceList;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class RvalAlignerCluster extends SwingWorker<Void, String> {
	
	//	private final CustomOutputStream customOut;
	private String row;
	private final logObj log;
	private RvalSequence Rseq;
	private final String profileFilePath;
	private final String seqListPath;
	private final boolean fullFasta;
	private final String swissProtPath;
	private JProgressBar progressBar;
	private ExecutorService executor;

	/**
	 * This ctor creates a worker thread that wraps around the NW aligner method by Nir Kalisman and provides input/output arguments to allow output
	 * other than STD.
	 *
	 * @param swissProtPath   path to full swissprot FASTA sequences
	 * @param seqListPath     path to selected FASTA input sequence
	 * @param profileFilePath path to profile for threading
	 * @param params          params object
	 */
	public RvalAlignerCluster(String swissProtPath, String seqListPath, String profileFilePath, RunParameters params) {
		this.profileFilePath = profileFilePath;
		this.seqListPath = seqListPath;
		this.fullFasta = params.getFullFasta();
		this.swissProtPath = swissProtPath;
		//		customOut = params.getCustomOut();
		log = new logObj();

		row = "Prot+Chain, Input seq length, *True* length, *True* score,Entries with higher score, Best result, \n";

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
		MySequenceList mySeqList = new MySequenceList(seqListPath);

		Rseq = new RvalSequence(profileFilePath);    // txt version of CSV "allWithSeq"

//		log.logString += "working on profile file: " + profileFilePath + "\n";
		System.out.println("working on profile file: " + profileFilePath + "\n");
//		log.logString += mySeqList.fileName().substring(mySeqList.fileName().lastIndexOf("/") + 1, mySeqList.fileName()
//				.lastIndexOf(".fasta")) + ":   ";
//		log.logString += "Structure positions: " + Rseq.size() + "   Length of *true* seq: " + mySeqList.get(0).seq().length() +
//				"\n---------------------------------------------------------------------------------------\n";
		System.out.println(mySeqList.fileName().substring(mySeqList.fileName().lastIndexOf("/") + 1, mySeqList.fileName().lastIndexOf(
				".fasta")) + ":   Structure positions: " + Rseq.size() + "   Length of *true* seq: " + mySeqList
				.get(0).seq().length() + "\n---------------------------------------------------------------------------------------\n");

		double sameSeqScore = singleRvalAlignerRun(mySeqList.get(0).seq(), Rseq);
		
		row += seqListPath.substring(0, seqListPath.indexOf(".fasta")) + ",";
		row += Rseq.size() + ", " + mySeqList.get(0).seq().length() + ", " + sameSeqScore + ", ";
		System.out.flush();

		if (fullFasta) {
			double largest = 0.0;
			int indOflargest = -1;
			int validSeqCounter = 0;
			int worseScore = 0;
			log.logString += "\nProcessing SwissProt sequences:\n";
			System.out.println("\nProcessing SwissProt sequences:\n");
			ExecutorCompletionService<Double[]> executorThreads = new ExecutorCompletionService<>(executor);
			List<Future<Double[]>> futures = new LinkedList<>();
			final int[] counter = {0};

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
					if (mySeq.seq().length() >= Rseq.size()) {
						final int finalCcc = ccc;
						counter[0]++;
						futures.add(executorThreads.submit(() -> {
							AminoAcidSequence AAseq = new AminoAcidSequence(mySeq.seq());
							NeedlemanWunchSolver NW = new NeedlemanWunchSolver(Rseq, AAseq, new RvalScoringScheme());

							return new Double[]{NW.alignmentScore(), (double) finalCcc};
						}));
					}
				}

			}
			Future<Double[]> completedThread;
			List<Double[]> resultsList = new LinkedList<>();
			while (counter[0] > 0) {
				completedThread = executorThreads.take();
				counter[0]--;
				resultsList.add(completedThread.get());
				if (counter[0] % 8192 == 0) {
					setProgress(Math.round((float)(swissProt.size()-counter[0]) / swissProt.size() * 100f));
					System.out.print(swissProt.size()-counter[0] + ".. ");
					publish();
				}
				if (counter[0] % 65536 == 0) {
					setProgress(Math.round((float)(swissProt.size()-counter[0]) / swissProt.size() * 100f));
					System.out.println(swissProt.size()-counter[0] + ".. ");
					publish();
				}
			}
			for (Double[] results : resultsList){
				Double score = results[0];
				int cccNum = results[1].intValue();
				if (score > -10000) {
					validSeqCounter++;
					if ((score > largest) & (score != sameSeqScore)) {
						largest = score;
						indOflargest = cccNum;
					}
					if (score > sameSeqScore) {
						worseScore++;
					}



				}

			}


			row += worseScore + ", " + largest + ", ";
			System.out.println();
			log.logString += worseScore + " entries out of valid " + validSeqCounter + " are with better score than the *true*. The " +
					"best score that is not the *true* sequence is: " + largest + "\n";
			System.out.println(worseScore + " entries out of valid " + validSeqCounter + " are with better score than the *true*. The " +
					"best score that is not the *true* sequence is: " + largest);

			if (worseScore > 0) {
				MySequence mySeq = swissProt.get(indOflargest);
				log.logString += "The best alignment is " + mySeq.title().substring(0, Math.min(mySeq.title().length() - 1, 30)) + "\n";
				singleRvalAlignerRun(mySeq.seq(), Rseq);
				System.out.println();
				System.out.println("The best alignment is " + mySeq.title().substring(0, Math.min(mySeq.title().length() - 1, 30)));
				System.out.println();

			}


		}

		row += "\n";
		File resultCSV = new File(profileFilePath.replace(".txt", "_ThreadResults.csv"));
		File logFile = new File(profileFilePath.replace(".txt", "_ThreadLog.txt"));

		FileWriter FW;
		try {
			FW = new FileWriter(resultCSV);
			FW.write(row);
			FW.close();

			FW = new FileWriter(logFile);
			FW.write(log.logString);
			FW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		publish("Finished threading results");
		setProgress(100);
		return null;
	}

	@Override
	protected void process(List<String> s) {
		for (String line : s) {
			System.out.print(line);
		}
		progressBar.setValue(getProgress());
		System.out.flush();
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

	private static class logObj {
		public String logString = "";

		public logObj() {
		}

	}
}

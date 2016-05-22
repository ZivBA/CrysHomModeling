package alignment;

import meshi.util.crossLinking.MySequence;
import meshi.util.crossLinking.MySequenceList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RvalAlignerCluster {
	public static double singleRvalAlignerRun(String seq , RvalSequence Rseq , boolean toPrintAlignment, logObj log) {
		AminoAcidSequence AAseq = new AminoAcidSequence(seq);
		NeedlemanWunchSolver NW = new NeedlemanWunchSolver(Rseq, AAseq, new RvalScoringScheme());
		if (toPrintAlignment) {
			NW.printAlignment();
 			System.out.println();

			log.logString += NW.logAlignment();
		}
		return NW.alignmentScore();
	}


	public static void main(String[] lines) {
		logObj log = new logObj();
		MySequenceList swissProt = new MySequenceList("/home/zivben/IdeaProjects/Results/uniprot_sprot_10_2015.fasta"); // all sequences
		System.out.println("Total num of SwissProt seqs: " + swissProt.size());
		MySequenceList mySeqList = new MySequenceList("3JAM_d.fasta");			// fasta of single chain
		RvalSequence Rseq = new RvalSequence("3JAM_d_profile.txt");             // txt version of CSV "allWithSeq"
		System.out.println("\n3JAM_d:  Structure positions: " + Rseq.size() + "   Length of *true* seq: " + mySeqList
				.get(0).seq().length() + "\n---------------------------------------------------------------------------------------");
		double sameSeqScore = singleRvalAlignerRun(mySeqList.get(0).seq() , Rseq, true,log);
		double largest = 0.0;
		int indOflargest = -1;
		int validSeqCounter = 0;
		int worseScore = 0;
		for (int ccc=0 ; ccc<swissProt.size() ; ccc++) {
			MySequence mySeq = swissProt.get(ccc);
			if (!(mySeq.seq().indexOf('X')>-1) &&
					!(mySeq.seq().indexOf('B')>-1) &&
					!(mySeq.seq().indexOf('J')>-1) &&
					!(mySeq.seq().indexOf('Z')>-1) &&
					!(mySeq.seq().indexOf('O')>-1) &&
					!(mySeq.seq().indexOf('U')>-1)) {
				if (mySeq.seq().length() >= Rseq.size()) {
					double score = singleRvalAlignerRun(mySeq.seq() , Rseq , false,log);
					if (score>-10000) {
						validSeqCounter++;
						if ((score>largest) & (score != sameSeqScore)) {
							largest = score;
							indOflargest = ccc;
						}
						if (score>sameSeqScore) {
							worseScore++;
						}
					}
				}
			}
		}
		System.out.println(worseScore + " entries out of valid " + validSeqCounter + " are with better score than the *true*. The best score that is not the *true* sequence is: " + largest);
		if (worseScore>0) {
			MySequence mySeq = swissProt.get(indOflargest);
			System.out.println("The best alignment is " + mySeq.title().substring(0,Math.min(mySeq.title().length()-1, 30)));
			singleRvalAlignerRun(mySeq.seq() , Rseq, true,log);
		}

	}


	public static void runThread(String swissProtPath, String seqListPath, String profileFilePath, boolean fullFasta) {

		logObj log = new logObj();

		String row = "Prot+Chain, Input seq length, *True* length, *True* score,Entries with higher score, Best result, \n";

		MySequenceList swissProt = new MySequenceList(swissProtPath); // all sequences

		System.out.println("Total num of SwissProt seqs: " + swissProt.size());

		MySequenceList mySeqList = new MySequenceList(seqListPath); // fasta of single chain

		RvalSequence Rseq = new RvalSequence(profileFilePath);	// txt version of CSV "allWithSeq"
		log.logString += "working on profile file: "+profileFilePath+"\n";
		log.logString += mySeqList.fileName().substring(mySeqList.fileName().lastIndexOf("/")+1,mySeqList.fileName()
				.lastIndexOf(
						".fasta"))+":   ";
		log.logString +="Structure positions: " + Rseq.size() + "   Length of *true* seq: " + mySeqList.get(0).seq().length() +
				"\n---------------------------------------------------------------------------------------\n";
		System.out.println("\n"+mySeqList.fileName().substring(mySeqList.fileName().lastIndexOf("/")+1,mySeqList.fileName().lastIndexOf(
				".fasta"))+":   Structure positions: " + Rseq.size() + "   Length of *true* seq: " + mySeqList
				.get(0).seq().length() + "\n---------------------------------------------------------------------------------------");

		double sameSeqScore = singleRvalAlignerRun(mySeqList.get(0).seq() , Rseq, true, log);

		row+= seqListPath.substring(0,seqListPath.indexOf(".fasta"))+",";
		row+= Rseq.size()+", "+ mySeqList.get(0).seq().length()+", "+sameSeqScore+", ";

		if (fullFasta) {
			double largest = 0.0;
			int indOflargest = -1;
			int validSeqCounter = 0;
			int worseScore = 0;
			log.logString +="\nProcessing SwissProt sequences:\n";
			for (int ccc=0 ; ccc<swissProt.size() ; ccc++) {
				if (ccc % 65536 ==0) {System.out.println();}
				if (ccc % 8192 == 0) {System.out.print(ccc+".. ");}
				MySequence mySeq = swissProt.get(ccc);
				if (!(mySeq.seq().indexOf('X')>-1) &&
						!(mySeq.seq().indexOf('B')>-1) &&
						!(mySeq.seq().indexOf('J')>-1) &&
						!(mySeq.seq().indexOf('Z')>-1) &&
						!(mySeq.seq().indexOf('O')>-1) &&
						!(mySeq.seq().indexOf('U')>-1)) {
					if (mySeq.seq().length() >= Rseq.size()) {
						double score = singleRvalAlignerRun(mySeq.seq() , Rseq , false, log);
						if (score>-10000) {
							validSeqCounter++;
							if ((score>largest) & (score != sameSeqScore)) {
								largest = score;
								indOflargest = ccc;
							}
							if (score>sameSeqScore) {
								worseScore++;
							}
						}
					}
				}
			}
			row += worseScore+", "+largest+", ";

			log.logString +=worseScore + " entries out of valid " + validSeqCounter + " are with better score than the *true*. The " +
					"best score that is not the *true* sequence is: " + largest+"\n";
			//		System.out.println(worseScore + " entries out of valid " + validSeqCounter + " are with better score than the *true*. The best score that is not the *true* sequence is: " + largest);
			if (worseScore>0) {
				MySequence mySeq = swissProt.get(indOflargest);
				log.logString +="The best alignment is " + mySeq.title().substring(0,Math.min(mySeq.title().length()-1, 30))+"\n";
				singleRvalAlignerRun(mySeq.seq() , Rseq, true,log);
	//			System.out.println("The best alignment is " + mySeq.title().substring(0,Math.min(mySeq.title().length()-1, 30)));
	//			System.out.println();
				System.out.println(log.logString);
			}
		}
		row+="\n";
		File resultCSV = new File(profileFilePath.replace(".txt","_ThreadResults.csv"));
		File logFile = new File(profileFilePath.replace(".txt","_ThreadLog.txt"));

		FileWriter FW;
		try {
			FW = new FileWriter(resultCSV);
			FW.write(row);
			FW.close();

			FW = new FileWriter(logFile);
			FW.write(log.logString );
			FW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}




	}

	private static class logObj{
		public String logString = "";
		public logObj() {
		}

	}
}

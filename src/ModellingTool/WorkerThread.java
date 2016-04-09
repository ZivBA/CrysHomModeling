package ModellingTool;

import UtilExceptions.MissingChainID;

import java.io.IOException;

/**
 * Created by zivben on 09/04/16.
 */
public class WorkerThread implements Runnable {
	private final RunParameters params;
	private CRYS_Score crysScore;

	public WorkerThread(RunParameters params) {
		this.params = params;
	}

	@Override
	public void run() {
		try {
			crysScore = new CRYS_Score(params);
			crysScore.getAcidDist();
			crysScore.scoreProtein();

		} catch (IOException | MissingChainID e) {
			System.err.println(e.getMessage());
			System.out.println("There was a problem processing one of the files.");
		}
	}
}

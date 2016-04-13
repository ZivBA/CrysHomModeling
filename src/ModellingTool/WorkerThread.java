package ModellingTool;

import UtilExceptions.MissingChainID;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Created by zivben on 09/04/16.
 */
public class WorkerThread implements Runnable {
	private final RunParameters params;
	private final ExecutorService executor;
	private CRYS_Score crysScore;


	public WorkerThread(RunParameters params, ExecutorService executor) {
		this.params = params;
		this.executor = executor;
	}

	@Override
	public void run() {
		try {
			crysScore = new CRYS_Score(params);
//			crysScore.getAcidDist();
			crysScore.scoreProtein(executor);

		} catch (IOException | MissingChainID e) {
			System.err.println(e.getMessage());
			System.out.println("There was a problem processing one of the files.");
		}
	}
}

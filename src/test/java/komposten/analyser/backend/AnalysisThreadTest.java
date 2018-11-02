package komposten.analyser.backend;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AnalysisThreadTest
{

	@Test
	void postRunnable_oneRunnable() throws InterruptedException
	{
		AnalysisThread thread = new AnalysisThread("Test");
		thread.start();
		
		ARunnable runnable = new ARunnable();
		thread.postRunnable(runnable);
		
		Thread.sleep(100);
		
		assertTrue(runnable.hasRun);
	}


	@Test
	void postRunnable_twoRunnables() throws InterruptedException
	{
		AnalysisThread thread = new AnalysisThread("Test");
		thread.start();

		ARunnable runnable1 = new ARunnable();
		ARunnable runnable2 = new ARunnable();
		thread.postRunnable(runnable1);
		thread.postRunnable(runnable2);
		
		Thread.sleep(100);

		assertTrue(runnable1.hasRun);
		assertTrue(runnable2.hasRun);
	}
	
	
	private class ARunnable implements Runnable
	{
		boolean hasRun;
		
		@Override
		public void run()
		{
			hasRun = true;
		}
	}
}

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
		
		assertTrue(waitForRunnable(runnable), "The runnable took more than " + TIMEOUT + "ms!");
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
		
		assertTrue(waitForRunnable(runnable2), "The second runnable took more than " + TIMEOUT + "ms!");
		assertTrue(runnable1.hasRun);
		assertTrue(runnable2.hasRun);
	}


	private static final long TIMEOUT = 5000;
	private boolean waitForRunnable(ARunnable runnable) throws InterruptedException
	{
		long timer = 0;
		while (!runnable.hasRun)
		{
			Thread.sleep(100);
			timer += 100;
			
			if (timer > TIMEOUT)
				return false;
		}
		
		return true;
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

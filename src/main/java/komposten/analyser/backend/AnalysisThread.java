package komposten.analyser.backend;

import java.util.LinkedList;
import java.util.Queue;

import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;

public class AnalysisThread extends Thread
{
	private Queue<Runnable> runnables;
	private volatile boolean running = true;

	{
		runnables = new LinkedList<>();
		setDaemon(true);
	}
	
	
	public AnalysisThread(String name)
	{
		super(name);
	}
	
	
	@Override
	public void run()
	{
		while (running)
		{
			if (!runnables.isEmpty())
				runnables.poll().run();

			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				LogUtils.log(Level.WARNING, AnalysisThread.class.getName(), "Sleep interrupted!", e, false);
				interrupt();
			}
		}
	}


	public void postRunnable(Runnable runnable)
	{
		runnables.add(runnable);
	}
}

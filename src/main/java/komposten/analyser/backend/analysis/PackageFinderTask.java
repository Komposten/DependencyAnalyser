package komposten.analyser.backend.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.util.Constants;
import komposten.analyser.backend.util.SourceUtil;
import komposten.utilities.tools.ExtensionFileFilter;

public class PackageFinderTask implements StateRunnable
{
	private static final ExtensionFileFilter FILE_FILTER;
	
	private File folder;
	private ThreadPoolExecutor threadPool;
	
	private boolean hasFinished;
	
	private List<PackageFinderTask> subTasks;
	private List<PackageData> outputList;
	
	static
	{
		FILE_FILTER = new ExtensionFileFilter(true, Constants.FILE_EXTENSION);
	}


	public PackageFinderTask(File folder, ThreadPoolExecutor threadPool)
	{
		this(folder, threadPool, Collections.synchronizedList(new ArrayList<>()));
	}
	
	
	private PackageFinderTask(File folder, ThreadPoolExecutor threadPool, List<PackageData> outputList)
	{
		this.folder = folder;
		this.threadPool = threadPool;
		this.outputList = outputList;
		this.subTasks = Collections.synchronizedList(new ArrayList<>());
	}
	
	
	@Override
	public boolean hasFinished()
	{
		boolean finished = hasFinished;
		
		synchronized (subTasks)
		{
			Iterator<PackageFinderTask> iterator = subTasks.iterator();
			while (iterator.hasNext())
			{
				if (!iterator.next().hasFinished())
					finished = false;
				else
					iterator.remove();
			}
		}

		return finished;
	}
	
	
	public List<PackageData> getPackageList()
	{
		return outputList;
	}


	@Override
	public void run()
	{
		try
		{
			checkFolder(folder);
			hasFinished = true;
		}
		catch (Exception e)
		{
			//NEXT_TASK PackageFinderTask; Exception handling.
			System.err.println("Exception in " + Thread.currentThread());
			e.printStackTrace();
		}
	}


	private void checkFolder(File folder)
	{
		List<File> fileList = new LinkedList<>();
		File[] filesInFolder = folder.listFiles(FILE_FILTER);
		
		if (filesInFolder == null)
			return;
		
		for (File file : filesInFolder)
		{
			if (Thread.currentThread().isInterrupted())
				return;
			
			if (file.isDirectory())
			{
				if (threadPool.getMaximumPoolSize() - threadPool.getActiveCount() > 0)
				{
					PackageFinderTask packageFinder = new PackageFinderTask(file, threadPool, outputList);
					threadPool.submit(packageFinder);
					subTasks.add(packageFinder);
				}
				else
				{
					checkFolder(file);
				}
			}
			else
			{
				fileList.add(file);
			}
		}
		
		if (!fileList.isEmpty())
		{
			File first = fileList.get(0);
			String packageName = findPackageName(first);
			
			PackageData data = new PackageData(packageName, folder, fileList.toArray(new File[fileList.size()]));

			synchronized (outputList)
			{
				if (!outputList.contains(data))
				{
					outputList.add(data);
				}
				else
				{
					PackageData original = outputList.get(outputList.indexOf(data));

					File[] files = Arrays.copyOf(original.sourceFiles, original.sourceFiles.length + data.sourceFiles.length);
					System.arraycopy(data.sourceFiles, 0, files, original.sourceFiles.length, data.sourceFiles.length);
					original.sourceFiles = files;
				}
			}
		}
	}


	private String findPackageName(File sourceFile)
	{
		String packageName = "<default package>";
		
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile)))
		{
			boolean lastEndedInComment = false;
			String line = "";
			
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				StringBuilder builder = new StringBuilder(line);
				lastEndedInComment = SourceUtil.removeComments(builder, lastEndedInComment);
				
				line = builder.toString().trim();
				
				if (line.startsWith("package"))
				{
					int indexOfPackage = line.indexOf("package");
					packageName = line.substring(indexOfPackage+7, line.indexOf(";", indexOfPackage)).trim();
					break;
				}
				else if (line.startsWith("import") || line.matches("^(public)?\\s+(abstract|final)?\\s+(class|interface|enum).*$"))
				{
					break;
				}
			}
		}
		catch (IOException e)
		{
			//NEXT_TASK PackageFinderTask; Exception handling.
		}
		
		return packageName;
	}
}

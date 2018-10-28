package komposten.analyser.gui.views.packages;

import komposten.analyser.gui.views.IconToggleButton;

public class CyclesOnlyButton extends IconToggleButton
{
	private PackageList packageList;


	public CyclesOnlyButton(PackageList packageList)
	{
		super("/buttons/show_cycles_only.png", "Show only packages that are part of a cycle.");
		
		this.packageList = packageList;
	}
	
	
	@Override
	protected void onClick()
	{
		packageList.setShowOnlyCycles(isSelected());
	}
}

package komposten.analyser.gui.views.packages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import komposten.analyser.gui.backend.Backend;

public class PackagePanel extends JPanel
{
	private JLabel header;
	private JToggleButton buttonOnlyCycles;
	private PackageFilterTextField fieldFilter;
	private JScrollPane scrollPane;
	private PackageList list;
	

	public PackagePanel(Backend backend)
	{
		setLayout(new GridBagLayout());
		setPreferredSize(new Dimension(200, getPreferredSize().height));
		setMinimumSize(new Dimension(150, getMinimumSize().height));
		
		list = new PackageList(backend);
		scrollPane = new JScrollPane(list);

		header = new JLabel("Packages");
		buttonOnlyCycles = new CyclesOnlyButton(list);
		fieldFilter = new PackageFilterTextField(list);
		
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.add(header, BorderLayout.CENTER);
		headerPanel.add(buttonOnlyCycles, BorderLayout.EAST);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		
		constraints.gridy = 0;
		add(headerPanel, constraints);
		
		constraints.gridy++;
		add(fieldFilter, constraints);
		
		constraints.gridy++;
		constraints.weighty = 1;
		add(scrollPane, constraints);
	}
}

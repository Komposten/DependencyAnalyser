package komposten.analyser.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import komposten.analyser.gui.backend.Backend;

public class AnalysisProgressDialog extends JDialog
{
	private JLabel header;
	private JLabel label;
	private Backend backend;

	public AnalysisProgressDialog(JFrame owner, Backend backend)
	{
		super(owner, "Analysing source", true);
		
		this.backend = backend;
		
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setResizable(false);
		addWindowListener(windowListener);
		
		header = new JLabel("");
		label = new JLabel("");

		label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		header.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		Border border = new CompoundBorder(new MatteBorder(0, 0, 1, 0, Color.gray), new EmptyBorder(5, 5, 5, 5));
		header.setBorder(border);
		border = new EmptyBorder(5, 5, 5, 5);
		label.setBorder(border);
		
		setPreferredSize(new Dimension(300, 100));
		
		LayoutManager layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.weightx = 1;
		getContentPane().add(header, constraints);
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.weighty = 1;
		getContentPane().add(label, constraints);
	}
	
	
	public void show(File fileBeingAnalysed)
	{
		setTitle("Analysing " + fileBeingAnalysed.getPath() + "...");
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}
	
	
	public void setText(String text)
	{
		setText(null, text);
	}
	
	
	/**
	 * @param headerText The text to assign to the header, or <code>null</code> if
	 *          the header should remain unchanged.
	 * @param text
	 */
	public void setText(String headerText, String text)
	{
		if (headerText != null)
			header.setText(headerText);
		label.setText("<html>"+text+"</html>"); //Wrapping in HTML tags to enable automatic line wrapping.
	}
	
	
	@Override
	public void setVisible(boolean b)
	{
		super.setVisible(b);
		
		if (!b)
			setText("", "");
	}
	
	
	private WindowAdapter windowListener = new WindowAdapter()
	{
		@Override
		public void windowClosing(WindowEvent e)
		{
			backend.abortAnalysis();
		}
	};
}

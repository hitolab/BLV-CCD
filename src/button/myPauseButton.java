package button;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import static main.BLV.curDir; 

public class myPauseButton extends JButton{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public myPauseButton(String filename){
		super(new ImageIcon(curDir+"/pic/pause.png"));
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setFocusPainted(false);
		this.setForeground(new Color(255, 0, 0));
		this.addActionListener(new ActionListener() {
		       public void actionPerformed(ActionEvent e) {
		    	   
		    	   
		       }
		    });
	}
}

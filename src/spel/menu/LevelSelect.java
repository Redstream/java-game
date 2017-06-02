package spel.menu;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.apache.commons.io.FileUtils;

import spel.Game;
import spel.level.Level;

public class LevelSelect extends JPanel {

	private static final long serialVersionUID = 1L;
	final JList<String> list;
	
	JEditorPane textarea;
	String[] info = new String[100];
	final Menu parent;

	public LevelSelect(final Menu parent)  {
		this.parent = parent;
		String[] maps = listMaps(Level.mapfolder);
		list = new JList<String>(maps);
		list.setSize(list.getWidth(), 100);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(30);
		
		if(maps.length == 0){
			try{
				URL inputUrl;
				File dest;
				
				inputUrl = getClass().getResource("/res/maps/olle.txt");
				dest = new File(Level.mapfolder+"//Olle.txt");
				FileUtils.copyURLToFile(inputUrl, dest);
				
				inputUrl = getClass().getResource("/res/maps/redstream.txt");
				dest = new File(Level.mapfolder+"//Redstream.txt");
				FileUtils.copyURLToFile(inputUrl, dest);
				
				inputUrl = getClass().getResource("/res/maps/500test.txt");
				dest = new File(Level.mapfolder+"//500test.txt");
				FileUtils.copyURLToFile(inputUrl, dest);
				
				
				inputUrl = getClass().getResource("/res/maps/promap.txt");
				dest = new File(Level.mapfolder+"//Promap.txt");
				FileUtils.copyURLToFile(inputUrl, dest);
				
			}catch(Exception ex){
				Game.information(2,ex.toString());
			}
		}
		JScrollPane jscrollpane = new JScrollPane(list);
		jscrollpane.setLocation(0, 0);
		add(jscrollpane);

		textarea = new JEditorPane("text/html", "");
		textarea.setEditable(false);
		add(textarea);
		setText("<b>Name:</b><br> <br><b>Description:</b><br> ");

		MouseListener mouseListener = new MouseAdapter() {
			String lastclicked = "";

			public void mouseClicked(MouseEvent e) {
				String level = (String) list.getSelectedValue();
				
				if (!lastclicked.equalsIgnoreCase(list.getSelectedValue())) {			
					try {
						BufferedReader in = new BufferedReader(new FileReader( Level.mapfolder + "\\" + level + ".desc.txt"));
						String s = "";
						String line;
						while ((line = in.readLine()) != null) {
							s += line + "<br>";
						}
						in.close();
						setText("<b>Name:</b><br> " + level + "<br><b>Description:</b><br>" + s);
					} catch (Exception ex) {
						setText("<b>Name:</b><br> " + level + "<br><b>Description:</b><br>No map info");
					}
					lastclicked = (String) list.getSelectedValue();
				} else {
					setLevel((String) list.getSelectedValue() + ".txt", false);
					
				}
			}
		};
		list.addMouseListener(mouseListener);

	}
	
	public void setLevel(String level, boolean jar){
		parent.changeCard("1");
		Game.game.setLevel(level, jar);		
		Game.game.togglePause();	
	}

	public void setText(String s) {
		textarea.setText(s);
	}

	public void updateList() {
		list.setListData(listMaps(Level.mapfolder));
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Menu.paintBg(g, this);
	}
	
	public static String[] listMaps(final File folder) {

		ArrayList<String> maps = new ArrayList<String>();
		if (!folder.exists()) {
			folder.mkdirs();
			Game.information(0,"New map folder created.");
		}
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listMaps(fileEntry);
			} else {
				if (fileEntry.getName().split(".desc.").length == 1) {
					maps.add(fileEntry.getName().split(".txt")[0]);
				}
			}
		}
		String[] mapsArray = new String[maps.size()];
		for (int i = 0; i < mapsArray.length; i++) {
			mapsArray[i] = maps.get(i);
		}
		return mapsArray;
	}

}
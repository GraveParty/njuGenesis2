package presentation.contenui;

import javax.swing.JPanel;

import presentation.mainui.MainUI;
import presentation.mainui.TitlePanel;
import assistance.ContentItem;
import assistance.MenuItem;

public class ContentController {

	//----------改变标题----------
	public void changeTitle(MenuItem m){
		JPanel title = MainUI.getTitlePanel();
		if(title.getComponentAt(0, 0)!=null){
			title.remove(title.getComponentAt(0, 0));
		}
		title.add(TitlePanel.getTitle(m));
		MainUI.getMainFrame().repaint();
	}

	//----------内容页跳转----------
	public void changeContent(ContentItem c){
		ContentPanel cp = new ContentPanel("");
		switch(c){
		case PlayerStats:cp = new PlayerStatsPanel();break;
		case TeamStats:cp = new TeamStatsPanel();break;
//		case Home:cp = new ContentPanel("img/content/homepanel.png");break;
		case PlayerSearch:cp = new PlayerSearchPanel();break;
		case TEAMOFALL:cp = new TeamOfAll();break;
		default:

		}
		if(MainUI.getBg().getComponentAt(220, 95)!=null){
			MainUI.getBg().remove(MainUI.getBg().getComponentAt(220, 95));
		}
		MainUI.addCom(cp.getContentPanel());
		

	}

}

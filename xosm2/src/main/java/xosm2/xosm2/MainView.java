package xosm2.xosm2;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

public class MainView extends VerticalLayout implements View {
	
	MainView(Navigator navigator,HorizontalSplitPanel panel){
		Redirect red = new Redirect(navigator,panel);
		this.addComponent(red);
		navigator.addView("",this);
		setWidth("100%");
	    setHeight("100%");
	    setMargin(false);
		
	}

}

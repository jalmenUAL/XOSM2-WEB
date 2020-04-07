package xosm2.xosm2;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

public class Redirect extends VerticalLayout implements View {

	
	Redirect(Navigator navigator,HorizontalSplitPanel panel){
		this.addComponent(panel);
		navigator.addView("Redirect",this);
		setWidth("100%");
	    setHeight("100%");
	    setMargin(false);
		
	}
}

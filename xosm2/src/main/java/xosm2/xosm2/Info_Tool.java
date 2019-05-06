package xosm2.xosm2;


import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class Info_Tool extends Window {
	
	Info_Tool(String Text){
	super();
	VerticalLayout vl = new VerticalLayout();
	Label l = new Label("<p align=\"justify\">"+Text+"</p>",ContentMode.HTML);
	l.setStyleName(ValoTheme.LABEL_BOLD);
	vl.addComponent(l);
	setContent(vl);
	}

}

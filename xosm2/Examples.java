package xosm2.xosm2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.declarative.ShouldWriteDataDelegate;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class Examples extends Window {
	
	
	
	Button ex1 = new Button("Retrieve the streets in London intersecting Haymarket street and touching Trafalgar Square");
	Button ex2 = new Button("Retrieve the restaurants in Roma further north to Picasso hotel");
	Button ex3 = new Button("Retrieve hotels of Vienna close to food venues (food venues = number of bars "
			+ "and restaurants bigger than 30)");
	Button ex4 = new Button("Retrieve the hotels of Munich with the greatest number of churches nearby");
	Button ex5 = new Button("Retrieve the size of park areas close to Karl-Liebknecht-Straße in Berlin");
	Button ex6 = new Button("Retrieve the top-star rating biggest hotels close to Via Dante in Milan");
	Button ex7 = new Button("Request taxi stops close to Carrousel du Louvre in Paris");
	Button ex8 = new Button("Retrieves free events of Madrid");
	Button ex9 = new Button("Retrieve Wikipedia information about places nearby to the intersection "
			+ "point of Calle Mayor and Calle de Esparteros in Madrid");
	Button ex10 = new Button("Retrieves the information provided by tixik.com around Picadilly in London");
			
	
	Examples(PopupButton p){
	super();
	HorizontalLayout hl = new HorizontalLayout();
    hl.addComponent(ex1);
    hl.addComponent(ex2);
    hl.addComponent(ex3);
    hl.addComponent(ex4);
    hl.addComponent(ex5);
    hl.addComponent(ex6);
    hl.addComponent(ex7);
    hl.addComponent(ex8);
    hl.addComponent(ex9);
    hl.addComponent(ex10);
	p.setContent(hl);
     
}
}

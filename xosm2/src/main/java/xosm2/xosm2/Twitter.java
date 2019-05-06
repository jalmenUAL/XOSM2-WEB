package xosm2.xosm2;

import org.w3c.dom.NodeList;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class Twitter extends Window {

	public void showInfoTwitter(NodeList Tweets) {
		VerticalLayout vl = new VerticalLayout();
		vl.setStyleName("twitter");
		HorizontalLayout images = new HorizontalLayout();
		images.setMargin(false);
		Image twitter = new Image();
		twitter.setSource(new ThemeResource("twitter.png"));
		twitter.setWidth("80pt");
		twitter.setHeight("80pt");
		Image image = new Image();
		image.setSource(new ThemeResource("map-banner.jpg"));
		image.setWidth("280pt");
		image.setHeight("80pt");
		images.addComponent(twitter);
		images.addComponent(image);
		Label head = new Label("Tweets");
		head.setStyleName(ValoTheme.LABEL_BOLD);
		vl.setHeightUndefined();
		vl.setWidthUndefined();
		this.setHeightUndefined();
		this.setWidthUndefined();
		vl.addComponent(images);
		vl.addComponents(head);
		if (Tweets == null) {
		} else {
			for (int i = 0; i < Tweets.getLength(); i++) {
				if (Tweets.item(i).hasChildNodes()) {
					Item_Twitter t = new Item_Twitter(Tweets.item(i));
					t.setHeightUndefined();
					t.setWidthUndefined();
					t.setSpacing(false);
					t.setMargin(false);
					vl.addComponent(t);
				}
			}
		}
		setContent(vl);
		center();
	}
}

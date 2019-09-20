package xosm2.xosm2;

import org.w3c.dom.NodeList;

import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class Youtube extends Window {

	public void showInfoYoutubeVideos(NodeList Tweets) {
		this.setWidth("800px");
		 
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		vl.setStyleName("youtube");
		HorizontalLayout images = new HorizontalLayout();
		images.setMargin(false);
        images.setWidth("100%");
		Image twitter = new Image();
		twitter.setSource(new ThemeResource("youtube.png"));
		twitter.setWidth("100%");
		twitter.setHeight("60pt");
		Image image = new Image();
		image.setSource(new ThemeResource("map-banner.jpg"));
		image.setWidth("100%");
		image.setHeight("60pt");
		images.addComponent(twitter);
		images.addComponent(image);
		Label head = new Label("Videos");
		head.setStyleName(ValoTheme.LABEL_BOLD);
		//vl.setHeightUndefined();
		vl.setWidthUndefined();
		this.setHeightUndefined();
		//this.setWidthUndefined();
		vl.addComponent(images);
		vl.addComponents(head);
		if (Tweets == null) {
		} else {
			for (int i = 0; i < Tweets.getLength(); i++) {
				
				
				if (Tweets.item(i).hasChildNodes()) {
					 
					Item_Youtube t = new Item_Youtube(Tweets.item(i),"videos");
					//if (t.video) { this.setWidth("800px");} else {this.setWidth("400px");}
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
	public void showInfoYoutubePlayLists(NodeList Tweets) {
		this.setWidth("800px");
		 
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		vl.setStyleName("youtube");
		HorizontalLayout images = new HorizontalLayout();
		images.setMargin(false);
        images.setWidth("100%");
		Image twitter = new Image();
		twitter.setSource(new ThemeResource("youtube.png"));
		twitter.setWidth("100%");
		twitter.setHeight("60pt");
		Image image = new Image();
		image.setSource(new ThemeResource("map-banner.jpg"));
		image.setWidth("100%");
		image.setHeight("60pt");
		images.addComponent(twitter);
		images.addComponent(image);
		Label head = new Label("PlayLists");
		head.setStyleName(ValoTheme.LABEL_BOLD);
		//vl.setHeightUndefined();
		vl.setWidthUndefined();
		this.setHeightUndefined();
		//this.setWidthUndefined();
		vl.addComponent(images);
		vl.addComponents(head);
		if (Tweets == null) {
		} else {
			for (int i = 0; i < Tweets.getLength(); i++) {
				
				
				if (Tweets.item(i).hasChildNodes()) {
					 
					Item_Youtube t = new Item_Youtube(Tweets.item(i),"playlists");
					//if (t.video) { this.setWidth("800px");} else {this.setWidth("400px");}
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
	public void showInfoYoutubeChannels(NodeList Tweets) {
		this.setWidth("800px");
		 
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		vl.setStyleName("youtube");
		HorizontalLayout images = new HorizontalLayout();
		images.setMargin(false);
        images.setWidth("100%");
		Image twitter = new Image();
		twitter.setSource(new ThemeResource("youtube.png"));
		twitter.setWidth("100%");
		twitter.setHeight("60pt");
		Image image = new Image();
		image.setSource(new ThemeResource("map-banner.jpg"));
		image.setWidth("100%");
		image.setHeight("60pt");
		images.addComponent(twitter);
		images.addComponent(image);
		Label head = new Label("Channels");
		head.setStyleName(ValoTheme.LABEL_BOLD);
		//vl.setHeightUndefined();
		vl.setWidthUndefined();
		this.setHeightUndefined();
		//this.setWidthUndefined();
		vl.addComponent(images);
		vl.addComponents(head);
		if (Tweets == null) {
		} else {
			for (int i = 0; i < Tweets.getLength(); i++) {
				
				
				if (Tweets.item(i).hasChildNodes()) {
					 
					Item_Youtube t = new Item_Youtube(Tweets.item(i),"channels");
					//if (t.video) { this.setWidth("800px");} else {this.setWidth("400px");}
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

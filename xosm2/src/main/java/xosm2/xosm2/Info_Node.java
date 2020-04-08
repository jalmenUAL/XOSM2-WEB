package xosm2.xosm2;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class Info_Node extends VerticalLayout {

	VerticalLayout l = new VerticalLayout();
	XOSM2 _main;
	Button t = new Button("See Details");

	Info_Node(XOSM2 main) {
		super();
		t.setEnabled(false);
		setWidth("100%");
		_main = main;
		l.setWidth("100%");
		l.setHeight("100%");
		l.setMargin(true);
		Button b = new Button("Close and Return to Main Menu");
		b.setWidth("100%");
		b.setStyleName(ValoTheme.BUTTON_PRIMARY);
		b.setIcon(VaadinIcons.CLOSE);
		t.setWidth("100%");
		t.setStyleName(ValoTheme.BUTTON_PRIMARY);
		 
		addComponent(b);
		addComponent(t);
		addComponent(l);
		b.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				//main.accordion2.setSplitPosition(100);
				main.in.setVisible(false);
				main.accordion.setSplitPosition(35);
				t.setEnabled(false);
				_main.removeWindow(_main.popup);
				_main.removeWindow(_main.popup2);
			}
		});
	}

	public void setInfoTwitterTweets(NodeList Tweets) {
		t.setEnabled(true);
		t.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				
				_main.removeWindow(_main.popup);
				_main.removeWindow(_main.popup2);
				if (!_main.popup.isAttached()) {
					_main.addWindow(_main.popup);
					_main.popup.center();
					
				}	
				
				_main.popup.showInfoTwitterTweets(Tweets);
				
			}
		});
	}
	
	public void setInfoTwitterUsers(NodeList Tweets) {
		t.setEnabled(true);
		t.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				
				_main.removeWindow(_main.popup);
				_main.removeWindow(_main.popup2);
				if (!_main.popup.isAttached()) {
					_main.addWindow(_main.popup);
					_main.popup.center();
					
				}	
				
				_main.popup.showInfoTwitterUsers(Tweets);
				
			}
		});
	}
	
	public void setInfoYoutubeVideos(NodeList Tweets) {
		t.setEnabled(true);
		t.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				
				_main.removeWindow(_main.popup);
				_main.removeWindow(_main.popup2);
				if (!_main.popup2.isAttached()) {
					_main.addWindow(_main.popup2);
					_main.popup2.center();
					
				}	
				
				_main.popup2.showInfoYoutubeVideos(Tweets);
				
			}
		});
	}
	
	public void setInfoYoutubePlayLists(NodeList Tweets) {
		t.setEnabled(true);
		t.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				
				_main.removeWindow(_main.popup);
				_main.removeWindow(_main.popup2);
				if (!_main.popup2.isAttached()) {
					_main.addWindow(_main.popup2);
					_main.popup2.center();
					
				}	
				
				_main.popup2.showInfoYoutubePlayLists(Tweets);
				
			}
		});
	}
	
	public void setInfoYoutubeChannels(NodeList Tweets) {
		t.setEnabled(true);
		t.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				
				_main.removeWindow(_main.popup);
				_main.removeWindow(_main.popup2);
				if (!_main.popup2.isAttached()) {
					_main.addWindow(_main.popup2);
					_main.popup2.center();
					
				}	
				
				_main.popup2.showInfoYoutubeChannels(Tweets);
				
			}
		});
	}

	public void setInfo(Node n) {
		l.removeAllComponents();
		setWidth("100%");
		Image lb = new Image();
		lb.setSource(new ThemeResource("XOSM.png"));
		l.addComponent(lb);
		l.setWidth("100%");
		l.setHeight("100%");
		l.setMargin(true);
		if (n == null) {
			Notification("Error","This is an Empty Node. Information cannot be displayed.");
		} else {
			NodeList cs = n.getChildNodes();
			if (cs == null) {
				Notification("Error","This is an Empty Node. Information cannot be displayed.");
			} else {
				for (int i = 0; i < cs.getLength(); i++) {
					NamedNodeMap atts = cs.item(i).getAttributes();
					if (atts == null) {
					} else {
						int pairs = atts.getLength() / 2;
						for (int j = 0; j < pairs; j++) {
							int k = j * 2;
							if (atts.item(k).getNodeName() == "ref") {
							} else {
								FormLayout h = new FormLayout();
								h.setSpacing(false);
								String value = atts.item(k + 1).getNodeValue();
								String htmlLink = value.replaceAll(
										"(?:https|http)://([\\w/%.\\-?&=!_:()áéíóú#]+(?!.*\\[/))",
										"<a href=\"$0\" target=\"_blank\">$1</a>");
								TextField field = new TextField(atts.item(k).getNodeValue());
								field.setValue(htmlLink);
								field.setEnabled(false);
								field.setStyleName("mytextfield");
								field.setWidth("100%");
								field.setHeight("100%");
								h.addComponent(field);
								h.setWidth("100%");
								h.setHeight("100%");
								l.addComponent(h);
							}
						}

					}

				}

			}
		}

	}
	void Notification(String Topic, String Message) {
		Notification notif = new Notification(
			    Topic,
			    Message,Notification.Type.TRAY_NOTIFICATION, true);
		notif.setDelayMsec(10000);
		notif.setPosition(Position.MIDDLE_CENTER);
		notif.show(Page.getCurrent());
	}
}

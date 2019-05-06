package xosm2.xosm2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.themes.ValoTheme;

public class Item_Twitter extends VerticalLayout {

	VerticalLayout l = new VerticalLayout();

	Item_Twitter(Node n) {
		super();
		HorizontalLayout tweet = new HorizontalLayout();
		NodeList childs = n.getChildNodes();
		String stext = "";
		String sretweet__count = "";
		String sfavorite__count = "";
		String sname = "";
		String suser = "";
		String sfecha = "";
		String simage = "";
		String sback = "";
		String rtname = "";
		String rtuser = "";
		NodeList rt = null;
		Boolean retweeted = false;
		for (int i = 0; i < childs.getLength(); i++) {
			if (childs.item(i).getNodeName() == "retweeted__status") {
				retweeted = true;
				rt = childs.item(i).getChildNodes();
			}
			if (childs.item(i).getNodeName() == "user") {
				NodeList ch = childs.item(i).getChildNodes();
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "name") {
						rtuser = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "screen__name") {
						rtname = ch.item(j).getTextContent();
					}

				}
			}
		}

		if (retweeted) {
			childs = rt;
		}
		for (int i = 0; i < childs.getLength(); i++) {

			if (childs.item(i).getNodeName() == "text") {
				stext = childs.item(i).getTextContent();
			}
			if (childs.item(i).getNodeName() == "retweet__count") {
				sretweet__count = childs.item(i).getTextContent();
			}
			if (childs.item(i).getNodeName() == "favorite__count") {
				sfavorite__count = childs.item(i).getTextContent();
			}
			if (childs.item(i).getNodeName() == "user") {
				NodeList ch = childs.item(i).getChildNodes();
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "screen__name") {
						sname = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "name") {
						suser = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "profile__background__image__url") {
						sback = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "profile__image__url") {
						simage = ch.item(j).getTextContent();
					}
				}
			}
			if (childs.item(i).getNodeName() == "created__at") {
				sfecha = childs.item(i).getTextContent();
			}

		}

		if (retweeted) {
			Label retweet = new Label("Retweeted by " + "<a href='http://twitter.com/search?q=" + rtname
					+ "'  target=\"_blank\">" + rtname + "</a>", ContentMode.HTML);
			l.addComponent(retweet);
		}
		Image image = new Image();
		image.setSource(new ExternalResource(simage));
		image.setWidth("60pt");
		image.setHeight("60pt");
		Label name = new Label(
				"<a href='http://twitter.com/search?q=" + sname + "'  target=\"_blank\">" + sname + "</a>",
				ContentMode.HTML);
		name.setStyleName(ValoTheme.LABEL_BOLD);
		Label date = new Label(sfecha);
		VerticalLayout content = new VerticalLayout();
		HorizontalLayout user = new HorizontalLayout();
		HorizontalLayout info = new HorizontalLayout();
		String etext = parse(stext);

		Label text = new Label(etext, ContentMode.HTML);
		text.setWidth("240pt");
		/*
		 * Embedded e = new Embedded(null, new ExternalResource(
		 * "http://www.youtube.com/v/wBCOFv-Clsc&hl=en_US&fs=1&"));
		 * 
		 * e.setMimeType("application/x-shockwave-flash");
		 * e.setParameter("allowFullScreen", "true"); e.setWidth("240pt");
		 * e.setHeight("180pt");
		 */
		Label nlikes = new Label(sfavorite__count);
		Label likes = new Label();
		likes.setIcon(VaadinIcons.HEART);
		Label nretweets = new Label(sretweet__count);
		Label retweets = new Label();
		retweets.setIcon(VaadinIcons.RETWEET);
		info.addComponent(likes);
		info.addComponent(nlikes);
		info.addComponent(retweets);
		info.addComponent(nretweets);
		info.setMargin(false);
		user.addComponent(name);
		user.addComponent(date);
		user.setMargin(false);
		content.addComponent(user);
		content.addComponent(text);
		/* content.addComponent(e); */
		content.addComponent(info);
		content.setMargin(false);
		tweet.addComponent(image);
		tweet.addComponent(content);
		setHeight("100%");
		setWidth("100%");
		l.addComponent(tweet);
		l.setSpacing(false);
		l.setMargin(false);
		addComponent(l);
	}

	String parse(String tweetText) {

		if (tweetText.contains("http:")) {
			int indexOfHttp = tweetText.indexOf("http:");
			int endPoint = (tweetText.indexOf(' ', indexOfHttp) != -1) ? tweetText.indexOf(' ', indexOfHttp)
					: tweetText.length();
			String url = tweetText.substring(indexOfHttp, endPoint);
			String targetUrlHtml = "<a href='" + url + "' target='_blank'>" + url + "</a>";
			tweetText = tweetText.replace(url, targetUrlHtml);
		}
		if (tweetText.contains("https:")) {
			int indexOfHttp = tweetText.indexOf("https:");
			int endPoint = (tweetText.indexOf(' ', indexOfHttp) != -1) ? tweetText.indexOf(' ', indexOfHttp)
					: tweetText.length();
			String url = tweetText.substring(indexOfHttp, endPoint);
			String targetUrlHtml = "<a href='" + url + "' target='_blank'>" + url + "</a>";
			tweetText = tweetText.replace(url, targetUrlHtml);
		}

		String patternStr = "(?:\\s|\\A)[##]+([A-Za-z0-9áéóúíñ]+)";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(tweetText);
		String result = "";
		while (matcher.find()) {
			result = matcher.group();
			result = result.replace(" ", "");
			String search = result.replace("#", "");
			String searchHTML = "<a href='http://twitter.com/search?q=" + search + "'  target=\"_blank\">" + result
					+ "</a>";
			tweetText = tweetText.replace(result, searchHTML);
		}
		patternStr = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(tweetText);
		while (matcher.find()) {
			result = matcher.group();
			result = result.replace(" ", "");
			String rawName = result.replace("@", "");
			String userHTML = "<a href='http://twitter.com/" + rawName + "' target=\"_blank\">" + result + "</a>";
			tweetText = tweetText.replace(result, userHTML);
		}
		return tweetText;
	}

}

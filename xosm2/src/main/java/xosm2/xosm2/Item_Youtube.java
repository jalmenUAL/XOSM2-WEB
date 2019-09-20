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

public class Item_Youtube extends VerticalLayout {

	VerticalLayout l = new VerticalLayout();
	Boolean video = false;

	Item_Youtube(Node n, String type) {
		super();

		if (type == "videos") {
			Item_Youtube_Videos(n);
		} else if (type == "playlists") {
			Item_Youtube_Playlists(n);
		} else {
			Item_Youtube_Channels(n);
		}

	}

	void Item_Youtube_Videos(Node n) {

		l.setWidth("100%");
		l.setHeightUndefined();
		
		HorizontalLayout tweet = new HorizontalLayout();
		tweet.setWidth("100%");

		NodeList childs = n.getChildNodes();

		String sdescription = "";
		String stitle = "";
		String spublishedAt = "";
		String sid = "";
		String slikeCount ="";
		String sdislikeCount ="";
		String sviewCount = "";
		String sback ="";

		//Boolean video = false;
		Boolean statistics = false;
		for (int i = 0; i < childs.getLength(); i++) {

			if (childs.item(i).getNodeName() == "id") {
				NodeList ch = childs.item(i).getChildNodes();
				 
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "videoId") {
						sid = ch.item(j).getTextContent();
						video = true;
					} 
				}
				 
			}
			if (childs.item(i).getNodeName() == "statistics") {
				NodeList ch = childs.item(i).getChildNodes();
				statistics = true;
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "dislikeCount") {
						sdislikeCount = ch.item(j).getTextContent();
						 
					} 
					if (ch.item(j).getNodeName() == "likeCount") {
						slikeCount = ch.item(j).getTextContent();
						 
					} 
					if (ch.item(j).getNodeName() == "viewCount") {
						sviewCount = ch.item(j).getTextContent();
						 
					} 
				}
				 
			}
			if (childs.item(i).getNodeName() == "snippet") {
				NodeList ch = childs.item(i).getChildNodes();
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "title") {
						stitle = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "description") {
						sdescription = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "publishedAt") {
						spublishedAt = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "channelId") {
						if (!video) {sid = ch.item(j).getTextContent();}
					}
					if (ch.item(j).getNodeName() == "thumbnails") {
							 
						NodeList tum = ch.item(j).getChildNodes();
						for (int k = 0; k < tum.getLength(); k++) {
							if (tum.item(k).getNodeName() == "high") {
								NodeList hg = tum.item(k).getChildNodes();
								for (int l=0;  l < hg.getLength(); l++)
								{
									if (hg.item(l).getNodeName() == "url") {
										sback = hg.item(l).getTextContent();
										//iback.setSource(new ExternalResource(sback));
									}
								}
							}
							
						
					
					}
					}

				}
			}
			
		}
		
		Label link = new Label();
		//Embedded e = new Embedded();
		Label e = new Label();
		e.setWidth("100%");
		if (video) {
			 
			e = new Label("<iframe width=\"800px\" height=\"400px\"\r\n" + 
					"src=\"https://www.youtube.com/embed/"+ sid +"\" allowfullscreen>\r\n" + 
					"</iframe>",ContentMode.HTML);
			
			//e = new Embedded(null, new ExternalResource("http://www.youtube.com/v/" + sid));

			//e.setMimeType("application/x-shockwave-flash");
			//e.setParameter("allowFullScreen", "true");
			//e.setWidth("100%");
			//e.setHeight("265px");
			
		} else  
			{link = new Label("<a href='http://youtube.com/channel/" + sid + "'target=\"_blank\">"
					+"<img src='"+ sback  + "'" + "style=\"width:800px; height:400px\"" +">" + "</a></>", 
					ContentMode.HTML);
			
			}
			
		 
		
        Label nlikeCount = new Label();
        Label llikeCount = new Label();
        Label ndislikeCount = new Label();
        Label ldislikeCount = new Label();
        Label nviewCount = new Label();
        Label lviewCount = new Label();
		if (statistics) {
			nlikeCount = new Label(slikeCount);
			llikeCount = new Label();
			llikeCount.setIcon(VaadinIcons.THUMBS_UP);
			ndislikeCount = new Label(sdislikeCount);
			ldislikeCount = new Label();
			ldislikeCount.setIcon(VaadinIcons.THUMBS_DOWN);
			nviewCount = new Label(sviewCount);
			lviewCount = new Label();
			lviewCount.setIcon(VaadinIcons.EYE);
		}

		VerticalLayout content = new VerticalLayout();
		String etitle = parse(stitle);
		Label title = new Label(etitle, ContentMode.HTML);
		title.setWidth("100%");
		String edescription = parse(sdescription);
		Label description = new Label(edescription, ContentMode.HTML);
		description.setWidth("800px");
		String epublishedAt = parse(spublishedAt);
		Label publishedAt = new Label(epublishedAt, ContentMode.HTML);
		publishedAt.setWidth("100%");

		content.addComponent(title);
		if (video) {content.addComponent(e);} else {content.addComponent(link);}
		content.addComponent(description);
		if (statistics)
		{
			HorizontalLayout lstatistics = new HorizontalLayout();
			lstatistics.addComponent(lviewCount);
			lstatistics.addComponent(nviewCount);
			lstatistics.addComponent(llikeCount);
			lstatistics.addComponent(nlikeCount);	
			lstatistics.addComponent(ldislikeCount);
			lstatistics.addComponent(ndislikeCount);
			content.addComponent(lstatistics);

		}
		//content.addComponent(publishedAt);

		content.setMargin(false);
		content.setWidth("100%");
		content.setHeightUndefined();


		tweet.addComponent(content);
		setHeight("100%");
		setWidth("100%");
		l.addComponent(tweet);
		l.setSpacing(false);
		l.setMargin(false);
 		addComponent(l);
	}

	void Item_Youtube_Playlists(Node n) {

		l.setWidth("100%");
		l.setHeightUndefined();
		HorizontalLayout tweet = new HorizontalLayout();
		tweet.setWidth("100%");

		NodeList childs = n.getChildNodes();

		String sdescription = "";
		String stitle = "";
		String spublishedAt = "";
		String sid = "";
		String ssubscriberCount ="";
		String svideoCount ="";
		String sviewCount = "";
		String sback ="";

		Boolean play = false;
		Boolean statistics = false;

		for (int i = 0; i < childs.getLength(); i++) {
			
			if (childs.item(i).getNodeName() == "id") {
				NodeList ch = childs.item(i).getChildNodes();
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "playlistId") {
						sid = ch.item(j).getTextContent();
						play = true;
					}

				}
			}
			
			if (childs.item(i).getNodeName() == "statistics") {
				NodeList ch = childs.item(i).getChildNodes();
				statistics = true;
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "subscriberCount") {
						ssubscriberCount = ch.item(j).getTextContent();
						 
					} 
					if (ch.item(j).getNodeName() == "videoCount") {
						svideoCount = ch.item(j).getTextContent();
						 
					} 
					if (ch.item(j).getNodeName() == "viewCount") {
						sviewCount = ch.item(j).getTextContent();
						 
					} 
				}
				 
			}

			if (childs.item(i).getNodeName() == "snippet") {
				NodeList ch = childs.item(i).getChildNodes();
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "title") {
						stitle = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "description") {
						sdescription = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "publishedAt") {
						spublishedAt = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "channelId") {
						if (!play) {sid = ch.item(j).getTextContent();}
						 
					}
					if (ch.item(j).getNodeName() == "thumbnails") {
						 
						NodeList tum = ch.item(j).getChildNodes();
						for (int k = 0; k < tum.getLength(); k++) {
							if (tum.item(k).getNodeName() == "high") {
								NodeList hg = tum.item(k).getChildNodes();
								for (int l=0;  l < hg.getLength(); l++)
								{
									if (hg.item(l).getNodeName() == "url") {
										sback = hg.item(l).getTextContent();
										//iback.setSource(new ExternalResource(sback));
									}
								}
							}
							
						}

				}
			
			
			}
			}
			
				 
		}

		Label link = new Label();
		if (play) {
			link = new Label("<a href='http://youtube.com/playlist?list=" + sid + "' target=\"_blank\">"
					+"<img src='"+ sback  + "'" + "style=\"width:800px; height:400px\"" +">" + "</a></>", 
							ContentMode.HTML);
			 
		} else 
			link = new Label("<a href='http://youtube.com/channel/" + sid + "'target=\"_blank\">"
					+"<img src='"+ sback  + "'" + "style=\"width:800px; height:400px\"" +">" + "</a></>", 
							ContentMode.HTML);
			 
		

		VerticalLayout content = new VerticalLayout();
		String etitle = parse(stitle);
		Label title = new Label(etitle, ContentMode.HTML);
		title.setWidth("100%");
		String edescription = parse(sdescription);
		Label description = new Label(edescription, ContentMode.HTML);
		description.setWidth("800px");
		String epublishedAt = parse(spublishedAt);
		Label publishedAt = new Label(epublishedAt, ContentMode.HTML);
		publishedAt.setWidth("100%");

		content.addComponent(title);
		content.addComponent(link);
		Label nsubscriberCount = new Label();
        Label lsubscriberCount = new Label();
        Label nvideoCount = new Label();
        Label lvideoCount = new Label();
        Label nviewCount = new Label();
        Label lviewCount = new Label();
		if (statistics) {
			nsubscriberCount = new Label(ssubscriberCount);
			lsubscriberCount = new Label();
			lsubscriberCount.setIcon(VaadinIcons.BELL);
			nvideoCount = new Label(svideoCount);
			lvideoCount = new Label();
			lvideoCount.setIcon(VaadinIcons.BOOK);
			nviewCount = new Label(sviewCount);
			lviewCount = new Label();
			lviewCount.setIcon(VaadinIcons.EYE);
		}
		
		content.addComponent(description);
		if (statistics)
		{
			HorizontalLayout lstatistics = new HorizontalLayout();
			lstatistics.addComponent(lsubscriberCount);
			lstatistics.addComponent(nsubscriberCount);
			lstatistics.addComponent(lviewCount);
			lstatistics.addComponent(nviewCount);
			lstatistics.addComponent(lvideoCount);
			lstatistics.addComponent(nvideoCount);
			content.addComponent(lstatistics);

		}
		//content.addComponent(publishedAt);

		content.setMargin(false);
		content.setWidth("100%");
		content.setHeightUndefined();


		tweet.addComponent(content);
		setHeight("100%");
		setWidth("100%");
		l.addComponent(tweet);
		l.setSpacing(false);
		l.setMargin(false);
 		addComponent(l);
	}

	void Item_Youtube_Channels(Node n) {

		l.setWidth("100%");
		l.setHeightUndefined();
		HorizontalLayout tweet = new HorizontalLayout();
		tweet.setWidth("100%");

		NodeList childs = n.getChildNodes();

		String sdescription = "";
		String stitle = "";
		String spublishedAt = "";
		String sid = "";
		String ssubscriberCount ="";
		String svideoCount ="";
		String sviewCount = "";
		String sback ="";
		Boolean statistics = false;
		 
		for (int i = 0; i < childs.getLength(); i++) {

			if (childs.item(i).getNodeName() == "id") {
				
				NodeList ch = childs.item(i).getChildNodes();
				if (ch.getLength()>0) {
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "channelId") {
						sid = ch.item(j).getTextContent();
 					}

				}
				} else  sid = childs.item(i).getTextContent();	
			}
			
			if (childs.item(i).getNodeName() == "statistics") {
				NodeList ch = childs.item(i).getChildNodes();
				statistics = true;
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "subscriberCount") {
						ssubscriberCount = ch.item(j).getTextContent();
						 
					} 
					if (ch.item(j).getNodeName() == "videoCount") {
						svideoCount = ch.item(j).getTextContent();
						 
					} 
					if (ch.item(j).getNodeName() == "viewCount") {
						sviewCount = ch.item(j).getTextContent();
						 
					} 
				}
				 
			}
			if (childs.item(i).getNodeName() == "snippet") {
				NodeList ch = childs.item(i).getChildNodes();
				for (int j = 0; j < ch.getLength(); j++) {
					if (ch.item(j).getNodeName() == "title") {
						stitle = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "description") {
						sdescription = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "publishedAt") {
						spublishedAt = ch.item(j).getTextContent();
					}
					if (ch.item(j).getNodeName() == "thumbnails") {
						 
						NodeList tum = ch.item(j).getChildNodes();
						for (int k = 0; k < tum.getLength(); k++) {
							if (tum.item(k).getNodeName() == "high") {
								NodeList hg = tum.item(k).getChildNodes();
								for (int l=0;  l < hg.getLength(); l++)
								{
									if (hg.item(l).getNodeName() == "url") {
										sback = hg.item(l).getTextContent();
										//iback.setSource(new ExternalResource(sback));
									}
								}
							}
						}
						}

				}
			}
			
			 

		}

		Label link = new Label("<a href='http://youtube.com/channel/" + sid + "' target=\"_blank\">"
				
				+ "<img src='"+ sback  + "'" + "style=\"width:800px; height:400px\"" +">" + "</a></>", 
						ContentMode.HTML);

		VerticalLayout content = new VerticalLayout();
		String etitle = parse(stitle);
		Label title = new Label(etitle, ContentMode.HTML);
		title.setWidth("100%");
		String edescription = parse(sdescription);
		Label description = new Label(edescription, ContentMode.HTML);
		description.setWidth("800px");
		String epublishedAt = parse(spublishedAt);
		Label publishedAt = new Label(epublishedAt, ContentMode.HTML);
		publishedAt.setWidth("100%");

		content.addComponent(title);
		content.addComponent(link);
		Label nsubscriberCount = new Label();
        Label lsubscriberCount = new Label();
        Label nvideoCount = new Label();
        Label lvideoCount = new Label();
        Label nviewCount = new Label();
        Label lviewCount = new Label();
		if (statistics) {
			nsubscriberCount = new Label(ssubscriberCount);
			lsubscriberCount = new Label();
			lsubscriberCount.setIcon(VaadinIcons.BELL);
			nvideoCount = new Label(svideoCount);
			lvideoCount = new Label();
			lvideoCount.setIcon(VaadinIcons.BOOK);
			nviewCount = new Label(sviewCount);
			lviewCount = new Label();
			lviewCount.setIcon(VaadinIcons.EYE);
		}
		content.addComponent(description);
		if (statistics)
		{
			HorizontalLayout lstatistics = new HorizontalLayout();
			lstatistics.addComponent(lsubscriberCount);
			lstatistics.addComponent(nsubscriberCount);
			lstatistics.addComponent(lviewCount);
			lstatistics.addComponent(nviewCount);
			lstatistics.addComponent(lvideoCount);
			lstatistics.addComponent(nvideoCount);
			content.addComponent(lstatistics);

		}
		//content.addComponent(publishedAt);

		content.setMargin(false);
		content.setWidth("100%");
		content.setHeightUndefined();

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

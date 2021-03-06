package xosm2.xosm2;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;

public class Query extends VerticalLayout {

	XOSM2 main;
	String address;
	Link api = new Link();
	BrowserWindowOpener opener = null;
	String nccolor;
	String ccolor;
	String cfill;
	String icon = "i1082.png";
	Button ex = new Button("Run");

	TextField layer = new TextField();
	AceEditor editor = new AceEditor();
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	List<NameValuePair> params2 = new ArrayList<NameValuePair>();
	List<NameValuePair> params3 = new ArrayList<NameValuePair>();
	Button see_link = new Button("Get API Rest Link");
	Set<String> layers = new HashSet<String>();
	Boolean socialquery = false;
	LoadingIndicatorWindow li = new LoadingIndicatorWindow("Please wait! Running in progress...");

	class Loader implements Runnable {
		@Override
		public void run() {

			main.access(new Runnable() {
				@Override
				public void run() {
					run_query();
					main.removeWindow(li);
					main.setPollInterval(-1);
				}
			});
		}
	}

	Query(XOSM2 m, String query) {
		super();
		main = m;
		editor.setTheme(AceTheme.textmate);
		editor.setHeight(150, Unit.PIXELS);
		editor.setWordWrap(true);
		editor.setValue(query);
		editor.setFontSize("13pt");
		editor.setShowPrintMargin(false);
		editor.setMode(AceMode.xquery);
		editor.setTheme(AceTheme.textmate);
		editor.setUseWorker(true);
		editor.setReadOnly(false);
		editor.setSizeFull();
		editor.setShowInvisibles(false);
		editor.setShowGutter(false);
		editor.setUseSoftTabs(false);
		editor.setWordWrap(true);
		editor.addValueChangeListener(new com.vaadin.data.HasValue.ValueChangeListener<String>() {
			@Override
			public void valueChange(com.vaadin.data.HasValue.ValueChangeEvent<String> event) {
				String call_query = "import module namespace xosm_social = \"xosm_social\" at \"XOSMSocial.xqy\";\r\n"
						+ "import module namespace xosm_item = \"xosm_item\" at \"XOSMItem.xqy\";\r\n"
						+ "import module namespace xosm_sp = \"xosm_sp\" at \"XOSMSpatial.xqy\";\r\n"
						+ "import module namespace xosm_kw = \"xosm_kw\" at \"XOSMKeyword.xqy\";\r\n"
						+ "import module namespace xosm_ag = \"xosm_ag\" at \"XOSMAggregation.xqy\";\r\n"
						+ "import module namespace xosm_open = \"xosm_open\" at \"XOSMOpenData.xqy\";\r\n"
						+ "import module namespace xosm_pbd = \"xosm_pbd\" at \"XOSMPostGIS.xqy\";\r\n\n"
						+ editor.getValue();
				api.setVisible(false);
				see_link.setVisible(true);
				main.nelat = main.map.getBounds().getNorthEastLat();
				main.nelon = main.map.getBounds().getNorthEastLon();
				main.swlat = main.map.getBounds().getSouthWestLat();
				main.swlon = main.map.getBounds().getSouthWestLon();
				params2.clear();
				params2.add(new BasicNameValuePair("query", call_query));
				api.setCaption("API Restful Link");
				opener.setResource(new ExternalResource("http://xosm.ual.es/xosmapiV2/XOSM/minLon/" + main.swlon
						+ "/minLat/" + main.swlat + "/maxLon/" + main.nelon + "/maxLat/" + main.nelat + "?"
						+ URLEncodedUtils.format(params2, "utf-8")));
				opener.setFeatures("");
				opener.setWindowName("_blank");
				opener.extend(api);
			}
		});

		ColorPicker ncpicker = new ColorPicker();
		ncpicker.setPosition(Page.getCurrent().getBrowserWindowWidth() / 2 - 246 / 2,
				Page.getCurrent().getBrowserWindowHeight() / 2 - 507 / 2);
		ncpicker.setCaption("Select Color of Open Ways");
		ncpicker.setSwatchesVisibility(false);
		ncpicker.setHistoryVisibility(false);
		ncpicker.setTextfieldVisibility(false);
		ncpicker.setHSVVisibility(false);
		ncpicker.addValueChangeListener(event -> {
			nccolor = event.getValue().getCSS();
		});
		ColorPicker cpicker = new ColorPicker();
		cpicker.setCaption("Select Border Color of Closed Ways");
		cpicker.setPosition(Page.getCurrent().getBrowserWindowWidth() / 2 - 246 / 2,
				Page.getCurrent().getBrowserWindowHeight() / 2 - 507 / 2);
		cpicker.setSwatchesVisibility(false);
		cpicker.setHistoryVisibility(false);
		cpicker.setTextfieldVisibility(false);
		cpicker.setHSVVisibility(false);
		cpicker.addValueChangeListener(event -> {
			ccolor = event.getValue().getCSS();
		});

		ColorPicker cfpicker = new ColorPicker();
		cfpicker.setCaption("Select Filling Color of Closed Ways");
		cfpicker.setPosition(Page.getCurrent().getBrowserWindowWidth() / 2 - 246 / 2,
				Page.getCurrent().getBrowserWindowHeight() / 2 - 507 / 2);
		cfpicker.setSwatchesVisibility(false);
		cfpicker.setHistoryVisibility(false);
		cfpicker.setTextfieldVisibility(false);
		cfpicker.setHSVVisibility(false);
		cfpicker.addValueChangeListener(event -> {
			cfill = event.getValue().getCSS();
		});
		ComboBox<Integer> eidos = new ComboBox<Integer>();
		eidos.setPlaceholder("Select the Icon for Nodes");

		eidos.setWidth("100%");
		eidos.setItemCaptionGenerator(item -> "");
		eidos.setItems(IntStream.range(0, 383).boxed().collect(Collectors.toList()));
		eidos.setItemIconGenerator(item -> new ThemeResource("i" + Integer.toString((1000 + item)) + ".png"));
		eidos.setSelectedItem(7);
		eidos.setEmptySelectionAllowed(false);
		eidos.addValueChangeListener(event -> {
			icon = "i" + Integer.toString(1000 + eidos.getValue()) + ".png";
		});
		layer.setWidth("100%");
		ex.setStyleName(ValoTheme.BUTTON_DANGER);
		ex.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				main.addWindow(li);
				main.setPollInterval(1000);
				new Thread(new Loader()).start();

			}
		});

		api.setStyleName(ValoTheme.BUTTON_LINK);
		api.setVisible(false);
		see_link.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		see_link.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String call_query = "import module namespace xosm_social = \"xosm_social\" at \"XOSMSocial.xqy\";\r\n"
						+ "import module namespace xosm_item = \"xosm_item\" at \"XOSMItem.xqy\";\r\n"
						+ "import module namespace xosm_sp = \"xosm_sp\" at \"XOSMSpatial.xqy\";\r\n"
						+ "import module namespace xosm_kw = \"xosm_kw\" at \"XOSMKeyword.xqy\";\r\n"
						+ "import module namespace xosm_ag = \"xosm_ag\" at \"XOSMAggregation.xqy\";\r\n"
						+ "import module namespace xosm_open = \"xosm_open\" at \"XOSMOpenData.xqy\";\r\n"
						+ "import module namespace xosm_pbd = \"xosm_pbd\" at \"XOSMPostGIS.xqy\";\r\n\n"
						+ editor.getValue();
				params3.clear();
				params3.add(new BasicNameValuePair("query", call_query));
				api.setCaption("API Restful Link");
				opener.setResource(new ExternalResource("http://xosm.ual.es/xosmapiV2/XOSM/minLon/" + main.swlon
						+ "/minLat/" + main.swlat + "/maxLon/" + main.nelon + "/maxLat/" + main.nelat + "?"
						+ URLEncodedUtils.format(params3, "utf-8")));
				opener.setFeatures("");
				opener.setWindowName("_blank");
				opener.extend(api);
				see_link.setVisible(false);
				api.setVisible(true);
			}
		});

		params.clear();
		params.add(new BasicNameValuePair("query", editor.getValue()));
		api.setCaption("API Restful Link");
		opener = new BrowserWindowOpener(new ExternalResource(
				"http://xosm.ual.es/xosmapiV2/XOSM/minLon/" + main.swlon + "/minLat/" + main.swlat + "/maxLon/"
						+ main.nelon + "/maxLat/" + main.nelat + "?" + URLEncodedUtils.format(params, "utf-8")));
		opener.setFeatures("");
		opener.setWindowName("_blank");
		opener.extend(api);
		VerticalLayout optionslayout = new VerticalLayout();
		optionslayout.setHeight("100%");
		optionslayout.setWidth("100%");
		optionslayout.setMargin(false);
		optionslayout.setSpacing(false);

		optionslayout.addComponent(ex);
		ex.setWidth("100%");
		ex.setIcon(VaadinIcons.PLAY);

		optionslayout.addComponent(see_link);
		see_link.setWidth("100%");
		see_link.setIcon(VaadinIcons.LINK);

		optionslayout.addComponent(api);
		api.setWidth("100%");

		layer.setPlaceholder("Type the name of the layer");
		optionslayout.addComponent(layer);

		optionslayout.addComponent(eidos);
		eidos.setWidth("100%");

		optionslayout.addComponent(ncpicker);
		ncpicker.setWidth("100%");

		optionslayout.addComponent(cpicker);
		cpicker.setWidth("100%");

		optionslayout.addComponent(cfpicker);
		cfpicker.setWidth("100%");

		Label title = new Label(
				"XOSM2: XQuery-based Query Language for OpenStreetMap. Version 2. University of Almería. 2020.<br>"
						+ "Jesús M. Almendros-Jiménez, Antonio Becerra-Terón and Manuel Torres.<br>",
				ContentMode.HTML);
		title.setWidth("100%");
		title.setStyleName(ValoTheme.LABEL_COLORED);

		optionslayout.addComponent(title);

		VerticalSplitPanel split = new VerticalSplitPanel();
		split.setHeight("100%");
		split.setWidth("100%");
		split.setSplitPosition(50);
		split.setFirstComponent(editor);
		split.setSecondComponent(optionslayout);

		addComponent(split);
		setSizeUndefined();
	}

	public void run_query() {

		if (layer.getValue() == "") {
			layer.setValue("default");
		}
		if (main.nodes.containsKey(layer.getValue()) || main.way.containsKey(layer.getValue())
				|| main.twinfop.containsKey(layer.getValue()) || main.twinfow.containsKey(layer.getValue())
				|| main.twp.containsKey(layer.getValue()) || main.tww.containsKey(layer.getValue())) {
			Notification("Warning", "Layer name already exists. Please clear the map area.");
		} else {
			// save_layer.setEnabled(true);
			if (main.map.getZoomLevel() < 0) {
				Notification("Warning", "Please take an smaller area");
			} else {
				main.nelat = main.map.getBounds().getNorthEastLat();
				main.nelon = main.map.getBounds().getNorthEastLon();
				main.swlat = main.map.getBounds().getSouthWestLat();
				main.swlon = main.map.getBounds().getSouthWestLon();

				address = main.api(main.swlon, main.swlat, main.nelon, main.nelat, editor.getValue());
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

				DocumentBuilder builder = null;
				try {
					builder = builderFactory.newDocumentBuilder();
				} catch (ParserConfigurationException e) {

					e.printStackTrace();
				}

				Document xmlDocument = null;

				try {
					xmlDocument = builder.parse(new InputSource(new StringReader(address)));

					XPath xPath = XPathFactory.newInstance().newXPath();

					NodeList twitter = (NodeList) xPath.compile("/social/twitter").evaluate(xmlDocument,
							XPathConstants.NODESET);

					NodeList youtube = (NodeList) xPath.compile("/social/youtube").evaluate(xmlDocument,
							XPathConstants.NODESET);

					NodeList social = (NodeList) xPath.compile("/social").evaluate(xmlDocument, XPathConstants.NODESET);

					if (twitter.getLength() > 0) {
						socialquery = true;
						Draw d = new Draw();
						main.Draw_xml_twitter(d, xPath, main.map, xmlDocument, nccolor, ccolor, cfill, icon);
						NodeList center = (NodeList) xPath.compile("/social/twitter/oneway/node").evaluate(xmlDocument,
								XPathConstants.NODESET);
						main.map.setCenter(
								Double.parseDouble(center.item(0).getAttributes().getNamedItem("lat").getNodeValue()),
								Double.parseDouble(center.item(0).getAttributes().getNamedItem("lon").getNodeValue()));
						Notification("Successful Execution", "Click on items to see information");
					}

					else {
						if (youtube.getLength() > 0) {
							socialquery = true;
							Draw d = new Draw();
							main.Draw_xml_youtube(d, xPath, main.map, xmlDocument, nccolor, ccolor, cfill, icon);
							NodeList center = (NodeList) xPath.compile("/social/youtube/oneway/node")
									.evaluate(xmlDocument, XPathConstants.NODESET);
							main.map.setCenter(
									Double.parseDouble(
											center.item(0).getAttributes().getNamedItem("lat").getNodeValue()),
									Double.parseDouble(
											center.item(0).getAttributes().getNamedItem("lon").getNodeValue()));

							Notification("Successful Execution", "Click on items to see information");
						}

						else {

							if (social.getLength() > 0) {
								Notification("Successful Execution", "The result of the query is empty");
							} else {
								NodeList no_osm = (NodeList) xPath.compile("/osm/text").evaluate(xmlDocument,
										XPathConstants.NODESET);

								if (no_osm.getLength() == 0) // OSM Result
								{
									Notification("Successful Execution", "Click on items to see information");
									socialquery = false;
									Draw d = new Draw();
									main.Draw_xml(d, xPath, main.map, xmlDocument, nccolor, ccolor, cfill, icon);
									NodeList center = (NodeList) xPath.compile("/osm/node").evaluate(xmlDocument,
											XPathConstants.NODESET);
									main.map.setCenter(
											Double.parseDouble(
													center.item(0).getAttributes().getNamedItem("lat").getNodeValue()),
											Double.parseDouble(
													center.item(0).getAttributes().getNamedItem("lon").getNodeValue()));

								} else // Text message
								{
									socialquery = true;
									NodeList text = (NodeList) xPath.compile("/osm/text/text()").evaluate(xmlDocument,
											XPathConstants.NODESET);
									if (text.getLength() > 0) {
										if (text.item(0).getNodeValue().equals("No Result")) // Empty Result
										{
											Notification("Successful Execution", "The result of the query is empty");
										} else // Numeric Result
										{
											Notification("Successful Execution",
													"The result of the query is " + text.item(0).getNodeValue());
										}
									} else // Error Message
									{
										socialquery = true;
										NodeList errorType = (NodeList) xPath.compile("/osm/errorType/text()")
												.evaluate(xmlDocument, XPathConstants.NODESET);
										NodeList errorDescription = (NodeList) xPath
												.compile("/osm/errorDescription/text()")
												.evaluate(xmlDocument, XPathConstants.NODESET);
										Notification("Error of Execution", errorType.item(0).getNodeValue() + " : "
												+ errorDescription.item(0).getNodeValue());
									}
								}
							}
						}
					}
				} catch (SAXException | IOException e) {
					Notification("Error", "The result of the query is non valid: " + address);
				} catch (XPathExpressionException e) {
					Notification("Error", "The result of the query is non valid");
					e.printStackTrace();
				}
			}
		}

	};

	public void setQuery(String query) {
		editor.setValue(query);
	}

	void Notification(String Topic, String Message) {
		Notification notif = new Notification(Topic, Message, Notification.Type.ERROR_MESSAGE);
		notif.setDelayMsec(10000);
		notif.setPosition(Position.MIDDLE_CENTER);
		notif.show(Page.getCurrent());
	}
}

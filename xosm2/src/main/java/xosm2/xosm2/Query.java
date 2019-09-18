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
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
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
	String icon = "i1007.png";
	Button ex = new Button("Run");
	TextField layer = new TextField();
	AceEditor editor = new AceEditor();
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	List<NameValuePair> params2 = new ArrayList<NameValuePair>();
	List<NameValuePair> params3 = new ArrayList<NameValuePair>();
	Button see_link = new Button("Get API Rest Link");
	Button save_layer = new Button("Save Layer");
	Button delete_layer = new Button("Delete Layer");
	Set<String> layers = new HashSet<String>();

	Query(XOSM2 m, String query) {
		super();
		main = m;
		editor.setTheme(AceTheme.textmate);
		editor.setHeight(150, Unit.PIXELS);
		editor.setWordWrap(true);
		/*editor.setValue("import module namespace xosm_item = \"xosm_item\" at \"XOSMItem.xqy\";\r\n" + 
				"import module namespace xosm_sp = \"xosm_sp\" at \"XOSMSpatial.xqy\";\r\n" + 
				"import module namespace xosm_kw = \"xosm_kw\" at \"XOSMKeyword.xqy\";\r\n" + 
				"import module namespace xosm_ag = \"xosm_ag\" at \"XOSMAggregation.xqy\";\r\n" + 
				"import module namespace xosm_open = \"xosm_open\" at \"XOSMOpenData.xqy\";\r\n" + 
				"import module namespace xosm_pbd = \"xosm_pbd\" at \"XOSMPostGIS.xqy\";\r\n\n" + query);*/
		editor.setValue(query);

		editor.setFontSize("14pt");
		editor.setShowPrintMargin(false);
		editor.setMode(AceMode.xquery);
		editor.setTheme(AceTheme.textmate);
		editor.setUseWorker(true);
		editor.setReadOnly(false);
		editor.setSizeFull();
		editor.setShowInvisibles(false);
		editor.setShowGutter(false);
		editor.setUseSoftTabs(false);
		editor.addValueChangeListener(new com.vaadin.data.HasValue.ValueChangeListener<String>() {
			@Override
			public void valueChange(com.vaadin.data.HasValue.ValueChangeEvent<String> event) {
				
				String call_query = "import module namespace xosm_social = \"xosm_social\" at \"XOSMSocial.xqy\";\r\n" + 		
						"import module namespace xosm_item = \"xosm_item\" at \"XOSMItem.xqy\";\r\n" + 
								"import module namespace xosm_sp = \"xosm_sp\" at \"XOSMSpatial.xqy\";\r\n" + 
								"import module namespace xosm_kw = \"xosm_kw\" at \"XOSMKeyword.xqy\";\r\n" + 
								"import module namespace xosm_ag = \"xosm_ag\" at \"XOSMAggregation.xqy\";\r\n" + 
								"import module namespace xosm_open = \"xosm_open\" at \"XOSMOpenData.xqy\";\r\n" + 
								"import module namespace xosm_pbd = \"xosm_pbd\" at \"XOSMPostGIS.xqy\";\r\n\n" +
						editor.getValue();
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
		layer.setValue("default");
		layer.setWidth("100%");
		layer.addValueChangeListener(event -> {
			save_layer.setEnabled(true);
		});
		ex.setStyleName(ValoTheme.BUTTON_DANGER);
		ex.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (layer.getValue() == "") {
					layer.setValue("default");
				}
				if (main.nodes.containsKey(layer.getValue()) 
						|| main.way.containsKey(layer.getValue())
						|| main.twinfop.containsKey(layer.getValue())
						|| main.twinfow.containsKey(layer.getValue())
						|| main.twp.containsKey(layer.getValue())
						|| main.tww.containsKey(layer.getValue())) {
					Notification.show("Layer name already exists");
				} else {
					save_layer.setEnabled(true);
					if (main.map.getZoomLevel() < 0) {
						Notification.show("Please take an smaller area");
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

							NodeList social = (NodeList) xPath.compile("/social").evaluate(xmlDocument,
									XPathConstants.NODESET);

							if (social.getLength() > 0) {
								Draw d = new Draw();
								main.Draw_xml_twitter(d, xPath, main.map, xmlDocument, nccolor, ccolor, cfill, icon);
								NodeList center = (NodeList) xPath.compile("/social/twitter/oneway/node")
										.evaluate(xmlDocument, XPathConstants.NODESET);
								main.map.setCenter(
										Double.parseDouble(
												center.item(0).getAttributes().getNamedItem("lat").getNodeValue()),
										Double.parseDouble(
												center.item(0).getAttributes().getNamedItem("lon").getNodeValue()));
								 
							} else {
								NodeList no_osm = (NodeList) xPath.compile("/osm/text").evaluate(xmlDocument,
										XPathConstants.NODESET);

								if (no_osm.getLength() == 0) // OSM Result
								{
									Notification.show("Click on items to see information");
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
									NodeList text = (NodeList) xPath.compile("/osm/text/text()").evaluate(xmlDocument,
											XPathConstants.NODESET);
									if (text.getLength() > 0) {
										if (text.item(0).getNodeValue().equals("No Result")) // Empty Result
										{
											Notification.show("The result of the query is empty");
										} else // Numeric Result
										{
											Notification
													.show("The result of the query is " + text.item(0).getNodeValue());
										}
									} else // Error Message
									{
										NodeList errorType = (NodeList) xPath.compile("/osm/errorType/text()")
												.evaluate(xmlDocument, XPathConstants.NODESET);
										NodeList errorDescription = (NodeList) xPath
												.compile("/osm/errorDescription/text()")
												.evaluate(xmlDocument, XPathConstants.NODESET);
										Notification.show(errorType.item(0).getNodeValue() + " : "
												+ errorDescription.item(0).getNodeValue());
									}
								}
							}

						} catch (SAXException | IOException e) {
							Notification.show("The result of the query is non valid: " + address);
						} catch (XPathExpressionException e) {
							Notification.show("The result of the query is non valid");
							e.printStackTrace();
						}
					}
				}
			}
			

		});

		api.setStyleName(ValoTheme.BUTTON_LINK);
		api.setVisible(false);
		see_link.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		see_link.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String call_query = "import module namespace xosm_social = \"xosm_social\" at \"XOSMSocial.xqy\";\r\n" + 		
						"import module namespace xosm_item = \"xosm_item\" at \"XOSMItem.xqy\";\r\n" + 
								"import module namespace xosm_sp = \"xosm_sp\" at \"XOSMSpatial.xqy\";\r\n" + 
								"import module namespace xosm_kw = \"xosm_kw\" at \"XOSMKeyword.xqy\";\r\n" + 
								"import module namespace xosm_ag = \"xosm_ag\" at \"XOSMAggregation.xqy\";\r\n" + 
								"import module namespace xosm_open = \"xosm_open\" at \"XOSMOpenData.xqy\";\r\n" + 
								"import module namespace xosm_pbd = \"xosm_pbd\" at \"XOSMPostGIS.xqy\";\r\n\n" +
						editor.getValue();
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
		save_layer.setStyleName(ValoTheme.BUTTON_PRIMARY);
		delete_layer.setStyleName(ValoTheme.BUTTON_PRIMARY);
		save_layer.setEnabled(false);
		delete_layer.setEnabled(false);
		save_layer.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				save_layer.setEnabled(false);
				delete_layer.setEnabled(true);
				main.api_post(address, layer.getValue());
			}
		});

		delete_layer.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				save_layer.setEnabled(true);
				delete_layer.setEnabled(false);
				System.out.println("Borrando");
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
		optionslayout.addComponent(ex);
		ex.setWidth("100%");
		ex.setIcon(VaadinIcons.PLAY);
		optionslayout.addComponent(see_link);
		see_link.setWidth("100%");
		see_link.setIcon(VaadinIcons.LINK);
		optionslayout.addComponent(api);
		api.setWidth("100%");
		Button lb = new Button("Select name for the layer");
		lb.setStyleName(ValoTheme.BUTTON_BORDERLESS);
		lb.setEnabled(false);
		lb.setWidth("100%");
		optionslayout.addComponent(lb);
		optionslayout.addComponent(layer);
		optionslayout.addComponent(save_layer);
		save_layer.setWidth("100%");
		save_layer.setIcon(VaadinIcons.ARCHIVE);
		optionslayout.addComponent(delete_layer);
		delete_layer.setWidth("100%");
		delete_layer.setIcon(VaadinIcons.CROSS_CUTLERY);
		optionslayout.setHeight("100%");
		optionslayout.setWidth("100%");
		optionslayout.setSpacing(false);
		optionslayout.setMargin(false);
		optionslayout.addComponent(eidos);
		eidos.setWidth("100%");
	    optionslayout.addComponent(ncpicker);
		ncpicker.setWidth("100%");
		optionslayout.addComponent(cpicker);
		cpicker.setWidth("100%");
		optionslayout.addComponent(cfpicker);
		cfpicker.setWidth("100%");

		/*Label title = new Label("XOSM: XQuery-based query language for OSM. Version 2."
				+ "Jesús M. Almendros-Jiménez, Antonio Becerra-Terón and Manuel Torres."
				+ "University of Almería. 2019. "
				+ "<a href='http://indalog.ual.es/WWW_pages/JesusAlmendros/Publications.html'>Information Systems Group Publications</a>",
				ContentMode.HTML);
		title.setWidth("100%");
		title.setStyleName(ValoTheme.LABEL_COLORED);*/
		optionslayout.setSpacing(false);
		VerticalSplitPanel split = new VerticalSplitPanel();
		split.setHeight("100%");
		split.setWidth("100%");
		split.setSplitPosition(50);
		split.setFirstComponent(editor);
		split.setSecondComponent(optionslayout);
		addComponent(split);
		setSizeUndefined();
	}

	public void setQuery(String query) {
		/*editor.setValue("import module namespace xosm_item = \"xosm_item\" at \"XOSMItem.xqy\";\r\n" + 
				"import module namespace xosm_sp = \"xosm_sp\" at \"XOSMSpatial.xqy\";\r\n" + 
				"import module namespace xosm_kw = \"xosm_kw\" at \"XOSMKeyword.xqy\";\r\n" + 
				"import module namespace xosm_ag = \"xosm_ag\" at \"XOSMAggregation.xqy\";\r\n" + 
				"import module namespace xosm_open = \"xosm_open\" at \"XOSMOpenData.xqy\";\r\n" + 
				"import module namespace xosm_pbd = \"xosm_pbd\" at \"XOSMPostGIS.xqy\";\r\n\n\n" + query);*/
		editor.setValue(query);
	}
}

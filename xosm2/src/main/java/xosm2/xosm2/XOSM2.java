package xosm2.xosm2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.vaadin.addon.leaflet.LCircle;
import org.vaadin.addon.leaflet.LCircleMarker;
import org.vaadin.addon.leaflet.LLayerGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import org.vaadin.addon.leaflet.LPolygon;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletClickEvent;
import org.vaadin.addon.leaflet.LeafletClickListener;
import org.vaadin.addon.leaflet.LeafletMoveEndEvent;
import org.vaadin.addon.leaflet.LeafletMoveEndListener;

import org.vaadin.addon.leaflet.shared.Bounds;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.viritin.layouts.MVerticalLayout;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

import elemental.json.JsonArray;

@Theme("mytheme")
public class XOSM2 extends UI {

	HorizontalSplitPanel accordion = new HorizontalSplitPanel();
	HorizontalSplitPanel accordion2 = new HorizontalSplitPanel();
	Twitter popup = new Twitter();
	Query q = null;
	Info_Node in = new Info_Node(this);
	Double swlat = 36.83645, swlon = -2.45516, nelat = 36.83912, nelon = -2.45007;
	XOSM2 this_ = this;
	String osm_team = "<h1 style=\"color:DodgerBlue;\">Aim of the Project</h1>"
			+ "<p>Volunteered geographic information (VGI) makes available a very large resource of geographic data.</p>"
			+ "<p>The exploitation of data coming from such resources requires an additional effort in the form of tools</p>"
			+ "<p>and effective processing techniques. One of the most established VGI is OpenStreetMap (OSM)</p>"
			+ "<p>offering data of urban and rural maps from the earth. XOSM (XQuery for OpenStreetMap),</p>"
			+ "<p>is a tool for the processing of geospatial queries on OSM. The tool is equipped with a</p>"
			+ "<p>rich query language based on a set of operators defined for OSM which have been implemented</p>"
			+ "<p>as a library of the XML query language XQuery. The library provides a rich repertoire of</p>"
			+ "<p>spatial, keyword and aggregation based functions, which act on the XML representation of</p>"
			+ "<p>an OSM layer. The use of the higher order facilities of XQuery makes possible the</p>"
			+ "<p>definition of complex geospatial queries involving spatial relations, keyword searching</p>"
			+ "<p>and aggregation functions. XOSM indexes OSM data enabling efficient retrieval of answers.</p>"
			+ "<p>The XOSM library also enables the definition of queries combining OSM layers and layers</p>"
			+ "<p>created from Linked Open Data resources (KML, GeoJSON, JSON, CSV and RDF)</p>"
			+ "<p>and Social Networks.The tool also provides an API to execute XQuery queries using the library.</p>"
			+ "<p style=\"color:Red;\">XOSM Team</p>"
			+ "<p style=\"color:Blue;\">Jesús Manuel Almendros-Jimenez jalmen@ual.es</p>"
			+ "<p style=\"color:Blue;\">Antonio Becerra-Terón abecerra@ual.es</p>"
			+ "<p style=\"color:Blue;\">Manuel Torres mtorres@ual.es</p>"
			+ "<p style=\"color:Green;\">Departamento de Informatica (University of Almería)</p>"
			+ "<p style=\"color:Green;\">04120 Almería (Spain)</p>";

	Info_Tool info_team = new Info_Tool(osm_team);
	String indexing = "PostGIS-indexed functions for elements retrieval\n" + "xosm_pbd:getLayerByBB(.):layer\n"
			+ "xosm_pbd:getLayerByName(.,name,distance):layer\n" + "xosm_pbd:getElementsByKeyword(.,keyword):layer\n"
			+ "xosm_pbd:getLayerByElement(.,element,distance):layer\n" + "xosm_pbd:getElementByName(.,name):layer\n";

	String data = "OSM element creation\n" + "xosm_item:point(name,lat,lon,tags):node\n"
			+ "xosm_item:way(name,segments):way\n" + "xosm_item:segment(id,refs,tags):segment\n"
			+ "xosm_item:ref(id,lon,lat):nd\n" + "xosm_item:tag(k,v):tag\n\n" + "OSM element manipulation\n"
			+ "element[@type=\"point\"]:Boolean\n" + "element[@type=\"way\"]:Boolean\n"
			+ "element[@type=\"polygon\"]:Boolean\n" + "xosm_item:lon(node):Double\n" + "xosm_item:lat(node):Double\n"
			+ "xosm_item:name(way):String\n" + "xosm_item:refs(way):Sequence of nd\n"
			+ "xosm_item:tags(element): Sequence of tag\n" + "xosm_item:segments(way): Sequence of segment\n"
			+ "xosm_item:id(element): String\n" + "xosm_item:length(element): Double\n"
			+ "xosm_item:area(element): Double\n" + "xosm_item:distance(element,element): Double\n";

	String spatial = "Coordinate based OSM Operators\n\n" + "Based on Distance\n"
			+ "xosm_sp:DWithIn(element1,element2,d):Boolean\n\n"
			+ "Based on Latitude and Longitude (p1,p2: node, w1,w2: way)\n"
			+ "xosm_sp:furtherNorthPoints(p1,p2):Boolean,\n" + "xosm_sp:furtherSouthPoints(p1,p2):Boolean,\n"
			+ "xosm_sp:furtherEastPoints(p1,p2):Boolean,\n" + "xosm_sp:furtherWestPoints(p1,p2):Boolean,\n"
			+ "xosm_sp:furtherNorthWays(w1,w2):Boolean,\n" + "xosm_sp:furtherSouthWays(w1,w2):Boolean,\n"
			+ "xosm_sp:furtherEastWays(w1,w2):Boolean,\n" + "xosm_sp:furtherWestWays(w1,w2):Boolean\n" + "\n"
			+ "Clementini based OSM Operators\n" + "xosm_sp:intersectionPoints(w1,w2):layer,\n"
			+ "xosm_sp:crossing(element1,element2):Boolean,\n" + "xosm_sp:nonCrossing(element1,element2):Boolean,\n"
			+ "xosm_sp:touching(element1,element2):Boolean,\n" + "xosm_sp:nonTouching(element1,element2):Boolean,\n"
			+ "xosm_sp:intersecting(element1,element2):Boolean,\n"
			+ "xosm_sp:nonIntersecting(element1,element2):Boolean,\n"
			+ "xosm_sp:containing(element1,element2):Boolean,\n" + "xosm_sp:nonContaining(element1,element2):Boolean,\n"
			+ "xosm_sp:within(element1,element2):Boolean,\n" + "xosm_sp:nonWithin(element1,element2):Boolean,\n"
			+ "xosm_sp:overlapping(element1,element2): Boolean,\n"
			+ "xosm_sp:nonOverlapping(element1,element2):Boolean,\n" + "xosm_sp:disjoint(element1,element2):Boolean,\n"
			+ "xosm_sp:nonDisjoint(element1,element2):Boolean\n";

	String keyword = "Keyword Operators (element: node/way)\n" + "xosm_kw:searchKeyword(element,keyword):Boolean\r\n"
			+ "xosm_kw:searchKeywordSet(element,(keyword1,...., keywordn)):Boolean\r\n"
			+ "xosm_kw:searchTag(element,k,v):Boolean\r\n" + "xosm_kw:getTagValue(element,k):Boolean\r\n";

	String aggregation = "Distributive Operators\r\n"
			+ "xosm_ag:topologicalCount(layer,element,topologicalRelation):Integer\r\n"
			+ "xosm_ag:metricMin(layer,metricOperator):layer\r\n" + "xosm_ag:metricMax(layer,metricOperator):layer\r\n"
			+ "xosm_ag:min(layer,metricOperator):Double\r\n" + "xosm_ag:max(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricSum(layer,metricOperator):Double\r\n" + "Algebraic Operators\r\n"
			+ "xosm_ag:metricAvg(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricTopCount(layer,metricOperator,k):layer\r\n"
			+ "xosm_ag:metricBottomCount(layer,metricOperator,k):layer\r\n" + "Holistic Operators\r\n"
			+ "xosm_ag:metricRank(layer,metricOperator,k):element\r\n"
			+ "xosm_ag:metricMedian(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricMode(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricRange(layer,metricOperator):Double\r\n";

	String lod = "JSON, GEOJSON, CSV, KML and WIKIPEDIA operators\n"
			+ "xosm_open:json2osm(url,path,name,id,lat,lon):layer\r\n" + "xosm_open:geojson2osm(url,name):layer\r\n"
			+ "xosm_open:csv2osm(url,name,lon,lat):layer\r\n" + "xosm_open:kml2osm(url,name):layer\r\n"
			+ "xosm_open:wikipediaElement2osm(node):layer\r\n" + "xosm_open:wikipediaCoordinates2osm(lon,lat):layer\r\n"
			+ "xosm_open:wikipediaName2osm(address):layer\r\n";

	String api =

			"GetLayerByName\r\n" + "http://xosm.ual.es/xosmapi/getLayerByName/minLon/{minLon}/minLat/{minLat}\n"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/name/{name}/distance/{distance}\r\n" + "GetLayerByElement\r\n"
					+ "http://xosm.ual.es/xosmapi/getLayerByElement/minLon/{minLon}/minLat/{minLat}\n"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/lon/{lon}/lat/{lat}/distance/{distance}\r\n"
					+ "GetElementByName\r\n"
					+ "http://xosm.ual.es/xosmapi/getElementByName/minLon/{minLon}/minLat/{minLat}\n"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/name/{name}\r\n" + "GetElementsByKeyword\r\n"
					+ "http://xosm.ual.es/xosmapi/getElementsByKeyword/minLon/{minLon}/minLat/{minLat}\n"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/keyword/{keyword}\r\n" + "GetLayerByBB\r\n"
					+ "http://xosm.ual.es/xosmapi/getLayerByBB/minLon/{minLon}/minLat/{minLat}\n"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}\r\n" + "Query\r\n"
					+ "http://xosm.ual.es/xosmapi/XOSMQuery/minLon/{minLon}/minLat/{minLat}\n"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}?query={query}\r\n";

	Help_Tool info_tool = new Help_Tool(indexing, data, spatial, keyword, aggregation, lod, api);

	LMap map = new LMap();
	Map<String, NodeList> nodes = new HashMap<String, NodeList>();
	Map<String, NodeList> way = new HashMap<String, NodeList>();
	Map<String, List<Node>> twp = new HashMap<String, List<Node>>();
	Map<String, List<Node>> tww = new HashMap<String, List<Node>>();
	Map<String, List<NodeList>> twinfop = new HashMap<String, List<NodeList>>();
	Map<String, List<NodeList>> twinfow = new HashMap<String, List<NodeList>>();

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		this.addDetachListener(new DetachListener() {
			public void detach(DetachEvent event) {
				System.out.println("######### Detached ##########");
			}
		});
		JavaScript.getCurrent().addFunction("aboutToClose", new JavaScriptFunction() {
			@Override
			public void call(JsonArray arguments) {
				System.out.println("Window/Tab is Closed.");
			}
		});
		Page.getCurrent().getJavaScript().execute(
				"window.onbeforeunload = function (e) " + "{ var e = e || window.event; aboutToClose(); return; };");

		LTileLayer osmTiles = new LOpenStreetMapLayer();
		osmTiles.setAttributionString("© OpenStreetMap Contributors");
		map.addBaseLayer(osmTiles, "OSM");
		map.setZoomLevel(17);
        popup.center();
		
		map.addMoveEndListener(new LeafletMoveEndListener() {
			@Override
			public void onMoveEnd(LeafletMoveEndEvent event) {
				Bounds b = event.getBounds();
				swlat = b.getSouthWestLat();
				swlon = b.getSouthWestLon();
				nelat = b.getNorthEastLat();
				nelon = b.getNorthEastLon();
				Notification.show(String.format("Coordinates (%.4f,%.4f ; %.4f,%.4f)", b.getSouthWestLat(),
						b.getSouthWestLon(), b.getNorthEastLat(), b.getNorthEastLon()), Type.TRAY_NOTIFICATION);
			}
		});
		map.setSizeFull();
		map.setCenter(41.90219, 12.49580);
		map.setCenter(36.838030858833, -2.4522979583778);
		q = new Query(this, "xosm_pbd:getElementsByKeyword(.,\"shop\")");
		
		
		MenuBar barmenu = new MenuBar();
		barmenu.setWidth("100%");
		 
		barmenu.addItem("Menu");
		
		
		// Define a common menu command for all the menu items.
				MenuBar.Command mycommand = new MenuBar.Command() {
				    public void menuSelected(MenuItem selectedItem) {
				        
				    }
				};
		
		MenuItem examples = barmenu.addItem("Examples",null,null);
		MenuItem helpi = barmenu.addItem("Help",null,null);
		MenuItem Info = barmenu.addItem("Info",null,null);
		MenuItem indexing = examples.addItem("Indexing Examples", null, null);
		MenuItem osm = examples.addItem("Layers Examples", null, null);
		MenuItem spatial = examples.addItem("Spatial Examples", null, null);
		MenuItem keyword = examples.addItem("Keyword Examples", null, null);
		MenuItem aggregation = examples.addItem("Aggregation Examples", null, null);
		MenuItem open = examples.addItem("Open Data Examples", null, null);
		MenuItem social = examples.addItem("Social Network Examples", null, null);
		MenuItem twitter = social.addItem("Twitter Examples",null,null);
		MenuItem youtube = social.addItem("Youtube Examples",null,null);
		
		
		MenuItem open1 = open.addItem("Retrieve the streets in the bounding box intersecting Haymarket street", null, null);
		MenuItem open2 = open.addItem("Retrieve the restaurants in Roma further north to Miami hotel", null, null);
		MenuItem open3 = open.addItem("Retrieve hotels of Vienna close (500 m) to food venues (food venues = number of bars and restaurants bigger than 10)", null, null);
		MenuItem open4 = open.addItem("Retrieve the hotels of Munich with the greatest number of churches nearby", null, null);
		MenuItem open5 =open.addItem("Retrieve the size of buidings close (500 m) to Karl-Liebknecht-Straße in Berlin", null, null);
		MenuItem open6 = open.addItem("Retrieve the biggest churchs close (1500 m) to Piazza del Duomo in Milan", null, null);
		MenuItem open7 = open.addItem("Request taxi stops close (500 m) to Bruxelles Central Station in Bruxelles", null, null);
		MenuItem open8 = open.addItem("Retrieves free events of Madrid", null, null);
		MenuItem open9 = open.addItem("Retrieve Wikipedia information about places nearby to the intersection point of Calle Mayor and Calle de Esparteros in Madrid", null, null);
		
		MenuItem twitter1 = twitter.addItem("Get Tweets sent by people about touristic places with more than 3 retweets", null, null);
		MenuItem twitter2 = twitter.addItem("Get Tweets sent by official accounts of touristic places", null, null);
		MenuItem twitter3 = twitter.addItem("Get Tweets sent by people in close areas (1km) to touristic places with more than 1 likes", null, null);
		MenuItem twitter4 = twitter.addItem("Get Tweets sent by people about amenity places with more than 3 retweets", null, null);
		MenuItem twitter5 = twitter.addItem("Get Tweets sent by official accounts of museums", null, null);
		MenuItem twitter6 = twitter.addItem("Get Tweets sent by people in close areas (1km) to restaurants", null, null);

		 
		MenuItem ind1 = indexing.addItem("Retrieve the elements of the bounding box close (500 m) to an street", null, null);
		MenuItem ind2 = indexing.addItem("Retrieve the shops of the bounding box", null, null);
		MenuItem ind3 = indexing.addItem("Retrieve an element of the bounding box", null, null);
		MenuItem ind4 = indexing.addItem("Retrieve the elements of the bounding box close (500 m) to an street", null, null);
		MenuItem osm1 = osm.addItem("Retrieve the ways of the bounding box", null, null);
		MenuItem osm2 = osm.addItem("Retrieve the points of the bounding box", null, null);
		MenuItem osm3 = osm.addItem("Rebuild the points of the bounding box close (500 m) to an street", null, null);
		MenuItem osm4 = osm.addItem("Rebuild the ways of the bounding box close (500 m) to an street", null, null);
		MenuItem spa1 = spatial.addItem("Retrieve the points of the bounding box within (300 m) to an street", null, null);
		MenuItem spa2 = spatial.addItem("Retrieve the points of the bounding box further west to a point", null, null);
		MenuItem spa3 = spatial.addItem("Retrieve the streets crossing an street", null, null);
		MenuItem spa4 = spatial.addItem("Retrieve the streets disjoint with an street", null, null);
		MenuItem key1 = keyword.addItem("Retrieve the amenity bars of the bounding box", null, null);
		MenuItem key2 = keyword.addItem("Retrieve the amenities of the bounding box", null, null);
		MenuItem key3 = keyword.addItem("Retrieve the bars and restaurants of the bounding box", null, null);
		MenuItem agg1 = aggregation.addItem("Retrieve the number of streets crossing an street", null, null);
		MenuItem agg2 = aggregation.addItem("Retrieve the buildings with maximum area close (500m) to an street", null, null);
		MenuItem agg3 = aggregation.addItem("Retrieve the sum of the length of highways close (500m) to an street", null, null);
		MenuItem agg4 = aggregation.addItem("Retrieve the average area of buildings close (500m) to an street", null, null);
		MenuItem agg5 = aggregation.addItem("Retrieve the 5 longest elements", null, null);
		MenuItem agg6 = aggregation.addItem("Retrieve the median of the area of elements close (500m) to an street", null, null);
		MenuItem agg7 = aggregation.addItem("Retrieve the mode of the area of elements close (500m) to an street", null, null);
		MenuItem agg8 = aggregation.addItem("Retrieve the biggest element close (500m) to an street", null, null);
		MenuItem agg9 = aggregation.addItem("Retrieve the range of area of elements close (500m) to an street", null, null);
		MenuItem op1 = open.addItem("Import geojson data", null, null);
		MenuItem op2 = open.addItem("Import kml data", null, null);
		MenuItem op4 = open.addItem("Import csv data", null, null);
		MenuItem op5 = open.addItem("Wikipedia information", null, null);

		

		
		 

		 
		
		

		
		
				

		popup.setResizable(false);
		Window examples_pbd = new Window();
		Window examples_simples = new Window();
		Button ex1 = new Button("Retrieve the streets in the bounding box intersecting Haymarket street");
		ex1.setWidth("100%");
		ex1.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex2 = new Button("Retrieve the restaurants in Roma further north to Miami hotel");
		ex2.setWidth("100%");
		ex2.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex3 = new Button("Retrieve hotels of Vienna close (500 m) to food venues (food venues = number of bars "
				+ "and restaurants bigger than 10)");
		ex3.setWidth("100%");
		ex3.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex4 = new Button("Retrieve the hotels of Munich with the greatest number of churches nearby");
		ex4.setWidth("100%");
		ex4.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex5 = new Button("Retrieve the size of buidings close (500 m) to Karl-Liebknecht-Straße in Berlin");
		ex5.setWidth("100%");
		ex5.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex6 = new Button("Retrieve the biggest churchs close (1500 m) to Piazza del Duomo in Milan");
		ex6.setWidth("100%");
		ex6.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex7 = new Button("Request taxi stops close (500 m) to Bruxelles Central Station in Bruxelles");
		ex7.setWidth("100%");
		ex7.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex8 = new Button("Retrieves free events of Madrid");
		ex8.setWidth("100%");
		ex8.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex9 = new Button("Retrieve Wikipedia information about places nearby to the intersection "
				+ "point of Calle Mayor and Calle de Esparteros in Madrid");
		ex9.setWidth("100%");
		ex9.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex10 = new Button("Get Tweets sent by people about touristic places with more than 3 retweets");
		ex10.setWidth("100%");
		ex10.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex11 = new Button("Get Tweets sent by official accounts of touristic places");
		ex11.setWidth("100%");
		ex11.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex12 = new Button("Get Tweets sent by people in close areas (1km) to touristic places with more than 1 likes");
		ex12.setWidth("100%");
		ex12.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex13 = new Button("Get Tweets sent by people about amenity places with more than 3 retweets");
		ex13.setWidth("100%");
		ex13.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex14 = new Button("Get Tweets sent by official accounts of museums");
		ex14.setWidth("100%");
		ex14.setStyleName(ValoTheme.BUTTON_LINK);
		Button ex15 = new Button("Get Tweets sent by people in close areas (1km) to restaurants");
		ex15.setWidth("100%");
		ex15.setStyleName(ValoTheme.BUTTON_LINK);

		VerticalLayout hl = new VerticalLayout();
		hl.setSizeUndefined();
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
		hl.addComponent(ex11);
		hl.addComponent(ex12);
		hl.addComponent(ex13);
		hl.addComponent(ex14);
		hl.addComponent(ex15);

		examples_pbd.setContent(hl);

		Button exam1 = new Button("Retrieve the elements of the bounding box close (500 m) to an street");
		exam1.setWidth("100%");
		exam1.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam2 = new Button("Retrieve the shops of the bounding box");
		exam2.setWidth("100%");
		exam2.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam3 = new Button("Retrieve an element of the bounding box");
		exam3.setWidth("100%");
		exam3.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam4 = new Button("Retrieve the elements of the bounding box close (500 m) to an street");
		exam4.setWidth("100%");
		exam4.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam5 = new Button("Retrieve the ways of the bounding box");
		exam5.setWidth("100%");
		exam5.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam6 = new Button("Retrieve the points of the bounding box");
		exam6.setWidth("100%");
		exam6.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam7 = new Button("Rebuild the points of the bounding box close (500 m) to an street");
		exam7.setWidth("100%");
		exam7.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam8 = new Button("Rebuild the ways of the bounding box close (500 m) to an street");
		exam8.setWidth("100%");
		exam8.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam9 = new Button("Retrieve the points of the bounding box within (300 m) to an street");
		exam9.setWidth("100%");
		exam9.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam10 = new Button("Retrieve the points of the bounding box further west to a point");
		exam10.setWidth("100%");
		exam10.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam11 = new Button("Retrieve the streets crossing an street");
		exam11.setWidth("100%");
		exam11.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam12 = new Button("Retrieve the streets disjoint with an street");
		exam12.setWidth("100%");
		exam12.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam13 = new Button("Retrieve the amenity bars of the bounding box");
		exam13.setWidth("100%");
		exam13.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam14 = new Button("Retrieve the amenities of the bounding box");
		exam14.setWidth("100%");
		exam14.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam15 = new Button("Retrieve the bars and restaurants of the bounding box");
		exam15.setWidth("100%");
		exam15.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam16 = new Button("Retrieve the number of streets crossing an street");
		exam16.setWidth("100%");
		exam16.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam17 = new Button("Retrieve the buildings with maximum area close (500m) to an street");
		exam17.setWidth("100%");
		exam17.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam18 = new Button("Retrieve the sum of the length of highways close (500m) to an street");
		exam18.setWidth("100%");
		exam18.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam19 = new Button("Retrieve the average area of buildings close (500m) to an street");
		exam19.setWidth("100%");
		exam19.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam20 = new Button("Retrieve the 5 longest elements");
		exam20.setWidth("100%");
		exam20.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam21 = new Button("Retrieve the median of the area of elements close (500m) to an street");
		exam21.setWidth("100%");
		exam21.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam22 = new Button("Retrieve the mode of the area of elements close (500m) to an street");
		exam22.setWidth("100%");
		exam22.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam23 = new Button("Retrieve the biggest element close (500m) to an street");
		exam23.setWidth("100%");
		exam23.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam24 = new Button("Retrieve the range of area of elements close (500m) to an street");
		exam24.setWidth("100%");
		exam24.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam25 = new Button("Import geojson data");
		exam25.setWidth("100%");
		exam25.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam26 = new Button("Import kml data");
		exam26.setWidth("100%");
		exam26.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam27 = new Button("Import csv data");
		exam27.setWidth("100%");
		exam27.setStyleName(ValoTheme.BUTTON_LINK);

		Button exam28 = new Button("Wikipedia information");
		exam28.setWidth("100%");
		exam28.setStyleName(ValoTheme.BUTTON_LINK);

		VerticalLayout hlp = new VerticalLayout();
		hlp.setSizeUndefined();
		hlp.addComponent(exam1);
		hlp.addComponent(exam2);
		hlp.addComponent(exam3);
		hlp.addComponent(exam4);
		hlp.addComponent(exam5);
		hlp.addComponent(exam6);
		hlp.addComponent(exam7);
		hlp.addComponent(exam8);
		hlp.addComponent(exam9);
		hlp.addComponent(exam10);
		hlp.addComponent(exam11);
		hlp.addComponent(exam12);
		hlp.addComponent(exam13);
		hlp.addComponent(exam14);
		hlp.addComponent(exam15);
		hlp.addComponent(exam16);
		hlp.addComponent(exam17);
		hlp.addComponent(exam18);
		hlp.addComponent(exam19);
		hlp.addComponent(exam20);
		hlp.addComponent(exam21);
		hlp.addComponent(exam22);
		hlp.addComponent(exam23);
		hlp.addComponent(exam24);
		hlp.addComponent(exam25);
		hlp.addComponent(exam26);
		hlp.addComponent(exam27);
		hlp.addComponent(exam28);

		examples_simples.setContent(hlp);

		// INDEXING

		String exq1 = "xosm_pbd:getLayerByName(.,\"Calle Calzada de Castro\",500)";

		String exq2 = "xosm_pbd:getElementsByKeyword(.,\"shop\")";

		String exq3 = "xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")";

		String exq4 = "let $e := xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")\n"
				+ "let $layer := xosm_pbd:getLayerByElement(.,$e,100)\n" + "return $layer";

		// OSM

		String exq5 = "xosm_pbd:getLayerByBB(.)[@type=\"way\"]";

		String exq6 = "xosm_pbd:getLayerByBB(.)[@type=\"point\"]";

		String exq7 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,\"Carrera de los Limoneros\",100)\r\n"
				+ "return\r\n" + "for $node in $layer[@type=\"point\"]\r\n"
				+ "return xosm_item:point(xosm_item:name($node),xosm_item:lon($node),xosm_item:lat($node),xosm_item:tags($node))";

		String exq8 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(. ,\"Carrera de los Limoneros\",100)\r\n"
				+ "return\r\n" + "for $node in $layer[@type=\"way\"]\r\n" + "let $nodes := xosm_item:nodes($node)\r\n"
				+ "for $t in xosm_item:segments($node)\r\n"
				+ "return xosm_item:way(xosm_item:name($node),xosm_item:segment(xosm_item:id($t),xosm_item:refs($t),xosm_item:tags($t)),$nodes)";

		// SPATIAL

		String exq9 = "let $layer := xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $e := xosm_pbd:getElementByName(.,'Calle Calzada de Castro')\r\n" + "return\r\n"
				+ "filter($layer,xosm_sp:DWithIn(?,$e,300))\r\n";

		String exq10 = "let $layer := xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $p := xosm_pbd:getElementByName(.,\"ACUYO IRIARTE\")\r\n" + "return\r\n"
				+ "filter($layer,xosm_sp:furtherWestPoints($p,?))\r\n";

		String exq11 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $cc := xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")\r\n" + "return\r\n"
				+ "filter($layer[@type=\"way\"],xosm_sp:crossing($cc,?)) ";

		String exq12 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $cc := xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")\r\n" + "return\r\n"
				+ "filter($layer[@type=\"way\"],xosm_sp:disjoint(?,$cc)) ";

		// KEYWORD

		String exq13 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n"
				+ "filter($layer,xosm_kw:searchTag(?,\"amenity\",\"bar\"))";

		String exq14 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n"
				+ "filter($layer,xosm_kw:searchKeyword(?,\"amenity\"))";

		String exq15 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n"
				+ "filter($layer,xosm_kw:searchKeywordSet(?,(\"bar\",\"restaurant\")))";

		// AGGREGATION

		String exq16 = "let $layer :=\r\n" + "xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $e := xosm_pbd:getElementByName(.,'Calle Calzada de Castro')\r\n"
				+ "return xosm_ag:topologicalCountG($layer,$e,function($x,$y){xosm_sp:crossing($x,$y)})";

		String exq17 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings := fn:filter($layer,xosm_kw:searchKeyword(?,'building'))\r\n"
				+ "return xosm_ag:metricMaxG($buildings,function($x){xosm_item:area($x)})";

		String exq18 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings := fn:filter($layer,xosm_kw:searchKeyword(?,'highway'))\r\n"
				+ "return xosm_ag:metricSumG($buildings,function($x){xosm_item:length($x)})";

		String exq19 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings := fn:filter($layer,xosm_kw:searchKeyword(?,'building'))\r\n"
				+ "return xosm_ag:metricAvgG($buildings,function($x){xosm_item:area($x)})";

		String exq20 = "let $layer :=\r\n" + "xosm_pbd:getLayerByBB(.)\r\n"
				+ "return xosm_ag:metricTopCountG($layer,function($x){xosm_item:length($x)},5)";

		String exq21 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricMedianG($layer[@type=\"way\"],function($x){xosm_item:length($x)})";

		String exq22 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricModeG($layer,function($x){xosm_item:area($x)})";

		String exq23 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricRankG($layer,function($x){xosm_item:area($x)},1)";

		String exq24 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricRangeG($layer,function($x){xosm_item:area($x)})";

		// OPEN DATA

		String exq25 = "xosm_open:geojson2osm(\r\n"
				+ "\"https://opendata.paris.fr/explore/dataset/paris_taxis_stations/download/?format=geojson&amp;timezone=UTC\",\"\")";

		String exq26 = "xosm_open:kml2osm(\r\n"
				+ "\"http://geodata.vermont.gov/datasets/b1ae7b7b110447c3b452d9cacffeed36_174.kml\",\r\n"
				+ "\"SiteName\")";

		String exq27 = "xosm_open:csv2osm(\"http://geodata.vermont.gov/datasets/b1ae7b7b110447c3b452d9cacffeed36_174.csv\",\r\n"
				+ "\"SiteName\",\"X\",\"Y\") ";

		String exq28 = "xosm_open:wikipediaName2osm(\"Almeria\")";

		String q1 = "let $hm := xosm_pbd:getElementByName(.,'Haymarket')\r\n"
				+ "let $bb := xosm_pbd:getLayerByBB(.)[@type=\"way\"]\r\n"
				+ "return fn:filter($bb,xosm_sp:intersecting(?,$hm))";

		String q2 = "let $hotel := xosm_pbd:getElementByName(.,'Hotel Miami')\r\n"
				+ "let $layer := xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n" + "fn:filter(\r\n"
				+ "fn:filter($layer,xosm_kw:searchKeyword(?,'restaurant')),\r\n"
				+ "xosm_sp:furtherNorthPoints($hotel,?)) ";

		String q3 = "for $hotel in xosm_pbd:getElementsByKeyword(.,'hotel')\r\n"
				+ "let $layer := xosm_pbd:getLayerByElement(., $hotel ,500)\r\n" + "where count(fn:filter\r\n"
				+ "($layer,xosm_kw:searchKeywordSet(?,('bar','restaurant')))) >= 10\r\n" + "return $hotel";

		String q4 = "let $hotel := xosm_pbd:getElementsByKeyword(.,'hotel')\r\n" + "let $f := function($hotel)\r\n"
				+ "{-(count(fn:filter(\r\n" + "xosm_pbd:getLayerByElement(.,$hotel,500),\r\n"
				+ "xosm_kw:searchKeyword(?,'church'))))}\r\n" + "return fn:sort($hotel,$f)[1]";

		String q5 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Karl-Liebknecht-Straße' ,500)\r\n"
				+ "let $buildings := fn:filter($layer,\r\n" + "xosm_kw:searchKeyword(?,'building'))\r\n"
				+ "return xosm_ag:metricSumG($buildings,function($x){xosm_item:area($x)})";

		String q6 = "let $layer := xosm_pbd:getLayerByName(.,'Piazza del Duomo',1500)\r\n" + "return\r\n"
				+ "xosm_ag:metricMaxG(\r\n"
				+ "filter($layer,xosm_kw:searchKeyword(?,'church')),function($x){xosm_item:area($x)})\r\n";

		// CAMBIAR

		String q7 = "let $open :=\r\n"
				+ "'https://opendata.bruxelles.be/explore/dataset/test-geojson-station-de-taxi/download/?format=geojson&amp;timezone=UTC'\r\n"
				+ "let $taxis :=\r\n" + "xosm_open:geojson2osm($open,'')\r\n" + "let $building :=\r\n"
				+ "xosm_pbd:getElementByName (.,'Bruxelles-Central - Brussel-Centraal') \r\n"
				+ "return fn:filter($taxis,xosm_sp:DWithIn($building,?,500))";
		String q8 = "let $open :=\r\n"
				+ "'http://data2.esrism.opendata.arcgis.com/datasets/51900577e33a4ba4ab59a691247aeee9_0.geojson'\r\n"
				+ "let $events :=\r\n " + "xosm_open:geojson2osm($open,'') \r\n" + "return fn:filter($events,\r\n"
				+ "function($p) {not(empty($p/node/tag[@k='GRATUITO' and @v='Sí']))})";

		String q9 = "let $x := xosm_pbd:getElementByName(.,'Calle Mayor')\r\n"
				+ "let $y := xosm_pbd:getElementByName(.,'Calle de Esparteros')\r\n" + "return\r\n"
				+ "for $i in xosm_sp:intersectionPoints($x,$y)\r\n" + "return xosm_open:wikipediaElement2osm($i) ";

		 
		String q10 ="<social>{\r\n" + 
				"for $tourism in xosm_pbd:getElementsByKeyword(.,\r\n" + 
				"\"tourism\")\r\n" + 
				"let $name := web:encode-url($tourism/@name)\r\n" + 
				"where $name\r\n" + 
				"let $address := concat(\"http://minerva.ual.es:8080/api.social/twitterSearchUser/q/\", $name)\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $user := data($json/json/_/screen__name)\r\n" + 
				"for $each in $user\r\n" + 
				"let $address :=\r\n" + 
				"concat(concat(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets/q/\",$user),\"?count=15\")\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $tweets := for $tweet in $json/json/statuses/_ where $tweet/retweet__count > 3\r\n" + 
				"return $tweet\r\n" + 
				"return\r\n" + 
				"<twitter number=\"{count($tweets)}\">{\r\n" + 
				"($tourism,\r\n" + 
				"<tweets>{$tweets\r\n" + 
				"}\r\n" + 
				"</tweets>\r\n" + 
				")\r\n" + 
				"}\r\n" + 
				"</twitter>\r\n" + 
				"}</social>";
		
		String q11="<social>{\r\n" + 
				"for $tourism in xosm_pbd:getElementsByKeyword(.,\r\n" + 
				"\"tourism\")\r\n" + 
				"let $name := web:encode-url($tourism/@name)\r\n" + 
				"where $name\r\n" + 
				"let $address := concat(\"http://minerva.ual.es:8080/api.social/twitterSearchUser/q/\", $name)\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $user := data($json/json/_/screen__name)\r\n" + 
				"for $each in $user\r\n" + 
				"let $address :=\r\n" + 
				"concat(concat(\"http://minerva.ual.es:8080/api.social/twitterUserTimeLine/screen_name/\",\r\n" + 
				"$each),\"?count=10\")\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $tweets := for $tweet in $json/json/_\r\n" + 
				"return $tweet\r\n" + 
				"return\r\n" + 
				"<twitter number=\"{count($tweets)}\">{\r\n" + 
				"($tourism,\r\n" + 
				"<tweets>{$tweets\r\n" + 
				"}\r\n" + 
				"</tweets>\r\n" + 
				")\r\n" + 
				"}\r\n" + 
				"</twitter>\r\n" + 
				"}</social>";
		
		String q12="<social>{\r\n" + 
				"for $tourism in xosm_pbd:getElementsByKeyword(.,\r\n" + 
				"\"tourism\")\r\n" + 
				"let $name := web:encode-url($tourism/@name)\r\n" + 
				"where $name\r\n" + 
				"let $address := concat(\"http://minerva.ual.es:8080/api.social/twitterSearchUser/q/\", $name)\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $user := data($json/json/_/screen__name)\r\n" + 
				"for $each in $user\r\n" + 
				"let $lon := data($tourism/node[1]/@lon), $lat := data($tourism/node[1]/@lat)\r\n" + 
				"let $geocode := concat(\"?geocode=\", concat(concat(concat(concat($lat, \",\"), $lon), \",\" ), \"1km\"))\r\n" + 
				"let $address :=\r\n" + 
				"concat(concat(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets/q/\",data($name)),\r\n" + 
				"$geocode)\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $tweets := for $tweet in $json/json/statuses/_ where $tweet/favorite__count > 1\r\n" + 
				"return $tweet\r\n" + 
				"return\r\n" + 
				"<twitter number=\"{count($tweets)}\">{\r\n" + 
				"($tourism,\r\n" + 
				"<tweets>{$tweets\r\n" + 
				"}\r\n" + 
				"</tweets>\r\n" + 
				")\r\n" + 
				"\r\n" + 
				"}\r\n" + 
				"</twitter>\r\n" + 
				"}</social> ";
		
		String q13="<social>{\r\n" + 
				"for $amenity in xosm_pbd:getElementsByKeyword(.,\r\n" + 
				"\"amenity\")\r\n" + 
				"let $name := web:encode-url(replace($amenity/@name, \"/\", \"\"))\r\n" + 
				"where $name\r\n" + 
				"let $address := concat(\"http://minerva.ual.es:8080/api.social/twitterSearchUser/q/\", $name)\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $user := data($json/json/_/screen__name)\r\n" + 
				"for $each in $user\r\n" + 
				"let $address :=\r\n" + 
				"concat(concat(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets/q/\",$user),\"?count=15\")\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $tweets := for $tweet in $json/json/statuses/_ where $tweet/retweet__count > 3\r\n" + 
				"return $tweet\r\n" + 
				"return\r\n" + 
				"<twitter number=\"{count($tweets)}\">{\r\n" + 
				"($amenity,\r\n" + 
				"<tweets>{$tweets\r\n" + 
				"}\r\n" + 
				"</tweets>\r\n" + 
				")\r\n" + 
				"}\r\n" + 
				"</twitter>\r\n" + 
				"}</social>";
		
		String q14="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\",\r\n" + 
				"\"museum\")\r\n" + 
				"let $name := web:encode-url($museum/@name)\r\n" + 
				"\r\n" + 
				"where $name\r\n" + 
				"let $address := concat(\"http://minerva.ual.es:8080/api.social/twitterSearchUser/q/\", $name)\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $user := data($json/json/_/screen__name)\r\n" + 
				"for $each in $user\r\n" + 
				"let $address :=\r\n" + 
				"concat(concat(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets/q/\",$user),\"?count=15\")\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $tweets := for $tweet in $json/json/statuses/_\r\n" + 
				"return $tweet\r\n" + 
				"return\r\n" + 
				"<twitter number=\"{count($tweets)}\">{\r\n" + 
				"($museum,\r\n" + 
				"<tweets>{$tweets\r\n" + 
				"}\r\n" + 
				"</tweets>\r\n" + 
				")\r\n" + 
				"}\r\n" + 
				"</twitter>\r\n" + 
				"}</social>";
		
		String q15="<social>{\r\n" + 
				"for $restaurant in xosm_pbd:getElementsByKV(.,\r\n" + 
				"\"amenity\", \"restaurant\")\r\n" + 
				"let $name := web:encode-url($restaurant/@name)\r\n" + 
				"where $name\r\n" + 
				"let $address := concat(\"http://minerva.ual.es:8080/api.social/twitterSearchUser/q/\", $name)\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $user := data($json/json/_/screen__name)\r\n" + 
				"for $each in $user\r\n" + 
				"let $lon := data($restaurant/node[1]/@lon), $lat := data($restaurant/node[1]/@lat)\r\n" + 
				"let $geocode := concat(\"?geocode=\", concat(concat(concat(concat($lat, \",\"), $lon), \",\" ), \"1km\"))\r\n" + 
				"let $address :=\r\n" + 
				"concat(concat(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets/q/\",data($name)),\r\n" + 
				"$geocode)\r\n" + 
				"\r\n" + 
				"let $text := fetch:text($address)\r\n" + 
				"let $json := json:parse($text)\r\n" + 
				"let $tweets := for $tweet in $json/json/statuses/_\r\n" + 
				"return $tweet\r\n" + 
				"return\r\n" + 
				"<twitter number=\"{count($tweets)}\">{\r\n" + 
				"($restaurant,\r\n" + 
				"<tweets>{$tweets\r\n" + 
				"}\r\n" + 
				"</tweets>\r\n" + 
				")\r\n" + 
				"}\r\n" + 
				"</twitter>\r\n" + 
				"}</social>";
		
		exam1.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq1);
				removeWindow(examples_simples);
				
			}

		});

		exam2.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq2);
				removeWindow(examples_simples);
			}

		});

		exam3.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq3);
				removeWindow(examples_simples);
			}

		});

		exam4.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq4);
				removeWindow(examples_simples);
			}

		});

		exam5.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq5);
				removeWindow(examples_simples);
			}

		});

		exam6.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq6);
				removeWindow(examples_simples);
			}

		});

		exam7.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq7);
				removeWindow(examples_simples);
			}

		});

		exam8.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq8);
				removeWindow(examples_simples);
			}

		});

		exam9.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq9);
				removeWindow(examples_simples);
			}

		});
		exam10.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq10);
				removeWindow(examples_simples);
			}

		});

		exam11.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq11);
				removeWindow(examples_simples);
			}

		});

		exam12.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq12);
				removeWindow(examples_simples);
			}

		});

		exam13.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq13);
				removeWindow(examples_simples);
			}

		});

		exam14.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq14);
				removeWindow(examples_simples);
			}

		});

		exam15.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq15);
				removeWindow(examples_simples);
			}

		});

		exam16.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq16);
				removeWindow(examples_simples);
			}

		});

		exam17.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq17);
				removeWindow(examples_simples);
			}

		});

		exam18.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq18);
				removeWindow(examples_simples);
			}

		});

		exam19.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq19);
				removeWindow(examples_simples);
			}

		});

		exam20.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq20);
				removeWindow(examples_simples);
			}

		});

		exam21.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq21);
				removeWindow(examples_simples);
			}

		});

		exam22.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq22);
				removeWindow(examples_simples);
			}

		});

		exam23.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq23);
				removeWindow(examples_simples);
			}

		});

		exam24.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq24);
				removeWindow(examples_simples);
			}

		});

		exam25.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq25);
				removeWindow(examples_simples);
			}

		});

		exam26.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq26);
				removeWindow(examples_simples);
			}

		});

		exam27.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq27);
				removeWindow(examples_simples);
			}

		});

		exam28.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq28);
				removeWindow(examples_simples);
			}

		});

		ex1.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(51.50884, -0.13201);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q1);
				removeWindow(examples_pbd);
			}

		});

		ex2.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(41.90219, 12.49580);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q2);
				removeWindow(examples_pbd);
			}

		});

		ex3.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(48.20817, 16.37382);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q3);
				removeWindow(examples_pbd);
			}

		});

		ex4.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(48.13513, 11.58198);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q4);
				removeWindow(examples_pbd);
			}

		});

		ex5.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(52.52250, 13.40952);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q5);
				removeWindow(examples_pbd);
			}

		});

		ex6.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(45.465820637638, 9.1893592282028);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q6);
				removeWindow(examples_pbd);
			}

		});

		ex7.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(50.8445, 4.3537);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q7);
				removeWindow(examples_pbd);
			}

		});

		ex8.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(40.42, -3.68);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q8);
				removeWindow(examples_pbd);
			}

		});

		ex9.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(40.4164, -3.70501);
				map.setZoomLevel(17);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q9);
				removeWindow(examples_pbd);
			}

		});
		
		ex10.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4096+40.4206)/2,(-3.7022+-3.688)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q10);
				removeWindow(examples_pbd);
			}

		});
		
		ex11.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				
				map.setCenter((40.4096+40.4206)/2,(-3.7022+-3.688)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q11);
				removeWindow(examples_pbd);
			}

		});
		
		ex12.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				
				map.setCenter((40.4096+40.4206)/2,(-3.7022+-3.688)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q12);
				removeWindow(examples_pbd);
			}

		});
		
		ex13.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter((36.83616+36.83906)/2,(-2.46659+-2.46074)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q13);
				removeWindow(examples_pbd);
			}

		});
		
		ex14.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				
				map.setCenter((40.4096+40.4206)/2,(-3.7022+-3.688)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q14);
				removeWindow(examples_pbd);
			}

		});
		
		ex15.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				map.setCenter(36.838030858833, -2.4522979583778);
				
				map.setCenter((40.4504+40.45933)/2,(-3.69032+-3.67885)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q15);
				removeWindow(examples_pbd);
			}

		});
		
		 

		Button searchb = new Button();
		searchb.setIcon(VaadinIcons.SEARCH);
		searchb.setWidth("100%");

		TextField searchtf = new TextField();
		searchtf.setWidth("100%");
		searchtf.setPlaceholder("Type a Place to Query");

		PopupButton examplesPBD = new PopupButton("More");
		examplesPBD.setWidth("100%");

		PopupButton examplesSIMPLES = new PopupButton("Examples");
		examplesSIMPLES.setWidth("100%");

		Button restart = new Button("Clear");
		restart.setIcon(VaadinIcons.REFRESH);
		restart.setWidth("100%");

		Button help = new Button("Help");
		help.setWidth("100%");
		help.setIcon(VaadinIcons.QUESTION);

		Button info = new Button("Info");
		info.setWidth("100%");
		info.setIcon(VaadinIcons.INFO);

		//HorizontalLayout buttons = new HorizontalLayout();
		 

		searchb.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				if (searchtf.getValue() == "") {
					Notification.show("Address cannot be empty");
				} else

				{
					XPath xPath = XPathFactory.newInstance().newXPath();
					String result = search(searchtf.getValue());
					DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

					DocumentBuilder builder = null;
					try {
						builder = builderFactory.newDocumentBuilder();
					} catch (ParserConfigurationException e) {

						e.printStackTrace();
					}
					Document xmlDocument = null;

					try {
						xmlDocument = builder.parse(new InputSource(new StringReader(result)));
					} catch (SAXException | IOException e) {

						e.printStackTrace();
					}

					if (xmlDocument == null) {
					} else {
						try {
							NodeList search = (NodeList) xPath.compile("/searchresults/place")
									.evaluate(xmlDocument, XPathConstants.NODESET);

							NodeList test = (NodeList) xPath.compile("/searchresults/place/@lat").evaluate(xmlDocument,
									XPathConstants.NODESET);

							if (test.getLength() == 0) {
								Notification.show("The address cannot be found");
							} else {
								Node lat = search.item(0).getAttributes().getNamedItem("lat");
								Node lon = search.item(0).getAttributes().getNamedItem("lon");
								map.setCenter(Float.parseFloat(lat.getNodeValue()),
										Float.parseFloat(lon.getNodeValue()));
								map.setZoomLevel(17);
								 
							}

						} catch (XPathExpressionException e) {

							e.printStackTrace();
						}
					}
				}
			}

		});

		examplesPBD.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				removeWindow(examples_pbd);
				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				addWindow(examples_pbd);
			}

		});

		examplesSIMPLES.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				removeWindow(examples_simples);
				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				addWindow(examples_simples);
			}

		});

		restart.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				nodes.clear();
				way.clear();
				twp.clear();
				tww.clear();
				twinfop.clear();
				twinfow.clear();
				map.removeAllComponents();
				LTileLayer osmTiles = new LOpenStreetMapLayer();
				osmTiles.setAttributionString("© OpenStreetMap Contributors");
				map.addBaseLayer(osmTiles, "OSM");
			}

		});

		help.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				removeWindow(info_tool);
				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				info_tool.setWidth("40%");
				addWindow(info_tool);
			}

		});

		info.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				removeWindow(info_team);
				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				info_team.setSizeUndefined();
				addWindow(info_team);
			}

		});

		HorizontalLayout all = new HorizontalLayout();
		all.setWidth("100%");
		HorizontalLayout search = new HorizontalLayout();
		search.addComponent(searchtf);
		search.addComponent(searchb);
		search.setWidth("100%");
		search.setExpandRatio(searchtf, 2.25f);
		search.setExpandRatio(searchb, 0.25f);
		//buttons.addComponent(examplesSIMPLES);
		//buttons.addComponent(examplesPBD);
		//buttons.addComponent(restart);
		//buttons.addComponent(help);
		//buttons.addComponent(info);
		//buttons.setWidth("100%");
		all.addComponent(barmenu);
		all.addComponent(restart);
		all.addComponent(search);
		all.setExpandRatio(barmenu,0.1f);
		all.setExpandRatio(restart,0.1f);
		all.setExpandRatio(search,0.8f);
		//all.addComponent(buttons);
		MVerticalLayout vl = new MVerticalLayout(all).expand(map);
		vl.setMargin(false);
		q.setSizeFull();
		q.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);
		accordion2.setSizeFull();
		accordion2.setSplitPosition(100);
		accordion2.setFirstComponent(vl);
		in.setWidth("100%");
		accordion2.setSecondComponent(in);
		accordion.setSizeFull();
		accordion.setSplitPosition(35);
		accordion.setFirstComponent(q);
		accordion.setSecondComponent(accordion2);
		setContent(accordion);
		setStyleName("layout-with-border3");
	}

	public String search(String address) {
		String xml = "";

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("q", address));
			params.add(new BasicNameValuePair("addressdetails", "1"));
			params.add(new BasicNameValuePair("format", "xml"));
			params.add(new BasicNameValuePair("limit", "1"));
			HttpGet request = new HttpGet(
					"https://nominatim.openstreetmap.org/" + "?" + URLEncodedUtils.format(params, "utf-8"));
			request.addHeader("content-type", "application/xml");
			HttpResponse result = httpClient.execute(request);
			xml = EntityUtils.toString(result.getEntity(), "UTF-8");
		} catch (IOException ex) {
		}
		return xml;
	}

	public void Draw_xml(Draw d, XPath xPath, LMap map, Document xmlDocument, String nccolor, String ccolor,
			String cfill, String icon) {
		LLayerGroup lanswer = new LLayerGroup();
		Draw_xml_nodes(lanswer, d, xPath, map, xmlDocument, icon);
		Draw_xml_ways(lanswer, d, xPath, map, xmlDocument, nccolor, ccolor, cfill);
		map.addOverlay(lanswer, q.layer.getValue());

	}

	public void Draw_xml_nodes(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument, String Icon) {

		if (xmlDocument == null) {
		} else {
			try {
				nodes.put(q.layer.getValue(),
						(NodeList) xPath.compile("/osm/node[tag]").evaluate(xmlDocument, XPathConstants.NODESET));
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			if (nodes == null) {
			} else
				for (int i = 0; i < nodes.get(q.layer.getValue()).getLength(); i++) {
					if ((!(nodes.get(q.layer.getValue()).item(i).getAttributes().getNamedItem("lat").getNodeValue()
							.equals("")))
							& (!(nodes.get(q.layer.getValue()).item(i).getAttributes().getNamedItem("lon")
									.getNodeValue().equals("")))) {
						LMarker leafletMarker = d.Draw_Node(
								Double.parseDouble(nodes.get(q.layer.getValue()).item(i).getAttributes()
										.getNamedItem("lat").getNodeValue()),
								Double.parseDouble(nodes.get(q.layer.getValue()).item(i).getAttributes()
										.getNamedItem("lon").getNodeValue()));
						leafletMarker.setIcon(new ThemeResource(Icon));
						lanswer.addComponent(leafletMarker); //
						int j = i;
						leafletMarker.addClickListener(new LeafletClickListener() {
							@Override
							public void onClick(LeafletClickEvent event) {
								in.setInfo(nodes.get(q.layer.getValue()).item(j));
								accordion2.setSplitPosition(65);
								accordion.setSplitPosition(0);
							}
						});
					}
				}
		}
	}

	public void Draw_xml_ways(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument, String nccolor,
			String ccolor, String cfill) {
		if (xmlDocument == null) {
		} else {
			try {
				way.put(q.layer.getValue(),
						(NodeList) xPath.compile("/osm/way").evaluate(xmlDocument, XPathConstants.NODESET));
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (way == null) {
			} else
				for (int i = 0; i < way.get(q.layer.getValue()).getLength(); i++) {
					NodeList children = way.get(q.layer.getValue()).item(i).getChildNodes();
					List<Point> p = new ArrayList<Point>();
					NodeList lat2 = null;
					NodeList lon2 = null;
					/*
					 * String lat2 = ""; String lon2 = "";
					 */
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName() == "nd") {
							try {
								/*
								 * lat2 =
								 * lat_position_node(children,children.item(j).getAttributes().getNamedItem(
								 * "ref") .getNodeValue());
								 */
								lat2 = (NodeList) xPath.compile("/osm/node[@id="
										+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue()
										+ "]/@lat").evaluate(xmlDocument, XPathConstants.NODESET);
							} catch (XPathExpressionException e) {

								e.printStackTrace();
							}
							try {
								/*
								 * lon2 =
								 * lon_position_node(children,children.item(j).getAttributes().getNamedItem(
								 * "ref") .getNodeValue());
								 */
								lon2 = (NodeList) xPath.compile("/osm/node[@id="
										+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue()
										+ "]/@lon").evaluate(xmlDocument, XPathConstants.NODESET);

							} catch (XPathExpressionException e) {

								e.printStackTrace();
							}

							try {
								p.add(new Point(Double.parseDouble(lat2.item(0).getNodeValue()),
										Double.parseDouble(lon2.item(0).getNodeValue())));
							} catch (Exception e) {
								e.printStackTrace();
							}

							/*
							 * p.add(new Point(Double.parseDouble(lat2), Double.parseDouble(lon2)));
							 */

						}

					}
					Point[] points = p.toArray(new Point[p.size()]);
					if (points[0].getLat().equals(points[points.length - 1].getLat())
							& points[0].getLon().equals(points[points.length - 1].getLon())) {
						LPolygon pl = d.Draw_Polygon(ccolor, cfill, points);
						lanswer.addComponent(pl); //
						int j = i;
						pl.addClickListener(new LeafletClickListener() {
							@Override
							public void onClick(LeafletClickEvent event) {
								in.setInfo(way.get(q.layer.getValue()).item(j));
								accordion2.setSplitPosition(65);
								accordion.setSplitPosition(0);
							}
						});
					} else {
						LPolyline pl = d.Draw_Polyline(nccolor, points);
						lanswer.addComponent(pl); //
						int j = i;
						pl.addClickListener(new LeafletClickListener() {
							@Override
							public void onClick(LeafletClickEvent event) {
								in.setInfo(way.get(q.layer.getValue()).item(j));
								accordion2.setSplitPosition(65);
								accordion.setSplitPosition(0);
							}
						});
					}
				}
		}

	}

	/*
	 * public String lon_position_node(NodeList children,String ref) {
	 * 
	 * String result = "";
	 * 
	 * for (int i=0; i<children.getLength();i++) { if
	 * (children.item(i).getNodeName()=="node" &&
	 * children.item(i).getAttributes().getNamedItem("id").getNodeValue()==ref) {
	 * result=children.item(i).getAttributes().getNamedItem("lon").getNodeValue();}
	 * } return result; };
	 * 
	 * public String lat_position_node(NodeList children,String ref) { String result
	 * = "";
	 * 
	 * for (int i=0; i<children.getLength();i++) { if
	 * (children.item(i).getNodeName()=="node" &&
	 * children.item(i).getAttributes().getNamedItem("id").getNodeValue()==ref) {
	 * result=children.item(i).getAttributes().getNamedItem("lat").getNodeValue();}
	 * } return result;
	 * 
	 * };
	 */

	public void Draw_xml_twitter(Draw d, XPath xPath, LMap map, Document xmlDocument, String nccolor, String ccolor,
			String cfill, String icon) {
		LLayerGroup lanswer = new LLayerGroup();
		Draw_xml_nodes_twitter(lanswer, d, xPath, map, xmlDocument, icon);
		Draw_xml_ways_twitter_marks(lanswer, d, xPath, map, xmlDocument, icon);
		Draw_xml_ways_twitter(lanswer, d, xPath, map, xmlDocument, nccolor, ccolor, cfill);
		map.addOverlay(lanswer, q.layer.getValue());
	}

	public void Draw_xml_nodes_twitter(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument,
			String Icon) {
		if (xmlDocument == null) {
		} else {
			NodeList nodes_list = null;
			try {
				nodes_list = (NodeList) xPath.compile("/social/twitter[oneway/node[tag]]").evaluate(xmlDocument,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			if (nodes_list == null) {
			} else {
				NodeList oneway = null;
				NodeList tweets = null;
				Node node = null;
				int ok = -1;
				int tk = -1;
				for (int i = 0; i < nodes_list.getLength(); i++) {
					if (nodes_list.item(i).hasChildNodes()) {
						NamedNodeMap atts = nodes_list.item(i).getAttributes();
						String number = atts.getNamedItem("number").getNodeValue();
						NodeList sobjects = nodes_list.item(i).getChildNodes();
						for (int ch = 0; ch < sobjects.getLength(); ch++) {
							if (sobjects.item(ch).getNodeName() == "oneway") {
								oneway = sobjects.item(ch).getChildNodes();
								for (int n = 0; n < oneway.getLength(); n++) {
									if (oneway.item(n).getNodeName() == "node") {
										ok++;
										node = oneway.item(n);
										if (twp.containsKey(q.layer.getValue())) {
											List<Node> current = twp.get(q.layer.getValue());
											current.add(node);
											twp.put(q.layer.getValue(), current);
										} else {
											List<Node> current = new ArrayList<Node>();
											current.add(node);
											twp.put(q.layer.getValue(), current);
										}
									}
								}
							}
							if (sobjects.item(ch).getNodeName() == "tweets") {
								tk++;
								tweets = sobjects.item(ch).getChildNodes();
								if (twinfop.containsKey(q.layer.getValue())) {
									List<NodeList> current = twinfop.get(q.layer.getValue());
									current.add(tweets);
									twinfop.put(q.layer.getValue(), current);
								} else {
									List<NodeList> current = new ArrayList<NodeList>();
									current.add(tweets);
									twinfop.put(q.layer.getValue(), current);
								}
							}
						}

						if ((!(node.getAttributes().getNamedItem("lat").getNodeValue().equals("")))
								& (!(node.getAttributes().getNamedItem("lon").getNodeValue().equals("")))) {
							LMarker leafletMarker = d.Draw_Node(
									Double.parseDouble(node.getAttributes().getNamedItem("lat").getNodeValue()),
									Double.parseDouble(node.getAttributes().getNamedItem("lon").getNodeValue()));
							String svgCode = "<svg width='64' height='64' viewBox='0 0 64 64' xmlns='http://www.w3.org/2000/svg' version='1.1' >\r\n"
									+ "<circle cx=\"32\" cy=\"32\" r=\"31\" fill=\"rgb( 0, 172, 237)\" stroke=\"white\" stroke-width=\"2\"/>\r\n"
									+ "<text text-anchor=\"middle\" x=\"50%\" y=\"50%\" dy=\".35em\" font-family=\"sans-serif\" font-size=\"32px\" fill=\"white\">"
									+ number + "</text>\r\n" + "</svg>";
							leafletMarker.setDivIcon(svgCode);
							lanswer.addComponent(leafletMarker);
							int j = ok;
							int s = tk;
							leafletMarker.addClickListener(new LeafletClickListener() {
								@Override
								public void onClick(LeafletClickEvent event) {
									in.setInfo(twp.get(q.layer.getValue()).get(j));
									in.setInfoTwitter(twinfop.get(q.layer.getValue()).get(s));
									accordion2.setSplitPosition(65);
									accordion.setSplitPosition(0);
								}
							});
						}
					}
				}
			}
		}
	}

	public void Draw_xml_ways_twitter_marks(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument,
			String Icon) {
		if (xmlDocument == null) {
		} else {
			NodeList nodes_list = null;
			try {
				nodes_list = (NodeList) xPath.compile("/social/twitter[oneway/way]").evaluate(xmlDocument,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (nodes_list == null) {
			} else {
				NodeList oneway = null;
				NodeList tweets = null;
				Node node = null;
				Node way = null;
				int owc = -1;
				for (int i = 0; i < nodes_list.getLength(); i++) {
					if (nodes_list.item(i).hasChildNodes()) {
						NamedNodeMap atts = nodes_list.item(i).getAttributes();
						String number = atts.getNamedItem("number").getNodeValue();
						NodeList sobjects = nodes_list.item(i).getChildNodes();
						
						for (int ch = 0; ch < sobjects.getLength(); ch++) {
							if (sobjects.item(ch).getNodeName() == "oneway") {
								owc++;
								oneway = sobjects.item(ch).getChildNodes();
								Boolean first_node = false;
								Boolean first_way = false;
								for (int n = 0; n < oneway.getLength(); n++) {
									
									if (oneway.item(n).getNodeName() == "way" && !first_way) {	
										first_way = true;
										 
										way = oneway.item(n);
										if (tww.containsKey(q.layer.getValue())) {
											List<Node> current = tww.get(q.layer.getValue());
											current.add(way);
											tww.put(q.layer.getValue(), current);
										} else {
											List<Node> current = new ArrayList<Node>();
											current.add(way);
											tww.put(q.layer.getValue(), current);
										}
									}
									if (oneway.item(n).getNodeName() == "node" && !first_node ) {
										first_node = true;
										node = oneway.item(n);									
									}
								}
							}
							
							
							if (sobjects.item(ch).getNodeName() == "tweets") {
								 
								tweets = sobjects.item(ch).getChildNodes();
								if (twinfow.containsKey(q.layer.getValue())) {
									List<NodeList> current = twinfow.get(q.layer.getValue());
									current.add(tweets);
									twinfow.put(q.layer.getValue(), current);
								} else {
									List<NodeList> current = new ArrayList<NodeList>();
									current.add(tweets);
									twinfow.put(q.layer.getValue(), current);
								}
							}
						}
						if ((!(node.getAttributes().getNamedItem("lat").getNodeValue().equals("")))
								& (!(node.getAttributes().getNamedItem("lon").getNodeValue().equals("")))) {
							LMarker leafletMarker = d.Draw_Node(
									Double.parseDouble(node.getAttributes().getNamedItem("lat").getNodeValue()),
									Double.parseDouble(node.getAttributes().getNamedItem("lon").getNodeValue()));
							String svgCode = "<svg width='64' height='64' viewBox='0 0 64 64' xmlns='http://www.w3.org/2000/svg' version='1.1' >\r\n"
									+ "<circle cx=\"32\" cy=\"32\" r=\"31\" fill=\"rgb( 0, 172, 237)\" stroke=\"white\" stroke-width=\"2\"/>\r\n"
									+ "<text text-anchor=\"middle\" x=\"50%\" y=\"50%\" dy=\".35em\" font-family=\"sans-serif\" font-size=\"32px\" fill=\"white\">"
									+ number + "</text>\r\n" + "</svg>";
							leafletMarker.setDivIcon(svgCode);
							lanswer.addComponent(leafletMarker);
							 
							 
							int r = owc;
							leafletMarker.addClickListener(new LeafletClickListener() {
								@Override
								public void onClick(LeafletClickEvent event) {								 
									in.setInfo(tww.get(q.layer.getValue()).get(r));
									in.setInfoTwitter(twinfow.get(q.layer.getValue()).get(r));
									accordion2.setSplitPosition(65);
									accordion.setSplitPosition(0);
								}
							});
							
						}
					}
				}
			}
		}
	}

	public void Draw_xml_ways_twitter(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument,
			String nccolor, String ccolor, String cfill) {
		if (xmlDocument == null) {
		} else {
			try {
				way.put(q.layer.getValue(), (NodeList) xPath.compile("/social/twitter/oneway/way").evaluate(xmlDocument,
						XPathConstants.NODESET));
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			if (way == null) {
			} else
			{
				for (int i = 0; i < way.get(q.layer.getValue()).getLength(); i++) {
					NodeList children = way.get(q.layer.getValue()).item(i).getChildNodes();
					List<Point> p = new ArrayList<Point>();
					NodeList lat2 = null;
					NodeList lon2 = null;
					/*
					 * String lat2 = ""; String lon2 = "";
					 */
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName() == "nd") {
							try {
								lat2 = (NodeList) xPath.compile("/social/twitter/oneway/node[@id="
										+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue()
										+ "]/@lat").evaluate(xmlDocument, XPathConstants.NODESET);

							} catch (XPathExpressionException e) {
								e.printStackTrace();
							}
							/*
							 * lat2 =
							 * lat_position_node(children,children.item(j).getAttributes().getNamedItem(
							 * "ref") .getNodeValue());
							 */
							try {
								lon2 = (NodeList) xPath.compile("/social/twitter/oneway/node[@id="
										+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue()
										+ "]/@lon").evaluate(xmlDocument, XPathConstants.NODESET);
							} catch (XPathExpressionException e) {
								e.printStackTrace();
							}
							/*
							 * lon2 =
							 * lon_position_node(children,children.item(j).getAttributes().getNamedItem(
							 * "ref") .getNodeValue());
							 */
							try {
								p.add(new Point(Double.parseDouble(lat2.item(0).getNodeValue()),
										Double.parseDouble(lon2.item(0).getNodeValue())));
							} catch (Exception e) {
								e.printStackTrace();
							}
							/*
							 * p.add(new Point(Double.parseDouble(lat2), Double.parseDouble(lon2)));
							 */
						}
					}
					Point[] points = p.toArray(new Point[p.size()]);
					if (points[0].getLat().equals(points[points.length - 1].getLat())
							& points[0].getLon().equals(points[points.length - 1].getLon())) {
						LPolygon pl = d.Draw_Polygon(ccolor, cfill, points);
						lanswer.addComponent(pl); 
						
					} else {
						LPolyline pl = d.Draw_Polyline(nccolor, points);
						lanswer.addComponent(pl); 
					 
					}
				}
			}
		}
	}

	public String api(Double minLon, Double minLat, Double maxLon, Double maxLat, String query) {
		String xml = "";

		String call_query = 
				"import module namespace xosm_item = \"xosm_item\" at \"XOSMItem.xqy\";\r\n" + 
				"import module namespace xosm_sp = \"xosm_sp\" at \"XOSMSpatial.xqy\";\r\n" + 
				"import module namespace xosm_kw = \"xosm_kw\" at \"XOSMKeyword.xqy\";\r\n" + 
				"import module namespace xosm_ag = \"xosm_ag\" at \"XOSMAggregation.xqy\";\r\n" + 
				"import module namespace xosm_open = \"xosm_open\" at \"XOSMOpenData.xqy\";\r\n" + 
				"import module namespace xosm_pbd = \"xosm_pbd\" at \"XOSMPostGIS.xqy\";\r\n\n" + query;
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("query", call_query));
			HttpGet request = new HttpGet("http://xosm.ual.es/xosmapiV2/XOSM/minLon/" + minLon + "/minLat/" + minLat
					+ "/maxLon/" + maxLon + "/maxLat/" + maxLat + "?" + URLEncodedUtils.format(params, "utf-8"));
			request.addHeader("content-type", "application/xml");
			HttpResponse result = httpClient.execute(request);
			xml = EntityUtils.toString(result.getEntity(), "UTF-8");
		} catch (IOException ex) {
		}
		return xml;
	}

	public void api_post(String xml, String layer) {
		try {
			String XMLData = xml;
			URL url = new URL("http://xosm.ual.es/xosmapiV2/postLayerByName/name/" + layer);
			String myParam = "osm=" + URLEncoder.encode(XMLData, "UTF-8");
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("Content-Length", Integer.toString(myParam.length()));
			httpConn.setDoOutput(true);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((httpConn.getOutputStream())));
			writer.write(myParam, 0, myParam.length());
			writer.flush();
			writer.close();
			System.out.println(httpConn.getResponseCode());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@WebServlet(urlPatterns = "/*", name = "XOSM2Servlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = XOSM2.class, productionMode = false)
	public static class XOSM2Servlet extends VaadinServlet {
	}
}

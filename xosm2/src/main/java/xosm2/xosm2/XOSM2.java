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
import com.vaadin.shared.Position;
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

//DELETE
//AYUDA

@Theme("mytheme")
public class XOSM2 extends UI {

	String type ="";
	String type2 = "";
	HorizontalSplitPanel accordion = new HorizontalSplitPanel();
	HorizontalSplitPanel accordion2 = new HorizontalSplitPanel();
	Twitter popup = new Twitter();
	Youtube popup2 = new Youtube();

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
		map.setZoomLevel(18);
        popup.center();
        popup.setHeightUndefined();
        popup.setResizable(false);
        popup2.center();
        popup2.setHeightUndefined();
        popup2.setResizable(false);
		
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
		 
		
		
		 

		// INDEXING

		String exq1 = "xosm_pbd:getLayerByName(.,\"Calle Calzada de Castro\",100)";

		String exq2 = "xosm_pbd:getElementsByKeyword(.,\"shop\")";

		String exq3 = "xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")";

		String exq4 = "let $e := xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")\n"
				+ "let $layer := xosm_pbd:getLayerByElement(.,$e,100)\n" + "return $layer";

		// OSM

		String exq5 = "xosm_pbd:getLayerByBB(.)[@type=\"way\"]";

		String exq6 = "xosm_pbd:getLayerByBB(.)[@type=\"point\"]";

		String exq7 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,\"Carrera de los Limoneros\",100)\r\n"
				+ "return\r\n" + "for $node in $layer[@type=\"point\"]\r\n"
				+ "return xosm_item:point(xosm_item:name($node),\r\n"
				+ "xosm_item:lon($node),xosm_item:lat($node),"
				+ "xosm_item:tags($node))\r\n";

		String exq8 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(. ,\"Carrera de los Limoneros\",100)\r\n"
				+ "return\r\n" + "for $node in $layer[@type=\"way\"]\r\n" + "let $nodes := xosm_item:nodes($node)\r\n"
				+ "for $t in xosm_item:segments($node)\r\n"
				+ "return xosm_item:way(xosm_item:name($node),\r\n"
				+ "xosm_item:segment(xosm_item:id($t),\r\n"
				+ "xosm_item:refs($t),xosm_item:tags($t)),$nodes)";

		// SPATIAL

		String exq9 = "let $layer := xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $e := xosm_pbd:getElementByName(.,'Calle Calzada de Castro')\r\n" + "return\r\n"
				+ "filter($layer,xosm_sp:DWithIn(?,$e,100))\r\n";

		String exq10 = "let $layer := xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $p := xosm_pbd:getElementByName(.,\"ACUYO IRIARTE\")\r\n" + "return\r\n"
				+ "filter($layer,xosm_sp:furtherWestPoints($p,?))\r\n";

		String exq11 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $cc := xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")\r\n" + "return\r\n"
				+ "filter($layer[@type=\"way\"],xosm_sp:crossing($cc,?)) ";

		String exq12 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $cc := xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")\r\n" + "return\r\n"
				+ "filter($layer[@type=\"way\"],xosm_sp:intersecting(?,$cc)) ";

		// KEYWORD

		String exq13 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n"
				+ "filter($layer,xosm_kw:searchTag(?,\"tourism\",\"hotel\"))";

		String exq14 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n"
				+ "filter($layer,xosm_kw:searchKeyword(?,\"amenity\"))";

		String exq15 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n"
				+ "filter($layer,xosm_kw:searchKeywordSet(?,(\"bar\",\"restaurant\")))";

		// AGGREGATION

		String exq16 = "let $layer :=\r\n" + "xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $e := xosm_pbd:getElementByName(.,'Calle Calzada de Castro')\r\n"
				+ "return xosm_ag:topologicalCount($layer,$e,\r\n"
				+ "function($x,$y){xosm_sp:crossing($x,$y)})";

		String exq17 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings := fn:filter($layer,xosm_kw:searchKeyword(?,'building'))\r\n"
				+ "return xosm_ag:metricMax($buildings,\r\n"
				+ "function($x){xosm_item:area($x)})";

		String exq18 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings := fn:filter($layer,xosm_kw:searchKeyword(?,'highway'))\r\n"
				+ "return xosm_ag:metricSum($buildings,\r\n"
				+ "function($x){xosm_item:length($x)})";

		String exq19 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings := fn:filter($layer,xosm_kw:searchKeyword(?,'building'))\r\n"
				+ "return xosm_ag:metricAvg($buildings,\r\n"
				+ "function($x){xosm_item:area($x)})";

		String exq20 = "let $layer :=\r\n" + "xosm_pbd:getLayerByBB(.)\r\n"
				+ "return xosm_ag:metricTopCount($layer,\r\n"
				+ "function($x){xosm_item:area($x)},5)";

		String exq21 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricMedian($layer[@type=\"way\"],\r\n"
				+ "function($x){xosm_item:length($x)})";

		String exq22 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricRange($layer,\r\n"
				+ "function($x){xosm_item:area($x)})";

		String exq23 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricRank($layer,\r\n"
				+ "function($x){xosm_item:area($x)},1)";

		String exq24 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricRange($layer,\r\n"
				+ "function($x){xosm_item:area($x)})";

		// OPEN DATA

		String exq25 = "xosm_open:geojson2osm(\r\n"
				+ "\"https://opendata.paris.fr/explore/dataset/paris_taxis_stations/download/?format=geojson&amp;timezone=UTC\",\"\")";

		String exq26 = "xosm_open:kml2osm(\r\n"
				+ "\"https://opendata.arcgis.com/datasets/7232e84f53494f5e9b131b81f92534b8_0.kml\",\r\n"
				+ "\"SiteName\")";

		//String exq27 = "xosm_open:csv2osm(\"http://geodata.vermont.gov/datasets/b1ae7b7b110447c3b452d9cacffeed36_174.csv\",\r\n"
		//		+ "\"SiteName\",\"X\",\"Y\") ";

		String exq28 = "xosm_open:wikipediaName2osm(\"Almeria\")";

		String q1 = "let $hm := xosm_pbd:getElementByName(.,'Haymarket')\r\n"
				+ "let $bb := xosm_pbd:getLayerByBB(.)[@type=\"way\"]\r\n"
				+ "return fn:filter($bb,xosm_sp:intersecting(?,$hm))";

		String q2 = "let $hotel := xosm_pbd:getElementByName(.,'Hotel Miami')\r\n"
				+ "let $layer := xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n" + "fn:filter(\r\n"
				+ "fn:filter($layer,xosm_kw:searchKeyword(?,'restaurant')),\r\n"
				+ "xosm_sp:furtherNorthPoints($hotel,?)) ";

		String q3 = "for $hotel in xosm_pbd:getElementsByKV(.,'tourism','hotel')\r\n" + 
				"let $layer := xosm_pbd:getLayerByElement(., $hotel ,500)\r\n" + 
				"where count(fn:filter\r\n" + 
				"($layer,xosm_kw:searchKeywordSet(?,('bar','restaurant')))) >= 10\r\n" + 
				"return $hotel";

		String q4 = "let $hotel := xosm_pbd:getElementsByKV(.,'tourism','hotel')\r\n" + 
				"let $f := function($hotel)\r\n" + 
				"{-(count(fn:filter(xosm_pbd:getLayerByElement(.,$hotel,500),\r\n" + 
				"xosm_kw:searchKeyword(?,'church'))))}\r\n" + 
				"return fn:sort($hotel,(),$f)[1]";

		String q5 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Karl-Liebknecht-Straße' ,500)\r\n"
				+ "let $buildings := fn:filter($layer,\r\n" + "xosm_kw:searchKeyword(?,'building'))\r\n"
				+ "return xosm_ag:metricSum($buildings,function($x){xosm_item:area($x)})";

		String q6 = "let $layer := xosm_pbd:getLayerByName(.,'Piazza del Duomo',1500)\r\n" + "return\r\n"
				+ "xosm_ag:metricMax(\r\n"
				+ "filter($layer,xosm_kw:searchKeyword(?,'church')),function($x){xosm_item:area($x)})\r\n";

		String q7 = "let $open :=\r\n" + 
				"'https://opendata.bruxelles.be/explore/dataset/test-geojson-station-de-taxi/download/?format=geojson&amp;timezone=UTC'\r\n" + 
				"let $taxis := xosm_open:geojson2osm($open,'')\r\n" + 
				"let $building := xosm_pbd:getElementByName(. ,'Bruxelles-Central - Brussel-Centraal') \r\n" + 
				"return fn:filter($taxis,xosm_sp:DWithIn($building,?,100))";
		
		String q8 = "let $open :=\r\n"
				+ "'http://data2.esrism.opendata.arcgis.com/datasets/51900577e33a4ba4ab59a691247aeee9_0.geojson'\r\n"
				+ "let $events :=\r\n" + "xosm_open:geojson2osm($open,'') \r\n" + "return fn:filter($events,\r\n"
				+ "function($p) {not(empty($p/node/tag[@k='GRATUITO' and @v='Sí']))})";

		String q9 = "let $x := xosm_pbd:getElementByName(.,'Calle Mayor')\r\n"
				+ "let $y := xosm_pbd:getElementByName(.,'Calle de Esparteros')\r\n" + "return\r\n"
				+ "for $i in xosm_sp:intersectionPoints($x,$y)\r\n" + "return xosm_open:wikipediaElement2osm($i) ";

		 
		String q10 ="<social>{\r\n" + 
				"let $hotels := xosm_pbd:getElementsByKV(., \"tourism\", \"hotel\")\r\n" + 
				"let $city := xosm_social:city($hotels[1])\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $name := data($hotel/@name)\r\n" + 
				"let $q := (if (contains(data($hotel/@name), \"Hotel\"))\r\n"+ 
				"then data($hotel/@name)\r\n" + 
				"else  if (contains(data($hotel/@name), \"Hostal\")) then \r\n" + 
				"data($hotel/@name) else\r\n" +
				"if (contains(data($hotel/@name), \"Apartament\"))\r\n" + 
				"then data($hotel/@name)\r\n" + 
				"else \"Hotel \" || data($hotel/@name))\r\n" + 
				"let $q := $q || \" \" || $city\r\n" + 
				"let $tweets :=\r\n"
				+"xosm_social:api\r\n(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets\", \r\n"
				+"map { 'q' : $q }, map { 'count' : 5 })/json/* \r\n"  
				+"return xosm_social:twitterSearchTweets($hotel, \r\n"
				+"$tweets)\r\n" + 
				"}</social>";
		
		String q11="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $q := replace($q,\" \", \"\")\r\n" + 
				"let $tweets := xosm_social:api\r\n(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets\",\r\n"
				+ "map { 'q' : $q}, map { 'count' : 10,\r\n"
				+ "'option' : 'hashtag' })/json/*\r\n" + 
				"return xosm_social:twitterSearchTweets($museum, $tweets)\r\n" + 
				"else ()\r\n" + 
				"}</social>";
		
		String q12="<social>{\r\n" + 
				"let $hotels := xosm_pbd:getElementsByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels[1])\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $name := data($hotel/@name)\r\n" + 
				"let $q := (if ($hotel//tag[@k=\"operator\"])\r\n"
				+ "then \"Operator \" || data($hotel//tag[@k=\"operator\"]/@v)\r\n" + 
				"else\r\n" + 
				"if (contains(data($hotel/@name), \"Hotel\"))\r\n"
				+ "then data($hotel/@name)\r\n" + 
				"else\r\n"
				+ "if (contains(data($hotel/@name), \"Hostal\")) then \r\n" + 
				"data($hotel/@name) else\r\n"
				+ "if (contains(data($hotel/@name), \"Apartament\"))\r\n" + 
				"then data($hotel/@name)\r\n" + 
				"else \"Hotel \" || data($hotel/@name))\r\n" + 
				"let $users := xosm_social:api\r\n(\"http://minerva.ual.es:8080/api.social/twitterSearchUser\",\r\n"
				+ "map { 'q' : $q,  'city' : $city },\r\n"
				+ "map { 'count' : 10 })/json/*\r\n" + 
				"return xosm_social:twitterSearchUser($hotel,$users)\r\n" + 
				"}</social>";
		
		String q13="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := data($museum/@name)\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $lon := data($museum/node[1]/@lon)\r\n" + 
				"let $lat := data($museum/node[1]/@lat)\r\n" + 
				"let $geocode := $lat ||  \",\" || $lon || \",\" || \"5km\" \r\n" + 
				"let $tweets :=\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets\", \r\n" + 
				"map { 'q' : $q }, map { 'count' : 10, 'geocode' : $geocode})/json/*\r\n" + 
				"return xosm_social:twitterSearchTweets($museum, $tweets)\r\n"+
				"else ()\r\n" + 
				"}</social>";
		
		String q14="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := data($museum/@name)\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $tweets :=\r\n(for $tweet in\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets\", \r\n" + 
				"map { 'q' : $q }, map { 'count' : 10 })/json/*\r\n" + 
				"where $tweet/favorite__count > 2 return $tweet) \r\n" + 
				"return xosm_social:twitterSearchTweets($museum, $tweets)\r\n"+
				"else ()\r\n" + 
				"}</social>";
		
		String q15="<social>{\r\n" + 
				"let $restaurants := xosm_pbd:getElementsByKV(., \"amenity\", \"restaurant\") \r\n" + 
				"let $city := xosm_social:city($restaurants[1])\r\n" + 
				"for $restaurant in $restaurants\r\n" + 
				"let $q := data($restaurant/@name) || \" \" || $city\r\n" + 
				"let $tweets :=\r\n(for $tweet in\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets\", \r\n" + 
				"map { 'q' : $q }, map { 'count' : 15})/json/* \r\n" + 
				"where $tweet/user/friends__count > 100 return $tweet)\r\n" + 
				"return xosm_social:twitterSearchTweets($restaurant, $tweets)\r\n"+
				"}</social>";
		
		String q16="<social>{\r\n" + 
				"let $hotels := xosm_pbd:getElementsByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels[1])\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $name := data($hotel/@name)\r\n" + 
				"let $q :=\r\n(if ($hotel//tag[@k=\"operator\"])\r\n"
				+ "then \"Operator \" || data($hotel//tag[@k=\"operator\"]/@v)\r\n" + 
				"else  \r\n" + 
				"if (contains(data($hotel/@name), \"Hotel\"))\r\n"
				+ "then data($hotel/@name)\r\n" + 
				"else\r\n"
				+ "if (contains(data($hotel/@name), \"Hostal\")) then \r\n" + 
				"data($hotel/@name) else\r\n"
				+ "if (contains(data($hotel/@name), \"Apartament\"))\r\n" + 
				"then data($hotel/@name)\r\n" + 
				"else \"Hotel \" || data($hotel/@name))\r\n" + 
				"let $users :=\r\n"
				+ "(for $user in\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchUser\", \r\n" + 
				"map { 'q' : $q,  'city' : $city }, map { 'count' : 1 })/json\r\n" + 
				"where $user/followers__count > 2000 return $user)\r\n" + 
				"return xosm_social:twitterSearchUser($hotel, $users)"+
				"}</social>";
		
		String q17="<social>{\r\n" + 
				"let $hotels := xosm_pbd:getElementsByKV(., \"tourism\", \"hotel\")\r\n" + 
				"let $city := xosm_social:city($hotels[1]) \r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $q := (if ($hotel//tag[@k=\"operator\"])\r\n"
				+ "then \"Operator \" || data($hotel//tag[@k=\"operator\"]/@v)\r\n" + 
				"else  \r\n" + 
				"if (contains(data($hotel/@name), \"Hotel\"))\r\n"
				+ "then data($hotel/@name)\r\n" + 
				"else  if (contains(data($hotel/@name), \"Hostal\")) then \r\n" + 
				"data($hotel/@name) else\r\n"
				+ "if (contains(data($hotel/@name), \"Apartament\"))\r\n" + 
				"then data($hotel/@name)\r\n" + 
				"else \"Hotel \" || data($hotel/@name))\r\n" + 
				"let $screen_name :=\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchUser\", \r\n" + 
				"map { 'q' : $q,  'city' : $city }, map { 'count' : 1 })/json/screen__name)\r\n" + 
				"let $tweets :=\r\n"
				+ "(for $tweet in\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterUserTimeLine\",\r\n" + 
				"map { 'screen_name' : $screen_name }, map { 'count' : 10 })/json/* \r\n" + 
				"where $tweet/favorite__count > 5 return $tweet)\r\n" + 
				"return xosm_social:twitterUserTimeLine($hotel,  $tweets)\r\n"+
				"}</social>";
		
		String q18="<social>{\r\n" + 
				"let $museums := xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $city := xosm_social:city($museums[1]) \r\n" + 
				"for $museum in $museums\r\n" + 
				"let $mention := data($museum/@name)\r\n" + 
				"return if (string-length($mention) > 0) then\r\n" + 
				"let $screen__name :=\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchUser\",\r\n"
				+ "map { 'q' : $mention,  'city' : $city },\r\n"
				+ "map { 'count' : 1 })/json/screen__name)\r\n" + 
				"let $tweets := xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets\",\r\n" + 
				"map { 'q' : $screen__name}, map { 'count' : 10, 'option' : 'mention' })/json/* \r\n" + 
				"return xosm_social:twitterSearchTweets($museum, $tweets)\r\n"+
				"else ()\r\n" + 
				"}</social>";
		
		String q19="<social>{\r\n" + 
				"let $hotels := xosm_pbd:getElementsByKV(., \"tourism\", \"hotel\")\r\n" + 
				"let $city := xosm_social:city($hotels[1])\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $name := data($hotel/@name)\r\n" + 
				"let $q :=\r\n"
				+ "(if ($hotel//tag[@k=\"operator\"])\r\n"
				+ "then \"Operator \" || data($hotel//tag[@k=\"operator\"]/@v)\r\n" + 
				"else  \r\n" + 
				"if (contains(data($hotel/@name), \"Hotel\"))\r\n"
				+ "then data($hotel/@name)\r\n" + 
				"else\r\n"
				+ "if (contains(data($hotel/@name), \"Hostal\")) then \r\n" + 
				"data($hotel/@name) else\r\n"
				+ "if (contains(data($hotel/@name), \"Apartament\"))\r\n" + 
				"then data($hotel/@name)\r\n" + 
				"else \"Hotel \" || data($hotel/@name))\r\n" + 
				"let $tweets :=  (\r\n" + 
				"for $q in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchUser\", \r\n" + 
				"map { 'q' :$q,  'city' : $city }, map { 'count' : 10 })/json/_/screen__name)\r\n" + 
				"return\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/twitterSearchTweets\",\r\n" + 
				"map { 'q' : $q}, map { 'count' : 10, 'option' : 'mention' })/json/* \r\n" + 
				")\r\n" + 
				"return xosm_social:twitterSearchTweets($hotel,$tweets)\r\n"+
				"}</social>";
		
		String q20="<social>{\r\n" + 
				"let $hotels := xosm_pbd:getElementsByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels[1])\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $name := data($hotel/@name)\r\n" + 
				"let $q := (if (contains(data($hotel/@name), \"Hotel\")) then data($hotel/@name)\r\n" + 
				"else  if (contains(data($hotel/@name), \"Hostal\")) then \r\n" + 
				"data($hotel/@name) else\r\n"
				+ "if (contains(data($hotel/@name), \"Apartament\"))\r\n" + 
				"then data($hotel/@name)\r\n" + 
				"else \"Hotel \" || data($hotel/@name))\r\n" + 
				"let $q := $q || \" \" || $city  \r\n" + 
				"let $videos:=\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeVideoSearch\",\r\n"
				+ "map { 'q' : $q }, map { 'maxResults' : 5 })/json/_\r\n" + 
				"return xosm_social:youtubeVideoSearch($hotel, $videos)\r\n"+ 
				"} </social>";
		
		String q21="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $channels :=\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeChannelSearch\",\r\n"
				+ "map { 'q' : $q}, map { 'maxResults' : 3 })/json/_\r\n" + 
				"return xosm_social:youtubeChannelSearch($museum, $channels)\r\n"+
				"else () \r\n" + 
				"} </social>";
		
		
		String q22="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $playlists := \r\n" + 
				"xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistSearch\",\r\n"
				+ "map { 'q' : $q}, map { 'maxResults' : 3 })/json/_\r\n" + 
				"return xosm_social:youtubePlaylistSearch($museum, $playlists)\r\n"+
				"else ()  \r\n" + 
				"} </social>";
		
		String q23="<social>{\r\n" + 
				"let $hotels := xosm_pbd:getElementsByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels[1])\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $name := data($hotel/@name)\r\n" + 
				"let $q :=\r\n"
				+ "(if (contains(data($hotel/@name), \"Hotel\"))\r\n"
				+ "then data($hotel/@name)\r\n" + 
				"else  if (contains(data($hotel/@name), \"Hostal\")) then \r\n" + 
				"data($hotel/@name) else\r\n"
				+ "if (contains(data($hotel/@name), \"Apartament\"))\r\n" + 
				"then data($hotel/@name)\r\n" + 
				"else \"Hotel \" || data($hotel/@name))\r\n" + 
				"let $q := $q || \" \" || $city\r\n" + 
				"let $videos :=\r\n"
				+ "(for $id in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeVideoSearch\", \r\n" + 
				"map { 'q' : $q }, map { })/json/_/id/videoId)\r\n" + 
				"return xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeVideoInfo\", \r\n" + 
				"map { 'id' : $id }, map { })/json/items/_[statistics/viewCount > 10])\r\n" + 
				"return xosm_social:youtubeVideoInfo($hotel, $videos)\r\n"+
				"} </social>";
		
		String q24="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $channels :=\r\n"
				+ "(for $id in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeChannelSearch\",\r\n"
				+ "map { 'q' : $q}, map { 'maxResults' : 3})/json/_/id/channelId)\r\n" + 
				"return xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeChannelInfo\", \r\n" + 
				"map {'id' : $id }, map {})/json/items/_[statistics/subscriberCount > 100])\r\n" + 
				"return xosm_social:youtubeChannelInfo($museum, $channels)\r\n"+
				"else ()  \r\n" + 
				"} </social>";
		
		String q25="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $playlists :=\r\n"
				+ "(for $id in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistSearch\",\r\n"
				+ "map { 'q' : $q}, map { })/json/_/id/playlistId)\r\n" + 
				"return\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistInfo\", \r\n" + 
				"map {'id' : $id }, map {})/json/items/_)\r\n" + 
				"return xosm_social:youtubePlaylistInfo($museum, $playlists)\r\n"+
				"else ()  \r\n" + 
				"} </social>";
		
		String q26="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $playlists :=\r\n"
				+ "(for $id in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistSearch\",\r\n"
				+ "map { 'q' : $q}, map { })/json/_/id/playlistId)\r\n" + 
				"return xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistItems\",\r\n"
				+ "map {'playlistId' : $id }, map {})/json/items/_)\r\n" + 
				"return xosm_social:youtubePlaylistItems($museum, $playlists)\r\n"+
				"else ()  \r\n" + 
				"} </social>";
		
		String q27="<social>{\r\n" + 
				"let $hotels := xosm_pbd:getElementsByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels[1])\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $name := data($hotel/@name)\r\n" + 
				"let $q :=\r\n"
				+ "(if (contains(data($hotel/@name), \"Hotel\"))\r\n"
				+ "then data($hotel/@name)\r\n" + 
				"else  if (contains(data($hotel/@name), \"Hostal\")) then \r\n" + 
				"data($hotel/@name) else\r\n"
				+ "if (contains(data($hotel/@name), \"Apartament\"))\r\n" + 
				"then data($hotel/@name)\r\n" + 
				"else \"Hotel \" || data($hotel/@name))\r\n" + 
				"let $q := $q || \" \" || $city\r\n" + 
				"let $videos := (\r\n" + 
				"for $playId in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistSearch\",\r\n"
				+ "map { 'q' : $q}, map {  })/json/_/id/playlistId)\r\n" + 
				"for $videoId in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistItems\", \r\n" + 
				"map { 'playlistId' : $playId}, map { })/json/items/_/snippet/resourceId/videoId)              \r\n" + 
				"return\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeVideoInfo\",\r\n"
				+ "map { 'id' : $videoId}, \r\n" + 
				"map { })/json/items/_\r\n" + 
				")\r\n" + 
				"return xosm_social:youtubeVideoInfo($hotel, $videos)\r\n"+
				"} </social>";
		
		String q28="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\") \r\n" + 
				"let $q := $museum/@name\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $channelId :=\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeChannelSearch\",\r\n"
				+ "map { 'q' : $q}, \r\n" + 
				"map { 'maxResults' : 1})/json/_/id/channelId)\r\n" + 
				"let $videos := (\r\n" + 
				"let $playId :=\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeChannelInfo\", \r\n" + 
				"map {'id' : $channelId }, map {})/json/items/_/contentDetails/relatedPlaylists/uploads)\r\n" + 
				"for $videoId in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistItems\", \r\n" + 
				"map { 'playlistId' : $playId}, map { })/json/items/_/snippet/resourceId/videoId)\r\n" + 
				"return\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeVideoInfo\",\r\n" + 
				"map { 'id' : $videoId}, map { })/json/items/_\r\n" + 
				")\r\n" + 
				"return xosm_social:youtubeVideoInfo($museum, $videos)\r\n"+
				"else ()\r\n" + 
				"} </social>";
		
		String q29="<social>{\r\n" + 
				"for $museum in xosm_pbd:getElementsByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"return if (string-length($q) > 0) then\r\n" + 
				"let $videos :=\r\n"
				+ "(for $channelId in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeChannelSearch\", \r\n" + 
				"map { 'q' : $q}, map { 'maxResults' : 3})/json/_/id/channelId) \r\n" + 
				"let $json :=\r\n"
				+ "xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeChannelInfo\", \r\n" + 
				"map { 'id' : $channelId }, map { })\r\n" + 
				"where $json/json/items/_/statistics/subscriberCount > 2000\r\n" + 
				"for $playId in\r\n"
				+ "data($json/json/items/_/contentDetails/relatedPlaylists/uploads) \r\n" + 
				"for $videoId in\r\n"
				+ "data(xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubePlaylistItems\", \r\n" + 
				"map { 'playlistId' : $playId} , map { })/json/items/_/snippet/resourceId/videoId)\r\n" + 
				"return xosm_social:api(\"http://minerva.ual.es:8080/api.social/youtubeVideoInfo\",\r\n"
				+ "map { 'id' : $videoId }, \r\n" + 
				"map { })/json/items/_)\r\n" + 
				"return \r\n" + 
				"xosm_social:youtubeVideoInfo($museum,$videos)\r\n"+
				"else ()\r\n" + 
				"}\r\n" + 
				"</social> ";
		
		MenuBar.Command cind1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq1);
				 
				
			}

		};

		MenuBar.Command cind2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq2);
				
			}

		};

		MenuBar.Command cind3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq3);
				 
			}

		};

		MenuBar.Command cind4 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq4);
				 
			}

		};

		MenuBar.Command cosm1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq5);
				 
			}

		};

		MenuBar.Command cosm2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq6);
				 
			}

		};

		MenuBar.Command cosm3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq7);
				 
			}

		};

		MenuBar.Command cosm4 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq8);
				 
			}

		};

		MenuBar.Command cspa1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq9);
				 
			}

		};
		MenuBar.Command cspa2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq10);
				 
			}

		};

		MenuBar.Command cspa3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq11);
				 
			}

		};

		MenuBar.Command cspa4 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq12);
				 
			}

		};

		MenuBar.Command ckey1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq13);
				 
			}

		};

		MenuBar.Command ckey2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq14);
				 
			}

		};

		MenuBar.Command ckey3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq15);
				 
			}

		};

		MenuBar.Command cagg1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq16);
				 
			}

		};

		MenuBar.Command cagg2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq17);
				 
			}

		};

		MenuBar.Command cagg3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq18);
				 
			}

		};

		MenuBar.Command cagg4 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq19);
				 
			}

		};

		MenuBar.Command cagg5 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq20);
				 
			}

		};

		MenuBar.Command cagg6 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq21);
				 
			}

		};

		MenuBar.Command cagg7 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq22);
				 
			}

		};

		MenuBar.Command cagg8 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq23);
				 
			}

		};

		MenuBar.Command cagg9 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq24);
				 
			}

		};

		MenuBar.Command cop1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq25);
				map.setZoomLevel(10);
			}

		};

		MenuBar.Command cop2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq26);
				map.setZoomLevel(10);
				
			}

		};

		/*MenuBar.Command cop3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq27);
				map.setZoomLevel(10);
				 
			}

		};*/

		MenuBar.Command cop4 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(41.90219, 12.49580);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(exq28);
				map.setZoomLevel(15);
				 
			}

		};

		MenuBar.Command copen1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(51.50884, -0.13201);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q1);
				 
			}

		};

		MenuBar.Command copen2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(41.90219, 12.49580);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q2);
				  
			}

		};

		MenuBar.Command copen3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(48.20817, 16.37382);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q3);
				  
			}

		};

		MenuBar.Command copen4 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(48.13513, 11.58198);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q4);
				  
			}

		};

		MenuBar.Command copen5 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q5);
				 
			}

		};

		MenuBar.Command copen6 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(45.465820637638, 9.1893592282028);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q6);
				 
			}

		};
		
		//ADDED

		MenuBar.Command copen7 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(50.8445, 4.3537);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q7);
				 
			}

		};
		
		

		MenuBar.Command copen8 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(40.42, -3.68);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q8);
				map.setZoomLevel(10);
			}

		};

		MenuBar.Command copen9 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);
				map.setCenter(40.4164, -3.70501);
				map.setZoomLevel(18);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q9);
				 
				 
			}

		};
		
		MenuBar.Command ctwitter1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q10);
				
			}

		};
		
		MenuBar.Command ctwitter2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q11);
				 
			}

		};
		
		MenuBar.Command ctwitter3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q12);
				 
			}

		};
		
		MenuBar.Command ctwitter4 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q13);
				 
			}

		};
		
		MenuBar.Command ctwitter5 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q14);
				 
			}

		};
		
		MenuBar.Command ctwitter6 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q15);
				 
			}

		};
		
		MenuBar.Command ctwitter7 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q16);
				 
			}

		};
		
		MenuBar.Command ctwitter8 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q17);
				 
			}

		};
		
		MenuBar.Command ctwitter9 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q18);
				 
			}

		};
		
		MenuBar.Command ctwitter10 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q19);
				 
			}

		};
		
		MenuBar.Command cyoutube1 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q20);
				 
			}

		};
		
		MenuBar.Command cyoutube2 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q21);
				 
			}

		};
		MenuBar.Command cyoutube3 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q22);
				 
			}

		};
		MenuBar.Command cyoutube4 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q23);
				 
			}

		};
		MenuBar.Command cyoutube5 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q24);
				 
			}

		};
		
		MenuBar.Command cyoutube6 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q25);
				 
			}

		};
		
		MenuBar.Command cyoutube7 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q26);
				 
			}

		};
		
		MenuBar.Command cyoutube8 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q27);
				 
			}

		};
		
		MenuBar.Command cyoutube9 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q28);
				 
			}

		};
		
		MenuBar.Command cyoutube10 = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				map.setCenter(36.838030858833, -2.4522979583778);		
				map.setCenter((40.4126+40.4168)/2,(-3.6961+-3.6887)/2);
				map.setZoomLevel(19);
				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();
				q.setQuery(q29);
				 
			}

		};
		
		MenuBar barmenu = new MenuBar();
		barmenu.setWidth("100%"); 
		barmenu.addItem("Menu");
		
		MenuItem examples = barmenu.addItem("Examples",null,null);
		MenuItem indexing = examples.addItem("Indexing Examples", null, null);
		MenuItem osm = examples.addItem("Layers Examples", null, null);
		MenuItem spatial = examples.addItem("Spatial Examples", null, null);
		MenuItem keyword = examples.addItem("Keyword Examples", null, null);
		MenuItem aggregation = examples.addItem("Aggregation Examples", null, null);
		MenuItem open = examples.addItem("Open Data Examples", null, null);
		MenuItem social = examples.addItem("Social Network Examples", null, null);
		MenuItem twitter = social.addItem("Twitter Examples",null,null);
		MenuItem youtube = social.addItem("Youtube Examples",null,null);
		
		
		 
		//MenuItem open1 = open.addItem("Retrieve the streets in the bounding box intersecting Haymarket street", null, copen1);		
		//MenuItem open2 = open.addItem("Retrieve the restaurants in Roma further north to Miami hotel", null, copen2);
		//MenuItem open3 = open.addItem("Retrieve hotels of Vienna close (500 m) to food venues (food venues = number of bars and restaurants bigger than 10)", null, copen3);
		//MenuItem open4 = open.addItem("Retrieve the hotels of Munich with the greatest number of churches nearby", null, copen4);
		//MenuItem open5 = open.addItem("Retrieve the size of buidings close (500 m) to Karl-Liebknecht-Straße in Berlin", null, copen5);
		//MenuItem open6 = open.addItem("Retrieve the biggest churchs close (1500 m) to Piazza del Duomo in Milan", null, copen6);
		
		MenuItem open7 = open.addItem("Request taxi stops close (100 m) to Bruxelles Central Station in Bruxelles", null, copen7);
		MenuItem open8 = open.addItem("Retrieves free events of Madrid", null, copen8);
		MenuItem open9 = open.addItem("Retrieve Wikipedia information about places nearby to the intersection point of Calle Mayor and Calle de Esparteros in Madrid", null, copen9);
		
		MenuItem twitter1 = twitter.addItem("Get tweets (max 5) about hotels around Fuente de Neptuno, Madrid", null, ctwitter1);
		MenuItem twitter2 = twitter.addItem("Get tweets (max 10) with #museum in Madrid cultural mile", null, ctwitter2);
		MenuItem twitter3 = twitter.addItem("Get relevant twitter accounts (max 10) for hotels around Fuente de Neptuno, Madrid", null, ctwitter3);
		MenuItem twitter4 = twitter.addItem("Get tweets (max 10) about museums in Madrid cultural mile geolocated within a radius of 5 km", null, ctwitter4);
		MenuItem twitter5 = twitter.addItem("Get tweets (max 10) about museums in Madrid cultural mile with more than 3 favorites", null, ctwitter5);
		MenuItem twitter6 = twitter.addItem("Get tweets (max 15) about restaurants around Plaza de Castilla, Madrid where the tweet user has more than 100 friends", null, ctwitter6);
		MenuItem twitter7 = twitter.addItem("Get the most relevant twitter account for hotels around Fuente de Neptuno, Madrid with more than 2000 followers", null, ctwitter7);
		MenuItem twitter8 = twitter.addItem("Get tweets (max 10) from the most relevant twitter account of the hotels around Fuente de Neptuno, Madrid", null, ctwitter8);
		MenuItem twitter9 = twitter.addItem("Get twwets (max 10) which include mentions to the most relevant twitter account for the museums in Madrid cultural mile", null, ctwitter9);
		MenuItem twitter10 = twitter.addItem("Get twwets (max 10) which include mentions to the most relevant twitter accounts (max 10) for the hotels around Fuente Neptuno, Madrid", null, ctwitter10);
		MenuItem youtube1 = youtube.addItem("Get yotube videos (max 5) about hotels around Fuente de Neptuno, Madrid", null, cyoutube1);
		MenuItem youtube2 = youtube.addItem("Get the three most relevant youtube channels for the museums in Madrid cultural mile", null, cyoutube2);
		MenuItem youtube3 = youtube.addItem("Get the three most relevant youtube playlists for the museums in Madrid cultural mile", null, cyoutube3);
		MenuItem youtube4 = youtube.addItem("Get the youtube video information (max 5, default value) for the hotels around Fuente de Neptuno, Madrid with more than 10 visualizations", null, cyoutube4);
		MenuItem youtube5 = youtube.addItem("Get the information for the three most relevant youtube channels for the museums in Madrid cultural mile with more than 100 subscribers", null, cyoutube5);
		MenuItem youtube6 = youtube.addItem("Get the information for the most relevant (max 5, default value) youtube playslists for the museums in Madrid cultural mile with more than 100 subscribers", null, cyoutube6);
		MenuItem youtube7 = youtube.addItem("Get the youtube video list including in the most relevant youtube playlists (max 5) for the museums in Madrid cultural mile", null, cyoutube7);
		MenuItem youtube8 = youtube.addItem("Get the youtube video information for the videos including in the most relevant (max 5) youtube playlists for the hotels around Fuente de Neptuno, Madrid", null, cyoutube8);
		MenuItem youtube9 = youtube.addItem("Get the youtube video information for the videos uploading in the most relevant youtube channel for the museums in Madrir cultural mile ", null, cyoutube9);
		MenuItem youtube10 = youtube.addItem("Get the youtube video information for the videos uploading in the three most relevant youtube channels with more than 2000 subscribers for the museums in Madrir cultural mile", null, cyoutube10);


		 
		MenuItem ind1 = indexing.addItem("Retrieve the elements of the bounding box close (100 m) to an street", null, cind1);
		MenuItem ind2 = indexing.addItem("Retrieve the shops of the bounding box", null, cind2);
		MenuItem ind3 = indexing.addItem("Retrieve an element of the bounding box", null, cind3);
		MenuItem ind4 = indexing.addItem("Retrieve the elements of the bounding box close (100 m) to an street", null, cind4);
		//MenuItem osm1 = osm.addItem("Retrieve the ways of the bounding box", null, cosm1);
		MenuItem osm2 = osm.addItem("Retrieve the points of the bounding box", null, cosm2);
		MenuItem osm3 = osm.addItem("Rebuild the points of the bounding box close (100 m) to an street", null, cosm3);
		MenuItem osm4 = osm.addItem("Rebuild the ways of the bounding box close (100 m) to an street", null, cosm4);
		MenuItem spa1 = spatial.addItem("Retrieve the points of the bounding box within (100 m) to an street", null, cspa1);
		MenuItem spa2 = spatial.addItem("Retrieve the points of the bounding box further west to a point", null, cspa2);
		MenuItem spa3 = spatial.addItem("Retrieve the streets crossing an street", null, cspa3);
		MenuItem spa4 = spatial.addItem("Retrieve the streets intersecting with an street", null, cspa4);
		MenuItem key1 = keyword.addItem("Retrieve the hotels of the bounding box", null, ckey1);
		MenuItem key2 = keyword.addItem("Retrieve the amenities of the bounding box", null, ckey2);
		MenuItem key3 = keyword.addItem("Retrieve the bars and restaurants of the bounding box", null, ckey3);
		MenuItem agg1 = aggregation.addItem("Retrieve the number of streets crossing an street", null, cagg1);
		MenuItem agg2 = aggregation.addItem("Retrieve the buildings with maximum area close (500m) to an street", null, cagg2);
		MenuItem agg3 = aggregation.addItem("Retrieve the sum of the length of highways close (500m) to an street", null, cagg3);
		MenuItem agg4 = aggregation.addItem("Retrieve the average area of buildings close (500m) to an street", null, cagg4);
		MenuItem agg5 = aggregation.addItem("Retrieve the 5 biggest elements", null, cagg5);
		MenuItem agg6 = aggregation.addItem("Retrieve the median of the area of elements close (500m) to an street", null, cagg6);
		MenuItem agg7 = aggregation.addItem("Retrieve the range of the area of elements close (500m) to an street", null, cagg7);
		MenuItem agg8 = aggregation.addItem("Retrieve the biggest element close (500m) to an street", null, cagg8);
		//MenuItem agg9 = aggregation.addItem("Retrieve the range of area of elements close (500m) to an street", null, cagg9);
		MenuItem op1 = open.addItem("Import geojson data", null, cop1);
		MenuItem op2 = open.addItem("Import kml data", null, cop2);
		//MenuItem op3 = open.addItem("Import csv data", null, cop3);
		MenuItem op4 = open.addItem("Wikipedia information", null, cop4);
		 

		Button searchb = new Button();
		searchb.setIcon(VaadinIcons.SEARCH);
		searchb.setWidth("100%");

		TextField searchtf = new TextField();
		searchtf.setWidth("100%");
		searchtf.setPlaceholder("Type a Place to Query");

		 

		Button restart = new Button("Clear");
		restart.setIcon(VaadinIcons.REFRESH);
		restart.setWidth("100%");

		 

		 
		 

		searchb.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				if (searchtf.getValue() == "") {
					Notification("Warning","Address cannot be empty");
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
								Notification("Error","The address cannot be found");
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

		MenuBar.Command chelp = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				removeWindow(info_tool);
				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				info_tool.setWidth("40%");
				addWindow(info_tool);
			}

		};

		MenuBar.Command cinfo = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				removeWindow(info_team);
				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				info_team.setSizeUndefined();
				addWindow(info_team);
			}

		};
		
		MenuItem helpi = barmenu.addItem("Help",null,chelp);
		MenuItem Info = barmenu.addItem("Info",null,cinfo);

		HorizontalLayout all = new HorizontalLayout();
		all.setWidth("100%");
		HorizontalLayout search = new HorizontalLayout();
		search.addComponent(searchtf);
		search.addComponent(searchb);
		search.setWidth("100%");
		search.setExpandRatio(searchtf, 2.25f);
		search.setExpandRatio(searchb, 0.25f);
		all.addComponent(barmenu);
		all.addComponent(restart);
		all.addComponent(search);
		all.setExpandRatio(barmenu,0.3f);
		all.setExpandRatio(restart,0.1f);
		all.setExpandRatio(search,0.6f);
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
					 
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName() == "nd") {
							try {
								 
								lat2 = (NodeList) xPath.compile("/osm/node[@id="
										+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue()
										+ "]/@lat").evaluate(xmlDocument, XPathConstants.NODESET);
							} catch (XPathExpressionException e) {

								e.printStackTrace();
							}
							try {
								 
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

	 

	public void Draw_xml_twitter(Draw d, XPath xPath, LMap map, Document xmlDocument, String nccolor, String ccolor,
			String cfill, String icon) {
		LLayerGroup lanswer = new LLayerGroup();
		Draw_xml_nodes_twitter(lanswer, d, xPath, map, xmlDocument, icon);
		Draw_xml_ways_twitter_marks(lanswer, d, xPath, map, xmlDocument, icon);
		Draw_xml_ways_twitter(lanswer, d, xPath, map, xmlDocument, nccolor, ccolor, cfill);
		map.addOverlay(lanswer, q.layer.getValue());
	}

	public void Draw_xml_nodes_youtube(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument,
			String Icon) {
		if (xmlDocument == null) {
		} else {
			NodeList nodes_list = null;
			try {
				nodes_list = (NodeList) xPath.compile("/social/youtube[oneway/node[tag]]").evaluate(xmlDocument,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			if (nodes_list == null) {
			} else {
				NodeList oneway = null;
				NodeList ytbs = null;
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
							if (sobjects.item(ch).getNodeName() == "videos") {
								type2 = "videos";
								tk++;
								ytbs = sobjects.item(ch).getChildNodes();
								if (twinfop.containsKey(q.layer.getValue())) {
									List<NodeList> current = twinfop.get(q.layer.getValue());
									current.add(ytbs);
									twinfop.put(q.layer.getValue(), current);
								} else {
									List<NodeList> current = new ArrayList<NodeList>();
									current.add(ytbs);
									twinfop.put(q.layer.getValue(), current);
								}
							}
							//CHANGES
							if (sobjects.item(ch).getNodeName() == "playlists") {
								type2 ="playlists";
								tk++;
								ytbs = sobjects.item(ch).getChildNodes();
								if (twinfop.containsKey(q.layer.getValue())) {
									List<NodeList> current = twinfop.get(q.layer.getValue());
									current.add(ytbs);
									twinfop.put(q.layer.getValue(), current);
								} else {
									List<NodeList> current = new ArrayList<NodeList>();
									current.add(ytbs);
									twinfop.put(q.layer.getValue(), current);
								}
							}
							//
							
							//CHANGES
							if (sobjects.item(ch).getNodeName() == "channels") {
								type2 ="channels";
								tk++;
								ytbs = sobjects.item(ch).getChildNodes();
								if (twinfop.containsKey(q.layer.getValue())) {
									List<NodeList> current = twinfop.get(q.layer.getValue());
									current.add(ytbs);
									twinfop.put(q.layer.getValue(), current);
								} else {
									List<NodeList> current = new ArrayList<NodeList>();
									current.add(ytbs);
									twinfop.put(q.layer.getValue(), current);
								}
							}
							//
							
						}

						if ((!(node.getAttributes().getNamedItem("lat").getNodeValue().equals("")))
								& (!(node.getAttributes().getNamedItem("lon").getNodeValue().equals("")))) {
							LMarker leafletMarker = d.Draw_Node(
									Double.parseDouble(node.getAttributes().getNamedItem("lat").getNodeValue()),
									Double.parseDouble(node.getAttributes().getNamedItem("lon").getNodeValue()));
							String svgCode = "<svg width='64' height='64' viewBox='0 0 64 64' xmlns='http://www.w3.org/2000/svg' version='1.1' >\r\n"
									+ "<circle cx=\"32\" cy=\"32\" r=\"31\" fill=\"rgb( 255, 0, 0)\" stroke=\"white\" stroke-width=\"2\"/>\r\n"
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
									if (type2=="videos") {in.setInfoYoutubeVideos(twinfop.get(q.layer.getValue()).get(s));}
									else
									if (type2=="playlists") {in.setInfoYoutubePlayLists(twinfop.get(q.layer.getValue()).get(s));}
									else {in.setInfoYoutubeChannels(twinfop.get(q.layer.getValue()).get(s));}
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

	public void Draw_xml_ways_youtube_marks(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument,
			String Icon) {
		if (xmlDocument == null) {
		} else {
			NodeList nodes_list = null;
			try {
				nodes_list = (NodeList) xPath.compile("/social/youtube[oneway/way]").evaluate(xmlDocument,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (nodes_list == null) {
			} else {
				NodeList oneway = null;
				NodeList ytbs = null;
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
							
							 
							
							if (sobjects.item(ch).getNodeName() == "videos") {
								type2 = "videos";
								ytbs = sobjects.item(ch).getChildNodes();
								if (twinfow.containsKey(q.layer.getValue())) {
									List<NodeList> current = twinfow.get(q.layer.getValue());
									current.add(ytbs);
									twinfow.put(q.layer.getValue(), current);
								} else {
									List<NodeList> current = new ArrayList<NodeList>();
									current.add(ytbs);
									twinfow.put(q.layer.getValue(), current);
								}
							}
							//CHANGES
							if (sobjects.item(ch).getNodeName() == "playlists") {
								type2 = "playlists";
								ytbs = sobjects.item(ch).getChildNodes();
								if (twinfow.containsKey(q.layer.getValue())) {
									List<NodeList> current = twinfow.get(q.layer.getValue());
									current.add(ytbs);
									twinfow.put(q.layer.getValue(), current);
								} else {
									List<NodeList> current = new ArrayList<NodeList>();
									current.add(ytbs);
									twinfow.put(q.layer.getValue(), current);
								}
							}
							//
							
							//CHANGES
							if (sobjects.item(ch).getNodeName() == "channels") {
								type2 = "channels";
								ytbs = sobjects.item(ch).getChildNodes();
								if (twinfow.containsKey(q.layer.getValue())) {
									List<NodeList> current = twinfow.get(q.layer.getValue());
									current.add(ytbs);
									twinfow.put(q.layer.getValue(), current);
								} else {
									List<NodeList> current = new ArrayList<NodeList>();
									current.add(ytbs);
									twinfow.put(q.layer.getValue(), current);
								}
							}
							//
							
						}
						if ((!(node.getAttributes().getNamedItem("lat").getNodeValue().equals("")))
								& (!(node.getAttributes().getNamedItem("lon").getNodeValue().equals("")))) {
							LMarker leafletMarker = d.Draw_Node(
									Double.parseDouble(node.getAttributes().getNamedItem("lat").getNodeValue()),
									Double.parseDouble(node.getAttributes().getNamedItem("lon").getNodeValue()));
							String svgCode = "<svg width='64' height='64' viewBox='0 0 64 64' xmlns='http://www.w3.org/2000/svg' version='1.1' >\r\n"
									+ "<circle cx=\"32\" cy=\"32\" r=\"31\" fill=\"rgb( 255, 0, 0)\" stroke=\"white\" stroke-width=\"2\"/>\r\n"
									+ "<text text-anchor=\"middle\" x=\"50%\" y=\"50%\" dy=\".35em\" font-family=\"sans-serif\" font-size=\"32px\" fill=\"white\">"
									+ number + "</text>\r\n" + "</svg>";
							leafletMarker.setDivIcon(svgCode);
							lanswer.addComponent(leafletMarker);
							 
							 
							int r = owc;
							leafletMarker.addClickListener(new LeafletClickListener() {
								@Override
								public void onClick(LeafletClickEvent event) {								 
									in.setInfo(tww.get(q.layer.getValue()).get(r));
									if (type2=="videos") {in.setInfoYoutubeVideos(twinfow.get(q.layer.getValue()).get(r));}
									else if (type2=="playlists") {in.setInfoYoutubePlayLists(twinfow.get(q.layer.getValue()).get(r));}
									else {in.setInfoYoutubeChannels(twinfow.get(q.layer.getValue()).get(r));}
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

	public void Draw_xml_ways_youtube(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument,
			String nccolor, String ccolor, String cfill) {
		if (xmlDocument == null) {
		} else {
			try {
				way.put(q.layer.getValue(), (NodeList) xPath.compile("/social/youtube/oneway/way").evaluate(xmlDocument,
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
					 
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName() == "nd") {
							try {
								lat2 = (NodeList) xPath.compile("/social/youtube/oneway/node[@id="
										+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue()
										+ "]/@lat").evaluate(xmlDocument, XPathConstants.NODESET);

							} catch (XPathExpressionException e) {
								e.printStackTrace();
							}
							 
							try {
								lon2 = (NodeList) xPath.compile("/social/youtube/oneway/node[@id="
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

	
	//YOUTUBE
	
	public void Draw_xml_youtube(Draw d, XPath xPath, LMap map, Document xmlDocument, String nccolor, String ccolor,
			String cfill, String icon) {
		LLayerGroup lanswer = new LLayerGroup();
		Draw_xml_nodes_youtube(lanswer, d, xPath, map, xmlDocument, icon);
		Draw_xml_ways_youtube_marks(lanswer, d, xPath, map, xmlDocument, icon);
		Draw_xml_ways_youtube(lanswer, d, xPath, map, xmlDocument, nccolor, ccolor, cfill);
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
								type = "tweets";
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
							//CHANGES
							if (sobjects.item(ch).getNodeName() == "users") {
								type ="users";
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
							//
							
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
									if (type=="tweets") {in.setInfoTwitterTweets(twinfop.get(q.layer.getValue()).get(s));}
									else {in.setInfoTwitterUsers(twinfop.get(q.layer.getValue()).get(s));}
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
								type = "tweets";
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
							//CHANGES
							if (sobjects.item(ch).getNodeName() == "users") {
								type = "users";
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
							//
							
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
									if (type=="tweets") {in.setInfoTwitterTweets(twinfow.get(q.layer.getValue()).get(r));}
									else {in.setInfoTwitterUsers(twinfow.get(q.layer.getValue()).get(r));}
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
					 
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName() == "nd") {
							try {
								lat2 = (NodeList) xPath.compile("/social/twitter/oneway/node[@id="
										+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue()
										+ "]/@lat").evaluate(xmlDocument, XPathConstants.NODESET);

							} catch (XPathExpressionException e) {
								e.printStackTrace();
							}
							 
							try {
								lon2 = (NodeList) xPath.compile("/social/twitter/oneway/node[@id="
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

		String call_query = "import module namespace xosm_social = \"xosm_social\" at \"XOSMSocial.xqy\";\r\n" +
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
	
	public Boolean api_delete(String layer) {
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			 
			HttpGet request = new HttpGet(" http://xosm.ual.es/xosmapiV2/getToken/layer/" + layer);
			request.addHeader("content-type", "application/xml");
			HttpResponse result = httpClient.execute(request);
			String token = EntityUtils.toString(result.getEntity(), "UTF-8");
			HttpGet request2 = new HttpGet("  http://xosm.ual.es/xosmapiV2/dropDatabase/"+layer+"/token/" + token);
		} catch (IOException ex) {
		}
		return true;
	}
	
	void Notification(String Topic, String Message) {
		Notification notif = new Notification(
			    Topic,
			    Message,Notification.Type.TRAY_NOTIFICATION, true);
		notif.setDelayMsec(10000);
		notif.setPosition(Position.MIDDLE_CENTER);
		notif.show(Page.getCurrent());
	}
	
	 

	@WebServlet(urlPatterns = "/*", name = "XOSM2Servlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = XOSM2.class, productionMode = false)
	public static class XOSM2Servlet extends VaadinServlet {
	}
}

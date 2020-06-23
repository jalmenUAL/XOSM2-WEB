package xosm2.xosm2;

import java.io.IOException;
import java.io.StringReader;
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
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@Theme("mytheme")
public class XOSM2 extends UI {

	String type = "";
	String type2 = "";
	HorizontalSplitPanel accordion = new HorizontalSplitPanel();
	HorizontalLayout accordion_right = new HorizontalLayout();
	Twitter popup = new Twitter();
	Youtube popup2 = new Youtube();

	Query q = null;
	Info_Node in = new Info_Node(this);
	Panel vinfo = new Panel();
	Double swlat = 36.83645, swlon = -2.45516, nelat = 36.83912, nelon = -2.45007;
	XOSM2 this_ = this;
	String osm_team = "<h1 style=\"color:DodgerRed;\">XOSM</h1>"
			+ "<p>XOSM is a Web tool and Query Language for <a href=\"https://www.openstreetmap.org/\">OpenStreetMap</a>.</p>"
			+ "<p>Using <a href=\"https://www.w3.org/XML/Query/\">XQuery</a> as language for queries definition</p>"
			+ "<p>and <a href=\"https://postgis.net/\">PosGis</a> as data storing and indexing.</p>"
			+ "<p>XOSM is able to handle a wide style of queries:</p>"
			+ "<p>From layer retrieval using distance and key-value pairs</p>" + "<p>to more complex queries</p>"
			+ "<p>involving spatial and aggregation operators.</p>"
			+ "<p>XOSM makes also possible to query Linked Open Data</p>" + "<p>and Social Networks.</p>"
			+ "<p>XOSM is equipped with an API Restful.</p>" + "<p>XOSM is specially suitable for querying urban maps</p>"
			+ "<p>enabling the retrieval of POIs and streets</p>" + "<p>providing rich information extracted from</p>"
			+ "<p>OpenStreetMap, Linked Open Data and Social Networks.</p>"
			+ "<p>Publications about XOSM can be found following</p>"
			+ "<a href=\"http://indalog.ual.es/WWW_pages/JesusAlmendros/Publications.html\">this link</a>"
			+ "<p style=\"color:Red;\">XOSM Team</p>"
			+ "<p style=\"color:Blue;\">Jesús Manuel Almendros-Jimenez jalmen@ual.es</p>"
			+ "<p style=\"color:Blue;\">Antonio Becerra-Terón abecerra@ual.es</p>"
			+ "<p style=\"color:Blue;\">Manuel Torres mtorres@ual.es</p>"
			+ "<p style=\"color:Green;\">Department of Informatics (University of Almería)</p>"
			+ "<p style=\"color:Green;\">04120 Almería (Spain)</p>";

	Info_Tool info_team = new Info_Tool(osm_team);
	String indexing = "PostGIS-indexed functions for layer retrieval\n" + "xosm_pbd:getLayerByBB(.):layer\n"
			+ "xosm_pbd:getLayerByName(.,name,distance):layer\n" + "xosm_pbd:getLayerByK(.,keyword):layer\n" 
			+ "xosm_pbd:getLayerByKV(.,keyword,value):layer\n"
			+ "xosm_pbd:getLayerByElement(.,element,distance):layer\n" + "xosm_pbd:getElementByName(.,name):item\n";

	String data = "OSM element creation\n" + "xosm_item:point(name,lat,lon,tags):node\n"
			+ "xosm_item:way(name,segments):way\n" + "xosm_item:segment(id,refs,tags):segment\n"
			+ "xosm_item:ref(id,lon,lat):nd\n" + "xosm_item:tag(k,v):tag\n" + "OSM element manipulation\n"
			+ "element[@type=\"point\"]:Boolean\n" + "element[@type=\"way\"]:Boolean\n"
			+ "element[@type=\"polygon\"]:Boolean\n" + "xosm_item:lon(node):Double\n" + "xosm_item:lat(node):Double\n"
			+ "xosm_item:name(way):String\n" + "xosm_item:refs(way):Sequence of nd\n"
			+ "xosm_item:tags(element): Sequence of tag\n" + "xosm_item:segments(way): Sequence of way\n"
			+ "xosm_item:id(element): String\n" + "xosm_item:length(element): Double\n"
			+ "xosm_item:area(element): Double\n" + "xosm_item:distance(element,element): Double\n";

	String spatial = "Coordinate based OSM Operators\r\n" + "Based on Distance\n"
			+ "xosm_sp:DWithIn(element1,element2,d):Boolean\r\n" + "Based on Latitude and Longitude\n"
			+ "xosm_sp:furtherNorthPoints(p1,p2):Boolean,\n" + "xosm_sp:furtherSouthPoints(p1,p2):Boolean,\n"
			+ "xosm_sp:furtherEastPoints(p1,p2):Boolean,\n" + "xosm_sp:furtherWestPoints(p1,p2):Boolean,\n"
			+ "xosm_sp:furtherNorthWays(w1,w2):Boolean,\n" + "xosm_sp:furtherSouthWays(w1,w2):Boolean,\n"
			+ "xosm_sp:furtherEastWays(w1,w2):Boolean,\n" + "xosm_sp:furtherWestWays(w1,w2):Boolean\n"
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

	String keyword = "xosm_kw:searchK(element,keyword):Boolean\r\n"
			+ "xosm_kw:searchKSet(element,(keyword1,...., keywordn)):Boolean\r\n"
			+ "xosm_kw:searchV(element,value):Boolean\r\n"
			+ "xosm_kw:searchVSet(element,(value1,...., valuen)):Boolean\r\n"
			+ "xosm_kw:searchTag(element,k,v):Boolean\r\n"; 

	String aggregation = "xosm_ag:topologicalCount(layer,element,topologicalOperator):Integer\r\n"
			+ "xosm_ag:metricMin(layer,metricOperator):layer\r\n" + "xosm_ag:metricMax(layer,metricOperator):layer\r\n"
			+ "xosm_ag:min(layer,metricOperator):Double\r\n" + "xosm_ag:max(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricSum(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricAvg(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricTopCount(layer,metricOperator,k):layer\r\n"
			+ "xosm_ag:metricBottomCount(layer,metricOperator,k):layer\r\n"
			+ "xosm_ag:metricRank(layer,metricOperator,k):item\r\n"
			+ "xosm_ag:metricMedian(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricMode(layer,metricOperator):Double\r\n"
			+ "xosm_ag:metricRange(layer,metricOperator):Double\r\n";

	String lod = "xosm_open:json2osm(url,path,name,id,lat,lon):layer\r\n" + "xosm_open:geojson2osm(url,name):layer\r\n"
			+ "xosm_open:csv2osm(url,name,lon,lat):layer\r\n" + "xosm_open:kml2osm(url,name):layer\r\n"
			+ "xosm_open:wikipediaElement2osm(node):item\r\n" + "xosm_open:wikipediaCoordinates2osm(lon,lat):item\r\n"
			+ "xosm_open:wikipediaName2osm(address):item\r\n";

	String social = "xosm_social:city(layer): String\r\n" + "xosm_social:api(apiCall,required,optional): json\r\n"
			+ "xosm_social:twitterSearchTweets(element,twitterResults): tweets\r\n"
			+ "xosm_social:twitterSearchUser(element,twitterResults): users\r\n"
			+ "xosm_social:twitterUserTimeLine(element,twitterResults): tweets\r\n"
			+ "xosm_social:twitterShowUser(element,twitterResults): userinfo\r\n"
			+ "xosm_social:youtubeVideoSearch(element,youtubeResults): videos\r\n"
			+ "xosm_social:youtubePlaylistSearch(element,youtubeResults): playlists\r\n"
			+ "xosm_social:youtubeChannelSearch(element,youtubeResults): channels\r\n"
			+ "xosm_social:youtubeVideoInfo(element,youtubeResults): videoinfo\r\n"
			+ "xosm_social:youtubePlaylistInfo(element,youtubeResults): playlistinfo\r\n"
			+ "xosm_social:youtubeChannelInfo(element,youtubeResults): channelinfo\r\n"
			+ "xosm_social:youtubePlaylistItems(element,youtubeResults): videos\r\n";

	String api =
					"http://xosm.ual.es/xosmapi/getLayerByBB/minLon/{minLon}/minLat/{minLat}"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}\r\n"
					+ "http://xosm.ual.es/xosmapi/getLayerByName/minLon/{minLon}/minLat/{minLat}"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/name/{name}/distance/{distance}\r\n"
					+ "http://xosm.ual.es/xosmapi/getLayerByElement/minLon/{minLon}/minLat/{minLat}"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/lon/{lon}/lat/{lat}/distance/{distance}\r\n"			
					+ "http://xosm.ual.es/xosmapi/getLayerByK/minLon/{minLon}/minLat/{minLat}"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/keyword/{keyword}\r\n"	
					+ "http://xosm.ual.es/xosmapi/getLayerByKV/minLon/{minLon}/minLat/{minLat}"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/keyword/{keyword}/value/{value}\r\n"
					+ "http://xosm.ual.es/xosmapi/getElementByName/minLon/{minLon}/minLat/{minLat}"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}/name/{name}\r\n"
					+ "http://xosm.ual.es/xosmapi/XOSMQuery/minLon/{minLon}/minLat/{minLat}"
					+ "/maxLon/{maxLon}/maxLat/{maxLat}?query={query}\r\n";

	String apisocial = 
			
			"http://xosm.ual.es/social.api/twitterSearchTweets/q/{q]\r\n"
			+ "http://xosm.ual.es/social.api/twitterSearchUser/city/{city}/q/{q}\r\n"
 			+ "http://xosm.ual.es/social.api/twitterUserTimeLine/screen_name/{screen_name}\r\n"
			+ "http://xosm.ual.es/social.api/twitterShowUser/screen_name/{screen_name}\r\n"
			+ "http://xosm.ual.es/social.api/youtubeVideoSearch/q/{q}\r\n"
			+ "http://xosm.ual.es/social.api/youtubePlaylistSearch/q/{q}\r\n"
			+ "http://xosm.ual.es/social.api/youtubeChannelSearch/q/{q}\r\n"
			+ "http://xosm.ual.es/social.api/youtubeVideoInfo/id/{id}\r\n"
			+ "http://xosm.ual.es/social.api/youtubePlaylistInfo/id/{id}\r\n"
			+ "http://xosm.ual.es/social.api/youtubeChannelInfo/id/{id}\r\n"
			+ "http://xosm.ual.es/social.api/youtubePlaylistItems/playlistId/{playlistId}\r\n";			

	Help_Tool info_tool = new Help_Tool(indexing, data, spatial, keyword, aggregation, lod, social, api, apisocial);

	LMap map = new LMap();
	Map<String, NodeList> nodes = new HashMap<String, NodeList>();
	Map<String, NodeList> way = new HashMap<String, NodeList>();
	Map<String, List<Node>> twp = new HashMap<String, List<Node>>();
	Map<String, List<Node>> tww = new HashMap<String, List<Node>>();
	Map<String, List<NodeList>> twinfop = new HashMap<String, List<NodeList>>();
	Map<String, List<NodeList>> twinfow = new HashMap<String, List<NodeList>>();

	@Override
	protected void init(VaadinRequest vaadinRequest) {

		Page.getCurrent().getJavaScript().execute("history.pushState({}, null, 'http://localhost:8080/xosm2');");

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

		String lat = vaadinRequest.getParameter("lat");
		String lon = vaadinRequest.getParameter("lon");

		if (!(lat == null) && !(lon == null)) {

			Boolean error = false;
			try {
				Double.parseDouble(lat);

			} catch (NumberFormatException e) {
				map.setCenter(36.838030858833, -2.4522979583778);
				error = true;
			}
			try {
				Double.parseDouble(lon);

			} catch (NumberFormatException e) {
				map.setCenter(36.838030858833, -2.4522979583778);
				error = true;
			}

			if (!error) {
				map.setCenter(Double.parseDouble(lat), Double.parseDouble(lon));
			} else {
				map.setCenter(36.838030858833, -2.4522979583778);
			}

		} else {
			map.setCenter(36.838030858833, -2.4522979583778);
		}
		String query = vaadinRequest.getParameter("query");
		if (query == null) {
			q = new Query(this, "xosm_pbd:getLayerByK(.,\"shop\")");
		} else {
			q = new Query(this, query);
		}

		// INDEXING

		String exq1 = "xosm_pbd:getLayerByName(.,\"Calle Calzada de Castro\",100)";

		String exq2 = "xosm_pbd:getLayerByK(.,\"shop\")";

		String exq3 = "xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")";

		String exq4 = "let $e :=\r\n" + "xosm_pbd:getElementByName(.,\"Calle Calzada de Castro\")\n"
				+ "let $layer :=\r\n" + "xosm_pbd:getLayerByElement(.,$e,100)\n" + "return $layer";

		// OSM

		String exq5 = "xosm_pbd:getLayerByBB(.)[@type=\"way\"]";

		String exq6 = "xosm_pbd:getLayerByBB(.)[@type=\"point\"]";

		String exq7 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,\"Carrera de los Limoneros\",100)\r\n"
				+ "return\r\n" + "for $node in $layer[@type=\"point\"]\r\n" + "return\r\n"
				+ "xosm_item:point(xosm_item:name($node),\r\n" + "xosm_item:lon($node),xosm_item:lat($node),"
				+ "xosm_item:tags($node))\r\n";

		String exq8 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(. ,\"Carrera de los Limoneros\",100)\r\n"
				+ "return\r\n" + "for $node in $layer[@type=\"way\"]\r\n" + "let $nodes := xosm_item:nodes($node)\r\n"
				+ "for $t in xosm_item:segments($node)\r\n" + "return xosm_item:way(xosm_item:name($node),\r\n"
				+ "xosm_item:segment(xosm_item:id($t),\r\n" + "xosm_item:refs($t),xosm_item:tags($t)),$nodes)";

		// SPATIAL

		String exq9 = "let $layer := xosm_pbd:getLayerByBB(.)\r\n" + "let $e :=\r\n"
				+ "xosm_pbd:getElementByName(.,'Calle Calzada de Castro')\r\n" + "return\r\n"
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

		String exq15 = "let $layer:= xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n" + "filter($layer,\r\n"
				+ "xosm_kw:searchKeywordSet(?,(\"bar\",\"restaurant\")))";

		// AGGREGATION

		String exq16 = "let $layer :=\r\n" + "xosm_pbd:getLayerByBB(.)\r\n"
				+ "let $e := xosm_pbd:getElementByName(.,'Calle Calzada de Castro')\r\n"
				+ "return xosm_ag:topologicalCount($layer,$e,\r\n" + "function($x,$y){xosm_sp:crossing($x,$y)})";

		String exq17 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings := \r\n" + "fn:filter($layer,xosm_kw:searchKeyword(?,'building'))\r\n"
				+ "return xosm_ag:metricMax($buildings,\r\n" + "function($x){xosm_item:area($x)})";

		String exq18 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings := \r\n" + "fn:filter($layer,xosm_kw:searchKeyword(?,'highway'))\r\n"
				+ "return xosm_ag:metricSum($buildings,\r\n" + "function($x){xosm_item:length($x)})";

		String exq19 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro' ,500)\r\n"
				+ "let $buildings :=\r\n" + "fn:filter($layer,xosm_kw:searchKeyword(?,'building'))\r\n"
				+ "return xosm_ag:metricAvg($buildings,\r\n" + "function($x){xosm_item:area($x)})";

		String exq20 = "let $layer :=\r\n" + "xosm_pbd:getLayerByBB(.)\r\n"
				+ "return xosm_ag:metricTopCount($layer,\r\n" + "function($x){xosm_item:area($x)},5)";

		String exq21 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricMedian($layer[@type=\"way\"],\r\n" + "function($x){xosm_item:length($x)})";

		String exq22 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricRange($layer,\r\n" + "function($x){xosm_item:area($x)})";

		String exq23 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricRank($layer,\r\n" + "function($x){xosm_item:area($x)},1)";

		String exq24 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Calle Calzada de Castro',500)\r\n"
				+ "return xosm_ag:metricRange($layer,\r\n" + "function($x){xosm_item:area($x)})";

		// OPEN DATA

		String exq25 = "xosm_open:geojson2osm(\r\n"
				+ "\"https://opendata.paris.fr/explore/dataset/paris_taxis_stations/download/?format=geojson&amp;timezone=UTC\",\"\")";

		String exq26 = "xosm_open:kml2osm(\r\n"
				+ "\"https://opendata.arcgis.com/datasets/7232e84f53494f5e9b131b81f92534b8_0.kml\",\r\n"
				+ "\"SiteName\")";

		// String exq27 =
		// "xosm_open:csv2osm(\"http://geodata.vermont.gov/datasets/b1ae7b7b110447c3b452d9cacffeed36_174.csv\",\r\n"
		// + "\"SiteName\",\"X\",\"Y\") ";

		String exq28 = "xosm_open:wikipediaName2osm(\"Almeria\")";

		String q1 = "let $hm := xosm_pbd:getElementByName(.,'Haymarket')\r\n"
				+ "let $bb := xosm_pbd:getLayerByBB(.)[@type=\"way\"]\r\n"
				+ "return fn:filter($bb,xosm_sp:intersecting(?,$hm))";

		String q2 = "let $hotel := xosm_pbd:getElementByName(.,'Hotel Miami')\r\n"
				+ "let $layer := xosm_pbd:getLayerByBB(.)\r\n" + "return\r\n" + "fn:filter(\r\n"
				+ "fn:filter($layer,xosm_kw:searchKeyword(?,'restaurant')),\r\n"
				+ "xosm_sp:furtherNorthPoints($hotel,?)) ";

		String q3 = "for $hotel in xosm_pbd:getLayerByKV(.,'tourism','hotel')\r\n"
				+ "let $layer := xosm_pbd:getLayerByElement(., $hotel ,500)\r\n" + "where count(\r\n" + "fn:filter\r\n"
				+ "($layer,\r\n" + "xosm_kw:searchKeywordSet(?,('bar','restaurant'))))>=10\r\n" + "return $hotel";

		String q4 = "let $hotel := xosm_pbd:getLayerByKV(.,'tourism','hotel')\r\n" + "let $f := function($hotel)\r\n"
				+ "{-(count(\r\n" + "fn:filter(xosm_pbd:getLayerByElement(.,$hotel,500),\r\n"
				+ "xosm_kw:searchKeyword(?,'church'))))}\r\n" + "return fn:sort($hotel,(),$f)[1]";

		/*
		 * String q5 = "let $layer :=\r\n" +
		 * "xosm_pbd:getLayerByName(.,'Karl-Liebknecht-Straße' ,500)\r\n" +
		 * "let $buildings := fn:filter($layer,\r\n" +
		 * "xosm_kw:searchKeyword(?,'building'))\r\n" +
		 * "return xosm_ag:metricSum($buildings,function($x){xosm_item:area($x)})";
		 */

		String q6 = "let $layer :=\r\n" + "xosm_pbd:getLayerByName(.,'Piazza del Duomo',1500)\r\n" + "return\r\n"
				+ "xosm_ag:metricMax(\r\n" + "filter($layer,xosm_kw:searchKeyword(?,'church')),\r\n"
				+ "function($x){xosm_item:area($x)})\r\n";

		String q7 = "let $open :=\r\n"
				+ "'https://opendata.bruxelles.be/explore/dataset/test-geojson-station-de-taxi/download/?format=geojson&amp;timezone=UTC'\r\n"
				+ "let $taxis := xosm_open:geojson2osm($open,'')\r\n" + "let $building :=\r\n"
				+ "xosm_pbd:getElementByName(. ,'Bruxelles-Central - Brussel-Centraal') \r\n"
				+ "return fn:filter($taxis,xosm_sp:DWithIn($building,?,100))";

		String q8 = "let $open :=\r\n"
				+ "'http://data2.esrism.opendata.arcgis.com/datasets/51900577e33a4ba4ab59a691247aeee9_0.geojson'\r\n"
				+ "let $events :=\r\n" + "xosm_open:geojson2osm($open,'') \r\n" + "return fn:filter($events,\r\n"
				+ "function($p)\r\n" + "{not(empty($p/node/tag[@k='GRATUITO' and @v='Sí']))})";

		String q9 = "let $x := xosm_pbd:getElementByName(.,'Calle Mayor')\r\n"
				+ "let $y := xosm_pbd:getElementByName(.,'Calle de Esparteros')\r\n" + "return\r\n"
				+ "for $i in xosm_sp:intersectionPoints($x,$y)\r\n" + "return xosm_open:wikipediaElement2osm($i) ";

		// SOCIAL
		
		String q10 = "<social>{\r\n" + 
				"let $ts := xosm_pbd:getLayerByK(., \"tourism\")\r\n" + 
				"let $city := xosm_social:city($ts)\r\n" + 
				"for $t in $ts\r\n" + 
				"let $q := data($t/@name) || \" \" || $city\r\n" + 
				"let $tweets :=\r\n" + 
				"xosm_social:api\r\n" + 
				"(\"http://xosm.ual.es/social.api/twitterSearchTweets\", \r\n" + 
				"map { 'q' : $q }, map { 'count' : 10 })/json/_ \r\n" + 
				"return\r\n" + 
				"xosm_social:twitterSearchTweets($t,$tweets)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q11 = "<social>{\r\n" + 
				"for $restaurant in xosm_pbd:getLayerByKV(., \"amenity\",\"restaurant\")\r\n" + 
				"let $q := xosm_social:hashtag($restaurant/@name) \r\n" + 
				"let $tweets := xosm_social:api(\r\n" + 
				"    \"http://xosm.ual.es/social.api/twitterSearchTweets\",\r\n" + 
				"    map { 'q' : $q},\r\n" + 
				"    map { 'count' : 10, 'option' : 'hashtag' })/json/_\r\n" + 
				"return xosm_social:twitterSearchTweets($restaurant, $tweets)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q12 = "<social>{\r\n" + 
				"let $hotels := xosm_pbd:getLayerByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels)\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $q := \"Hotel\" || \" \" || data($hotel/@name)\r\n" + 
				"let $users :=\r\n" + 
				"xosm_social:api\r\n" + 
				"(\"http://xosm.ual.es/social.api/twitterSearchUser\",\r\n" + 
				"map { 'q' : $q,  'city' : $city },map { 'count' : 10 })/json/_\r\n" + 
				"return\r\n" + 
				"xosm_social:twitterSearchUser($hotel,$users)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q13 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := data($museum/@name)\r\n" + 
				"let $lon := data($museum/node[1]/@lon)\r\n" + 
				"let $lat := data($museum/node[1]/@lat)\r\n" + 
				"let $geocode := $lat ||  \",\" || $lon || \",\" || \"5km\" \r\n" + 
				"let $tweets :=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchTweets\", \r\n" + 
				"map { 'q' : $q }, map { 'count' : 10, 'geocode' : $geocode})/json/_\r\n" + 
				"return\r\n" + 
				"xosm_social:twitterSearchTweets($museum, $tweets)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q14 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := data($museum/@name)\r\n" + 
				"let $tweets :=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchTweets\", \r\n" + 
				"map { 'q' : $q }, map { 'count' : 10 })/json/_[favorite__count > 2]\r\n" + 
				"return\r\n" + 
				"xosm_social:twitterSearchTweets($museum, $tweets)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q15 = "<social>{\r\n" + 
				"let $restaurants :=\r\n" + 
				"xosm_pbd:getLayerByKV(., \"amenity\", \"restaurant\") \r\n" + 
				"let $city := xosm_social:city($restaurants)\r\n" + 
				"for $restaurant in $restaurants\r\n" + 
				"let $q := data($restaurant/@name) || \" \" || $city\r\n" + 
				"let $tweets :=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchTweets\", \r\n" + 
				"map { 'q' : $q }, map { 'count' : 15})/json/_ [user/friends__count > 100]\r\n" + 
				"return\r\n" + 
				"xosm_social:twitterSearchTweets($restaurant, $tweets)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q16 = "<social>{\r\n" + 
				"let $hotels := xosm_pbd:getLayerByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels)\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $q := \"Hotel\" || \" \" || data($hotel/@name)\r\n" + 
				"let $users :=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchUser\", \r\n" + 
				"map { 'q' : $q,  'city' : $city }, "
				+ "map { 'count' : 10 })/json/_[followers__count > 2000]\r\n" + 
				"return\r\n" + 
				"xosm_social:twitterSearchUser($hotel, $users)"
				+"}</social>\r\n";

		String q17 = "<social>{\r\n" + 
				"let $hotels := xosm_pbd:getLayerByKV(., \"tourism\", \"hotel\")\r\n" + 
				"let $city := xosm_social:city($hotels)\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $q := \"Hotel\" || \" \" || data($hotel/@name)\r\n" + 
				"let $screen_name :=\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchUser\", \r\n" + 
				"map { 'q' : $q,  'city' : $city }, map { 'count' : 1 })/json/screen__name)\r\n" + 
				"let $tweets :=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterUserTimeLine\",\r\n" + 
				"map { 'screen_name' : $screen_name }, map { 'count' : 10 })/json/_[favorite__count > 5]\r\n" + 
				"return\r\n" + 
				"xosm_social:twitterUserTimeLine($hotel,  $tweets)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q18 = "<social>{\r\n" + 
				"let $museums := xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $city := xosm_social:city($museums) \r\n" + 
				"for $museum in $museums\r\n" + 
				"let $screen__name :=\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchUser\",\r\n" + 
				"map { 'q' : data($museum/@name) ,  'city' : $city },\r\n" + 
				"map { 'count' : 1 })/json/screen__name)\r\n" + 
				"let $tweets :=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchTweets\",\r\n" + 
				"map { 'q' : $screen__name}, map { 'count' : 10, 'option' : 'mention' })/json/_\r\n" + 
				"return\r\n" + 
				"xosm_social:twitterSearchTweets($museum, $tweets)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q19 = "<social>{\r\n" + 
				"let $hotels := xosm_pbd:getLayerByKV(., \"tourism\", \"hotel\")\r\n" + 
				"let $city := xosm_social:city($hotels)\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $q := \"Hotel\" || \" \" || data($hotel/@name)\r\n" + 
				"let $screen__name :=  \r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchUser\", \r\n" + 
				"map { 'q' :$q,  'city' : $city }, map { 'count' : 1 })/json/screen__name)\r\n" + 
				"let $tweets := \r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/twitterSearchTweets\",\r\n" + 
				"map { 'q' : $q}, map { 'count' : 10, 'option' : 'mention' })/json/_\r\n" + 
				"return\r\n" + 
				"xosm_social:twitterSearchTweets($hotel,$tweets)\r\n" + 
				"}</social>\r\n" + 
				"";

		String q20 = "<social>{\r\n" + 
				"let $hotels := xosm_pbd:getLayerByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels)\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $q := \"Hotel\" || \" \" || data($hotel/@name) || \" \" || $city  \r\n" + 
				"let $videos:=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeVideoSearch\",\r\n" + 
				"map { 'q' : $q }, map { 'maxResults' : 5 })/json/_\r\n" + 
				"return\r\n" + 
				"xosm_social:youtubeVideoSearch($hotel, $videos)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q21 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"let $channels :=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeChannelSearch\",\r\n" + 
				"map { 'q' : $q}, map { 'maxResults' : 3 })/json/_\r\n" + 
				"return\r\n" + 
				"xosm_social:youtubeChannelSearch($museum, $channels)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q22 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"let $playlists := \r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistSearch\",\r\n" + 
				"map { 'q' : $q}, map { 'maxResults' : 3 })/json/_\r\n" + 
				"return\r\n" + 
				"xosm_social:youtubePlaylistSearch($museum, $playlists)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q23 = "<social>{\r\n" + 
				"let $hotels := xosm_pbd:getLayerByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels)\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $q := \"Hotel\" || \" \" || data($hotel/@name) || \" \" || $city\r\n" + 
				"let $videos :=\r\n" + 
				"(for $id in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeVideoSearch\", \r\n" + 
				"map { 'q' : $q }, map { })/json/_/id/videoId)\r\n" + 
				"return\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeVideoInfo\", \r\n" + 
				"map { 'id' : $id }, map { })/json/items/_[statistics/viewCount > 10])\r\n" + 
				"return\r\n" + 
				"xosm_social:youtubeVideoInfo($hotel, $videos)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q24 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"let $channels :=\r\n" + 
				"(for $id in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeChannelSearch\",\r\n" + 
				"map { 'q' : $q}, map { 'maxResults' : 3})/json/_/id/channelId)\r\n" + 
				"return\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeChannelInfo\", \r\n" + 
				"map {'id' : $id }, map {})/json/items/_[statistics/subscriberCount > 100])\r\n" + 
				"return\r\n" + 
				"xosm_social:youtubeChannelInfo($museum, $channels)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q25 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"let $playlists :=\r\n" + 
				"(for $id in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistSearch\",\r\n" + 
				"map { 'q' : $q}, map { })/json/_/id/playlistId)\r\n" + 
				"return\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistInfo\", \r\n" + 
				"map {'id' : $id }, map {})/json/items/_)\r\n" + 
				"return\r\n" + 
				"xosm_social:youtubePlaylistInfo($museum, $playlists)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q26 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"let $playlists :=\r\n" + 
				"(for $id in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistSearch\",\r\n" + 
				"map { 'q' : $q}, map { })/json/_/id/playlistId)\r\n" + 
				"return xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistItems\",\r\n" + 
				"map {'playlistId' : $id }, map {})/json/items/_)\r\n" + 
				"return\r\n" + 
				"xosm_social:youtubePlaylistItems($museum, $playlists)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q27 = "<social>{\r\n" + 
				"let $hotels := xosm_pbd:getLayerByKV(., \"tourism\", \"hotel\") \r\n" + 
				"let $city := xosm_social:city($hotels)\r\n" + 
				"for $hotel in $hotels\r\n" + 
				"let $q := \"Hotel\" || \" \" || data($hotel/@name) || \" \" || $city\r\n" + 
				"let $videos := (\r\n" + 
				"for $playId in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistSearch\",\r\n" + 
				"map { 'q' : $q}, map {  })/json/_/id/playlistId)\r\n" + 
				"for $videoId in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistItems\", \r\n" + 
				"map { 'playlistId' : $playId}, map { })/json/items/_/snippet/resourceId/videoId)\r\n" + 
				"return\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeVideoInfo\",\r\n" + 
				"map { 'id' : $videoId},map { })/json/items/_)\r\n" + 
				"return xosm_social:youtubeVideoInfo($hotel, $videos)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q28 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\") \r\n" + 
				"let $q := $museum/@name\r\n" + 
				"let $channelId :=\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeChannelSearch\",\r\n" + 
				"map { 'q' : $q}, \r\n" + 
				"map { 'maxResults' : 1})/json/_/id/channelId)\r\n" + 
				"let $videos := (\r\n" + 
				"let $playId :=\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeChannelInfo\", \r\n" + 
				"map {'id' : $channelId }, map {})/json/items/_/contentDetails/relatedPlaylists/uploads)\r\n" + 
				"for $videoId in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistItems\", \r\n" + 
				"map { 'playlistId' : $playId}, map { })/json/items/_/snippet/resourceId/videoId)\r\n" + 
				"return\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeVideoInfo\",\r\n" + 
				"map { 'id' : $videoId}, map { })/json/items/_\r\n" + 
				")return\r\n" + 
				"xosm_social:youtubeVideoInfo($museum, $videos)\r\n" + 
				"} </social>\r\n" + 
				"";

		String q29 = "<social>{\r\n" + 
				"for $museum in xosm_pbd:getLayerByKV(., \"tourism\", \"museum\")\r\n" + 
				"let $q := $museum/@name\r\n" + 
				"let $videos :=\r\n" + 
				"(for $channelId in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeChannelSearch\", \r\n" + 
				"map { 'q' : $q}, map {})/json/_/id/channelId) \r\n" + 
				"let $json :=\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeChannelInfo\", \r\n" + 
				"map { 'id' : $channelId }, map { })[json/items/_/statistics/subscriberCount > 2000]\r\n" + 
				"for $playId in\r\n" + 
				"data($json/json/items/_/contentDetails/relatedPlaylists/uploads) \r\n" + 
				"for $videoId in\r\n" + 
				"data(xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubePlaylistItems\", \r\n" + 
				"map { 'playlistId' : $playId} , map { })/json/items/_/snippet/resourceId/videoId)\r\n" + 
				"return\r\n" + 
				"xosm_social:api(\r\n" + 
				"\"http://xosm.ual.es/social.api/youtubeVideoInfo\",\r\n" + 
				"map { 'id' : $videoId },map { })/json/items/_)\r\n" + 
				"return \r\n" + 
				"xosm_social:youtubeVideoInfo($museum,$videos)\r\n" + 
				"}\r\n" + 
				"</social> \r\n" + 
				" ";

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

		/*
		 * MenuBar.Command cop3 = new MenuBar.Command() { public void
		 * menuSelected(MenuItem selectedItem) { map.setCenter(41.90219, 12.49580);
		 * map.setCenter(36.838030858833, -2.4522979583778); map.setZoomLevel(18); nelat
		 * = map.getBounds().getNorthEastLat(); nelon =
		 * map.getBounds().getNorthEastLon(); swlat = map.getBounds().getSouthWestLat();
		 * swlon = map.getBounds().getSouthWestLon(); q.setQuery(exq27);
		 * map.setZoomLevel(10);
		 * 
		 * }
		 * 
		 * };
		 */

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

		/*
		 * MenuBar.Command copen5 = new MenuBar.Command() { public void
		 * menuSelected(MenuItem selectedItem) { map.setCenter(36.838030858833,
		 * -2.4522979583778); map.setCenter(36.838030858833, -2.4522979583778);
		 * map.setZoomLevel(18); nelat = map.getBounds().getNorthEastLat(); nelon =
		 * map.getBounds().getNorthEastLon(); swlat = map.getBounds().getSouthWestLat();
		 * swlon = map.getBounds().getSouthWestLon(); q.setQuery(q5);
		 * 
		 * }
		 * 
		 * };
		 */

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

		// ADDED

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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
				map.setCenter((40.4126 + 40.4168) / 2, (-3.6961 + -3.6887) / 2);
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
		// barmenu.addItem("Menu");

		MenuItem examples = barmenu.addItem("Examples", null, null);
		MenuItem indexing = examples.addItem("Indexing Examples", null, null);
		MenuItem osm = examples.addItem("Layers Examples", null, null);
		MenuItem spatial = examples.addItem("Spatial Examples", null, null);
		MenuItem keyword = examples.addItem("Keyword Examples", null, null);
		MenuItem aggregation = examples.addItem("Aggregation Examples", null, null);
		MenuItem open = examples.addItem("Open Data Examples", null, null);
		MenuItem mixed = examples.addItem("Mixed Examples", null, null);
		MenuItem social = examples.addItem("Social Network Examples", null, null);
		MenuItem twitter = social.addItem("Twitter Examples", null, null);
		MenuItem youtube = social.addItem("Youtube Examples", null, null);

		indexing.addItem("Retrieve the elements of the bounding box close (100 m) to an street", null, cind1);
		indexing.addItem("Retrieve the shops of the bounding box", null, cind2);
		indexing.addItem("Retrieve an element of the bounding box", null, cind3);
		indexing.addItem("Retrieve the elements of the bounding box close (100 m) to an street", null, cind4);
		// osm.addItem("Retrieve the ways of the bounding box", null, cosm1);
		osm.addItem("Retrieve the points of the bounding box", null, cosm2);
		osm.addItem("Rebuild the points of the bounding box close (100 m) to an street", null, cosm3);
		osm.addItem("Rebuild the ways of the bounding box close (100 m) to an street", null, cosm4);
		spatial.addItem("Retrieve the points of the bounding box within (100 m) to an street", null, cspa1);
		spatial.addItem("Retrieve the points of the bounding box further west to a point", null, cspa2);
		spatial.addItem("Retrieve the streets crossing an street", null, cspa3);
		spatial.addItem("Retrieve the streets intersecting with an street", null, cspa4);
		keyword.addItem("Retrieve the hotels of the bounding box", null, ckey1);
		keyword.addItem("Retrieve the amenities of the bounding box", null, ckey2);
		keyword.addItem("Retrieve the bars and restaurants of the bounding box", null, ckey3);
		aggregation.addItem("Retrieve the number of streets crossing an street", null, cagg1);
		aggregation.addItem("Retrieve the buildings with maximum area close (500m) to an street", null, cagg2);
		aggregation.addItem("Retrieve the sum of the length of highways close (500m) to an street", null, cagg3);
		aggregation.addItem("Retrieve the average area of buildings close (500m) to an street", null, cagg4);
		aggregation.addItem("Retrieve the 5 biggest elements", null, cagg5);
		aggregation.addItem("Retrieve the median of the area of elements close (500m) to an street", null, cagg6);
		aggregation.addItem("Retrieve the range of the area of elements close (500m) to an street", null, cagg7);
		aggregation.addItem("Retrieve the biggest element close (500m) to an street", null, cagg8);
		// aggregation.addItem("Retrieve the range of area of elements close (500m) to
		// an street", null, cagg9);
		open.addItem("Import geojson data", null, cop1);
		open.addItem("Import kml data", null, cop2);
		// open.addItem("Import csv data", null, cop3);
		open.addItem("Wikipedia information", null, cop4);
		open.addItem("Request taxi stops close (100 m) to Bruxelles Central Station in Bruxelles", null, copen7);
		open.addItem("Retrieves free events of Madrid", null, copen8);
		open.addItem(
				"Retrieve Wikipedia information about places nearby to the intersection point of Calle Mayor and Calle de Esparteros in Madrid",
				null, copen9);
		mixed.addItem("Retrieve the streets in the bounding box intersecting Haymarket street", null, copen1);
		mixed.addItem("Retrieve the restaurants in Roma further north to Miami hotel", null, copen2);
		mixed.addItem(
				"Retrieve hotels of Vienna close (500 m) to food venues (food venues = number of bars and restaurants bigger than 10)",
				null, copen3);
		mixed.addItem("Retrieve the hotels of Munich with the greatest number of churches nearby", null, copen4);
		// mixed.addItem("Retrieve the size of buidings close (500 m) to
		// Karl-Liebknecht-Straße in Berlin", null, copen5);
		mixed.addItem("Retrieve the biggest churchs close (1500 m) to Piazza del Duomo in Milan", null, copen6);
		
		
		
		twitter.addItem("Get Top 10 tweets about touristic places", null, ctwitter1);
		twitter.addItem("Get Top 10 tweets with hashtag the name of restaurants", null, ctwitter2);
		twitter.addItem("Get Top 10 accounts about hotels", null,
				ctwitter3);
		twitter.addItem("Get Top 10 tweets within a radius of 5 km from museums",
				null, ctwitter4);
		twitter.addItem("Get Top 10 tweets about museums with more than 3 favorites", null,
				ctwitter5);
		twitter.addItem(
				"Get tweets about restaurants posted by users with more than 100 friends from Top 15",
				null, ctwitter6);
		twitter.addItem(
				"Get accounts about hotels with more than 2000 followers from Top 10",
				null, ctwitter7);
		twitter.addItem(
				"Get tweets with more than 5 favorites from Top 10 of the Top account of hotels",
				null, ctwitter8);
		twitter.addItem(
				"Get Top 10 tweets mentioning the Top account of musseums",
				null, ctwitter9);
		twitter.addItem(
				"Get Top 10 tweets mentioning the Top account of hotels",
				null, ctwitter10);
		
		
		youtube.addItem("Get Top 5 videos about hotels", null, cyoutube1);
		youtube.addItem("Get Top 3 channels of museums", null,
				cyoutube2);
		youtube.addItem("Get Top 3 playlists of museums", null,
				cyoutube3);
		youtube.addItem(
				"Get videos about hotels with more than 10 views",
				null, cyoutube4);
		youtube.addItem(
				"Get channels about museums with more than 100 subscribers",
				null, cyoutube5);
		youtube.addItem(
				"Get info of playlists about museums",
				null, cyoutube6);
		youtube.addItem(
				"Get videos of playlists about museums",
				null, cyoutube7);
		youtube.addItem(
				"Get info of videos in playlists about hotels",
				null, cyoutube8);
		youtube.addItem(
				"Get info of videos in the Top channel of hotels",
				null, cyoutube9);
		youtube.addItem(
				"Get info of videos in channels of hotels with more than 2000 subscribers",
				null, cyoutube10);

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
					Notification("Warning", "Address cannot be empty");
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
							NodeList search = (NodeList) xPath.compile("/searchresults/place").evaluate(xmlDocument,
									XPathConstants.NODESET);

							NodeList test = (NodeList) xPath.compile("/searchresults/place/@lat").evaluate(xmlDocument,
									XPathConstants.NODESET);

							if (test.getLength() == 0) {
								Notification("Error", "The address cannot be found");
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
				// accordion_right.setSplitPosition(100);
				vinfo.setVisible(false);
				accordion.setSplitPosition(40);
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
				// accordion_right.setSplitPosition(100);
				vinfo.setVisible(false);
				accordion.setSplitPosition(40);
				info_tool.setWidth("55%");
				// info_tool.center();
				addWindow(info_tool);
			}

		};

		MenuBar.Command cinfo = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				removeWindow(info_team);
				// accordion_right.setSplitPosition(100);
				vinfo.setVisible(false);
				accordion.setSplitPosition(40);
				info_team.setWidth("25%");
				// info_team.center();
				addWindow(info_team);
			}

		};

		MenuItem xosminf = barmenu.addItem("Info", null, null);
		xosminf.addItem("Team", null, cinfo);
		xosminf.addItem("Help", null, chelp);
		barmenu.addItem("", null, null);

		HorizontalLayout all = new HorizontalLayout();
		all.setWidth("100%");
		HorizontalLayout search = new HorizontalLayout();
		search.addComponent(searchtf);
		search.addComponent(searchb);
		search.setWidth("100%");
		search.setExpandRatio(searchtf, 0.9f);
		search.setExpandRatio(searchb, 0.1f);
		all.addComponent(barmenu);
		all.addComponent(restart);
		all.addComponent(search);
		all.setExpandRatio(barmenu, 0.2f);
		all.setExpandRatio(restart, 0.1f);
		all.setExpandRatio(search, 0.7f);
		MVerticalLayout vl = new MVerticalLayout(all).expand(map);
		vl.setMargin(false);
		q.setSizeFull();
		q.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);
		
		vinfo.setSizeFull();
		vinfo.setContent(in);
		vinfo.setVisible(false);
		
		accordion_right.setSizeFull();
		accordion_right.addComponent(vl);
		accordion_right.addComponent(vinfo);
		accordion_right.setExpandRatio(vl, 0.65f);
		accordion_right.setExpandRatio(vinfo, 0.35f);
		
		
		
		accordion.setSizeFull();
		accordion.setSplitPosition(40);
		accordion.setFirstComponent(q);
		accordion.setSecondComponent(accordion_right);
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
								// accordion_right.setSplitPosition(65);
								vinfo.setVisible(true);
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
								// accordion_right.setSplitPosition(65);
								vinfo.setVisible(true);
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
								// accordion_right.setSplitPosition(65);
								vinfo.setVisible(true);
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
							// CHANGES
							if (sobjects.item(ch).getNodeName() == "playlists") {
								type2 = "playlists";
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

							// CHANGES
							if (sobjects.item(ch).getNodeName() == "channels") {
								type2 = "channels";
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
									if (type2 == "videos") {
										in.setInfoYoutubeVideos(twinfop.get(q.layer.getValue()).get(s));
									} else if (type2 == "playlists") {
										in.setInfoYoutubePlayLists(twinfop.get(q.layer.getValue()).get(s));
									} else {
										in.setInfoYoutubeChannels(twinfop.get(q.layer.getValue()).get(s));
									}
									// accordion_right.setSplitPosition(65);
									vinfo.setVisible(true);
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
									if (oneway.item(n).getNodeName() == "node" && !first_node) {
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
							// CHANGES
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

							// CHANGES
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
									if (type2 == "videos") {
										in.setInfoYoutubeVideos(twinfow.get(q.layer.getValue()).get(r));
									} else if (type2 == "playlists") {
										in.setInfoYoutubePlayLists(twinfow.get(q.layer.getValue()).get(r));
									} else {
										in.setInfoYoutubeChannels(twinfow.get(q.layer.getValue()).get(r));
									}
									// accordion_right.setSplitPosition(65);
									vinfo.setVisible(true);
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
			} else {
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

	// YOUTUBE

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
							// CHANGES
							if (sobjects.item(ch).getNodeName() == "users") {
								type = "users";
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
									if (type == "tweets") {
										in.setInfoTwitterTweets(twinfop.get(q.layer.getValue()).get(s));
									} else {
										in.setInfoTwitterUsers(twinfop.get(q.layer.getValue()).get(s));
									}
									// accordion_right.setSplitPosition(65);
									vinfo.setVisible(true);
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
									if (oneway.item(n).getNodeName() == "node" && !first_node) {
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
							// CHANGES
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
									if (type == "tweets") {
										in.setInfoTwitterTweets(twinfow.get(q.layer.getValue()).get(r));
									} else {
										in.setInfoTwitterUsers(twinfow.get(q.layer.getValue()).get(r));
									}
									// accordion_right.setSplitPosition(65);
									vinfo.setVisible(true);
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
			} else {
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

		String call_query = "import module namespace xosm_social = \"xosm_social\" at \"XOSMSocial.xqy\";\r\n"
				+ "import module namespace xosm_item = \"xosm_item\" at \"XOSMItem.xqy\";\r\n"
				+ "import module namespace xosm_sp = \"xosm_sp\" at \"XOSMSpatial.xqy\";\r\n"
				+ "import module namespace xosm_kw = \"xosm_kw\" at \"XOSMKeyword.xqy\";\r\n"
				+ "import module namespace xosm_ag = \"xosm_ag\" at \"XOSMAggregation.xqy\";\r\n"
				+ "import module namespace xosm_open = \"xosm_open\" at \"XOSMOpenData.xqy\";\r\n"
				+ "import module namespace xosm_pbd = \"xosm_pbd\" at \"XOSMPostGIS.xqy\";\r\n\n" + query;

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

	void Notification(String Topic, String Message) {
		Notification notif = new Notification(Topic, Message, Notification.Type.TRAY_NOTIFICATION, true);
		notif.setDelayMsec(10000);
		notif.setPosition(Position.MIDDLE_CENTER);
		notif.show(Page.getCurrent());
	}

	@WebServlet(urlPatterns = "/*", name = "XOSM2Servlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = XOSM2.class, productionMode = false)
	public static class XOSM2Servlet extends VaadinServlet {
	}
}

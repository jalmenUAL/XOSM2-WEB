package xosm2.xosm2;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.http.client.methods.HttpPost;
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
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of an HTML page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@Theme("mytheme")
public class XOSM2 extends UI {

	HorizontalSplitPanel accordion = new HorizontalSplitPanel();
	HorizontalSplitPanel accordion2 = new HorizontalSplitPanel();
	
	
	Query q = null; 
	
	Info_Node in = new Info_Node(this);
	
	Double swlat = 36.83645, swlon = -2.45516, nelat = 36.83912, nelon = -2.45007;
	
	XOSM2 this_ = this;

	String osm_team = "Aim of the Project:\r\n"
			+ "Volunteered geographic information (VGI) makes available a very large resource of geographic data.\r\n"
			+ "The exploitation of data coming from such resources requires an additional effort in the form of tools\r\n"
			+ "and effective processing techniques. One of the most established VGI is OpenStreetMap (OSM)\r\n"
			+ "offering data of urban and rural maps from the earth. XOSM (XQuery for OpenStreetMap),\r\n"
			+ "is a tool for the processing of geospatial queries on OSM. The tool is equipped with a\r\n"
			+ "rich query language based on a set of operators defined for OSM which have been implemented\r\n"
			+ "as a library of the XML query language XQuery. The library provides a rich repertoire of\r\n"
			+ "spatial, keyword and aggregation based functions, which act on the XML representation of\r\n"
			+ "an OSM layer. The use of the higher order facilities of XQuery makes possible the\r\n"
			+ "definition of complex geospatial queries involving spatial relations, keyword searching\r\n"
			+ "and aggregation functions. XOSM indexes OSM data enabling efficient retrieval of answers.\r\n"
			+ "The XOSM library also enables the definition of queries combining OSM layers and layers\r\n"
			+ "created from Linked Open Data resources (KML, GeoJSON, CSV and RDF). The tool also provides an\r\n"
			+ "API to execute XQuery queries using the library.\r\n" + "\r\n" + "XOSM Team\r\n"
			+ "Jesús Manuel Almendros-Jimenez jalmen@ual.es\r\n" + "Antonio Becerra-Terón abecerra@ual.es\r\n"
			+ "Manuel Torres mtorres@ual.es\r\n" + "Departamento de Informatica (University of Almería)\r\n"
			+ "04120 Almería (Spain)";

	Info_Tool info_team = new Info_Tool(osm_team);

	String indexing = "Indexing functions\n" + "Two indexing strategies are considered\n"
			+ "RLD: R*-tree indexing with OSM live data and PBD:\n"
			+ "PostGIS-based indexing with OSM backup data (OSM planet).\n" + "\n"
			+ "For switching from RLD and PBD the namespace has to be changed:\n"
			+ "xosm_rld represents RLD, and xosm_pbd represents PBD.\n" + "RLD\n"
			+ "xosm_rld:getLayerByName(map, name, distance)\n"
			+ "xosm_rld:getLayerByElement(map, OSMelement, distance)\n" + "xosm_rld:getElementByName(map, name)\n"
			+ "xosm_rld:getElementsByKeyword(map, keyword)\n" + "xosm_rld:getLayerByBB(map)\n" + "PBD\n"
			+ "xosm_pbd:getLayerByName(map, name, distance)\n"
			+ "xosm_pbd:getLayerByElement(map, OSMelement, distance)\n" + "xosm_pbd:getElementByName(map, name)\n"
			+ "xosm_pbd:getElementsByKeyword(map, keyword)\n" + "xosm_pbd:getLayerByBB(map)";
	
	String spatial = "XOSM is equipped with a repertoire of spatial operators able\n"
			+ "to check specific spatial relationships over elements in OSM maps.\n"
			+ "Two kind of operators are considered:\n"
			+ "Coordinate based OSM Operators and Clementini based OSM Operators\n" + 
			"\n" + 
			"Coordinate based OSM Operators\n"+ 
			"Based on Distance\n" + 
			"xosm_sp:DWithin(s1,s2,d)\n" + 
			"Based on Latitude and Longitude\n" + 
			"xosm_sp:furtherNorthPoints(p1,p2), xosm_sp:furtherSouthPoints(p1,p2),\n"
			+"xosm_sp:furtherEastPoints(p1,p2), xosm_sp:furtherWestPoints(p1,p2),\n "
			+"xosm_sp:furtherNorthWays(s1,s2),xosm_sp:furtherSouthWays(s1,s2),\n"
			+"xosm_sp:furtherEastWays(s1,s2),xosm_sp:furtherWestWays(s1,s2)\n"
			+"\n" + 
			"Clementini based OSM Operators\n" + 
			"xosm_sp:wayIn(p,s), xosm_sp:waySame(p1,p2),\n"
			+ "xosm_sp:intersectionPoints(s1,s2), xosm_sp:crossing(s1,s2),\n"
			+ "xosm_sp:nonCrossing(s1,s2), xosm_sp:touching(s1,s2),\n"
			+ "xosm_sp:nonTouching(s1,s2), xosm_sp:intersecting(s1,s2),\n"
			+ "xosm_sp:nonIntersecting(s1,s2), xosm_sp:containing(s1,s2),\n"
			+ "xosm_sp:nonContaining(s1,s2), xosm_sp:within(s1,s2),\n"
			+ "xosm_sp:nonWithin(s1,s2), xosm_sp:overlapping(s1,s2),\n"
			+ "xosm_sp:nonOverlapping(s1,s2), xosm_sp:disjoint(s1,s2), xosm_sp:nonDisjoint(s1,s2)\n";
	
	String keyword ="XOSM is equipped with a repertoire of Keyword Operators,\n"
			+"suitable for the retrieval of elements in OSM maps by keyword.\n"+
			"Keyword Operators manipulates pairs @k and @v in OSM maps.\n" + 
			"\r\n" + 
			"xosm_kw:searchKeyword(osmElement,keyword)\r\n" + 
			"xosm_kw:searchKeywordSet(osmElement,(keyword1,...., keywordn))\r\n" + 
			"xosm_kw:searchTag(osmElement, kValue, vValue)\r\n" + 
			"xosm_kw:getTag(osmElement, kValue)\r\n";
	
	
	String aggregation ="XOSM is equipped with a repertoire of Aggregation Operators\n"+
			"able to summarize and rank data on OSM maps:\n"+
			"count, min, max, average, median, mode, etc.\r\n" + 
			"\r\n" + 
			"Distributive Operators\r\n" + 
			"xosm_ag:topologicalCount(osmElements,osmElement,topologicalRelation)\r\n" + 
			"xosm_ag:metricMin(osmElements,metricOperator)\r\n" + 
			"xosm_ag:metricMax(osmElements,metricOperator)\r\n" + 
			"xosm_ag:metricSum(osmElements,metricOperator)\r\n" + 
			"xosm_ag:minDistance(osmElements, osmElement)\r\n" + 
			"xosm_ag:maxDistance(osmElements, osmElement)\r\n" + 
			"Algebraic Operators\r\n" + 
			"xosm_ag:metricAvg(osmElements,metricOperator)\r\n" + 
			"xosm_ag:avgDistance(osmElements, osmElement)\r\n" + 
			"xosm_ag:metricStdev(osmElements,metricOperator)\r\n" + 
			"xosm_ag:metricTopCount(osmElements,metricOperator,k)\r\n" + 
			"xosm_ag:metricBottomCount(osmElements,metricOperator,k)\r\n" + 
			"xosm_ag:topCountDistance(osmElements, k, osmElement)\r\n" + 
			"xosm_ag:bottomCountDistance(osmelement, k, osmElement)\r\n" + 
			"Holistic Operators\r\n" + 
			"xosm_ag:metricMedian(osmElements,metricOperator)\r\n" + 
			"xosm_ag:metricMode(osmElements,metricOperator)\r\n" + 
			"xosm_ag:metricRank(osmElements,metricOperator)\r\n" + 
			"xosm_ag:metricRange(osmElements,metricOperator)\r\n";
			
	String lod = "GeoJSON, KML, CSV, XML (tixik.com)\n"+
			"and RDF (dbpedia) documents can be retrieved\n"+
			"by XOSM and transformed into OSM.\n"+
			"The following set of functions is available.\r\n" + 
			"\r\n" + 
			"xosm_open:json(url,labelname,kn,vn,kw,vw)\r\n" + 
			"xosm_open:kml(url,labelname,kn,vn,kw,vm)\r\n" + 
			"xosm_open:csv(url,columnname,longitude,latitude)\r\n" + 
			"xosm_open:wiki_element(osmElement)\r\n" + 
			"xosm_open:wiki_coordinates(longitude,latitude)\r\n" + 
			"xosm_open:wiki_name(map,name)\r\n" + 
			"xosm_open:tixik_element(osmElement)\r\n" + 
			"xosm_open:tixik_coordinates(longitude,latitude)\r\n" + 
			"xosm_open:tixik_name(map,name)\r\n";
	
	
	String api ="\r\n" + 
			"XOSM REST API for OSM\r\n" + 
			"XOSM enables to use a REST API for querying OSM maps.\n"+
			"The API can be used for the retrieval of layers by distance,\n"+
			"keyword and bounding box, as well as to execute queries on OSM layers.\n"+
			"Coordinates of a bounding box have to be provided.\r\n" + 
			"\r\n" + 
			"GetLayerByName\r\n" + 
			"http://xosm.ual.es/xosmapi/getLayerByName/minLon/{minLon}/minLat/{minLat}\n"+
			"/maxLon/{maxLon}/maxLat/{maxLat}/name/{name}/distance/{distance}\r\n" + 
			"GetLayerByElement\r\n" + 
			"http://xosm.ual.es/xosmapi/getLayerByElement/minLon/{minLon}/minLat/{minLat}\n"+
			"/maxLon/{maxLon}/maxLat/{maxLat}/lon/{lon}/lat/{lat}/distance/{distance}\r\n" + 
			"GetElementByName\r\n" + 
			"http://xosm.ual.es/xosmapi/getElementByName/minLon/{minLon}/minLat/{minLat}\n"+
			"/maxLon/{maxLon}/maxLat/{maxLat}/name/{name}\r\n" + 
			"GetElementsByKeyword\r\n" + 
			"http://xosm.ual.es/xosmapi/getElementsByKeyword/minLon/{minLon}/minLat/{minLat}\n"+
			"/maxLon/{maxLon}/maxLat/{maxLat}/keyword/{keyword}\r\n" + 
			"GetLayerByBB\r\n" + 
			"http://xosm.ual.es/xosmapi/getLayerByBB/minLon/{minLon}/minLat/{minLat}\n"+
			"/maxLon/{maxLon}/maxLat/{maxLat}\r\n" + 
			"Query\r\n" + 
			"http://xosm.ual.es/xosmapi/XOSMQuery/minLon/{minLon}/minLat/{minLat}\n"+
			"/maxLon/{maxLon}/maxLat/{maxLat}?query={query}\r\n";
			

	Help_Tool info_tool = new Help_Tool(indexing,spatial,keyword,aggregation,lod,api);

	LMap map = new LMap();
	
	String LayerAnswer = "Answer";

	NodeList nodes = null;
	
	NodeList way = null;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
	
		 

		LTileLayer osmTiles = new LOpenStreetMapLayer();
		osmTiles.setAttributionString("© OpenStreetMap Contributors");
		map.addBaseLayer(osmTiles, "OSM");
		map.setZoomLevel(17);
		 

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
		 map.setCenter(36.83645,-2.45516);
		 
		 q = new Query(this,"xosm_pbd:getElementsByKeyword(.,\"bar\")");

		 Window examples_pbd = new Window();
		//Window examples_rld = new Window();

		Button ex1 = new Button("Retrieve the streets in London intersecting Haymarket street and touching Pall Mall");
		ex1.setWidth("100%");
		ex1.setStyleName(ValoTheme.BUTTON_LINK); //.BUTTON_LINK);

		Button ex2 = new Button("Retrieve the restaurants in Roma further north to Miami hotel");
		ex2.setWidth("100%");
		ex2.setStyleName(ValoTheme.BUTTON_LINK);

		Button ex3 = new Button("Retrieve hotels of Vienna close to food venues (food venues = number of bars "
				+ "and restaurants bigger than 10)");
		ex3.setWidth("100%");
		ex3.setStyleName(ValoTheme.BUTTON_LINK);

		Button ex4 = new Button("Retrieve the hotels of Munich with the greatest number of churches nearby");
		ex4.setWidth("100%");
		ex4.setStyleName(ValoTheme.BUTTON_LINK);

		Button ex5 = new Button("Retrieve the size of buidings close to Karl-Liebknecht-Straße in Berlin");
		ex5.setWidth("100%");
		ex5.setStyleName(ValoTheme.BUTTON_LINK);

		Button ex6 = new Button("Retrieve the top-star rating biggest hotels close to Via Dante in Milan");
		ex6.setWidth("100%");
		ex6.setStyleName(ValoTheme.BUTTON_LINK);

		Button ex7 = new Button("Request taxi stops close to Bruxelles Central Station in Bruxelles");
		ex7.setWidth("100%");
		ex7.setStyleName(ValoTheme.BUTTON_LINK);

		Button ex8 = new Button("Retrieves free events of Madrid");
		ex8.setWidth("100%");
		ex8.setStyleName(ValoTheme.BUTTON_LINK);

		Button ex9 = new Button("Retrieve Wikipedia information about places nearby to the intersection "
				+ "point of Calle Mayor and Calle de Esparteros in Madrid");
		ex9.setWidth("100%");
		ex9.setStyleName(ValoTheme.BUTTON_LINK);

		Button ex10 = new Button("Retrieves the information provided by tixik.com around Picadilly in London");
		ex10.setWidth("100%");
		ex10.setStyleName(ValoTheme.BUTTON_LINK);

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

		examples_pbd.setContent(hl);
		
		
		
		//nelat = map.getBounds().getNorthEastLat();
		//nelon = map.getBounds().getNorthEastLon();
		//swlat = map.getBounds().getSouthWestLat();
		//swlon = map.getBounds().getSouthWestLon();
		
		

		String q1 =
				"let $layer := xosm_pbd:getLayerByName(. ,'Haymarket',0)\r\n"
						+ "let $s := xosm_pbd:getElementByName(. ,'Haymarket')\r\n"
						+ "let $ts := xosm_pbd:getElementByName(. ,'Pall Mall')\r\n"
						+ "return fn:filter(fn:filter($layer,xosm_sp:intersecting(?,$s)),xosm_sp:touching(?,$ts))";

		String q2 =
				"let $layer := xosm_pbd:getLayerByBB(.)\r\n"
						+ "let $hotel := xosm_pbd:getElementByName(. ,'Hotel Miami')\r\n"
						+ "return fn:filter(fn:filter($layer,xosm_kw:searchKeyword(?,'restaurant')),\r\n"
						+ "xosm_sp:furtherNorthPoints($hotel ,?)) ";

		String q3 =
				"for $hotel in xosm_pbd:getElementsByKeyword(. ,'hotel')[@type='point' or @type='area']\r\n"
						+ "let $layer := xosm_pbd:getLayerByElement(. , $hotel ,200)\r\n"
						+ "where count(fn:filter($layer , xosm_kw:searchKeywordSet(?, ('bar','restaurant')))) >= 10\r\n"
						+ "return $hotel";

		String q4 =
				"let $hotel := xosm_pbd:getElementsByKeyword(. ,'hotel')\r\n"
				+ "let $f := function($hotel){ -(count(fn:filter(xosm_pbd:getLayerByElement(. , $hotel ,100),\r\n"
				+ "xosm_kw:searchKeyword(?,'church'))))}\r\n" + "return fn:sort($hotel ,$f)[1]";

		String q5 =  
				"let $layer := xosm_pbd:getLayerByName(. ,'Karl-Liebknecht-Straße' ,100)\r\n"
						+ "let $buildings := fn:filter($layer ,xosm_kw:searchKeyword (?,'building'))\r\n"
						+ "return xosm_ag:metricSum($buildings ,'area')";
		String q6 = 
				"let $layer := xosm_pbd:getLayerByName(. ,'Via Dante' ,350)\r\n"
						+ "let $hotels := fn:filter($layer,xosm_kw:searchKeyword(?,'hotel'))\r\n"
						+ "return xosm_ag:metricMax(xosm_ag:metricMax($hotels ,'stars'), 'area')";
		String q7 = 
				"let $taxis := "
				+ "xosm_open:json('https://opendata.bruxelles.be/explore/dataset/test-geojson-station-de-taxi/download/?format=geojson&amp;timezone=UTC',"
				+ "'','amenity','taxi','highway','*')\r\n"
				+ "let $building := xosm_pbd:getElementByName (. ,'Bruxelles-Central - Brussel-Centraal') \r\n"
				+ "return fn:filter($taxis,xosm_sp:DWithIn($building,?,500))";
		String q8 = 
				"let $events := "
				+ "xosm_open:json('http://data2.esrism.opendata.arcgis.com/datasets/51900577e33a4ba4ab59a691247aeee9_0.geojson','EVENTO','place','*','area','yes') \r\n"
				+ "return fn:filter($events ,function($p) {not(empty($p/node/tag[@k='GRATUITO' and @v='Sí']))})";
		String q9 = 
				"let $x := xosm_pbd:getElementByName(. ,'Calle Mayor')\r\n"
				+ "let $y := xosm_pbd:getElementByName(. ,'Calle de Esparteros')\r\n"
				+ "return for $i in xosm_sp:intersectionPoints($x,$y)\r\n" + "return xosm_open:wiki_element($i) ";
		String q10 =  
				"let $x := xosm_pbd:getElementByName(. ,'Piccadilly')\r\n" + "return xosm_open:tixik_element($x) ";

		/*Button rex1 = new Button(
				"Retrieve the streets in London intersecting Haymarket street and touching Trafalgar Square");
		rex1.setWidth("100%");
		rex1.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex2 = new Button("Retrieve the restaurants in Roma further north to Miami Hotel");
		rex2.setWidth("100%");
		rex2.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex3 = new Button("Retrieve hotels of Vienna close to food venues (food venues = number of bars "
				+ "and restaurants bigger than 10)");
		rex3.setWidth("100%");
		rex3.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex4 = new Button("Retrieve the hotels of Munich with the greatest number of churches nearby");
		rex4.setWidth("100%");
		rex4.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex5 = new Button("Retrieve the size of buildings close to Karl-Liebknecht-Straße in Berlin");
		rex5.setWidth("100%");
		rex5.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex6 = new Button("Retrieve the top-star rating biggest hotels close to Via Dante in Milan");
		rex6.setWidth("100%");
		rex6.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex7 = new Button("Request taxi stops close to Bruxelles Central Station in Bruxelles");
		rex7.setWidth("100%");
		rex7.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex8 = new Button("Retrieves free events of Madrid");
		rex8.setWidth("100%");
		rex8.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex9 = new Button("Retrieve Wikipedia information about places nearby to the intersection "
				+ "point of Calle Mayor and Calle de Esparteros in Madrid");
		rex9.setSizeUndefined();
		rex9.setStyleName(ValoTheme.BUTTON_LINK);

		Button rex10 = new Button("Retrieves the information provided by tixik.com around Picadilly in London");
		rex10.setWidth("100%");
		rex10.setStyleName(ValoTheme.BUTTON_LINK);

		VerticalLayout hl2 = new VerticalLayout();

		hl2.setSizeUndefined();
		hl2.addComponent(rex1);
		hl2.addComponent(rex2);
		hl2.addComponent(rex3);
		hl2.addComponent(rex4);
		hl2.addComponent(rex5);
		hl2.addComponent(rex6);
		hl2.addComponent(rex7);
		hl2.addComponent(rex8);
		hl2.addComponent(rex9);
		hl2.addComponent(rex10);

		examples_rld.setContent(hl2);

		Query rq1 = new Query(this_,
				"let $layer := xosm_rld:getLayerByName(. ,'Haymarket',0)\r\n"
						+ "let $s := xosm_rld:getElementByName(. ,'Haymarket')\r\n"
						+ "let $ts := xosm_rld:getElementByName(. ,'Pall Mall')\r\n"
						+ "return fn:filter(fn:filter($layer,xosm_sp:intersecting(?,$s)),xosm_sp:touching(?,$ts))");

		Query rq2 = new Query(this,
				"let $layer := xosm_rld:getLayerByBB(.)\r\n"
						+ "let $hotel := xosm_rld:getElementByName(. ,'Hotel Miami')\r\n"
						+ "return fn:filter(fn:filter($layer,xosm_kw:searchKeyword(?,'restaurant')),\r\n"
						+ "xosm_sp:furtherNorthPoints($hotel ,?)) ");

		Query rq3 = new Query(this,
				"for $hotel in xosm_rld:getElementsByKeyword(. ,'hotel')[@type='point' or @type='area']\r\n"
						+ "let $layer := xosm_rld:getLayerByElement(. , $hotel ,200)\r\n"
						+ "where count(fn:filter($layer , xosm_kw:searchKeywordSet(?, ('bar','restaurant')))) >= 10\r\n"
						+ "return $hotel");

		Query rq4 = new Query(this, "let $hotel := xosm_rld:getElementsByKeyword(. ,'hotel')\r\n"
				+ "let $f := function($hotel){ -(count(fn:filter(xosm_rld:getLayerByElement(. , $hotel ,100),\r\n"
				+ "xosm_kw:searchKeyword(?,'church'))))}\r\n" + "return fn:sort($hotel ,$f)[1]");

		Query rq5 = new Query(this,
				"let $layer := xosm_rld:getLayerByName(. ,'Karl-Liebknecht-Straße' ,100)\r\n"
						+ "let $buildings := fn:filter($layer ,xosm_kw:searchKeyword (?,'building'))\r\n"
						+ "return xosm_ag:metricSum($buildings ,'area')");
		Query rq6 = new Query(this,
				"let $layer := xosm_rld:getLayerByName(. ,'Via Dante' ,350)\r\n"
						+ "let $hotels := fn:filter($layer,xosm_kw:searchKeyword(?,'hotel'))\r\n"
						+ "return xosm_ag:metricMax(xosm_ag:metricMax($hotels ,'stars'), 'area')");
		Query rq7 = new Query(this, "let $taxis := "
				+ "xosm_open:json('https://opendata.bruxelles.be/explore/dataset/test-geojson-station-de-taxi/download/?format=geojson&amp;timezone=UTC',"
				+ "'','amenity','taxi','highway','*')\r\n"
				+ "let $building := xosm_rld:getElementByName (. ,'Bruxelles-Central - Brussel-Centraal') \r\n"
				+ "return fn:filter($taxis,xosm_sp:DWithIn($building,?,500))");
		Query rq8 = new Query(this, "let $events := "
				+ "xosm_open:json('http://data2.esrism.opendata.arcgis.com/datasets/51900577e33a4ba4ab59a691247aeee9_0.geojson','EVENTO','place','*','area','yes') \r\n"
				+ "return fn:filter($events ,function($p) {not(empty($p/node/tag[@k='GRATUITO' and @v='Sí']))})");
		Query rq9 = new Query(this, "let $x := xosm_rld:getElementByName(. ,'Calle Mayor')\r\n"
				+ "let $y := xosm_rld:getElementByName(. ,'Calle de Esparteros')\r\n"
				+ "return for $i in xosm_sp:intersectionPoints($x,$y)\r\n" + "return xosm_open:wiki_element($i) ");
		Query rq10 = new Query(this,
				"let $x := xosm_rld:getElementByName(. ,'Piccadilly')\r\n" + "return xosm_open:tixik_element($x) ");
		*/

		ex1.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(51.50884, -0.13201);
				
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

				map.setCenter(41.90219, 12.49580);

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

				map.setCenter(48.20817, 16.37382);

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

				map.setCenter(48.13513, 11.58198);

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

				map.setCenter(52.52250, 13.40952);

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

				map.setCenter(45.465820637638, 9.1893592282028);

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

				map.setCenter(50.8445, 4.3537);

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

				map.setCenter(40.42, -3.68);
				

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

				map.setCenter(40.4164, -3.70501);

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

				map.setCenter(51.50308, -0.15207);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				q.setQuery(q10);

				removeWindow(examples_pbd);
			}

		});
		
		/*

		rex1.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(51.50884, -0.13201);

				//rq1.setHeight("800px");
				//rq1.setWidth("100%");
				rq1.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq1.set_events();
				//addWindow(rq1);
				accordion.addComponent(rq1);

				removeWindow(examples_rld);
			}

		});

		rex2.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(41.90219, 12.49580);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq2.setHeight("800px");
				//rq2.setWidth("100%");
				rq2.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq2.set_events();
				//addWindow(rq2);
				accordion.addComponent(rq2);

				removeWindow(examples_rld);
			}

		});

		rex3.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(48.20817, 16.37382);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq3.setHeight("800px");
				//rq3.setWidth("100%");
				rq3.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq3.set_events();
				//addWindow(rq3);
				accordion.addComponent(rq3);

				removeWindow(examples_rld);
			}

		});

		rex4.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(48.13513, 11.58198);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq4.setHeight("800px");
				//rq4.setWidth("100%");
				rq4.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq4.set_events();
				//addWindow(rq4);
				accordion.addComponent(rq4);

				removeWindow(examples_rld);
			}

		});

		rex5.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(52.52250, 13.40952);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq5.setHeight("800px");
				//rq5.setWidth("100%");
				rq5.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq5.set_events();
				//addWindow(rq5);
				accordion.addComponent(rq5);

				removeWindow(examples_rld);
			}

		});

		rex6.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(45.465820637638, 9.1893592282028);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq6.setHeight("800px");
				//rq6.setWidth("100%");
				rq6.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq6.set_events();
				//addWindow(rq6);
				accordion.addComponent(rq6);

				removeWindow(examples_rld);
			}

		});

		rex7.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(50.8445, 4.3537);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq7.setHeight("800px");
				//rq7.setWidth("100%");
				rq7.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq7.set_events();
				//addWindow(rq7);
				accordion.addComponent(rq7);

				removeWindow(examples_rld);
			}

		});

		rex8.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(40.42, -3.68);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq8.setWidth("100%");
				// rq8.set_events();
				rq8.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				//rq8.setHeight("800px");
				
				//addWindow(rq8);
				accordion.addComponent(rq8);

				removeWindow(examples_rld);
			}

		});

		rex9.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(40.4164, -3.70501);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq9.setHeight("800px");
				//rq9.setWidth("100%");
				rq9.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq9.set_events();
				//addWindow(rq9);
				accordion.addComponent(rq9);

				removeWindow(examples_rld);
			}

		});

		rex10.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				map.setCenter(51.50308, -0.15207);

				nelat = map.getBounds().getNorthEastLat();
				nelon = map.getBounds().getNorthEastLon();
				swlat = map.getBounds().getSouthWestLat();
				swlon = map.getBounds().getSouthWestLon();

				//rq10.setHeight("800px");
				//rq10.setWidth("100%");
				rq10.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);

				// rq10.set_events();
				//addWindow(rq10);
				accordion.addComponent(rq10);

				removeWindow(examples_rld);
			}

		});
		*/
		
		/*
		PopupButton examplesRLD = new PopupButton("Examples of Real Live Data");
		examplesRLD.setStyleName(ValoTheme.BUTTON_LINK);
		examplesRLD.setWidth("100%");
		examplesRLD.setIcon(VaadinIcons.ARCHIVE);
		*/
		
		Button searchb  = new Button();
		searchb.setIcon(VaadinIcons.SEARCH);
		searchb.setWidth("100%");
		
		TextField searchtf = new TextField();
		searchtf.setWidth("100%");
		
		
		
		PopupButton examplesPBD = new PopupButton("Examples");
		//examplesPBD.setStyleName(ValoTheme.BUTTON_PRIMARY);
		examplesPBD.setWidth("100%");
		examplesPBD.setIcon(VaadinIcons.ARCHIVE);
	
		Button restart = new Button("Clear Layers");
		//restart.setStyleName(ValoTheme.BUTTON_DANGER);
		restart.setIcon(VaadinIcons.REFRESH);
		restart.setWidth("100%");
		
		Button help = new Button("XOSM Help");
		//help.setStyleName(ValoTheme.BUTTON_DANGER);
		help.setWidth("100%");
		help.setIcon(VaadinIcons.QUESTION);
		
		Button info = new Button("XOSM Info");
		//info.setStyleName(ValoTheme.BUTTON_DANGER);
		info.setWidth("100%");
		info.setIcon(VaadinIcons.INFO);

		HorizontalLayout buttons = new HorizontalLayout();
		
		 
		searchb.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				if (searchtf.getValue()=="") {Notification.show("Address cannot be empty");}
				else 
					
				{
					XPath xPath = XPathFactory.newInstance().newXPath();
				    String result = search(searchtf.getValue());
				    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				    
				    DocumentBuilder builder = null;
					try {
						builder = builderFactory.newDocumentBuilder();
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Document xmlDocument = null;

					try {
						xmlDocument = builder.parse(new InputSource(new StringReader(result)));
					} catch (SAXException | IOException e) {
						// TODO Auto-generated catch block
						 
						e.printStackTrace();
					}
				    
					if (xmlDocument==null) {}
					else
					{
					try {
						nodes = (NodeList) xPath.compile("/searchresults/place").evaluate(xmlDocument, XPathConstants.NODESET);
						
						NodeList test = (NodeList) xPath.compile("/searchresults/place/@lat").evaluate(xmlDocument, XPathConstants.NODESET);
						
						if (test.getLength()==0) { 	Notification.show(
								"The address cannot be found");}
						else {
						 Node lat = nodes.item(0).getAttributes().getNamedItem("lat");
						 Node lon = nodes.item(0).getAttributes().getNamedItem("lon");
						 map.setCenter(Float.parseFloat(lat.getNodeValue()),Float.parseFloat(lon.getNodeValue()));
							map.setZoomLevel(17);
						}
						
					} catch (XPathExpressionException e) {
						// TODO Auto-generated catch block

						e.printStackTrace();
					}
				}
				}
			}

		});

		examplesPBD.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				addWindow(examples_pbd);

			}

		});

		restart.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				map.setZoomLevel(17);
				 map.setCenter(36.83645,-2.45516);
				 
				map.removeAllComponents();
				LTileLayer osmTiles = new LOpenStreetMapLayer();
				osmTiles.setAttributionString("© OpenStreetMap Contributors");
				map.addBaseLayer(osmTiles, "OSM");

			}

		});

		help.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				info_tool.setWidth("40%");
				addWindow(info_tool);

			}

		});

		info.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				accordion2.setSplitPosition(100);
				accordion.setSplitPosition(35);
				info_team.setSizeUndefined();
				addWindow(info_team);
			}

		});

		 
		//buttons.addComponent(examplesRLD);
		
		HorizontalLayout all = new HorizontalLayout();
		all.setWidth("100%");
		
		HorizontalLayout search = new HorizontalLayout();
		
		search.addComponent(searchtf);
		search.addComponent(searchb);
		search.setWidth("100%");
		
		 search.setExpandRatio(searchtf, 2.5f);
		 search.setExpandRatio(searchb, 0.5f);
		
		
		buttons.addComponent(examplesPBD);
		buttons.addComponent(restart);
		buttons.addComponent(help);
		buttons.addComponent(info);
		buttons.setWidth("100%");
		
		all.addComponent(search);
		all.addComponent(buttons);

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
	
//https://nominatim.openstreetmap.org/?addressdetails=1&q=bakery+in+berlin+wedding&format=xml&limit=1
	
	public String search(String address) {
		String xml = "";

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("q", address));
			params.add(new BasicNameValuePair("addressdetails", "1"));
			params.add(new BasicNameValuePair("format", "xml"));
			params.add(new BasicNameValuePair("limit", "1"));
			

			HttpGet request = new HttpGet("https://nominatim.openstreetmap.org/" + "?" + URLEncodedUtils.format(params, "utf-8"));

			request.addHeader("content-type", "application/xml");

			HttpResponse result = httpClient.execute(request);

			xml = EntityUtils.toString(result.getEntity(), "UTF-8");

		} catch (IOException ex) {
		}
		return xml;
	}
	
	
	public void Draw_xml(Draw d, XPath xPath, LMap map, Document xmlDocument,String nccolor,String ccolor,String cfill,String icon) {

		LLayerGroup lanswer = new LLayerGroup();

		Draw_xml_nodes(lanswer, d, xPath, map, xmlDocument,icon);
		Draw_xml_ways(lanswer, d, xPath, map, xmlDocument,nccolor,ccolor,cfill);
		map.addOverlay(lanswer, LayerAnswer);
	     

	}
	
	

	public void Draw_xml_nodes(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument,String Icon) {

		if (xmlDocument==null) {}
		else
		{
		try {
			nodes = (NodeList) xPath.compile("/osm/node[tag]").evaluate(xmlDocument, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}

		if (nodes == null) {
		} else
			for (int i = 0; i < nodes.getLength(); i++) {

				if ((!(nodes.item(i).getAttributes().getNamedItem("lat").getNodeValue() == ""))
						& (!(nodes.item(i).getAttributes().getNamedItem("lon").getNodeValue() == ""))) {

					LMarker leafletMarker = d.Draw_Node(
							Double.parseDouble(nodes.item(i).getAttributes().getNamedItem("lat").getNodeValue()),
							Double.parseDouble(nodes.item(i).getAttributes().getNamedItem("lon").getNodeValue()),
							"Point", "Point", "xosm_address.png");

					leafletMarker.setIcon(new ThemeResource(Icon));

					lanswer.addComponent(leafletMarker); //

					int j = i;

					leafletMarker.addClickListener(new LeafletClickListener() {

						@Override
						public void onClick(LeafletClickEvent event) {

							//Info_Node in = new Info_Node(nodes.item(j));
							
							in.setInfo(nodes.item(j));
							accordion2.setSplitPosition(65);
							accordion.setSplitPosition(0);

							//in.setSizeUndefined();
							//in.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);
							//addWindow(in);
						}
					});
				}

			}
	}
	}
	

	public void Draw_xml_ways(LLayerGroup lanswer, Draw d, XPath xPath, LMap map, Document xmlDocument,String nccolor, String ccolor, String cfill) {

		if (xmlDocument==null) {}
		else
		{	
		try {
			way = (NodeList) xPath.compile("/osm/way").evaluate(xmlDocument, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (way == null) {
		} else

			for (int i = 0; i < way.getLength(); i++) {

				NodeList children = way.item(i).getChildNodes();

				List<Point> p = new ArrayList<Point>();

				NodeList lat2 = null;
				NodeList lon2 = null;
				for (int j = 0; j < children.getLength(); j++) {
					if (children.item(j).getNodeName() == "nd") {

						try {
							lat2 = (NodeList) xPath.compile("/osm/node[@id="
									+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue() + "]/@lat")
									.evaluate(xmlDocument, XPathConstants.NODESET);
						} catch (XPathExpressionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						try {
							lon2 = (NodeList) xPath.compile("/osm/node[@id="
									+ children.item(j).getAttributes().getNamedItem("ref").getNodeValue() + "]/@lon")
									.evaluate(xmlDocument, XPathConstants.NODESET);
						} catch (XPathExpressionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						p.add(new Point(Double.parseDouble(lat2.item(0).getNodeValue()),
								Double.parseDouble(lon2.item(0).getNodeValue())));

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

							//Info_Node in = new Info_Node(way.item(j));
							
							in.setInfo(way.item(j));
							accordion2.setSplitPosition(65);
							accordion.setSplitPosition(0);

							//in.setSizeUndefined();
							//in.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);
							//addWindow(in);
						}
					});
				} else {
					LPolyline pl = d.Draw_Polyline(nccolor, points);

					lanswer.addComponent(pl); //

					int j = i;

					pl.addClickListener(new LeafletClickListener() {

						@Override
						public void onClick(LeafletClickEvent event) {

							//Info_Node in = new Info_Node(way.item(j));

							//in.setSizeUndefined();
							//in.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);
							//addWindow(in);
							
							in.setInfo(way.item(j));
							accordion2.setSplitPosition(65);
							accordion.setSplitPosition(0);
						}
					});
				}

			}
		
	}

	}

	public String api(Double minLon, Double minLat, Double maxLon, Double maxLat, String query) {
		String xml = "";

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("query", query));

			HttpGet request = new HttpGet("http://xosm.ual.es/xosmapi/XOSMQuery/minLon/" + minLon + "/minLat/" + minLat
					+ "/maxLon/" + maxLon + "/maxLat/" + maxLat + "?" + URLEncodedUtils.format(params, "utf-8"));

			request.addHeader("content-type", "application/xml");

			HttpResponse result = httpClient.execute(request);

			xml = EntityUtils.toString(result.getEntity(), "UTF-8");

		} catch (IOException ex) {
		}
		return xml;
	}
	
	public void api_post(String xml,String layer) {
		 

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("osm", xml));

			HttpPost request = new HttpPost("http://xosm.ual.es/xosmapi/XOSMLayer/name/"+layer+"?" + URLEncodedUtils.format(params, "utf-8"));
 
			HttpResponse result = httpClient.execute(request);

		} catch (IOException ex) {
		}
		 
	}

	@WebServlet(urlPatterns = "/*", name = "XOSM2Servlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = XOSM2.class, productionMode = false)
	public static class XOSM2Servlet extends VaadinServlet {
	}
}

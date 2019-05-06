package xosm2.xosm2;

import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LPolygon;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.shared.Point;

public class Draw {

	public LMarker Draw_Node(double x, double y) {

		LMarker leafletMarker = new LMarker(x, y);
		
		

		return leafletMarker;

	}

	public LPolyline Draw_Polyline(String color, Point... lp) {

		LPolyline pl = new LPolyline(lp);

		pl.setColor(color);
		pl.setStroke(true);
		pl.setDashArray("3,5");

		return pl;

	}

	public LPolygon Draw_Polygon(String border, String fill, Point... lp) {

		LPolygon pl = new LPolygon(lp);

		pl.setColor(border);
		pl.setFill(true);
		pl.setFillColor(fill);
		pl.setStroke(true);
		pl.setDashArray("9");

		return pl;

	}

}

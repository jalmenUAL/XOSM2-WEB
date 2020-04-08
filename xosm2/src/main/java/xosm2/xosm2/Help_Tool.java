package xosm2.xosm2;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class Help_Tool extends Window {

	
	Help_Tool(String Index, String Data, String Spatial, String Keyword, String Aggregation, String LOD, String social, String API){
		super();
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeUndefined();
		Label i = new Label(Index,ContentMode.PREFORMATTED);
		Label d = new Label(Data,ContentMode.PREFORMATTED);
		Label s = new Label(Spatial,ContentMode.PREFORMATTED);
		Label k = new Label(Keyword,ContentMode.PREFORMATTED);
		Label a = new Label(Aggregation,ContentMode.PREFORMATTED);
		Label lo = new Label(LOD,ContentMode.PREFORMATTED);
		Label so = new Label(social,ContentMode.PREFORMATTED);
		Label api = new Label(API,ContentMode.PREFORMATTED);
		i.setStyleName(ValoTheme.LABEL_BOLD);
		d.setStyleName(ValoTheme.LABEL_BOLD);
		s.setStyleName(ValoTheme.LABEL_BOLD);
		k.setStyleName(ValoTheme.LABEL_BOLD);
		a.setStyleName(ValoTheme.LABEL_BOLD);
		lo.setStyleName(ValoTheme.LABEL_BOLD);
		so.setStyleName(ValoTheme.LABEL_BOLD);
		api.setStyleName(ValoTheme.LABEL_BOLD);
		TabSheet tabsheet = new TabSheet();
		tabsheet.setSizeUndefined();
		tabsheet.addTab(i, "INDEX");
		tabsheet.addTab(d, "OSM");
		tabsheet.addTab(s, "SPATIAL");
		tabsheet.addTab(k, "KEYWORD");
		tabsheet.addTab(a, "AGGREGATION");
		tabsheet.addTab(lo, "LINKED OPEN DATA");
		tabsheet.addTab(lo, "SOCIAL NETWORKS");
		tabsheet.addTab(api, "API restful");
		vl.addComponent(tabsheet);
		setContent(vl);
		}
}

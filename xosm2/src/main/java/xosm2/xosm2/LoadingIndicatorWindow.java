package xosm2.xosm2;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class LoadingIndicatorWindow extends Window {

public LoadingIndicatorWindow(String text) {
	
	 
    center();
    setVisible(true);
    setResizable(false);
    setDraggable(false);
    setModal(true);
    setClosable(false);
    setCaption("Loading...");
     
    VerticalLayout layout = new VerticalLayout();
    layout.setMargin(true);
    layout.setWidth("100%");

    Label progressLabel = new Label(text);
    progressLabel.setSizeFull();
    progressLabel.setStyleName(ValoTheme.LABEL_LARGE);

    ProgressBar progressBar = new ProgressBar();
    progressBar.setSizeFull();
    progressBar.setIndeterminate(true);
    progressBar.setVisible(true);
    progressBar.setStyleName(ValoTheme.PROGRESSBAR_POINT);
    

    layout.addComponent(progressLabel);
    layout.addComponent(progressBar);

    layout.setComponentAlignment(progressLabel, Alignment.MIDDLE_CENTER);
    layout.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);
    setContent(layout);
	}

}
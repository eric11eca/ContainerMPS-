package com.chen.eric.ui.views;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Date;
import java.util.List;

import com.chen.eric.backend.Container;
import com.chen.eric.backend.Vessel;
import com.chen.eric.backend.service.DataContainer;
import com.chen.eric.ui.MainLayout;
import com.chen.eric.ui.components.FlexBoxLayout;
import com.chen.eric.ui.components.ListItem;
import com.chen.eric.ui.components.detailsdrawer.DetailsDrawer;
import com.chen.eric.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.chen.eric.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.chen.eric.ui.layout.size.Bottom;
import com.chen.eric.ui.layout.size.Horizontal;
import com.chen.eric.ui.layout.size.Top;
import com.chen.eric.ui.util.LumoStyles;
import com.chen.eric.ui.util.UIUtils;
import com.chen.eric.ui.util.css.BoxSizing;
import com.chen.eric.ui.util.css.WhiteSpace;
import com.helger.commons.csv.CSVReader;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.DebouncePhase;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@PageTitle("Container")
@Route(value = "container", layout = MainLayout.class)
public class ContainerView extends SplitViewFrame {

	private Grid<Container> grid;
	private ListDataProvider<Container> dataProvider;
	private DetailsDrawer detailsDrawer;
	private File tempFile;
	private String filter = "";
	private Container tempContainer;
	private Integer containerID;
	
	private DataContainer dataContainer = DataContainer.getInstance();

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		setViewContent(createContent());
		setViewDetails(createDetailsDrawer());
	}

	private Component createContent() {
		FlexBoxLayout content = new FlexBoxLayout(
				new VerticalLayout(createToolBar(), createGrid()));
		content.setBoxSizing(BoxSizing.BORDER_BOX);
		content.setHeightFull();
		content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
		return content;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private HorizontalLayout createToolBar() {
		TextField searchBar = new TextField();
        searchBar.setPlaceholder("Search...");
        searchBar.setWidth("50%");
        searchBar.setValueChangeMode(ValueChangeMode.EAGER);
        searchBar.setPrefixComponent(VaadinIcon.SEARCH.create());
        Icon closeIcon = new Icon("lumo", "cross");
        closeIcon.setVisible(false);
        ComponentUtil.addListener(closeIcon, ClickEvent.class,
                (ComponentEventListener) e -> searchBar.clear());
        searchBar.setSuffixComponent(closeIcon);
        
        Select<String> searchFilter = new Select<>();
        searchFilter.setItems("ID", "isPayed", "Owner", "weight", "volume", "type");
        searchFilter.setLabel("Search Filter");
        searchFilter.addValueChangeListener(e -> filter = e.getValue());

        searchBar.getElement().addEventListener("value-changed", event -> {
            closeIcon.setVisible(!searchBar.getValue().isEmpty());  
            
            if (filter.isEmpty() || searchBar.getValue().isEmpty()) {
            	dataContainer.getContainerRecords();
            } else {
            	dataContainer.getContainerRecordsByParams(filter, searchBar.getValue());
            }
            
	        dataProvider = DataProvider.ofCollection(dataContainer.containerRecords.values());
	        grid.setDataProvider(dataProvider);
        }).debounce(300, DebouncePhase.TRAILING);
        
        HorizontalLayout toolBar = new HorizontalLayout(uploadProducts(), searchFilter, searchBar);
        toolBar.setAlignItems(Alignment.BASELINE);
        toolBar.setSpacing(true);
        toolBar.setPadding(true);
        
        return toolBar;
	}

	private Grid<Container> createGrid() {
		dataContainer.getContainerRecords();
		dataProvider = DataProvider.ofCollection(dataContainer.containerRecords.values());
		grid = new Grid<>();
		grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
		grid.setDataProvider(dataProvider);
		grid.setHeightByRows(true);
		grid.setWidthFull();
		
		grid.addColumn(Container::getContainerID)
				.setAutoWidth(true)
				.setFlexGrow(0)
				.setSortable(true)
				.setHeader("Container ID");
		grid.addColumn(Container::getType)
				.setAutoWidth(true)
				.setFlexGrow(0)
				.setSortable(true)
				.setHeader("Type");
		grid.addColumn(Container::getOwner)
				.setAutoWidth(true)
				.setFlexGrow(0)
				.setSortable(true)
				.setHeader("Owner");
		grid.addColumn(this::createFee)
				.setAutoWidth(true)
				.setFlexGrow(0)
				.setSortable(true)
				.setHeader("Service Fee");
		grid.addComponentColumn(this::createActive)
				.setAutoWidth(true)
				.setFlexGrow(0)
				.setSortable(true)
				.setHeader("Fee Payed");
		grid.addComponentColumn(this::create3DVolume)
				.setAutoWidth(true)
				.setFlexGrow(0)
				.setSortable(true)
				.setHeader("Size")
				.setTextAlign(ColumnTextAlign.END);
		grid.addColumn(this::createWeight)
				.setAutoWidth(true)
				.setFlexGrow(0)
				.setSortable(true)
				.setHeader("Size")
				.setTextAlign(ColumnTextAlign.END);
		grid.addColumn(new ComponentRenderer<>(this::createRemoveButton))
				.setFlexGrow(0).setWidth("130px")
				.setResizable(true)
				.setTextAlign(ColumnTextAlign.CENTER);
		return grid;
	}
	
	private Component createActive(Container container) {
		Icon icon;
		if (container.isPayed()) {
			icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
		} else {
			icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
		}
		return icon;
	}

	private String createFee(Container container) {
		return UIUtils.formatAmount(container.getFee());
	}

	private String createWeight(Container container) {
		return UIUtils.formatAmount(container.getWeight());
	}

	private Component create3DVolume(Container container) {
		NumberField updateLength = new NumberField();
        updateLength.setWidth("20%");
        updateLength.setValue(container.getLength());

        NumberField updateWidth = new NumberField();
        updateWidth.setWidth("20%");
        updateWidth.setValue(container.getWidth());

        NumberField updateHeight = new NumberField();
        updateHeight.setWidth("20%");
        updateHeight.setValue(container.getHeight());

        NumberField showVolume = new NumberField();
        showVolume.setWidth("20%");
        showVolume.setValue(
        		container.getLength() * 
        		container.getWidth() * 
        		container.getHeight());

		HorizontalLayout sizeLayer = new HorizontalLayout(
				updateLength, updateWidth, updateHeight, showVolume);
		sizeLayer.setAlignItems(Alignment.BASELINE);
		return sizeLayer;
	}
	
	private Button createRemoveButton(Container contianer) {
		Button button = new Button(new Icon(VaadinIcon.TRASH), clickEvent -> {
            dataContainer.deleteVesselRecords(contianer.getContainerID());
            dataContainer.getVesselRecords();
    		dataProvider = DataProvider.ofCollection(dataContainer.containerRecords.values());
    		grid.setDataProvider(dataProvider);
        });
        button.setClassName("delete-button");
        button.addThemeName("small");
        return button;
    }

	private DetailsDrawer createDetailsDrawer() {
		detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

		DetailsDrawerHeader detailsDrawerHeader = new DetailsDrawerHeader("Vessel Details");
		detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
		
		DetailsDrawerFooter detailsDrawerFooter = new DetailsDrawerFooter();
		detailsDrawerFooter.addSaveListener(e -> {
			if (tempContainer != null && containerID != null) {
				int code = dataContainer.updateContainerRecords(tempContainer, containerID);
				if (code > 10) {
					dataContainer.getContainerRecords();
					dataProvider = DataProvider.ofCollection(dataContainer.containerRecords.values());
					grid.setDataProvider(dataProvider);
					Notification.show("Succesfully Updated the Data! WITH CODE: " + code, 4000, Notification.Position.BOTTOM_CENTER);
				} else if (code == 1) {
					Notification.show("This Vessel Does Not Exist");
				} else {
					Notification.show("ERROR: UPDATE FAILED!");
				}
			}
		});
			
		detailsDrawer.setHeader(detailsDrawerHeader);
		detailsDrawer.setFooter(detailsDrawerFooter);

		return detailsDrawer;
	}

	private void showDetails(Container container) {
		tempContainer = new Container();
		containerID = container.getContainerID();
		detailsDrawer.setContent(createDetails(container));
		detailsDrawer.show();
	}

	private Component createDetails(Container container) {
		TextField updateID = new TextField();
		updateID.setValue(String.valueOf(container.getContainerID()));
		updateID.setWidth("50%");
		updateID.addValueChangeListener(e-> {
			tempContainer.setContainerID(Integer.valueOf(e.getValue()));
		});
		
		TextField updateOwner = new TextField();
		updateOwner.setWidth("50%");
		updateOwner.setValue(String.valueOf(container.getOwner()));
		updateOwner.addValueChangeListener(e-> {
			tempContainer.setOwner(e.getValue());
		});
		
		Select<String> typePicker = new Select<>();
		typePicker.setItems("Normal", "Reefer", "Hazard", "Illegal");
		typePicker.setValue(container.getType());
		typePicker.setWidth("30%");
		typePicker.addValueChangeListener(
        		e -> tempContainer.setType(e.getValue()));
        
		NumberField updateWeight = new NumberField();
        updateWeight.setWidth("50%");
        updateWeight.setValue(container.getWeight());
        updateWeight.addValueChangeListener(e-> {
        	tempContainer.setWeight(e.getValue());
		});
		
		NumberField updateFee = new NumberField();
		updateFee.setWidth("30%");
		updateFee.setValue(container.getFee());
		updateFee.addValueChangeListener(e-> {
			tempContainer.setFee(e.getValue());
		});
        
        Select<String> payedPicker = new Select<>();
        payedPicker.setItems("Payed", "Not Payed");
        if (container.isPayed()) {
        	payedPicker.setValue("Payed");
        } else {
        	payedPicker.setValue("Not Payed");
        }
        payedPicker.setWidth("30%");
        payedPicker.addValueChangeListener(e ->{
        	if (e.getValue().equals("Payed")) {
        		tempContainer.setPayed(true);
        	} else {
        		tempContainer.setPayed(false);
        	}
        });
        
        NumberField updateLength = new NumberField();
        updateLength.setWidth("20%");
        updateLength.setValue(container.getLength());
        updateLength.addValueChangeListener(e-> {
        	tempContainer.setLength(e.getValue());
		});
        NumberField updateWidth = new NumberField();
        updateWidth.setWidth("20%");
        updateWidth.setValue(container.getWidth());
        updateWidth.addValueChangeListener(e-> {
        	tempContainer.setWidth(e.getValue());
		});
        NumberField updateHeight = new NumberField();
        updateHeight.setWidth("20%");
        updateHeight.setValue(container.getHeight());
        updateHeight.addValueChangeListener(e-> {
        	tempContainer.setHeight(e.getValue());
		});
        NumberField showVolume = new NumberField();
        showVolume.setWidth("20%");
        showVolume.setValue(
        		container.getLength() * 
        		container.getWidth() * 
        		container.getHeight());

		HorizontalLayout sizeLayer = new HorizontalLayout(
				updateLength, updateWidth, updateHeight, showVolume);
		sizeLayer.setAlignItems(Alignment.BASELINE);
		
		HorizontalLayout feeLayer= new HorizontalLayout(updateFee, payedPicker);
		feeLayer.setAlignItems(Alignment.BASELINE);
		
		ListItem status = new ListItem(
				UIUtils.createTertiaryIcon(VaadinIcon.ANCHOR), updateID, "Vessel");
		
		status.getContent().setAlignItems(FlexComponent.Alignment.BASELINE);
		status.getContent().setSpacing(Bottom.XS);
		
		ListItem from = new ListItem(
				UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD_ALT),
				sizeLayer , "Departure");
		ListItem to = new ListItem(
				UIUtils.createTertiaryIcon(VaadinIcon.DOWNLOAD_ALT),
				feeLayer, "Destination");
		ListItem amount = new ListItem(
				UIUtils.createTertiaryIcon(VaadinIcon.SCALE),
				updateOwner, "Capacity");
		ListItem dateArival = new ListItem(
				UIUtils.createTertiaryIcon(VaadinIcon.CALENDAR),
				typePicker, "Arrival Date");
		ListItem dateDeparture = new ListItem(
				UIUtils.createTertiaryIcon(VaadinIcon.CALENDAR),
				updateWeight, "Departure Date");

		for (ListItem item : new ListItem[]{
				status, from, to, amount, dateArival, dateDeparture}) {
			item.setReverse(true);
			item.setWhiteSpace(WhiteSpace.PRE_LINE);
		}

		Div details = new Div(status, from, to, amount, dateArival, dateDeparture);
		details.addClassName(LumoStyles.Padding.Vertical.S);
		return details;
	}	
	
	private Component uploadProducts() {
    	Upload upload = new Upload(new Receiver() {
			@Override
    	      public OutputStream receiveUpload(String filename, String mimeType) {
    	        try {
    	          tempFile = File.createTempFile("temp", ".csv");
    	          return new FileOutputStream(tempFile);
    	        } catch (IOException e) {
    	          e.printStackTrace();
    	          return null;
    	        }}});
    	
    	    upload.addSucceededListener(e -> {
    	        try {
    	          DataInputStream in = new DataInputStream(new FileInputStream(tempFile));
    	          buildContainerFromCSV(in);
    	          tempFile.delete();
    	        } catch (IOException e1) {
    	          e1.printStackTrace();
    	        }
    	    });
    	    upload.setWidthFull();
    	upload.setHeight("50%");
    	return upload;
    }
    
    private void buildContainerFromCSV(DataInputStream in) {
    	CSVReader csvReader;
		try {
			csvReader = new CSVReader(new InputStreamReader(in,"utf-8"));
	    	List<String> record;
			while ((record = csvReader.readNext()) != null) {
				Vessel v = new Vessel();
				
				v.setVesselID(Integer.parseInt(record.get(0)));
				v.setCapacity(Integer.parseInt(record.get(1)));
				v.setDepartDate(Date.valueOf(record.get(2)));
				v.setArivalDate(Date.valueOf(record.get(3)));
				v.setDepartedFromCountry(record.get(4));
				v.setDepartedFromState(record.get(5));
				v.setDepartedFromCity(record.get(6));
				v.setDestinationCountry(record.get(7));
				v.setDestinationState(record.get(8));
				v.setDestinationCity(record.get(9));
				dataContainer.insertVesselRecords(v);
			}
			dataContainer.getVesselRecords();
			dataProvider = DataProvider.ofCollection(dataContainer.containerRecords.values());
			grid.setDataProvider(dataProvider);
		} catch (IOException e) {
			e.printStackTrace();
		}	
    }
}

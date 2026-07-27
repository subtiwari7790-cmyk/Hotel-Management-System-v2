import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.ArrayList;

enum RoomType {
    SINGLE(2000), DOUBLE(3000), DELUXE(5000);

    private double price;

    RoomType(double price){ this.price = price; }
    public double getPrice(){ return price; }
}

class Customer {
    private String name, contact, request;
    private int roomNo;

    public Customer(String n,String c,int r,String req){
        name=n; contact=c; roomNo=r; request=req;
    }

    public String getName(){ return name; }
    public String getContact(){ return contact; }
    public int getRoomNo(){ return roomNo; }
    public String getRequest(){ return request; }
}

class Room {
    private int roomNo;
    private RoomType type;
    private boolean booked=false;
    private LocalDate in,out;
    private double extra=0;

    public Room(int n,RoomType t){ roomNo=n; type=t; }

    public int getRoomNo(){ return roomNo; }
    public String getType(){ return type.name(); }
    public boolean isBooked(){ return booked; }

    public void book(LocalDate i,LocalDate o,double e){
        booked=true; in=i; out=o; extra=e;
    }

    public void checkout(){
        booked=false; in=null; out=null; extra=0;
    }

    public int getDays(){
        if(in==null||out==null) return 0;
        return (int)(out.toEpochDay()-in.toEpochDay());
    }

    public double bill(){
        return getDays()*type.getPrice()+extra;
    }
}

public class HotelApp extends Application {

    ArrayList<Room> roomList=new ArrayList<>();
    ArrayList<Customer> customerList=new ArrayList<>();

    ObservableList<Room> rooms=FXCollections.observableArrayList();
    ObservableList<Customer> customers=FXCollections.observableArrayList();

    TableView<Room> roomTable=new TableView<>(rooms);
    TableView<Customer> custTable=new TableView<>(customers);

    double revenue=0;
    Label revenueLabel=new Label("Revenue: 0");

    public void start(Stage stage){
        stage.setScene(new Scene(loginUI(stage),400,300));
        stage.setTitle("Login");
        stage.show();
    }

    VBox loginUI(Stage stage){

        Label title = new Label("Login");

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Label msg = new Label();

        Button loginBtn = new Button("Login");

        loginBtn.setOnAction(e -> {
            if(username.getText().equals("admin") && password.getText().equals("1234")){

                TabPane tabs=new TabPane();

                tabs.getTabs().add(addRoomTab());
                tabs.getTabs().add(bookingTab());
                tabs.getTabs().add(viewRoomTab());
                tabs.getTabs().add(customerTab());

                stage.setScene(new Scene(tabs,1100,650));

            } else {
                msg.setText("Invalid Username or Password");
            }
        });

        VBox layout = new VBox(15,title,username,password,loginBtn,msg);
        layout.setPadding(new Insets(20));

        return layout;
    }

    Tab addRoomTab(){

        TextField room=new TextField();
        ComboBox<String> type=new ComboBox<>();
        type.getItems().addAll("SINGLE","DOUBLE","DELUXE");

        Button add=new Button("Add Room");

        add.setOnAction(e->{
            Room r=new Room(Integer.parseInt(room.getText()),
                    RoomType.valueOf(type.getValue()));

            roomList.add(r);
            rooms.add(r);
        });

        GridPane g=new GridPane();
        g.setHgap(10); g.setVgap(10);

        g.add(new Label("Room No"),0,0); g.add(room,1,0);
        g.add(new Label("Type"),0,1); g.add(type,1,1);
        g.add(add,1,2);

        return new Tab("Add Room",g);
    }

    Tab bookingTab(){

        TextField room=new TextField();
        TextField name=new TextField();
        TextField contact=new TextField();
        TextField request=new TextField();

        DatePicker in=new DatePicker();
        DatePicker out=new DatePicker();

        CheckBox food=new CheckBox("Food (+500/day)");
        CheckBox laundry=new CheckBox("Laundry (+300)");
        CheckBox wifi=new CheckBox("Wifi (+200)");

        Button book=new Button("Book");
        Button checkout=new Button("Checkout");

        book.setOnAction(e->{

            if(out.getValue().isBefore(in.getValue())) return;

            int days=(int)(out.getValue().toEpochDay()-in.getValue().toEpochDay());
            double extra=0;

            if(food.isSelected()) extra+=500*days;
            if(laundry.isSelected()) extra+=300;
            if(wifi.isSelected()) extra+=200;

            for(Room r:roomList){
                if(r.getRoomNo()==Integer.parseInt(room.getText())&&!r.isBooked()){

                    r.book(in.getValue(),out.getValue(),extra);

                    Customer c=new Customer(
                            name.getText(),
                            contact.getText(),
                            r.getRoomNo(),
                            request.getText()
                    );

                    customerList.add(c);
                    customers.add(c);
                }
            }

            roomTable.refresh();
            custTable.refresh();
        });

        checkout.setOnAction(e->{

            for(Room r:roomList){
                if(r.getRoomNo()==Integer.parseInt(room.getText())){
                    revenue+=r.bill();
                    r.checkout();
                }
            }

            customerList.removeIf(c->c.getRoomNo()==Integer.parseInt(room.getText()));
            customers.removeIf(c->c.getRoomNo()==Integer.parseInt(room.getText()));

            revenueLabel.setText("Revenue: "+revenue);

            roomTable.refresh();
            custTable.refresh();
        });

        GridPane g=new GridPane();
        g.setHgap(10); g.setVgap(10);

        g.add(new Label("Room"),0,0); g.add(room,1,0);
        g.add(new Label("Name"),0,1); g.add(name,1,1);
        g.add(new Label("Contact"),0,2); g.add(contact,1,2);
        g.add(new Label("Check-In"),0,3); g.add(in,1,3);
        g.add(new Label("Check-Out"),0,4); g.add(out,1,4);
        g.add(new Label("Request"),0,5); g.add(request,1,5);

        VBox services=new VBox(5,food,laundry,wifi);

        HBox buttons=new HBox(10,book,checkout);

        VBox layout=new VBox(10,g,services,buttons,revenueLabel);
        layout.setPadding(new Insets(10));

        return new Tab("Booking",layout);
    }

    Tab viewRoomTab(){

        TableColumn<Room,Integer> c1=new TableColumn<>("Room");
        c1.setCellValueFactory(d->new SimpleIntegerProperty(d.getValue().getRoomNo()).asObject());

        TableColumn<Room,String> c2=new TableColumn<>("Type");
        c2.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getType()));

        TableColumn<Room,String> c3=new TableColumn<>("Status");
        c3.setCellValueFactory(d->new SimpleStringProperty(d.getValue().isBooked()?"Booked":"Available"));

        TableColumn<Room,Double> c4=new TableColumn<>("Bill");
        c4.setCellValueFactory(d->new SimpleDoubleProperty(d.getValue().bill()).asObject());

        roomTable.getColumns().addAll(c1,c2,c3,c4);

        Button all=new Button("All");
        Button booked=new Button("Booked");
        Button available=new Button("Available");

        all.setOnAction(e->roomTable.setItems(rooms));

        booked.setOnAction(e->{
            ObservableList<Room> temp=FXCollections.observableArrayList();
            for(Room r:rooms) if(r.isBooked()) temp.add(r);
            roomTable.setItems(temp);
        });

        available.setOnAction(e->{
            ObservableList<Room> temp=FXCollections.observableArrayList();
            for(Room r:rooms) if(!r.isBooked()) temp.add(r);
            roomTable.setItems(temp);
        });

        VBox box=new VBox(10,new HBox(10,all,booked,available),roomTable);
        box.setPadding(new Insets(10));

        return new Tab("Rooms",box);
    }

    Tab customerTab(){

        TableColumn<Customer,String> c1=new TableColumn<>("Name");
        c1.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Customer,String> c2=new TableColumn<>("Contact");
        c2.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getContact()));

        TableColumn<Customer,Integer> c3=new TableColumn<>("Room");
        c3.setCellValueFactory(d->new SimpleIntegerProperty(d.getValue().getRoomNo()).asObject());

        TableColumn<Customer,String> c4=new TableColumn<>("Request");
        c4.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getRequest()));

        custTable.getColumns().addAll(c1,c2,c3,c4);

        VBox box=new VBox(10,custTable);
        box.setPadding(new Insets(10));

        return new Tab("Customers",box);
    }

    public static void main(String[] args){
        launch(args);
    }
}
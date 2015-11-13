package io.galya.files;

import io.galya.files.model.CompareFoldersAppException;
import io.galya.files.model.FileInfoWrapper;
import io.galya.files.model.Timer;
import io.galya.files.model.UniqueFilesExtractedListener;
import io.galya.files.util.FileSystemUtils;
import io.galya.files.util.UniqueFilesExtractor;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Galya on 10/11/15.
 */
public class CompareFoldersApp extends Application implements UniqueFilesExtractedListener {

    private static final String APP_NAME = "Compare Folders";
    private static final int APP_WIDTH = 800;
    private static final int APP_HEIGHT = 600;

    private static final String ROOT_CSS_PATH = "css/root.css";

    private static final Color HEADER_BACKGROUND_COLOR = Color.web("0x484c4f", 1.0);
    private static final Color HEADER_INNER_SHADOW_COLOR = Color.web("0x2c2c2c", 1.0);
    private static final String HEADER_LOGO_IMAGE_PATH = "images/folders_400.png";
    private static final String HEADER_LOGO_NAME_PATH = "images/logo.png";
    private static final String APP_ICON_PATH = "images/app_icon.png";

    private Timer timer;
    private ProgressIndicator progressIndicator;
    private TableView<FileInfoWrapper> tableView;
    private ObservableList<FileInfoWrapper> tableData;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        timer = new Timer();

        primaryStage.setTitle(APP_NAME);
        primaryStage.setMinWidth(APP_WIDTH);
        primaryStage.setMinHeight(APP_HEIGHT);
        String appIconPath = new File(classLoader.getResource(APP_ICON_PATH).getFile()).toURI().toURL().toString();
        primaryStage.getIcons().add(new Image(appIconPath));

        GridPane gridLayout = new GridPane();
        gridLayout.setId("root-pane");
        gridLayout.setHgap(10);
        gridLayout.setVgap(15);
        gridLayout.setPadding(new Insets(0, 0, 10, 0));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(20);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30);
        col2.setHalignment(HPos.CENTER);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(30);
        col3.setHalignment(HPos.CENTER);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(20);
        gridLayout.getColumnConstraints().addAll(col1, col2, col3, col4);

        RowConstraints row = new RowConstraints();
        row.setValignment(VPos.CENTER);
        RowConstraints lastRow = new RowConstraints();
        lastRow.setValignment(VPos.CENTER);
        lastRow.setVgrow(Priority.ALWAYS);
        gridLayout.getRowConstraints().addAll(row, row, row, row, row, lastRow);

        HBox headerLayout = new HBox();
        headerLayout.setAlignment(Pos.CENTER_LEFT);
        headerLayout.setBackground(new Background(new BackgroundFill(HEADER_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setOffsetX(0);
        innerShadow.setOffsetY(-8);
        innerShadow.setColor(HEADER_INNER_SHADOW_COLOR);
        headerLayout.setEffect(innerShadow);

        String logoImagePath = new File(classLoader.getResource(HEADER_LOGO_IMAGE_PATH).getFile()).toURI().toURL().toString();
        ImageView logoImageView = new ImageView(logoImagePath);
        logoImageView.setPreserveRatio(true);
        logoImageView.setFitHeight(100);
        logoImageView.setSmooth(true);

        String logoNamePath = new File(classLoader.getResource(HEADER_LOGO_NAME_PATH).getFile()).toURI().toURL().toString();
        ImageView logoNameView = new ImageView(logoNamePath);

        headerLayout.getChildren().addAll(logoImageView, logoNameView);

        Label firstFolderLabel = new Label("First folder:");
        Label secondFolderLabel = new Label("Second folder:");

        final Label firstFolderPathLabel = new Label("No folder selected");
        firstFolderPathLabel.setWrapText(true);

        final Label secondFolderPathLabel = new Label("No folder selected");
        secondFolderPathLabel.setWrapText(true);

        final DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Choose folder");
        final File defaultInitialFolder = folderChooser.getInitialDirectory();

        final File[] directoriesForComparison = new File[2];

        Button chooseFolderOneButton = new Button();
        chooseFolderOneButton.setText("Browse...");
        chooseFolderOneButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (directoriesForComparison[0] != null) {
                    folderChooser.setInitialDirectory(directoriesForComparison[0].getAbsoluteFile());
                } else {
                    folderChooser.setInitialDirectory(defaultInitialFolder);
                }
                File firstDirectory = folderChooser.showDialog(primaryStage);
                if (firstDirectory != null) {
                    directoriesForComparison[0] = firstDirectory;
                    firstFolderPathLabel.setText(firstDirectory.getAbsolutePath());
                }
            }
        });

        Button chooseFolderTwoButton = new Button();
        chooseFolderTwoButton.setText("Browse...");
        chooseFolderTwoButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (directoriesForComparison[1] != null) {
                    folderChooser.setInitialDirectory(directoriesForComparison[1].getAbsoluteFile());
                } else {
                    folderChooser.setInitialDirectory(defaultInitialFolder);
                }
                File secondDirectory = folderChooser.showDialog(primaryStage);
                if (secondDirectory != null) {
                    directoriesForComparison[1] = secondDirectory;
                    secondFolderPathLabel.setText(secondDirectory.getAbsolutePath());
                }
            }
        });

        firstFolderLabel.setLabelFor(chooseFolderOneButton);
        secondFolderLabel.setLabelFor(chooseFolderTwoButton);

        Button compareButton = new Button("Compare");
        compareButton.setId("compare-button");
        compareButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tableData.clear();
                tableView.setVisible(false);

                if (directoriesForComparison[0] == null || directoriesForComparison[1] == null) {
                    showInfoDialog(primaryStage, "No Folder Selected", "Please select folders for comparison!");
                } else if (directoriesForComparison[0].equals(directoriesForComparison[1])) {
                    showInfoDialog(primaryStage, "Same Folders Selected", "Same folders selected. Please select different folders!");
                } else if (FileSystemUtils.areNestedDirectories(directoriesForComparison[0], directoriesForComparison[1])) {
                    showInfoDialog(primaryStage, "Nested Directories", "One of the folders is nested into the other. Please select different folders!");
                } else {
                    try {
                        startUniqueFilesExtraction(directoriesForComparison);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });

        tableView = new TableView<>();
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<FileInfoWrapper, String> fileNameCol = new TableColumn<>("File Name");
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<FileInfoWrapper, String> hashCodeCol = new TableColumn<>("Hash Code");
        hashCodeCol.setCellValueFactory(new PropertyValueFactory<>("hashCode"));

        TableColumn<FileInfoWrapper, Integer> countCol = new TableColumn<>("Count");
        countCol.setCellValueFactory(new PropertyValueFactory<>("count"));

        TableColumn<FileInfoWrapper, Long> sizeCol = new TableColumn<>("Size (Bytes)");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<FileInfoWrapper, Collection<Path>> pathsCol = new TableColumn<>("Paths");
        pathsCol.setCellValueFactory(new PropertyValueFactory<>("paths"));

        tableView.getColumns().addAll(fileNameCol, hashCodeCol, countCol, sizeCol, pathsCol);
        tableData = FXCollections.observableArrayList();
        tableView.setItems(tableData);

        tableView.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                Pane header = (Pane) tableView.lookup("TableHeaderRow");
                if (header != null) {
                    header.setBackground(new Background(new BackgroundFill(HEADER_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        });

        tableView.setVisible(false);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setScaleX(0.2);
        progressIndicator.setScaleY(0.2);
        progressIndicator.setVisible(false);

        gridLayout.add(headerLayout, 0, 0, 4, 1);
        gridLayout.add(firstFolderLabel, 1, 1);
        gridLayout.add(secondFolderLabel, 2, 1);
        gridLayout.add(firstFolderPathLabel, 1, 2);
        gridLayout.add(secondFolderPathLabel, 2, 2);
        gridLayout.add(chooseFolderOneButton, 1, 3);
        gridLayout.add(chooseFolderTwoButton, 2, 3);
        gridLayout.add(compareButton, 1, 4, 2, 1);
        gridLayout.add(progressIndicator, 1, 5, 2, 1);
        gridLayout.add(tableView, 0, 5, 4, 1);

        Scene scene = new Scene(gridLayout, APP_WIDTH, APP_HEIGHT);
        scene.getStylesheets().add(ROOT_CSS_PATH);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void onUniqueFilesExtracted(Collection<FileInfoWrapper> uniqueFiles) {
        timer.stop();
        progressIndicator.setVisible(false);

        SortedSet<FileInfoWrapper> sortedFiles = new TreeSet<>(uniqueFiles);
        try {
            tableData.addAll(sortedFiles);
            tableView.setVisible(true);
            showInfoDialog(tableView.getScene().getWindow(), "Files Comparison Finished", "Files comparison done in " + timer.getInterval(Timer.UNIT.SECONDS) + " sec. " + sortedFiles.size() + " unique files found.");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void startUniqueFilesExtraction(File[] directoriesForComparison) throws NoSuchAlgorithmException, CompareFoldersAppException, IOException {
        HashSet<Path> absolutePathsToFolders = new HashSet<>();
        for (File folder : directoriesForComparison) {
            absolutePathsToFolders.add(folder.toPath());
        }

        timer.start();
        progressIndicator.setVisible(true);
        new UniqueFilesExtractor(absolutePathsToFolders).extract(CompareFoldersApp.this);
    }

    private void showInfoDialog(Window window, String title, String content) {
        final double ALERT_HEIGHT = 135;
        final double ALERT_WIDTH = 425;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeight(ALERT_HEIGHT);
        alert.setWidth(ALERT_WIDTH);
        alert.setResizable(false);
        alert.setX(window.getX() + window.getWidth() / 2 - ALERT_WIDTH / 2);
        alert.setY(window.getY() + window.getHeight() / 2 - ALERT_HEIGHT / 2);
        //System.out.println("x: " + (window.getX() + window.getWidth() / 2 - ALERT_WIDTH / 2));
        //System.out.println("y: " + (window.getY() + window.getHeight() / 2 - ALERT_HEIGHT / 2));
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
        //System.out.println(alert.getHeight());
        //System.out.println(alert.getWidth());
    }
}
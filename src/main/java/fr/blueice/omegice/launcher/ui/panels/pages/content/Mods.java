package fr.blueice.omegice.launcher.ui.panels.pages.content;

import fr.blueice.omegice.launcher.ui.PanelManager;
import fr.blueice.omegice.launcher.ui.panels.pages.content.ContentPanel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.concurrent.atomic.AtomicInteger;

public class Mods extends ContentPanel {
    GridPane contentPane = new GridPane();

    @Override
    public String getName() {
        return "mods";
    }

    @Override
    public String getStylesheetPath() {
        return "css/content/mods.css";
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);

        // Background
        this.layout.getStyleClass().add("mods-layout");
        this.layout.setPadding(new Insets(40));
        setCanTakeAllSize(this.layout);

        // Content
        contentPane.getStyleClass().add("content-pane");
        setCanTakeAllSize(contentPane);
        this.layout.getChildren().add(contentPane);

        // Titre
        Label title = new Label("Mods");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, FontPosture.REGULAR, 25f));
        title.getStyleClass().add("mods-title");
        setLeft(title);
        setCanTakeAllSize(title);
        setTop(title);
        title.setTextAlignment(TextAlignment.LEFT);
        title.setTranslateY(40d);
        title.setTranslateX(25d);
        contentPane.getChildren().add(title);

        AtomicInteger CT = new AtomicInteger();
        Button CTBtn = new Button("Crafting Tweaks");
        CTBtn.getStyleClass().add("mods-btn");
        setCanTakeAllSize(CTBtn);
        setBottom(CTBtn);
        setCenterH(CTBtn);
        CTBtn.setOnMouseClicked(e -> {

        });
         contentPane.getChildren().add(CTBtn);

    }
}


package fr.blueice.omegice.launcher.ui.panels.pages.content;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import fr.blueice.omegice.launcher.Launcher;
import fr.blueice.omegice.launcher.game.MinecraftInfos;
import fr.blueice.omegice.launcher.ui.PanelManager;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;
import fr.flowarg.flowupdater.download.json.CurseFileInfo;
import fr.flowarg.flowupdater.download.json.CurseModPackInfo;
import fr.flowarg.flowupdater.download.json.OptiFineInfo;
import fr.flowarg.flowupdater.utils.ModFileDeleter;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import fr.flowarg.flowupdater.versions.ForgeVersionBuilder;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.*;
import fr.theshark34.openlauncherlib.util.Saver;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.nio.file.Path;
import java.text.DecimalFormat;

public class Home extends ContentPanel {
    private final Saver saver = Launcher.getInstance().getSaver();
    GridPane boxPane = new GridPane();
    ProgressBar progressBar = new ProgressBar();
    Label stepLabel = new Label();
    Label fileLabel = new Label();
    boolean isDownloading = false;

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public String getStylesheetPath() {
        return "css/content/home.css";
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setValignment(VPos.CENTER);
        rowConstraints.setMinHeight(75);
        rowConstraints.setMaxHeight(75);
        this.layout.getRowConstraints().addAll(rowConstraints, new RowConstraints());
        boxPane.getStyleClass().add("box-pane");
        setCanTakeAllSize(boxPane);
        boxPane.setPadding(new Insets(20));
        this.layout.add(boxPane, 0, 0);
        this.layout.getStyleClass().add("home-layout");

        progressBar.getStyleClass().add("download-progress");
        stepLabel.getStyleClass().add("download-status");
        fileLabel.getStyleClass().add("download-status");

        progressBar.setTranslateY(-15);
        setCenterH(progressBar);
        setCanTakeAllWidth(progressBar);

        stepLabel.setTranslateY(5);
        setCenterH(stepLabel);
        setCanTakeAllSize(stepLabel);

        fileLabel.setTranslateY(20);
        setCenterH(fileLabel);
        setCanTakeAllSize(fileLabel);

        this.showPlayButton();
    }

    private void showPlayButton() {
        boxPane.getChildren().clear();
        Button playBtn = new Button("Jouer");
        FontAwesomeIconView playIcon = new FontAwesomeIconView(FontAwesomeIcon.GAMEPAD);
        playIcon.getStyleClass().add("play-icon");
        setCanTakeAllSize(playBtn);
        setCenterH(playBtn);
        setCenterV(playBtn);
        playBtn.getStyleClass().add("play-btn");
        playBtn.setGraphic(playIcon);
        playBtn.setOnMouseClicked(e -> this.play());
        boxPane.getChildren().add(playBtn);
    }

    private void play() {
        isDownloading = true;
        boxPane.getChildren().clear();
        setProgress(0, 0);
        boxPane.getChildren().addAll(progressBar, stepLabel, fileLabel);

        Platform.runLater(() -> new Thread(this::update).start());
    }

    public void update() {
        IProgressCallback callback = new IProgressCallback() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#.#");
            private String stepTxt = "";
            private String percentTxt = "0.0%";

            @Override
            public void step(Step step) {
                Platform.runLater(() -> {
                    stepTxt = StepInfo.valueOf(step.name()).getDetails();
                    setStatus(String.format("%s (%s)", stepTxt, percentTxt));
                });
            }

            public void update(long downloaded, long max) {
                Platform.runLater(() -> {
                    percentTxt = decimalFormat.format(downloaded * 100.d / max) + "%";
                    setStatus(String.format("%s (%s)", stepTxt, percentTxt));
                    setProgress(downloaded, max);
                });
            }

            @Override
            public void onFileDownloaded(Path path) {
                Platform.runLater(() -> {
                    String p = path.toString();
                    fileLabel.setText("..." + p.replace(Launcher.getInstance().getLauncherDir().toFile().getAbsolutePath(), ""));
                });
            }
        };

        try {
            final VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                    .withName(MinecraftInfos.GAME_VERSION)
                    .withSnapshot(Boolean.parseBoolean(MinecraftInfos.GAME_VERSION))
                    .build();
            final UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder()
                    .build();

            final AbstractForgeVersion forgeVersion = new ForgeVersionBuilder(ForgeVersionBuilder.ForgeVersionType.NEW)
                    .withForgeVersion("36.2.2")
                    .withCurseMods(CurseFileInfo.getFilesFromJson("https://dev-omegice.pantheonsite.io/Mods.json"))
                    .build();

            final FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                    .withVanillaVersion(vanillaVersion)
                    .withModLoaderVersion(forgeVersion)
                    .withLogger(Launcher.getInstance().getLogger())
                    .withProgressCallback(callback)
                    .withUpdaterOptions(options)
                    .build();

            updater.update(Launcher.getInstance().getLauncherDir());
            this.startGame(updater.getVanillaVersion().getName());
        } catch (Exception exception) {
            Launcher.getInstance().getLogger().err(exception.toString());
            exception.printStackTrace();
            Platform.runLater(() -> panelManager.getStage().show());
        }
    }

    public void startGame(String gameVersion) {
        GameInfos infos = new GameInfos(
                "omegice",
                true,
                new GameVersion(gameVersion, MinecraftInfos.OLL_GAME_TYPE.setNFVD(MinecraftInfos.OLL_FORGE_DISCRIMINATOR)),
                new GameTweak[]{}
        );

        try {
            ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(infos, GameFolder.FLOW_UPDATER, Launcher.getInstance().getAuthInfos());
            profile.getVmArgs().add(this.getRamArgsFromSaver());
            ExternalLauncher launcher = new ExternalLauncher(profile);

            Platform.runLater(() -> panelManager.getStage().hide());

            Process p = launcher.launch();

            Platform.runLater(() -> {
                try {
                    p.waitFor();
                    Platform.exit();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            Launcher.getInstance().getLogger().err(exception.toString());
        }
    }

    public String getRamArgsFromSaver() {
        int val = 1024;
        try {
            if (saver.get("maxRam") != null) {
                val = Integer.parseInt(saver.get("maxRam"));
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException error) {
            saver.set("maxRam", String.valueOf(val));
            saver.save();
        }

        return "-Xmx" + val + "M";
    }

    public void setStatus(String status) {
        this.stepLabel.setText(status);
    }

    public void setProgress(double current, double max) {
        this.progressBar.setProgress(current / max);
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public enum StepInfo {
        READ("Lecture du fichier json..."),
        DL_LIBS("Téléchargement des libraries..."),
        DL_ASSETS("Téléchargement des ressources..."),
        EXTRACT_NATIVES("Extraction des natives..."),
        FORGE("Installation de forge..."),
        FABRIC("Installation de fabric..."),
        MODS("Téléchargement des mods..."),
        EXTERNAL_FILES("Téléchargement des fichier externes..."),
        POST_EXECUTIONS("Exécution post-installation..."),
        MOD_LOADER("Chargement des mods"),
        MOD_PACK("Installation du mod pack"),
        INTEGRATION("Integration"),
        END("Finit !");
        String details;

        StepInfo(String details) {
            this.details = details;
        }

        public String getDetails() {
            return details;
        }
    }
}

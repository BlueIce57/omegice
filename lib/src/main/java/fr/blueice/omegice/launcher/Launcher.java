package fr.blueice.omegice.launcher;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import java.io.File;
import java.util.Arrays;

import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import fr.theshark34.openlauncherlib.minecraft.GameInfos;
import fr.theshark34.openlauncherlib.minecraft.GameTweak;
import fr.theshark34.openlauncherlib.minecraft.GameType;
import fr.theshark34.openlauncherlib.minecraft.GameVersion;
import fr.theshark34.openlauncherlib.minecraft.MinecraftLauncher;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.swinger.Swinger;

public class  Launcher {

	public static final GameVersion OI_VERSION = new GameVersion("1.12.2", GameType.V1_8_HIGHER);
	public static final GameInfos OI_INFOS = new GameInfos("OmegIce", OI_VERSION, new GameTweak[] {GameTweak.FORGE});
	public static final File OI_DIR = OI_INFOS.getGameDir();

	private static AuthInfos authInfos;
    private static Thread updateThread;
	
	public static void authMicrosoft(String email, String password) throws MicrosoftAuthenticationException {
	    MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
	    MicrosoftAuthResult result = authenticator.loginWithCredentials(email, password);
	    authInfos = new AuthInfos(result.getProfile().getName(), result.getAccessToken(), result.getProfile().getId());
	}
	
	public static void update() throws Exception {
		SUpdate su = new SUpdate("https://dev-omegice.pantheonsite.io", OI_DIR);
		
        updateThread = new Thread() {
            private int val;
            private int max;

            @Override
            public void run() {
                while(!this.isInterrupted()) {
                	if(BarAPI.getNumberOfFileToDownload() ==0 ) {
                		LauncherFrame.getInstance().getLauncherPanel().setInfoText("Verification des fichier");
                		continue;
                	}
                    val = (int) (BarAPI.getNumberOfTotalDownloadedBytes() / 1000);
                    max = (int) (BarAPI.getNumberOfTotalBytesToDownload() /1000);
                    
                    LauncherFrame.getInstance().getLauncherPanel().getProgressBar().setMaximum(max);
                    LauncherFrame.getInstance().getLauncherPanel().getProgressBar().setValue(val);

                    LauncherFrame.getInstance().getLauncherPanel().setInfoText("Telechargement des fichiers " +
                            BarAPI.getNumberOfDownloadedFiles() + "/" + BarAPI.getNumberOfFileToDownload() + "       " +
                                 Swinger.percentage(val, max) + "%");
                }
            }
        };
        updateThread.start();

       su.start(); 
       updateThread.interrupt();
	}
	
	public static void launch() throws LaunchException
	{
		ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(OI_INFOS, GameFolder.BASIC, authInfos);
		profile.getArgs().addAll(Arrays.asList("--server", "46.4.94.213", "--port", "60287"));
		ExternalLauncher launcher = new ExternalLauncher(profile);
		
		Process p = launcher.launch();
		
		try
		{
			Thread.sleep(5000L);
			LauncherFrame.getInstance().setVisible(false);
			p.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		System.exit(0);
		
	}

    public static void interruptThread() {
    	updateThread.interrupt();
    }
    

	
}

package com.duffy;

import com.duffy.model.Account;
import com.duffy.model.BlizzardApiItem;
import com.duffy.model.GarrisonMission;
import com.duffy.model.InProgressMissionData;
import com.duffy.model.push.InProgressMissionDataPushRequest;
import com.duffy.model.push.PushRegIdResponse;
import com.duffy.model.push.PushRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JsonMapperConfigurator;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Hello world!
 */
public class Main {

    private static final File BASE_WOW_PATH = new File("G:\\Program Files (x86)\\World of Warcraft");

    private static Client client = ClientBuilder.newClient();

    private static String pushToken;
    private static String userId;

    static {
        JacksonJsonProvider provider = new JacksonJsonProvider();
        provider.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        client.register(provider);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        SystemTrayUtil.showSystemTray();

        if(args.length < 1)
            System.exit(0);

        System.out.println(args[0]);

        userId = args[0];
        updatePushToken();

        System.out.println(pushToken);

        List<Thread> watcherThreads = new ArrayList<>();

        for(File f : getCharacterPaths()){
            File savedVars = new File(f, "/SavedVariables");
            if(savedVars.exists()) {
                watcherThreads.add(addWatcherToDirectories(savedVars));
            }
        }


        //grabMissionData();

        for(Thread t : watcherThreads){
            t.join();
        }

    }

    private static void updatePushToken(){
        if(userId != null) {
            pushToken = client
                    .target("http://gnpushserver-jayd.rhcloud.com/app/push/" + userId)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(PushRegIdResponse.class)
                    .pushId;
        }
    }

    private static Thread addWatcherToDirectories(File file) throws IOException{
        // get the directory we want to watch, using the Paths singleton class
        Path toWatch = Paths.get(file.toURI());
        if (toWatch == null) {
            throw new UnsupportedOperationException("Directory not found");
        }

        // make a new watch service that we can register interest in
        // directories and files with.
        WatchService myWatcher = toWatch.getFileSystem().newWatchService();

        // start the file watcher thread below
        MyWatchQueueReader fileWatcher = new MyWatchQueueReader(myWatcher);
        Thread th = new Thread(fileWatcher, "FileWatcher");
        th.start();

        // register a file
        toWatch.register(myWatcher, ENTRY_CREATE, ENTRY_MODIFY);
        return th;
    }


    private static File getAccountPath() {
        return new File(BASE_WOW_PATH, "/WTF/Account/JAYD1616/Silvermoon/");
    }

    private static File[] getCharacterPaths() {
        return getAccountPath().listFiles();
    }

    private static void grabMissionData() throws IOException {
        List<Account> accounts = new ArrayList<>();

        for (File f : getCharacterPaths()) {
            InProgressMissionData data = grabMissionData(new File(f, "/SavedVariables/GarrisonNotify.lua"));
            if (data != null) {
                accounts.add(new Account(f.getName(), data));
            }
        }

        push(accounts);
    }

    private static InProgressMissionData grabMissionData(File garrisonSavedFile) throws IOException {

        if (!garrisonSavedFile.exists() || !garrisonSavedFile.canRead()) return null;

        try (InputStream is = new FileInputStream(garrisonSavedFile)) {
            String s = IOUtils.toString(is)
                    .replace("InProgressMissionData = ", "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace("=", ":")
                    .replaceAll("\r|\n|\t", "")
                    .replace(",}", "}")
                    .replaceAll("nil", "null");
            System.out.println(s);
            Gson gson = new Gson();
            InProgressMissionData missionData = gson.fromJson(s, InProgressMissionData.class);

            preProcessRewards(missionData);

            return missionData;
        }
    }

    private static void preProcessRewards(InProgressMissionData missionData) {
        if(missionData == null) return;
        for (GarrisonMission m : missionData.values()) {
            for (GarrisonMission.Reward reward : m.rewards.values()) {

                if(reward.followerXP != null){
                    reward.quantity = reward.followerXP;
                }

                if(reward.icon != null && reward.icon.equals(GarrisonMission.Reward.FOLLOWER_XP_ICON)) {
                    //wat do?
                } else if (reward.icon != null) {
                    reward.imageUrl = "http://media.blizzard.com/wow/icons/56/" + reward.icon.substring(16) + ".jpg"; //remove "Interface\Icons\"
                } else {
                    BlizzardApiItem blizzardApiItem = client.target("http://us.battle.net/api/wow/item/" + reward.itemID).request().get(BlizzardApiItem.class);
                    reward.imageUrl = "http://media.blizzard.com/wow/icons/56/" + blizzardApiItem.icon + ".jpg";
                    reward.quantity = blizzardApiItem.itemLevel;
                }
            }
        }
    }

    private static void push(List<Account> accounts) {
        InProgressMissionDataPushRequest pushRequest = new InProgressMissionDataPushRequest(accounts, pushToken);


        Response response = client.target("https://android.googleapis.com/gcm/send")
                .request()
                .header("Authorization", "key=AIzaSyDE2DwkQmxljaDTSsbq_NYMCQaNj1_lx6E")
                .post(Entity.entity(pushRequest, MediaType.APPLICATION_JSON));

        System.out.println("push response: " + response.getStatus());
        try {
            String responseString =  IOUtils.toString((InputStream) response.getEntity());
            System.out.println("push response: " + responseString);
            if(responseString.contains("\"failure\":1")){
                updatePushToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MyWatchQueueReader implements Runnable {

        /**
         * the watchService that is passed in from above
         */
        private WatchService myWatcher;

        public MyWatchQueueReader(WatchService myWatcher) {
            this.myWatcher = myWatcher;
        }

        /**
         * In order to implement a file watcher, we loop forever
         * ensuring requesting to take the next item from the file
         * watchers queue.
         */
        @Override
        public void run() {
            try {
                // get the first event before looping
                WatchKey key = myWatcher.take();
                while (key != null) {
                    // we have a polled event, now we traverse it and
                    // receive all the states from it
                    boolean updateGarrisonData = false;
                    for (WatchEvent event : key.pollEvents()) {
//                        System.out.printf("Received %s event for file: %s\n",
//                                event.kind(), event.context());
                        if (event.context().toString().equals("GarrisonNotify.lua")) {
                            updateGarrisonData = true;
                        }
                    }

                    if (updateGarrisonData) {
                        try {
                            grabMissionData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    key.reset();
                    key = myWatcher.take();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Stopping thread");
        }
    }

}

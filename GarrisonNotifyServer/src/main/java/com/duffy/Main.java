package com.duffy;

import com.duffy.model.Account;
import com.duffy.model.InProgressMissionData;
import com.duffy.model.push.InProgressMissionDataPushRequest;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
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

    private static final File BASE_WOW_PATH = new File("C:\\Program Files (x86)\\World of Warcraft");

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello World!");

        grabMissionData();

        // get the directory we want to watch, using the Paths singleton class
        Path toWatch = Paths.get(getAccountPath().toURI());
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
        th.join();


    }


    private static File getAccountPath() {
        return new File(BASE_WOW_PATH, "/WTF/Account/JAYD1616/Silvermoon/");
    }
    private static File[] getCharacterPaths(){
       return getAccountPath().listFiles();
    }

    private static void grabMissionData() throws IOException{
        List<Account> accounts = new ArrayList<>();

        for(File f : getCharacterPaths()){
            InProgressMissionData data = grabMissionData(new File(f, "/SavedVariables/GarrisonNotify.lua"));
            accounts.add(new Account(f.getName(), data));
        }

        //push(accounts);
    }

    private static InProgressMissionData grabMissionData(File garrisonSavedFile) throws IOException {

        if(!garrisonSavedFile.exists() || !garrisonSavedFile.canRead()) return null;

        try (InputStream is = new FileInputStream(garrisonSavedFile)) {
            String s = IOUtils.toString(is)
                    .replace("InProgressMissionData = ", "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace("=", ":")
                    .replaceAll("\r|\n|\t", "")
                    .replace(",}", "}");
            System.out.println(s);
            Gson gson = new Gson();
            return gson.fromJson(s, InProgressMissionData.class);
        }
    }

    private static void push(List<Account> accounts){
        InProgressMissionDataPushRequest pushRequest = new InProgressMissionDataPushRequest(accounts, "APA91bHcf7nVHXqYoe2J9WOaE3KfJxSs_b5Hua_vRNlG-o8UHsfVAwyeWnmGmEP_zdExqpm2sn8D5KFYLXh_hbvBUSs_VTQDQ2FHKSIV5AMMKaaRbj6Z5PPRDeKz_bs1CCdbATbQhk4d6VAkD8fK7GBw7N7ZS6q8M8gPN3Rs713LM8GyhQJk3GQ");

        Client client = ClientBuilder.newClient();
        client.register(JacksonJsonProvider.class);
        Response response = client.target("https://android.googleapis.com/gcm/send")
                .request()
                .header("Authorization", "key=AIzaSyDE2DwkQmxljaDTSsbq_NYMCQaNj1_lx6E")
                .post(Entity.entity(pushRequest, MediaType.APPLICATION_JSON));

        System.out.println("push response: " + response.getStatus());
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

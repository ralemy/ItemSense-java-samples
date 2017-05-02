package net.alemy.samples.itemsense;

/**
 * Created by ralemy on 4/27/17.
 * Simple commandline showing the use of Itemsense-java
 */

import com.impinj.itemsense.client.coordinator.CoordinatorApiController;
import com.impinj.itemsense.client.coordinator.job.Job;
import com.impinj.itemsense.client.coordinator.job.JobResponse;
import com.impinj.itemsense.client.data.DataApiController;
import com.impinj.itemsense.client.data.item.Item;
import com.impinj.itemsense.client.data.item.ItemResponse;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


public class MainClass {

    private static final String username = "admin";
    private static final String password ="admindefault";
    private static final String ItemSenseUrl = "http://healthcare.itemsense.impinj.com/itemsense/";
    private static final String RecipeName = "JavaDemo";
    private static final String FacilityName = "JavaDemo";

    public static void main(String[] args) throws InterruptedException{
        //Suppress restricted headers warnings: http://stackoverflow.com/questions/11147330/httpurlconnection-wont-let-me-set-via-header
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        System.out.println("Connecting to ItemSense, Authenticating and creating a coordinator");
        Client client = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.basic(username, password));
        CoordinatorApiController coordinator = new CoordinatorApiController( client,
                URI.create(ItemSenseUrl));

        System.out.println("Creating the job request object");

        Job job = new Job();
        job.setFacility(FacilityName);
        job.setRecipeName(RecipeName);
        job.setDurationSeconds(0);
        job.setStartDelay("PT0S");  //0 seconds as ISO-8601

        System.out.println("Starting the job");
        JobResponse response= coordinator.getJobController().startJob(job);
        ZonedDateTime start = response.getCreationTime();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); //as ISO String
        String fromTime = start.format(format);

        System.out.println("Waiting 20 seconds for the job to start running");
        Thread.sleep(20000);

        System.out.println("Getting reads since job creation: " + fromTime);
        Client dataClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.basic(username, password));
        DataApiController dataController = new DataApiController( dataClient,
                URI.create(ItemSenseUrl));

        Map<String, Object> params = new HashMap<>();
        params.put("fromTime",fromTime);
        ItemResponse itemResponse = dataController.getItemController().getItems(params);
        Item[] items = itemResponse.getItems();
        for(Item item : items)
            System.out.println(item.toString());

        System.out.println("Stopping the Job");
        coordinator.getJobController().stopJob(response.getId());
    }

}

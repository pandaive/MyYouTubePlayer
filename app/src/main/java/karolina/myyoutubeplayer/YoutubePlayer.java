package karolina.myyoutubeplayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class YoutubePlayer extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static YouTube youtube;
    private YouTubePlayer player;
    private String nextPageToken = "";
    private boolean ifVideoPlayed;
    private static final int NUMBER_OF_TRIES = 10;

    public static final String EXTRA_YOUTUBE_CATEGORY = "category";
    public static final String EXTRA_MINUTES = "minutes";
    public static final String EXTRA_SECONDS = "seconds";
    private Integer minutes, seconds;
    private String watchChoice;
    private Button newVideo;
    private Button finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        //przechwytywanie parametrów
        Intent intent = getIntent();
        minutes = (Integer) intent.getExtras().getSerializable(EXTRA_MINUTES);
        seconds = (Integer) intent.getExtras().getSerializable(EXTRA_SECONDS);
        watchChoice = (String) intent.getExtras().getSerializable(EXTRA_YOUTUBE_CATEGORY);

        //przyciski
        newVideo = (Button) findViewById(R.id.new_video);
        newVideo.setOnClickListener(getVideos);

        finish = (Button) findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView textView = (TextView) findViewById(R.id.textview);

        //zaczynamy..
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        loadYoutube();
    }


    private void loadYoutube(){
        //YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        YouTubePlayerFragment youTubeView = (YouTubePlayerFragment)getFragmentManager().findFragmentById(R.id.youtube_view);

        youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this);
    }

    private View.OnClickListener getVideos = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            while(!playVideo()){
            };
        }
    };

    private boolean playVideo() {
        ifVideoPlayed = true;
        Thread play = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                String videoId = null;
                Map<String, Integer> apiResults = null;
                while (i++ <= NUMBER_OF_TRIES) {
                    try {
                        apiResults = getConvertedIdVideoDurations(getVideoIdDurations(getVideoIdList(watchChoice)));

                        videoId = getMatchingVideoId(apiResults, minutes * 60 + seconds, 5);
                        if (videoId != null) {
                            break;
                        }
                    }
                    catch(NullPointerException e) {
                        e.printStackTrace();
                        ifVideoPlayed = false; //try again if failed
                    }

                }

                if (videoId != null) {
                    try {
                        player.loadVideo(videoId);
                    }
                    catch (IllegalStateException e) {
                        e.printStackTrace();
                        finish();
                    }
                }
                else {
                    YoutubePlayer.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(YoutubePlayer.this).create();
                            alertDialog.setTitle("Not found");
                            alertDialog.setMessage("Nie znaleziono filmu :( może spróbuj zmienić czas?");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });
                            alertDialog.show();
                        }
                    });
                }
            }
        });
        play.start();

        if (!ifVideoPlayed)
            play.stop();

        //player.cueVideo("y6GaPkkGZGw");
        return ifVideoPlayed;
    }

    public String getMatchingVideoId(Map<String, Integer> mappedIdsDurations, Integer desiredDuration, Integer offset){
        boolean lookForIdentical;
        try {
            lookForIdentical = mappedIdsDurations.containsValue(desiredDuration);
        }
        catch (NullPointerException e) {
            return null;
        }

        for(Map.Entry<String, Integer> entry : mappedIdsDurations.entrySet()){
            Integer value = entry.getValue();
            if(lookForIdentical){
                if(value == desiredDuration){
                    return entry.getKey();
                }
            }
            else if(value >= desiredDuration - offset && value <= desiredDuration + offset){
                // Pierwszy pasujący
                return entry.getKey();
            }
        }
        return null;
    }

    public Map<String, Integer> getConvertedIdVideoDurations(Map<String,String> unparsedMap) {
        Map<String, Integer> idsConvertedDurations = new TreeMap<String, Integer>();

        try {
            for (Map.Entry<String, String> entry : unparsedMap.entrySet()) {
                idsConvertedDurations.put(entry.getKey(), (int) getTimeFromString(entry.getValue()));
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
        return idsConvertedDurations;
    }

    private Map<String, String> getVideoIdDurations(List<String> videoIdList) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("https://www.googleapis.com/youtube/v3/videos?part=contentDetails&key=" + DeveloperKey.DEVELOPER_KEY + "&id=");

        try {
            for (int i = 0; i < videoIdList.size(); i++) {
                if (i == videoIdList.size()-1)
                    queryString.append(URLEncoder.encode(videoIdList.get(i) , "UTF-8"));
                else
                    queryString.append(URLEncoder.encode(videoIdList.get(i) + ",", "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }

        Map<String, String> idDurationMap = new TreeMap<String, String>();
        List<String> durationList = new ArrayList<String>();
        JSONObject mainObject = null;
        try {
            mainObject = new JSONObject(getHTTPJsonString(queryString.toString()));
            JSONArray JsonItems = mainObject.getJSONArray("items");

            for (int i=0; i < JsonItems.length(); i++) {
                try {
                    JSONObject singleItem = JsonItems.getJSONObject(i);
                    JSONObject singleContentDetail = singleItem.getJSONObject("contentDetails");
                    durationList.add(singleContentDetail.getString("duration"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(videoIdList.size() == JsonItems.length()){
                for(int i=0; i<videoIdList.size(); i++){
                    idDurationMap.put(videoIdList.get(i), durationList.get(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return idDurationMap;
    }

    private List<String> getVideoIdList(String watchChoice) {
        String duration = "short";
        //adjust video duration
        if (minutes >= 4 && minutes <= 20)
            duration = "medium";
        else if (minutes > 20)
            duration = "long";

        try {
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {

                }
            }).setApplicationName("API Project").build();

            StringBuilder queryString = new StringBuilder();
            queryString.append("https://www.googleapis.com/youtube/v3/search?part=snippet&q=");
            queryString.append(watchChoice);
            queryString.append("&maxResults=50&videoDuration=" + duration + "&type=video&key=" + DeveloperKey.DEVELOPER_KEY);

            if(!nextPageToken.isEmpty()){
                queryString.append("&pageToken=");
                queryString.append(URLEncoder.encode(nextPageToken, "UTF-8"));
            }


            JSONObject jObject = new JSONObject(getHTTPJsonString(queryString.toString()));
            try {
                this.nextPageToken = jObject.getString("nextPageToken");

                if(nextPageToken == null){
                    return null;
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

            JSONArray jArray = jObject.getJSONArray("items");

            List<String> videoIdList = new ArrayList<String>();

            for (int i=0; i < jArray.length(); i++) {
                try {
                    JSONObject singleItem = jArray.getJSONObject(i);
                    JSONObject singleId = singleItem.getJSONObject("id");
                    videoIdList.add(singleId.getString("videoId"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return videoIdList;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private String getHTTPJsonString(String address){
        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpGet httpget = new HttpGet(address);
        httpget.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            Log.e("error", "error", e);
        }
        finally {
            try{
                if(inputStream != null)
                    inputStream.close();
            }catch(Exception squish){}
        }
        return result;
    }

    private double getTimeFromString(String duration) {
        double time = 0;
        boolean hourexists = false, minutesexists = false, secondsexists = false;
        if (duration.contains("H"))
            hourexists = true;
        if (duration.contains("M"))
            minutesexists = true;
        if (duration.contains("S"))
            secondsexists = true;
        if (hourexists) {
            String hour = "";
            hour = duration.substring(duration.indexOf("T") + 1,
                    duration.indexOf("H"));
            time += (Double.parseDouble(hour))*(3600);
        }
        if (minutesexists) {
            String minutes = "";
            if (hourexists)
                minutes = duration.substring(duration.indexOf("H") + 1,
                        duration.indexOf("M"));
            else
                minutes = duration.substring(duration.indexOf("T") + 1,
                        duration.indexOf("M"));
            time += (Double.parseDouble(minutes))*(60);
        }
        if (secondsexists) {
            String seconds = "";
            if (hourexists) {
                if (minutesexists)
                    seconds = duration.substring(duration.indexOf("M") + 1,
                            duration.indexOf("S"));
                else
                    seconds = duration.substring(duration.indexOf("H") + 1,
                            duration.indexOf("S"));
            } else if (minutesexists)
                seconds = duration.substring(duration.indexOf("M") + 1,
                        duration.indexOf("S"));
            else
                seconds = duration.substring(duration.indexOf("T") + 1,
                        duration.indexOf("S"));
            if (seconds.length() == 1)
                seconds = "0." + seconds;
            time += Double.parseDouble(seconds);
        }
        return time;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {

        this.player = youTubePlayer;

        //disable automatic fullscreen
        int controlFlags = player.getFullscreenControlFlags();
        controlFlags &= ~YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE;
        controlFlags &= YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI;
        controlFlags &= YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT;
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        player.setFullscreenControlFlags(controlFlags);
        //disable fullscreen option
        player.setShowFullscreenButton(false);
        newVideo.performClick();

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, 1).show();
        } else {
            String errorMessage = String.format("YouTube error :(", errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }
}
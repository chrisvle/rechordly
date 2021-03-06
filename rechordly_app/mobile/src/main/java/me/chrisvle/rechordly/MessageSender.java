package me.chrisvle.rechordly;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class MessageSender extends Service {
    public MessageSender() {
    }

    private GoogleApiClient mApiClient;
    private static final String START_EDIT = "/edit";
    private static final String START_LYRICS = "/lyrics";
    private static final String START_MAIN = "/start_main";

    private String duration;
    private String lyrics_bool;

    @Override
    public void onCreate() {
        super.onCreate();

        /* Initialize the googleAPIClient for message passing */
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        /* Successfully connected */
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        /* Connection was interrupted */
                    }
                })
                .build();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //if intent is map send message to phone with /start_map
        String start = intent.getStringExtra("START");
        duration = intent.getStringExtra("Duration");
        lyrics_bool = intent.getStringExtra("Lyrics");
        String edit_message = "crop|" + lyrics_bool + "|" + duration;
        String lyric_message = "lyrics|" + lyrics_bool + "|" + duration;
        if (start != null) {
            if (start.equalsIgnoreCase("edit")) {
                Toast.makeText(this, "Opening Edit On Watch", Toast.LENGTH_SHORT).show();
                sendMessage(START_EDIT, edit_message);
            } else if (start.equalsIgnoreCase("main")) {
                Toast.makeText(this, "Opening Main on Watch", Toast.LENGTH_SHORT).show();
                sendMessage(START_MAIN, "");
            } else if (start.equalsIgnoreCase("lyrics")) {
                Toast.makeText(this, "Opening Main on Watch", Toast.LENGTH_SHORT).show();
                sendMessage(START_LYRICS, lyric_message);
            }
        } else {
            Toast.makeText(this, "intent retrieval failed", Toast.LENGTH_SHORT).show();
        }
        //if intent is shake send message to phone with /start_shake
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //kill messaging api here
        mApiClient.disconnect();
    }

    private void sendMessage(final String path, final String message) {
        Log.d("Message", "Sent!");
        new Thread( new Runnable() {
            @Override
            public void run() {
                mApiClient.blockingConnect(200, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, message.getBytes() );
//                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
//                            mApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}


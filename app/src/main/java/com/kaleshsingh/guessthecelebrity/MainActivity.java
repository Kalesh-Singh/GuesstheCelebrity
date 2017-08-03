package com.kaleshsingh.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // Source:
    // view-source:http://www.posh24.se/kandisar

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];

    ImageView imageView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;


    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while(data != -1){
                    char currentCharacter = (char) data;
                    result += currentCharacter;
                    data = inputStreamReader.read();
                }

                return result;
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class  ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void celebChosen(View view){
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),
                    "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();
        }

        createNewQuestion();

    }

    public void createNewQuestion(){
        Random rand = new Random();
        chosenCeleb = rand.nextInt(celebURLs.size());

        ImageDownloader imageDownloader = new ImageDownloader();
        Bitmap celebImage;

        try {
            celebImage = imageDownloader.execute(celebURLs.get(chosenCeleb)).get();

            imageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = rand.nextInt(4);


            int inCorrectAnswerLocation;

            for(int i = 0; i < 4; ++i){
                if(i == locationOfCorrectAnswer){
                    answers[i] = celebNames.get(chosenCeleb);
                }
                else{
                    inCorrectAnswerLocation = rand.nextInt(celebNames.size());

                    while(inCorrectAnswerLocation == chosenCeleb){
                        inCorrectAnswerLocation = rand.nextInt(celebNames.size());
                    }

                    answers[i] = celebNames.get(inCorrectAnswerLocation);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while(m.find()){
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()){
                celebNames.add(m.group(1));
            }

            // Create new question
            createNewQuestion();

        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        catch(ExecutionException e){
            e.printStackTrace();
        }

        // Test Data Collected

        /*for(String url : celebURLs){
            System.out.println(url);
        }

        for(String name  : celebNames){
            System.out.println(name);
        }*/


    }
}

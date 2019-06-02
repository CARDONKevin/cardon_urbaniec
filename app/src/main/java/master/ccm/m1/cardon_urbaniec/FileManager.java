package master.ccm.m1.cardon_urbaniec;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileManager {

    // on set le nom du fichier qui stokera nos url
    private static String NAMEFILE ="listeUrl.txt";

    //fonction permettant d'ecrire dans un fichier
    public static void writeToFile(String data, Context context) {


        try {
            //on ouvre le fichier pour ecrire


            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(NAMEFILE, Context.MODE_APPEND));
            // on ajoute à la fin du fichier l'url en question
            outputStreamWriter.append(data);
            outputStreamWriter.append(";");
            outputStreamWriter.close();

        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    //fonction de suppression des données dans le fichier
    public static void writeErased( Context context) {
        try {
            //on ouvre le fichier pour ecrire
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(NAMEFILE, Context.MODE_PRIVATE));
            //on fait un write pour remplacer le contenue par une chaine vide
            outputStreamWriter.write("");
            //on ferme le fichier
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    //fonction permettant de lire un fichier
    public static String[] readFromFile(Context context) {
        String[] ret = null;
        try {
            InputStream inputStream = context.openFileInput(NAMEFILE);
            if ( inputStream != null ) {
                //on ouvre le fichier pour le lire
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString ;
                String stringBuilder ="" ;
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    Log.i("Remi_LOG", "stringBuilder : " + receiveString);
                    stringBuilder += receiveString;


                }

                ret=stringBuilder.split(";");
                Log.i("Remi_LOG", "length ret : " + ret.length);
                //bufferedReader.close();
                inputStream.close();
                /*

                ArrayList<String> stringTabBuilder = new ArrayList<String>();

                ret = new String[stringTabBuilder.size()];
                int x = 0;
                for (String uneChaine : stringTabBuilder)
                {
                    ret[x] = uneChaine;
                    x++;
                }*/

            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }
}

package master.ccm.m1.cardon_urbaniec;

import android.app.Service;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe de notre service de téléchargement
 */
public class NotreServiceDeTelechargement extends Service {
    private NotreServiceDeTelechargement.NotreBinder notreBinder = new NotreServiceDeTelechargement.NotreBinder();
    private MainActivity mainActivity;
    private String nomDeFichier;
    private HashMap<Bitmap, String> mapNomFichier;
    private static final String EXTENSION = ".jpg";

    /**
     * Classe interne qui retourne la classe NotreServiceDeTelechargement
     */
    public class NotreBinder extends Binder {


        public NotreServiceDeTelechargement seConnecter(MainActivity activity) {
            mainActivity = activity;
            mapNomFichier = new HashMap<>();
            return NotreServiceDeTelechargement.this;
        }
    }

    /**
     * Ecoute la connexion
     * @param intent
     * @return Ibinder pour la liaison avec la connexion de l'activité
     */
    @Override
    public IBinder onBind(Intent intent) {
        return notreBinder;
    }

    /**
     * Lance la tâche de téléchargement asynchrone
     * @param url est une chaine définissant l'url à de l'image à récupérer
     */
    public void executeUneTacheDeTelechargement(String url){
        this.setNomDeFichier(url);
        new NotreServiceDeTelechargement.DownloadTask(this.getNomDeFichier()).execute(stringToURL(url));
    }

    public String getNomDeFichier() {
        return nomDeFichier;
    }

    public void setNomDeFichier(String nomDeFichier) {
        nomDeFichier = nomDeFichier.replace("/", "");

        if (nomDeFichier.length() >= 260){
            nomDeFichier = nomDeFichier.substring(0, 250);
        }

        this.nomDeFichier = nomDeFichier;
    }

    public String getUrlDuBitmapConcerne(Bitmap bitmap){
        return this.mapNomFichier.get(bitmap);
    }

    /**
     * Permet la conversion de chaine de caracteres en URL
     * @param urlString est la chaine à convertir
     */
    protected URL stringToURL(String urlString) {
        try {
            URL url = new URL(urlString);
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Permet la sauvegarde de l'image dans le stockage externe
     * @param bitmap est l'objet nécessaire à la sauvegarde de l'image
     */
    protected Uri saveImageToInternalStorage(Bitmap bitmap){
        // Récupération du contexte
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // Création du fichier de l'image à sauvegarder
        File file = new File(Environment.getExternalStorageDirectory(), this.mapNomFichier.get(bitmap) + EXTENSION);

        try{
            // Créer le flux de sortie du fichier
            OutputStream stream = null;

            // si le fichier existe, il est alors écrasé
            stream = new FileOutputStream(file);

            // Compression de l'objet Bitmap au format JPEG .jpg
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

            // Nettoyage de la diffusion
            stream.flush();

            // fermeture de la diffusion
            stream.close();

        }catch (IOException e)
        {
            // En cas d'erreur, l'affiché
            e.printStackTrace();
        }

        // Parser l'url de la galerie d'image en uri
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        // retourner la valeur de l'uri
        return savedImageURI;
    }

    /**
     * Classe interne s'occupant du téléchargement de l'image de façon asynchrone
     */
    private class DownloadTask extends AsyncTask<URL,Void,Bitmap> {
        private String nomFichier;

        public DownloadTask(String nomFichier) {
            this.nomFichier = nomFichier;
        }

        public String getNomFichier() {
            return nomFichier;
        }

        public void setNomFichier(String nomFichier) {
            this.nomFichier = nomFichier;
        }

        /**
         * Avant l'éxécution de la tâche.
         */
        protected void onPreExecute(){
            // Rien faire avant la tâche
        }

        /**
         * Télécharge en arrière plan, n'est pas fait dans le Thread de l'UI
         */
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;

            try{
                // Initialise une nouvelle connexion HTTP vers une url
                connection = (HttpURLConnection) url.openConnection();

                // connexion à l'url
                connection.connect();

                // Obtention du flux d'entrée à partir de la connexion http url
                InputStream inputStream = connection.getInputStream();

                /*
                         BufferedInputStream ajoute des fonctionnalités à un autre flux d’entrée, à savoir: la possibilité de mettre en tampon l'entrée et de supporter les méthodes de marquage et de réinitialisation.
                         Crée un BufferedInputStream et enregistre son argument, le flux d'entrée dans, pour une utilisation ultérieure.
                */
                // Initialise un nouveau BufferedInputStream à partir de InputStream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                         Bitmap decodeStream permet de décoder un flux d'entrée en un bitmap. Si le flux d'entrée est nul ou ne peut pas être utilisé pour décoder un bitmap, la fonction renvoie null.
                         Résultats: Bitmap: bitmap décodé, ou null si les données de l'image n'ont pas pu être décodées.
                */
                // Convertit un BufferedInputStream en un objet Bitmap
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // retourne le bitmap téléchargé
                return bmp;

            }catch(IOException e){
                // affichage de l'erreur en cas d'erreur
                e.printStackTrace();
            }finally{
                // déconnecte la requête http
                connection.disconnect();
            }
            return null;
        }

        /**
         * Quand l'éxécution de la tâche est fini, demande à l'activité de finir le processus du loader.
         * Permet de dire que maintenant l'activité peut sauver lier une image Uri à sa ImageView avec
         * stopProcessus
         */
        protected void onPostExecute(Bitmap result){
            mapNomFichier.put(result, getNomFichier());
            mainActivity.stopProcessus(result);
        }

    }
}

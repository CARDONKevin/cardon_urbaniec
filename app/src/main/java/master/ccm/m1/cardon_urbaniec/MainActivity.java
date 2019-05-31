package master.ccm.m1.cardon_urbaniec;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Activité principale de l'application
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Liste des variables
     */
    private NotreServiceDeTelechargement notreServiceTelechargement;
    private ProgressDialog mProgressDialog;
    private EditText editText;
    private ImageView mImageView;
    private ListView listViewImage;
    private Button boutonTelechargerListeChecked;
    private String[] tableauChaines;
    private int nombreATelecharger;
    private int nombreDejaTelecharger;
    private HashMap<String, ImageView> mapImageViewAppartenantUrl;
    /**
     * Instantiation des données à la création du composant
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapImageViewAppartenantUrl = new HashMap<>();
        nombreATelecharger = 0;
        nombreDejaTelecharger = 0;

        // affectation des objets de la vue
        editText = findViewById(R.id.editText_url_download);

        // initialise l'objet pour notre loader (Progres Dialog)
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Affectation du titre
        mProgressDialog.setTitle("AsyncTask");
        // Affectation du message contenu
        mProgressDialog.setMessage("Le téléchargement est en cours de traitement...");

        // Lie le service
        Intent intent = new Intent(this, NotreServiceDeTelechargement.class);
        bindService(intent, maConnexion, Context.BIND_AUTO_CREATE);

        //Récupération de la listView
        listViewImage = findViewById(R.id.listViewImage);

        //récuperation des urls des image
        tableauChaines = getResources().getStringArray(R.array.tableau_image);

        //adapter fais le lien entre la liste et le tableau d'image
        ArrayAdapter<String> monArrayAdapter = new ArrayAdapter(this, R.layout.descripteur_liste_image, R.id.tv_url_image, tableauChaines);
        listViewImage.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listViewImage.setAdapter(monArrayAdapter);

        boutonTelechargerListeChecked = findViewById(R.id.id_btn_telecharger_list_view_items);
        boutonTelechargerListeChecked.setEnabled(false);

        boutonTelechargerListeChecked.setOnClickListener(new Button.OnClickListener(){
            @Override

            public void onClick(View v) {
                displayProcessus();
                ListView notreLayout=  findViewById(R.id.listViewImage);
                int count = notreLayout.getChildCount();

                for (int i = 0; i < count; i++) {
                    View contenenurdeCheckBoxes = notreLayout.getChildAt(i);
                    View childCheckBox = ((ViewGroup) contenenurdeCheckBoxes).getChildAt(2);
                    if (childCheckBox instanceof AppCompatCheckBox) {
                        if (childCheckBox.isSelected()){
                            DemanderTelechargementItemListView(childCheckBox);
                            childCheckBox.setSelected(!childCheckBox.isSelected());
                            childCheckBox.setEnabled(false);
                        }
                    }
                }
            }});
    }


    /**
     * Vérification des permissions au démarrage
     * Permission d'écrire sur le stockage externe
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            //Demande la permission si non trouvé

            Log.d("PERMISSION_ANDROID", "Permission non attribuée dans l'environnement");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        } else {
            Log.d("PERMISSION_ANDROID", "Permission déjà attribuée à l'application");
        }
    }

    /**
     * Instancie notre connexion à notre service
     */
    private ServiceConnection maConnexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notreServiceTelechargement = ((NotreServiceDeTelechargement.NotreBinder) service).seConnecter(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notreServiceTelechargement = null;
        }
    };

    public void actualiserMessageProgressDialog(){
        mProgressDialog.setMessage("Le téléchargement est en cours de traitement..." +
                " Déjà " + this.nombreDejaTelecharger + " sur " +
                this.nombreATelecharger + " image(s) téléchargée(s)");
    }

    /**
     * Début du téléchargemment, affichage du loader tant que le téléchargement n'est pas fini
     */
    public void displayProcessus() {
        this.actualiserMessageProgressDialog();
        mProgressDialog.show();
    }

    /**
     * Fin du téléchargement, sauvegarde de l'image et fin du loader
     * @param result est un objet Bitmap
     */
    public void stopProcessus(Bitmap result) {
        this.nombreDejaTelecharger++;
        this.actualiserMessageProgressDialog();
        if (result != null) {
            // Sauvegarde l'image dans le stockage
            Uri imageInternalUri = this.notreServiceTelechargement.saveImageToInternalStorage(result);
            // affecte l'image télécharger
            ((ImageView) this.mapImageViewAppartenantUrl.get(notreServiceTelechargement.getUrlDuBitmapConcerne(result))).setImageURI(imageInternalUri);
        } else {
            // Notifie l'utilisateur en cas d'erreur de téléchargement
            Toast.makeText(this, "Error with the download of your image", Toast.LENGTH_LONG).show();
        }

        // Cacher le loader
        if (this.nombreATelecharger == this.nombreDejaTelecharger){
            mProgressDialog.dismiss();
            this.nombreDejaTelecharger = 0;
            this.nombreATelecharger = 0;
            boutonTelechargerListeChecked.setEnabled(false);

        }
    }

    /**
     * Demande le téléchargement au clic sur le bouton télécharger
     * Utilise la valeur de l'url pour le téléchargement
     * @param view est un objet View
     */
    public void DemanderTelechargement(View view) {
        this.notreServiceTelechargement.executeUneTacheDeTelechargement(editText.getText().toString());
    }

    public void DemanderTelechargementItemListView(View view) {
        String url = ((TextView) ((LinearLayout) view.getParent()).getChildAt(1)).getText().toString();
        this.notreServiceTelechargement.executeUneTacheDeTelechargement(url);
        mImageView = (ImageView) ((LinearLayout) view.getParent()).getChildAt(0);

        url = url.replace("/", "");

        if (url.length() >= 260){
            url = url.substring(0, 250);
        }
        this.mapImageViewAppartenantUrl.put(url, mImageView);
    }

    /**
     * Change le status de la checkbox en checked ou unchecked en cas de clic dessus
     * Vérifie aussi si le bouton télécharger de la liste doit être disable ou enable
     * si au moins une seule est checked, bouton telecharger enable = true
     * augmente de 1 le compteur de telechargement si une coche est cochée, sinon décremente de 1
     * @param view est un objet View
     */
    public void ChangeStatus(View view) {
        CheckBox cb = (CheckBox) view;
        cb.setSelected(!cb.isSelected());

        if (cb.isSelected()){
            this.nombreATelecharger++;
        }
        else {
            this.nombreATelecharger--;
        }

        boolean siUneCaseEstCache = false;
        ListView notreLayout=  findViewById(R.id.listViewImage);
        int count = notreLayout.getChildCount();

        for (int i = 0; i < count; i++) {
            View contenenurdeCheckBoxes = notreLayout.getChildAt(i);
            View childCheckBox = ((ViewGroup) contenenurdeCheckBoxes).getChildAt(2);
            if (childCheckBox instanceof AppCompatCheckBox) {
                if (childCheckBox.isSelected()){
                    siUneCaseEstCache = true;
                }
            }
        }

        if (siUneCaseEstCache){
            boutonTelechargerListeChecked.setEnabled(true);
        }
        else {
            boutonTelechargerListeChecked.setEnabled(false);
        }
    }

    public void cliquerSurUnLayoutPourVoirImage(View view) {
        Intent intent = new Intent(this, VoirImageActivite.class);
        Bundle bundle = new Bundle();
        bundle.putString("UrlDeMonImageFormatString", ((TextView)((ViewGroup) view).getChildAt(1)).getText().toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}

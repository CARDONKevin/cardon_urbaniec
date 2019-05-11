package master.ccm.m1.cardon_urbaniec;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    private String[] tableauChaines;
    /**
     * Instantiation des données à la création du composant
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // affectation des objets de la vue
        mImageView = findViewById(R.id.imageView_image_afficher);
        editText = findViewById(R.id.editText_url_download);

        // initialise l'objet pour notre loader (Progres Dialog)
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Affectation du titre
        mProgressDialog.setTitle("AsyncTask");
        // Affectation du message contenu
        mProgressDialog.setMessage("Please wait, we are downloading your image file...");

        // Lie le service
        Intent intent = new Intent(this, NotreServiceDeTelechargement.class);
        bindService(intent, maConnexion, Context.BIND_AUTO_CREATE);

        //Récupération de la listView
        listViewImage = findViewById(R.id.listViewImage);

        //récuperation des urls des image
        tableauChaines = getResources().getStringArray(R.array.tableau_image);

        //adapter fais le lien entre la liste et le tableau d'image
        ArrayAdapter<String> monArrayAdapter = new ArrayAdapter(this, R.layout.descripteur_liste_image, R.id.tv_url_image, tableauChaines);
        listViewImage.setAdapter(monArrayAdapter);

        listViewImage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String chaineSelectionnee = tableauChaines[position];
                Toast.makeText(view.getContext(), chaineSelectionnee, Toast.LENGTH_LONG).show();

            }
        });
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

    /**
     * Début du téléchargemment, affichage du loader tant que le téléchargement n'est pas fini
     */
    public void displayProcessus() {
        mProgressDialog.show();
    }

    /**
     * Fin du téléchargement, sauvegarde de l'image et fin du loader
     * @param result est un objet Bitmap
     */
    public void stopProcessus(Bitmap result) {
        if (result != null) {
            // Sauvegarde l'image dans le stockage
            Uri imageInternalUri = this.notreServiceTelechargement.saveImageToInternalStorage(result);
            // affecte l'image télécharger
            mImageView.setImageURI(imageInternalUri);
        } else {
            // Notifie l'utilisateur en cas d'erreur de téléchargement
            Toast.makeText(this, "Error with the download of your image", Toast.LENGTH_LONG).show();
        }

        // Cacher le loader
        mProgressDialog.dismiss();
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
        this.notreServiceTelechargement.executeUneTacheDeTelechargement(((TextView) ((LinearLayout) view.getParent()).getChildAt(1)).getText().toString());
        //Toast.makeText(view.getContext(),"click", Toast.LENGTH_LONG).show();
        mImageView = (ImageView) ((LinearLayout) view.getParent()).getChildAt(0);
        Log.i("LogRemiViewParent", ((TextView) ((LinearLayout) view.getParent()).getChildAt(1)).getText().toString());
    }
}

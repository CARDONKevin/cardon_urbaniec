package master.ccm.m1.cardon_urbaniec;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class VoirImageActivite extends AppCompatActivity {
    private static final String EXTENSION = ".jpg";
    private TextView urlDeNotreImage;
    private ImageView imageViewDeNotreImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voir_image_activite);

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString("UrlDeMonImageFormatString");

        urlDeNotreImage = findViewById(R.id.id_text_view_url_visible);
        urlDeNotreImage.setText(url);

        String nomDeFichier = url.replace("/", "");

        if (nomDeFichier.length() >= 260){
            nomDeFichier = nomDeFichier.substring(0, 250);
        }

        imageViewDeNotreImage = findViewById(R.id.imageView_image_en_grand);
        imageViewDeNotreImage.setImageURI(Uri.parse(Environment.getExternalStorageDirectory().toString()+"/"+nomDeFichier+EXTENSION));
    }

    public void FermerActiviteVisible(View view) {
        finish();
    }
}

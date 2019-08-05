package com.geneowak.alc_4_challenge_2;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    public static final int PICTURE_RESULT = 42;
    private FirebaseDatabase eFirebaseDatabase;
    private DatabaseReference eDatabaseReference;
    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;
    ImageView eImageView;
    TravelDeal eDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        FirebaseUtil.openFbReference("traveldeals", this);
        eFirebaseDatabase = FirebaseUtil.sFirebaseDatabase;
        eDatabaseReference = FirebaseUtil.sDatabaseReference;
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtPrice = findViewById(R.id.txtPrice);
        eImageView = findViewById(R.id.image);

        Intent intent = getIntent();
        eDeal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (eDeal == null) {
            eDeal = new TravelDeal();
        }
        txtTitle.setText(eDeal.getTitle());
        txtDescription.setText(eDeal.getDescription());
        txtPrice.setText(eDeal.getPrice());
        showImage(eDeal.getImageUrl());
        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.sStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String url = ref.getDownloadUrl().toString();
                    String picName = taskSnapshot.getMetadata().getReference().getPath();
                    eDeal.setImageUrl(url);
                    eDeal.setImageName(picName);
                    Log.d("url: ", url);
                    Log.d("Name", picName);
                    showImage(url);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                backToList();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTitle.requestFocus();
    }

    private void saveDeal() {
        eDeal.setTitle(txtTitle.getText().toString());
        eDeal.setDescription(txtDescription.getText().toString());
        eDeal.setPrice(txtPrice.getText().toString());

        if (eDeal.getId() == null) {
            eDatabaseReference.push().setValue(eDeal);
        } else {
            eDatabaseReference.child(eDeal.getId()).setValue(eDeal);
        }
    }

    public void deleteDeal() {
        if (eDeal == null) {
            Toast.makeText(this, "Please save the deal before deleting.", Toast.LENGTH_LONG).show();
            return;
        }
        eDatabaseReference.child(eDeal.getId()).removeValue();
        Log.d("image name: ", "eDeal.getImageName()");
        if (eDeal.getImageName() != null && !eDeal.getImageName().isEmpty()) {
            StorageReference picRef = FirebaseUtil.sStorage.getReference().child(eDeal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image ", "Image deleted successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image: ", e.getMessage());
                }
            });
        }
    }

    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        // enable edit menus only for admins
        menu.findItem(R.id.delete_menu).setVisible(FirebaseUtil.isAdmin);
        menu.findItem(R.id.save_menu).setVisible(FirebaseUtil.isAdmin);
        enableEditTexts(FirebaseUtil.isAdmin);
        return true;
    }

    private void enableEditTexts(boolean isEnabled) {
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
//            Picasso.get().load(url).resize(width, width * 2 / 3).centerCrop().into(eImageView);
            Picasso.get()
                    .load(url)
                    .resize(width, width * 2 / 3)
                    .centerCrop()
                    .into(eImageView);
        }
    }
}

/*
 * Generate keystore
 * keytool -genkey -alias androiddebugkey1 -keyalg RSA -keystore keystore.jks
 */
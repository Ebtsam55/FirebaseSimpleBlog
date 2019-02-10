package com.example.firebaseblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static com.theartofdev.edmodo.cropper.CropImage.activity;

public class SetupActivity extends AppCompatActivity {

    private EditText setupNameField;
    private ImageButton setupImageButton;
    private Button setupSubmitButton;
    private Uri imageUri=null;

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorageImages ;

    private ProgressDialog progressDialog;

    final static int GALLERY_REQUEST = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupImageButton = findViewById(R.id.setupImage);
        setupNameField = findViewById(R.id.setupName);
        setupSubmitButton = findViewById(R.id.setupSubmitButton);

        progressDialog=new ProgressDialog(this);

        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        mStorageImages=FirebaseStorage.getInstance().getReference().child("profile_images");

        setupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccout();
            }
        });

        setupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri mImageUri = data.getData();

    // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

    // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(mImageUri)
                    .start(this);

    // for fragment (DO NOT use `getActivity()`)
            CropImage.activity()
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                setupImageButton.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void startSetupAccout()
    {
        final String setupName = setupNameField.getText().toString();
        final String userUid=mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(setupName)&& imageUri!=null)
        {
            progressDialog.setMessage("Finishing Setup ...");
            progressDialog.show();

           StorageReference filePath = mStorageImages.child(imageUri.getLastPathSegment());
           filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   final Task<Uri> task =taskSnapshot.getMetadata().getReference().getDownloadUrl();
                   task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                       @Override
                       public void onSuccess(Uri uri) {

                           if(task.isSuccessful())
                           {
                               mDatabaseUsers.child(userUid).child("name").setValue(setupName);
                               mDatabaseUsers.child(userUid).child("image").setValue(uri.toString());

                               progressDialog.dismiss();

                               Intent homeIntent = new Intent(SetupActivity.this, HomeActivity.class);
                               homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                               startActivity(homeIntent);

                           }

                       }
                   });
               }
           });



        }

    }

}

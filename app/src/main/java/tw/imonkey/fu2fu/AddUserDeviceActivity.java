package tw.imonkey.fu2fu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static tw.imonkey.fu2fu.MainActivity.devicePrefs;

public class AddUserDeviceActivity extends AppCompatActivity {
    private static final int RC_CHOOSE_PHOTO = 101;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE =102 ;
    //private static final int RC_IMAGE_PERMS = 103;
    StorageReference mImageRef;
    DatabaseReference mAddUserFile, mAddUserDevice;
    String memberEmail,deviceId ,token;// deviceId=shopId=topics_id
    ImageView imageViewAddUser;
    Uri selectedImage ;
    EditText editTextAddCompanyId;
    EditText editTextAddUser;
    EditText editTextAddDescription;
    EditText editTextAddPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_device);
        Bundle extras = getIntent().getExtras();
        memberEmail =extras.getString("memberEmail");
        imageViewAddUser=(ImageView)(findViewById(R.id.imageViewAddUser));
        editTextAddCompanyId = (EditText) (findViewById(R.id.editTextAddCompanyId));
        editTextAddUser = (EditText) (findViewById(R.id.editTextAddUser));
        editTextAddDescription = (EditText) (findViewById(R.id.editTextAddDescription));
        editTextAddPhone = (EditText) (findViewById(R.id.editTextPhone));
        if (ContextCompat.checkSelfPermission(AddUserDeviceActivity.this,
                READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddUserDeviceActivity.this,
                    READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(AddUserDeviceActivity.this,
                        new String[]{READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void addDevice(View view){
        String companyId = editTextAddCompanyId.getText().toString().trim();
        String username = editTextAddUser.getText().toString().trim();
        String description = editTextAddDescription.getText().toString().trim();
        String phone = editTextAddPhone.getText().toString().trim();
        if (!(TextUtils.isEmpty(phone) || TextUtils.isEmpty(username)|| TextUtils.isEmpty(description))) {
            mAddUserDevice = FirebaseDatabase.getInstance().getReference("/DEVICE/");
            deviceId =mAddUserDevice.push().getKey();
            Map<String, Object> addDevice = new HashMap<>();
            addDevice.put("companyId",companyId);
            addDevice.put("device",username);
            addDevice.put("phone",phone);
            addDevice.put("deviceType","主機");
            addDevice.put("description",description);
            addDevice.put("masterEmail",memberEmail) ;
            Map<String, Object> user = new HashMap<>();
            user.put(memberEmail.replace(".","_"),memberEmail);
            addDevice.put("users",user);
            addDevice.put("timeStamp", ServerValue.TIMESTAMP);
            addDevice.put("topics_id",deviceId) ;
            mAddUserDevice.child(deviceId).setValue(addDevice);

            mAddUserFile= FirebaseDatabase.getInstance().getReference("/USER/" +memberEmail.replace(".", "_"));
            token = FirebaseInstanceId.getInstance().getToken();
            Map<String, Object> addUser = new HashMap<>();
            addUser.put("memberEmail",memberEmail);
            addUser.put("deviceId",deviceId);
            addUser.put("username",username);
            addUser.put("phone",phone);
            addUser.put("token",token);
            addUser.put("timeStamp", ServerValue.TIMESTAMP);
            mAddUserFile.setValue(addUser);
            FirebaseMessaging.getInstance().subscribeToTopic(deviceId);
            SharedPreferences.Editor editor = getSharedPreferences(devicePrefs, Context.MODE_PRIVATE).edit();
            editor.putString("deviceId",deviceId);
            editor.putString("myEmail",memberEmail);
            editor.apply();
            if (selectedImage!=null) {
                uploadPhoto(selectedImage);
            }
            Toast.makeText(this, "add user...", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                selectedImage = data.getData();
                imageViewAddUser.setImageURI(selectedImage);

            } else {
                Toast.makeText(this, "No image chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void choosePhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    protected void uploadPhoto(Uri uri) {

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        // Upload to Firebase Storage
        String devicePhotoPath = "/devicePhoto/"+deviceId;
        mImageRef = FirebaseStorage.getInstance().getReference(devicePhotoPath);
        mImageRef.putFile(uri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //      Log.d("TAG", "uploadPhoto:onSuccess:" +
                        //              taskSnapshot.getMetadata().getReference().getPath());
                        Toast.makeText(AddUserDeviceActivity.this, "Image uploaded",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AddUserDeviceActivity.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);


                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "uploadPhoto:onError", e);
                        Toast.makeText(AddUserDeviceActivity.this, "Upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


package com.example.todooo.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.todooo.LoginActivity;
import com.example.todo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {
    View view;
    TextView tvName, tvEmail;
    ImageView ivEditName, ivEditEmail, ivEditPassword;
    Button btnLogout;

    // view for image view
    private CircleImageView imageView;

    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    StorageReference image;

    String path = "/data/user/0/com.example.todooo/app_imageDir/";

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    String UID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivEditEmail = view.findViewById(R.id.ivEditEmail);
        ivEditName = view.findViewById(R.id.ivEditName);
        ivEditPassword = view.findViewById(R.id.ivEditPassword);
        btnLogout = view.findViewById(R.id.btnLogout);
        imageView = view.findViewById(R.id.circleImageView);


        // [CHECK ĐĂNG NHẬP CHƯA?]
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        } else {
            UID = currentUser.getUid();
        }
        // [KẾT THÚC CHECK ĐĂNG NHẬP CHƯA?]
        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);

        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        tvEmail.setText(email);
        tvName.setText(name);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
        ivEditName.setOnClickListener(v -> updateName(name, currentUser, tvName));
        ivEditEmail.setOnClickListener(v -> updateEmail(email, currentUser, tvEmail));
        ivEditPassword.setOnClickListener(v -> updatePassWord(email, currentUser));

        //UPLOAD IMAGE
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        image = storage.getReference().child("images/" + UID + ".png");
        loadImageFromStorage(path,imageView);
        try {
            File file = File.createTempFile("image", ".png");
            image.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    saveToInternalStorage(bitmap);
//                    imageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageView.setOnClickListener(v -> {
            if(ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
            }
            else {
                startGallery();
            }
        });

        return view;
    }


    private void startGallery() {
        Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        cameraIntent.setType("image/*");
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, 1000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super method removed
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                filePath = data.getData();
                Bitmap bitmapImage = null;
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                    saveToInternalStorage(bitmapImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(bitmapImage);
                uploadImage();
            }
        }
    }


    // UploadImage method
    private void uploadImage() {
        if (filePath != null) {
            // Defining the child of storageReference
            StorageReference ref = storageReference.child("images/" + UID + ".png");
            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myPath=new File(directory,UID + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path, ImageView iv)
    {

        try {
            File f=new File(path, UID + ".png");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            ImageView img=(ImageView)findViewById(R.id.imgPicker);
            iv.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    private void updatePassWord(String email, FirebaseUser currentUser) {
        final Dialog dialog = createDialog(R.layout.dialog_update_info);

        EditText et = dialog.findViewById(R.id.etUpdate);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);

        tvTitle.setText(getResources().getString(R.string.enter_old_password));

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String password = et.getText().toString().trim();
            if (!TextUtils.isEmpty(password)) {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, password);

                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                openEditPassDialog(currentUser);
                            }
                        });
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), R.string.error_pass, Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();

    }

    private void openEditPassDialog(FirebaseUser currentUser) {
        final Dialog dialog = createDialog(R.layout.dialog_update_info);

        EditText et = dialog.findViewById(R.id.etUpdate);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);

        tvTitle.setText(getResources().getString(R.string.enter_password));

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String password = et.getText().toString().trim();
            if (!TextUtils.isEmpty(password)) {
                currentUser.updatePassword(password)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), R.string.error_pass, Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();
    }


    private void updateEmail(String info, FirebaseUser currentUser, TextView tv) {
        final Dialog dialog = createDialog(R.layout.dialog_update_info);

        EditText et = dialog.findViewById(R.id.etUpdate);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);

        tvTitle.setText(getResources().getString(R.string.enter_password));

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String password = et.getText().toString().trim();
            if (!TextUtils.isEmpty(password)) {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(info, password);

                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                openEditEmailDialog(info, currentUser, tv);
                            }
                        });
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), R.string.error_pass, Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();

    }

    private void openEditEmailDialog(String info, FirebaseUser currentUser, TextView tv) {
        final Dialog dialog = createDialog(R.layout.dialog_update_info);

        EditText et = dialog.findViewById(R.id.etUpdate);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);

        et.setText(info);
        tvTitle.setText(getResources().getString(R.string.update_email));

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String newEmail = et.getText().toString().trim();
            if (!TextUtils.isEmpty(newEmail)) {
                currentUser.updateEmail(newEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    tv.setText(newEmail);
                                    Toast.makeText(getContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), R.string.update_fail, Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();
    }

    private void updateName(String info, FirebaseUser currentUser, TextView tv) {
        final Dialog dialog = createDialog(R.layout.dialog_update_info);

        EditText et = dialog.findViewById(R.id.etUpdate);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);

        et.setText(info);
        tvTitle.setText(getResources().getString(R.string.update_name));

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String newName = et.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

                currentUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    tv.setText(newName);
                                    Toast.makeText(getContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), R.string.update_fail, Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();
    }

    private Dialog createDialog(int menu) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(menu);

        Window window = dialog.getWindow();
        if (window == null) {
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttr = window.getAttributes();
        windowAttr.gravity = Gravity.CENTER;
        window.setAttributes(windowAttr);
        dialog.setCancelable(true);
        return dialog;
    }

}
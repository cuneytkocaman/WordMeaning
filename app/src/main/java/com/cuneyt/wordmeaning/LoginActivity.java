package com.cuneyt.wordmeaning;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cuneyt.wordmeaning.assistantclass.DateTime;
import com.cuneyt.wordmeaning.assistantclass.MobileDeviceName;
import com.cuneyt.wordmeaning.entities.ErrorLogModel;
import com.cuneyt.wordmeaning.entities.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private UserModel userModel = new UserModel();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceError;
    private MobileDeviceName deviceName = new MobileDeviceName();
    private DateTime dateTime = new DateTime();
    private ErrorLogModel errorLogModel = new ErrorLogModel();

    private TextView textBtLogin, textBtRegisterPage;
    private EditText editLoginName, editLoginPassword;

    private void visualObject(){
        textBtLogin = findViewById(R.id.textBtLogin);
        textBtRegisterPage = findViewById(R.id.textBtRegisterPage);
        editLoginName = findViewById(R.id.editLoginName);
        editLoginPassword = findViewById(R.id.editLoginPassword);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            internetCheck();

            try {

                visualObject();

                firebaseAuth = FirebaseAuth.getInstance();
                firebaseUser = firebaseAuth.getCurrentUser();

                textBtLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loginUser();
                    }
                });

                textBtRegisterPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent regIntent = new Intent(LoginActivity.this, ResigterActivity.class);
                        startActivity(regIntent);
                    }
                });

            } catch (Exception e){

                String mobileDevName = deviceName.deviceName().toString();
                String err = String.valueOf(e);
                String date = dateTime.currentlyDateTime("dd.MM.yyyy HH:mm:ss");

                errorLogModel = new ErrorLogModel(mobileDevName, "LoginActivity", err, date);
                referenceError = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_error));
                referenceError.push().setValue(errorLogModel);
            }

            return insets;
        });
    }

    public void loginUser(){
        String inputUser = editLoginName.getText().toString() + "@word.com";
        String inputPass = editLoginPassword.getText().toString();

        userModel.setName(inputUser);
        userModel.setPassword(inputPass);

        if (!TextUtils.isEmpty(inputUser) && !TextUtils.isEmpty(inputPass)){

            firebaseAuth.signInWithEmailAndPassword(userModel.getName(), userModel.getPassword())
                    .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intentMain);
                            finish();

                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Kullanıcı adı veya parola hatalı.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Kullancı adı veya Parola boş geçilemez.", Toast.LENGTH_LONG).show();
        }
    }

    public void onStart() {

        if (firebaseUser != null) {

            Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intentMain);
            finish();
        }
        super.onStart();
    }

    private void internetCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {


        } else {
            AlertDialog.Builder builderNet = new AlertDialog.Builder(LoginActivity.this);
            //  builderNet.setMessage("İnternet bağlantınızı kontrol edin.");
            builderNet.setCancelable(true);
            View viewClose = getLayoutInflater().inflate(R.layout.alert_internet_check, null);

            TextView textBtAlertYes = viewClose.findViewById(R.id.textBtAlertNetYes);

            builderNet.setView(viewClose);

            AlertDialog alertDialog = builderNet.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            textBtAlertYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            alertDialog.show();
        }
    }
}
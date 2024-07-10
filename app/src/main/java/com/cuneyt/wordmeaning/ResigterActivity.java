package com.cuneyt.wordmeaning;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.cuneyt.wordmeaning.assistantclass.MonthYear;
import com.cuneyt.wordmeaning.entities.ErrorLogModel;
import com.cuneyt.wordmeaning.entities.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResigterActivity extends AppCompatActivity {

    private UserModel userModel = new UserModel();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceUser, referenceError;
    private ErrorLogModel errorLogModel = new ErrorLogModel();
    private MonthYear monthYear = new MonthYear();
    private DateTime dateTime = new DateTime();
    MobileDeviceName deviceName = new MobileDeviceName();
    private TextView textBtRegister;
    private EditText editRegName, editRegPassword, editRegPasswordAgain;

    private void visualObject(){
        textBtRegister = findViewById(R.id.textBtRegister);
        editRegName = findViewById(R.id.editRegName);
        editRegPassword = findViewById(R.id.editRegPassword);
        editRegPasswordAgain = findViewById(R.id.editRegPasswordAgain);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resigter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            internetCheck();

            try {
                visualObject();

                firebaseAuth = FirebaseAuth.getInstance();

                textBtRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String regName = editRegName.getText().toString();
                        String pass = editRegPassword.getText().toString();
                        String passAgain = editRegPasswordAgain.getText().toString();

                        int passLenght = pass.length();
                        int regNameLehght = regName.length(); // İsmin ve parolanın uzunlukları alındı.

                        if (TextUtils.isEmpty(editRegName.getText().toString()) ||
                                TextUtils.isEmpty(editRegPassword.getText().toString()) ||
                                TextUtils.isEmpty(editRegPasswordAgain.getText().toString())) {

                            Toast.makeText(ResigterActivity.this, "Alanlar boş geçilemez.", Toast.LENGTH_SHORT).show();

                        } else if (passLenght < 6){

                            Toast.makeText(ResigterActivity.this, "Parola en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show();

                        } else if (!pass.equals(passAgain)){

                            Toast.makeText(ResigterActivity.this, "Parolalar aynı değil.", Toast.LENGTH_SHORT).show();

                        } else if (regNameLehght <= 2){

                            Toast.makeText(ResigterActivity.this, "Kullanıcı adı 3 karakterden uzun olmalıdır.", Toast.LENGTH_SHORT).show();

                        } else {
                            registerUser();
                        }
                    }
                });
            } catch (Exception e){

                String mobileDevName = deviceName.deviceName().toString();
                String err = String.valueOf(e);
                String date = dateTime.currentlyDateTime("dd.MM.yyyy HH:mm:ss");

                errorLogModel = new ErrorLogModel(mobileDevName, "RegisterActivity", err, date);
                referenceError = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_error));
                referenceError.push().setValue(errorLogModel);
            }


            return insets;
        });
    }

    private void registerUser(){

        String date = monthYear.currentlyDateTime("dd.MM.yyyy HH:mm:ss").toString();
        String inputUserName = editRegName.getText().toString() + "@word.com";
        String inputPass = editRegPassword.getText().toString();

        userModel.setName(inputUserName);
        userModel.setPassword(inputPass);
        userModel.setMemberDateTime(date);

        checkUserName(inputUserName); // Aynı isimli kullanıcı var mı yok mu kontrol edildi.

        firebaseAuth.createUserWithEmailAndPassword(userModel.getName(), userModel.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // addCompleteListener: İşlemin durumu hakkında bilgi verir.
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){ // Kayıt başarılı ise çalışır ve kullanıcı kaydı tamamlanır.

                            firebaseUser = firebaseAuth.getCurrentUser(); // Giriş yapan kullanıcı
                            referenceUser = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.db_user));

                            referenceUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String id = firebaseUser.getUid().toString();
                                    String userName = editRegName.getText().toString();
                                    String member = userModel.getMemberDateTime().toString();

                                    userModel = new UserModel(id, userName, member);

                                    referenceUser.child(id).setValue(userModel); // Kullanıcı DB'ye eklendi.

                                    Intent intentLogin = new Intent(ResigterActivity.this, MainActivity.class);
                                    //intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intentLogin);
                                    finish();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            Toast.makeText(ResigterActivity.this, "Kayıt başarılı.", Toast.LENGTH_LONG).show();

                        } else {

                            Toast.makeText(ResigterActivity.this, "Kayıt başarısız.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkUserName(String againName){
        firebaseAuth.fetchSignInMethodsForEmail(againName).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()){

                    boolean check = !task.getResult().getSignInMethods().isEmpty();

                    if (!check){

                        //     Toast.makeText(RegisterActivity.this, "Başarılı", Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(ResigterActivity.this, "Böyle bir kullanıcı mevcut.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void internetCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {


        } else {
            AlertDialog.Builder builderNet = new AlertDialog.Builder(ResigterActivity.this);
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
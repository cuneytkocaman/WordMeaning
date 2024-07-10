package com.cuneyt.wordmeaning;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cuneyt.wordmeaning.adapter.WordRvAdapter;
import com.cuneyt.wordmeaning.assistantclass.RandomId;
import com.cuneyt.wordmeaning.entities.UserModel;
import com.cuneyt.wordmeaning.entities.WordModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private UserModel userModel = new UserModel();
    private WordModel wordModel = new WordModel();
    private RandomId randomId = new RandomId();
    private DatabaseReference referenceUser, referenceWord, referenceLang;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ArrayList<WordModel> wordModels = new ArrayList<>();
    private WordRvAdapter wordAdapter;
    private RecyclerView rvWord;
    private TextView txtBtLangChoice, txtWordCount;
    private FloatingActionButton fabBtAdd, fabBtInfo, fabBtLogout;
    private SearchView searchView;

    private void visualObject() {
        rvWord = findViewById(R.id.rvWord);
        txtBtLangChoice = findViewById(R.id.txtBtLangChoice);
        fabBtAdd = findViewById(R.id.fabBtAdd);
        txtWordCount = findViewById(R.id.txtWordCount);
        fabBtLogout = findViewById(R.id.fabBtLogout);
        searchView = findViewById(R.id.searchView);
        fabBtInfo = findViewById(R.id.fabBtInfo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            visualObject();

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser(); // Oturum açmış kullanıcı alındı.
            referenceUser = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_user)); // Kullanıcı tablosu oluşturuldu.
            referenceWord = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_word));
            referenceLang = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_language_choice));

            fabBtAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  Toast.makeText(MainActivity.this, "Dil: " + languages.get(spLanguage.getSelectedItemPosition()), Toast.LENGTH_SHORT).show();

                    if (txtBtLangChoice.getText().toString().equals("Dil Seçin")) {
                        Toast.makeText(MainActivity.this, "Dil Seçiniz.", Toast.LENGTH_SHORT).show();

                    } else {
                        add();
                    }
                }

            });

            txtBtLangChoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    languageChoice();
                }
            });

            fabBtLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });

            fabBtInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                    startActivity(intent);
                }
            });

            readLanguage();
            search();
            fabButtonHideinRecyclerView();

            return insets;
        });
    }

    public void add() {

        AlertDialog.Builder builderAdd = new AlertDialog.Builder(MainActivity.this);
        View viewAdd = getLayoutInflater().inflate(R.layout.alert_word_add, null);

        EditText editWord = viewAdd.findViewById(R.id.editWord);
        EditText editMeaning = viewAdd.findViewById(R.id.editMeaning);
        EditText editDescription = viewAdd.findViewById(R.id.editDescription);
        //    TextView txtChoiseLangTitle = viewAdd.findViewById(R.id.txtChoiseLangTitle);
        TextView txtBtYes = viewAdd.findViewById(R.id.txtBtYes);
        TextView txtBtNo = viewAdd.findViewById(R.id.txtBtNo);

        //     txtChoiseLangTitle.setText(txtBtLangChoise.getText().toString());

        builderAdd.setView(viewAdd);

        AlertDialog dialogAdd = builderAdd.create();
        dialogAdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        txtBtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editWord.getText().toString()) ||
                        TextUtils.isEmpty(editMeaning.getText().toString())) { // Başlık ve Tutar bilgilerinin boş geçilmemesi sağlandı.

                    Toast.makeText(MainActivity.this, "Bilgileri giriniz.", Toast.LENGTH_SHORT).show();

                } else {

                    String randId = randomId.randomUUID();

                    referenceUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String word = editWord.getText().toString();
                            String meaning = editMeaning.getText().toString();
                            String desc = editDescription.getText().toString();
                            String lang = txtBtLangChoice.getText().toString();

                            Spannable spWord = new SpannableString(word); // String parçalama sınıfı.
                            Spannable spMeaning = new SpannableString(meaning);
                            String wordLetter = spWord.subSequence(0, 1).toString(); // Her kök ID'sinin başına eklenmesi için başlığın 1. harfi alındı. Bu işlem Firebase'de okumayı kolaylaştırmak için yapıldı.
                            String meaningLetter = spMeaning.subSequence(0, 1).toString();

                            String uniqueId = wordLetter + meaningLetter + "-" + randId;

                            wordModel = new WordModel(uniqueId, lang, word, meaning, desc);

                            referenceWord.child(uniqueId).setValue(wordModel);

                            dialogAdd.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        txtBtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAdd.dismiss();
            }
        });

        dialogAdd.show();
    }

    public void show(String lang) {

        rvWord.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, true); // VERTICAL, true: RecyclerView'e eklenen veri en alttan üste doğru eklenir.
        linearLayoutManager.setStackFromEnd(true); // RecyclerView'e eklenen veri sayfayı otomatik kaydırır.
        rvWord.setItemAnimator(new DefaultItemAnimator());
        rvWord.setLayoutManager(linearLayoutManager);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewInflater = inflater.inflate(R.layout.design_row_word, null); // Tasarım bağlandı.

        wordAdapter = new WordRvAdapter(this, wordModels);
        rvWord.setAdapter(wordAdapter);

        String userId = firebaseUser.getUid().toString();

        referenceWord = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_word)).child(userId).child(lang);

        referenceWord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                wordModels.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    WordModel wordM = dataSnapshot.getValue(WordModel.class);

                    wordModels.add(wordM);

                    rvItemCount();
                }


                Collections.sort(wordModels, new Comparator<WordModel>() { //RecyclerView A->Z sıralama
                    @Override
                    public int compare(WordModel wModel, WordModel t1) {
                        return t1.getWord().compareToIgnoreCase(wModel.getWord());
                        // return Integer.compare(t1.getNumber(), ggModel.getNumber());
                    }
                });

                wordAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void languageChoice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.alert_language_choice, null);

        RadioGroup radioGroupLang = view.findViewById(R.id.radioGroupLang);
        TextView txtBtYes = view.findViewById(R.id.txtBtYes);
        TextView txtBtNo = view.findViewById(R.id.txtBtNo);

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

        radioGroupLang.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
            }
        });

        txtBtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int radioSelectId = radioGroupLang.getCheckedRadioButtonId();

                if (radioSelectId == -1) {
                    Toast.makeText(MainActivity.this, "Dil Seçiniz.", Toast.LENGTH_SHORT).show();
                } else {
                    RadioButton radioButtonLang = radioGroupLang.findViewById(radioSelectId);

                    String radioLang = radioButtonLang.getText().toString();

                    txtBtLangChoice.setText(radioLang);

                    show(radioLang);

                    String choiceId = "Choice";

                    referenceLang = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_language_choice));

                    referenceUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String currentUserId = firebaseUser.getUid().toString();

                            HashMap<String, Object> updLanguage = new HashMap<>();
                            updLanguage.put("id", choiceId);
                            updLanguage.put("language", radioLang);

                            referenceLang.child(currentUserId).child(choiceId).updateChildren(updLanguage);

                            rvItemCount();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    dialog.dismiss();
                }
            }
        });

        txtBtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void readLanguage() {
        String currentUserId = firebaseUser.getUid().toString();

        referenceLang = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.db_language_choice)).child(currentUserId);

        referenceLang.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    if (dataSnapshot.equals(null)) {

                    } else {

                        WordModel wordModel1 = dataSnapshot.getValue(WordModel.class);
                        String lang = wordModel1.getLanguage().toString();

                        show(lang);
                        rvItemCount();

                        txtBtLangChoice.setText(lang);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void fabButtonHideinRecyclerView(){

        rvWord.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    fabBtAdd.show();
                    txtWordCount.setVisibility(View.VISIBLE);
                } else if (dy > 0) {
                    fabBtAdd.hide();
                    txtWordCount.setVisibility(View.INVISIBLE);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void rvItemCount() { // RecyclerView'deki elemanları saymak için yapılan işlemler.

        String currentUserId = firebaseAuth.getCurrentUser().getUid(); // Diecast'ler geçerli kullanıcı ID'si altında tutulduğu için, geçerli kullanıcının ID'si alındı.

        referenceWord = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_word)); // Diecast sayısını öğrenmek için DB'ye bağlanıldı.

        referenceWord.child(currentUserId).child(txtBtLangChoice.getText().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int countWord = (int) snapshot.getChildrenCount(); // Sayma işlemi yaptırıldı.
                    txtWordCount.setText(String.valueOf(countWord));

                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void search() { // RecyclerView Arama İşlemleri

        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                ArrayList<WordModel> newWords = new ArrayList<>();

                for (WordModel word : wordModels) {

                    if (word.getWord().toLowerCase().contains(s.toLowerCase()) ||
                            word.getMeaning().toLowerCase().contains(s.toLowerCase())) { // Araç markasına göre arama yaptırıldı.

                        newWords.add(word);
                    }
                }

                wordAdapter.setWordModels(newWords);
                wordAdapter.notifyDataSetChanged();

                return true;
            }
        });
    }

    public void logout(){

        FirebaseAuth.getInstance().signOut();

        Intent intentLogout = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intentLogout);
        finish();
    }
}
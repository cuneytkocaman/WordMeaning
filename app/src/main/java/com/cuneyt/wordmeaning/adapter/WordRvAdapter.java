package com.cuneyt.wordmeaning.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cuneyt.wordmeaning.R;
import com.cuneyt.wordmeaning.entities.WordModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WordRvAdapter extends RecyclerView.Adapter<WordRvAdapter.MyViewHolder> {
    public Context context;
    public ArrayList<WordModel> wordModels;
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceTable, referenceUser;

    public WordRvAdapter(Context context, ArrayList<WordModel> wordModels) {
        this.context = context;
        this.wordModels = wordModels;
    }

    public void setWordModels(ArrayList<WordModel> wordModels) {
        this.wordModels = wordModels;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtWord, txtMeaning;
        ConstraintLayout constRow;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtWord = itemView.findViewById(R.id.txtWord);
            txtMeaning = itemView.findViewById(R.id.txtMeaning);
            constRow = itemView.findViewById(R.id.constRow);

        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.design_row_word, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String id = wordModels.get(position).getId();
        String word = wordModels.get(position).getWord();
        String meaning = wordModels.get(position).getMeaning();
        String language = wordModels.get(position).getLanguage();
        String description = wordModels.get(position).getDescription();

        holder.txtWord.setText(word);
        holder.txtMeaning.setText(meaning);

        holder.constRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                delete(holder, id, language);
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return wordModels.size();
    }

    public void delete(MyViewHolder holder, String id, String language) {

        holder.constRow.setOnLongClickListener(new View.OnLongClickListener() { // Silme işlemleri
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builderDelete = new AlertDialog.Builder(context);
                View viewDelete = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.alert_delete, null);

                TextView textAlertYes = viewDelete.findViewById(R.id.textAlertYes);
                TextView textAlertNo = viewDelete.findViewById(R.id.textAlertNo);

                builderDelete.setView(viewDelete);
                AlertDialog dialogDelete = builderDelete.create();
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                textAlertYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        referenceUser = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_user));
                        referenceTable = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_word));

                        referenceUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString(); // Online kullanıcının ID'di alındı.

                                referenceTable.child(currentUser).child(language).child(id).removeValue();

                                Toast.makeText(context, "Veri silindi.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        dialogDelete.dismiss();
                    }
                });

                textAlertNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialogDelete.dismiss();
                    }
                });

                dialogDelete.show();

                return true;
            }
        });
    }

}

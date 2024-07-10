package com.cuneyt.wordmeaning.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cuneyt.wordmeaning.R;
import com.cuneyt.wordmeaning.entities.WordModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtWord = itemView.findViewById(R.id.txtWord);
            txtMeaning = itemView.findViewById(R.id.txtMeaning);
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
        String description = wordModels.get(position).getDescription();

        holder.txtWord.setText(word);
        holder.txtMeaning.setText(meaning);

     //   firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public int getItemCount() {
        return wordModels.size();
    }


}

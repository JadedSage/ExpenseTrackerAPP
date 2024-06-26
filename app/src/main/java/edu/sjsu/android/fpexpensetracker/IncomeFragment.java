package edu.sjsu.android.fpexpensetracker;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import edu.sjsu.android.fpexpensetracker.Model.Data;

public class IncomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Data, MyViewHolder> adapter;
    private TextView incomeTotalSum;
    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;
    private Button btnUpdate;
    private Button btnDelete;
    private String type;
    private String note;
    private int amount;
    private String post_Key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myview = inflater.inflate(R.layout.fragment_income, container, false);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);

        incomeTotalSum = myview.findViewById(R.id.income_txt_result);

        recyclerView = myview.findViewById(R.id.recycler_id_income);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalValue = 0;

                for (DataSnapshot mySnapShot: snapshot.getChildren()){
                    Data data = mySnapShot.getValue(Data.class);
                    totalValue += data.getAmount();
                    String stTotalValue = String.valueOf(totalValue);
                    incomeTotalSum.setText("$" + stTotalValue + ".00");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return myview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mIncomeDatabase, Data.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.income_recycler_data, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setAmount(model.getAmount());
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        post_Key = getRef(position).getKey();
                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();
                        updateDataItem();
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void updateDataItem(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.update_data_item, null);

        myDialog.setView(myview);

        edtAmount = myview.findViewById(R.id.ammount_edt);
        edtType = myview.findViewById(R.id.type_edt);
        edtNote = myview.findViewById(R.id.note_edt);

        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());

        btnUpdate = myview.findViewById(R.id.btn_Upd_Update);
        btnDelete = myview.findViewById(R.id.btn_Upd_Delete);

        AlertDialog dialog = myDialog.create();
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mdAmount = String.valueOf(amount);

                type = edtType.getText().toString().trim();
                note = edtNote.getText().toString().trim();
                mdAmount = edtAmount.getText().toString().trim();

                int myAmount = Integer.parseInt(mdAmount);

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(myAmount, type, note, post_Key, mDate);

                mIncomeDatabase.child(post_Key).setValue(data);

                dialog.dismiss();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIncomeDatabase.child(post_Key).removeValue();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
    }

    void setType(String type) {
        TextView mType = mView.findViewById(R.id.type_txt_income);
        mType.setText(type);
    }

    void setNote(String note) {
        TextView mNote = mView.findViewById(R.id.note_txt_income);
        mNote.setText(note);
    }

    void setDate(String date) {
        TextView mDate = mView.findViewById(R.id.date_txt_income);
        mDate.setText(date);
    }

    void setAmount(int amount) {
        TextView mAmount = mView.findViewById(R.id.amount_txt_income);
        mAmount.setText("$" + amount);
    }
}

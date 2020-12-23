package com.upt.cti.smartwallet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upt.cti.smartwallet.model.Payment;
import com.upt.cti.smartwallet.ui.PaymentAdapter;

import java.util.ArrayList;
import java.util.List;

public class Payments extends AppCompatActivity {
    private TextView tStatus;
    private Button bPrevious, bNext;
    private DatabaseReference databaseReference;
    private FloatingActionButton fabAdd;
    private ListView listPayments;
    private PaymentAdapter adapter;
    private List<Payment> payments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        AppState.get().setDatabaseReference(databaseReference);

        initializeComponents();
        getPaymentsFromDB();
        onFabButtonClick();
        onListItemClick();
    }

    public void initializeComponents(){
        tStatus = (TextView) findViewById(R.id.tStatus);
        bPrevious = (Button) findViewById(R.id.bPrevious);
        bNext = (Button) findViewById(R.id.bNext);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        listPayments = (ListView) findViewById(R.id.listPayments);
    }

    public void getPaymentsFromDB( ){
        databaseReference.child("wallet").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    Payment payment = snapshot.getValue(Payment.class);
                    payment.setTimestamp(snapshot.getKey());
                    payments.add(payment);

                    adapter = new PaymentAdapter(Payments.this, R.layout.item_payment, payments);
                    listPayments.setAdapter(adapter);
                    tStatus.setText("Found " + payments.size() + " payments in DB");
                } catch (Exception e) {
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i = 0; i < payments.size(); i++) {
                    if (payments.get(i).timestamp.equals(snapshot.getKey().toString()))
                        try {
                            Payment updatePayment = snapshot.getValue(Payment.class);
                            updatePayment.setTimestamp(snapshot.getKey());

                            payments.set(i, updatePayment);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < payments.size(); i++) {
                    if (payments.get(i).timestamp.equals(snapshot.getKey()))
                        payments.remove(i);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onFabButtonClick(){
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppState.get().setCurrentPayment(null);
                Intent intent = new Intent(Payments.this, AddPayment.class);
                startActivity(intent);
            }
        });

    }

    public void onListItemClick() {
        listPayments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppState.get().setCurrentPayment(payments.get(position));

                startActivity(new Intent(Payments.this, AddPayment.class));
            }
        });
    }

}
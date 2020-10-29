package com.ingoox.contatos;

import android.Manifest;
import androidx.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ingoox.contatos.data.AppDatabase;
import com.ingoox.contatos.data.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 100;

    private AppDatabase db;
    private RecyclerViewAdapter adapter;
    private List<Contact> contacts = new ArrayList<>();

    private String selectedPhoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "contacts").allowMainThreadQueries().build();

        // Get all contacts from database
        contacts = db.contactDao().getAllContacts();

        if (contacts.isEmpty()) {
            Snackbar.make(findViewById(R.id.recycler_view),
                    R.string.snack_text_no_contacts,
                    Snackbar.LENGTH_SHORT).show();
        }

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Setup Divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        // Open EditActivity to create a contact
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent
                        (ContactsActivity.this, EditActivity.class));
            }
        });

        // Hide FloatingActionButton when scrolling down
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fab.hide();
                } else if (dy < 0) {
                    fab.show();
                }
            }
        });
    }

    // Handle permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPhone();
            }
        }
    }

    private void callPhone() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + selectedPhoneNumber));

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            this.startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        contacts = db.contactDao().getAllContacts();
        adapter.notifyDataSetChanged();
    }

    private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView tvName;
        private final TextView tvNumber;
        private final ImageView phoneIcon;

        ItemHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));
            tvName = itemView.findViewById(R.id.contact_name_tv);
            tvNumber = itemView.findViewById(R.id.contact_number_tv);
            phoneIcon = itemView.findViewById(R.id.phoneIcon);

            itemView.setOnClickListener(this);
            phoneIcon.setOnClickListener(this);
            phoneIcon.setOnLongClickListener(this);
        }

        // Bind method
        void bind(Contact contact) {
            tvName.setText(contact.getName());
            tvNumber.setText(contact.getNumber());
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == phoneIcon.getId()) {
                selectedPhoneNumber = tvNumber.getText().toString();
                callPhone();
            } else {
                Intent intent = new Intent(ContactsActivity.this, EditActivity.class);
                // Start EditActivity passing contact's ID
                int contactId = contacts.get(getAdapterPosition()).getId();
                intent.putExtra("contact_id", contactId);
                startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (view.getId() == phoneIcon.getId()) {
                selectedPhoneNumber = tvNumber.getText().toString();
                String url = "https://api.whatsapp.com/send?phone=" + selectedPhoneNumber;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
            return true;
        }
    }

    // RecyclerView.Adapter<RecyclerViewAdapter.ItemHolder>
    // when ItemHolder is inner class of RecyclerViewAdapter
    private class RecyclerViewAdapter extends RecyclerView.Adapter<ItemHolder> {
        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ContactsActivity.this);
            return new ItemHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            holder.bind(contacts.get(position));
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}

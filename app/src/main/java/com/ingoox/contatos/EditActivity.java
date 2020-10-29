package com.ingoox.contatos;

import androidx.room.Room;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.ingoox.contatos.data.AppDatabase;
import com.ingoox.contatos.data.Contact;

public class EditActivity extends AppCompatActivity
{
    private AppDatabase db;
    private int id;

    private EditText etName;
    private EditText etNumber;
    private EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etName = findViewById(R.id.name_edit_text);
        etNumber = findViewById(R.id.number_edit_text);
        etEmail = findViewById(R.id.email_edit_text);

        // Setup database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "contacts").allowMainThreadQueries().build();

        // Get selected contact ID, default to -1 when no ID is passed from ContactsActivity
        id = getIntent().getIntExtra("contact_id", -1);

        // Create activity in "New contact" or "Edit contact" mode
        if (id == -1)
        {
            setTitle(getString(R.string.title_create_contact));
        }
        else
        {
            setTitle(getString(R.string.title_edit_contact));
            Contact contact = db.contactDao().getContact(id);
            etName.setText(contact.getName());
            etNumber.setText(contact.getNumber());
            etEmail.setText(contact.getEmail());
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                saveContact();
                break;
            case R.id.action_delete:
                deleteContact();
                break;
            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveContact()
    {
        if (getString(etNumber).isEmpty())
        {
            etNumber.setError("Contato sem número");
        }
        else
        {
            Contact contact = new Contact(getString(etName), getString(etNumber), getString(etEmail));

            // Insert or Update contact based on "Mode" of activity
            if (id == -1)
            {
                // Insert new contact into database
                db.contactDao().insert(contact);
            }
            else
            {
                // Update existing contact on database
                contact.setId(id);
                db.contactDao().update(contact);
            }

            finish();
        }
    }

    private void deleteContact()
    {
        if (id != -1)
        {
            Contact contact = new Contact("", "", "");
            contact.setId(id);
            db.contactDao().delete(contact);
        }

        finish();
    }

    // Get string from EditText
    private String getString(EditText editText)
    {
        return editText.getText().toString().trim();
    }
}

package com.ingoox.contatos.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao
{
    @Insert
    void insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("SELECT * FROM contact WHERE id = :contactId")
    Contact getContact(int contactId);

    @Query("SELECT * FROM contact")
    List<Contact> getAllContacts();
}

package pro.adamzielonka.converter.activities.edit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import pro.adamzielonka.converter.R;
import pro.adamzielonka.converter.activities.StartActivity;
import pro.adamzielonka.converter.activities.abstractes.EditActivity;
import pro.adamzielonka.converter.adapters.MyArrayAdapter;
import pro.adamzielonka.converter.models.database.CloudMeasure;
import pro.adamzielonka.converter.models.database.User;
import pro.adamzielonka.converter.models.user.Measure;
import pro.adamzielonka.converter.models.user.Prefix;
import pro.adamzielonka.converter.models.user.Unit;
import pro.adamzielonka.converter.services.MyUploadService;
import pro.adamzielonka.converter.tools.Test;
import pro.adamzielonka.converter.tools.Tests;

import static pro.adamzielonka.converter.tools.Code.REQUEST_EDIT_ACTIVITY;
import static pro.adamzielonka.converter.tools.Code.REQUEST_SAVE_TO_DOWNLOAD;
import static pro.adamzielonka.converter.tools.FileTools.getFileUri;
import static pro.adamzielonka.converter.tools.FileTools.getGson;
import static pro.adamzielonka.converter.tools.FileTools.isExternalStorageWritable;
import static pro.adamzielonka.converter.tools.Language.getLangCode;
import static pro.adamzielonka.converter.tools.Language.getLanguageWords;
import static pro.adamzielonka.converter.tools.Message.showError;
import static pro.adamzielonka.converter.tools.Message.showSuccess;
import static pro.adamzielonka.converter.tools.Permissions.getReadAndWritePermissionsStorage;

public class EditMeasureActivity extends EditActivity {

    private DatabaseReference mDatabase;
    private static final String TAG = "EditMeasureActivity";
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void addItems() {
        setTitle(R.string.title_activity_edit_measure);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                hideProgressDialog();
                if (intent.getAction() != null)
                    switch (intent.getAction()) {
                        case MyUploadService.UPLOAD_COMPLETED:
                            showSuccess(EditMeasureActivity.this, R.string.success_upload);
                            break;
                        case MyUploadService.UPLOAD_ERROR:
                            showError(EditMeasureActivity.this, R.string.msg_error_upload);
                            break;
                    }
            }
        };
        ArrayAdapter<Unit> adapter = new MyArrayAdapter<Unit>(getApplicationContext(), userMeasure.units) {
            @Override
            public void setView(Unit item, TextView textPrimary, TextView textSecondary) {
                String description = getLanguageWords(item.descriptionPrefix, userMeasure.global)
                        + getLanguageWords(item.description, userMeasure.global);
                String unitName = item.symbol + (!description.isEmpty() ? " - " + description : "");

                StringBuilder prefixes = new StringBuilder("");
                for (Prefix prefix : item.prefixes) {
                    prefixes.append(prefix.symbol).append(item.symbol);
                    prefixes.append(" ");
                }
                textPrimary.setText(unitName);
                textSecondary.setText(prefixes.toString());
            }
        };

        addItemTitle(R.string.list_title_language);
        addItemText(R.string.list_item_language_available, () -> concreteMeasure.languages.toString(),
                () -> startActivityForResult(setEditIntent(EditLanguagesActivity.class), REQUEST_EDIT_ACTIVITY));
        addItemList(R.string.list_item_language_global, () -> concreteMeasure.global, () -> concreteMeasure.getGlobalLangs(),
                () -> concreteMeasure.getGlobalID(), id -> userMeasure.global = concreteMeasure.getGlobalFromID(id));
        addItemTitle(R.string.list_title_Measure);
        addItemText(R.string.list_item_name, () -> userMeasure.getName(userMeasure.global), name -> userMeasure.setName(concreteMeasure.global, name));
        addItemTextIf(R.string.list_item_units_order, () -> userMeasure.units.size() > 0, () -> concreteMeasure.getUnitsOrder(),
                () -> startActivityForResult(setEditIntent(EditOrderUnitsActivity.class), REQUEST_EDIT_ACTIVITY));
        addItemListIF(R.string.list_item_measure_default_1, () -> userMeasure.units.size() > 0, () -> concreteMeasure.concreteUnits.get(concreteMeasure.displayFrom).name,
                () -> concreteMeasure.getUnitsSymbol(), () -> userMeasure.displayFrom, id -> userMeasure.displayFrom = id);
        addItemListIF(R.string.list_item_measure_default_2, () -> userMeasure.units.size() > 0, () -> concreteMeasure.concreteUnits.get(concreteMeasure.displayTo).name,
                () -> concreteMeasure.getUnitsSymbol(), () -> userMeasure.displayTo, id -> userMeasure.displayTo = id);
        addItemTitle(R.string.list_title_units);
        addItemsAdapter(adapter, () -> userMeasure.units, position -> {
            unit = adapter.getItem(position);
            startActivityForResult(setEditIntent(EditUnitActivity.class), REQUEST_EDIT_ACTIVITY);
        });
        addItem(R.string.list_item_add_unit, () -> newAlertDialogCreate(R.string.dialog_unit_symbol, EditUnitActivity.class,
                this::newUnit, new Test(symbol -> Tests.isUnique(symbol, userMeasure.units), R.string.error_symbol_unit_already_exist)));
    }

    private void newUnit(String symbol) {
        unit = new Unit();
        unit.symbol = symbol;
        userMeasure.units.add(unit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_measure, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_delete_converter:
                newAlertDialogDelete(R.string.delete_measure_title, () -> {
                    if (getFileStreamPath(concreteMeasure.concreteFileName).delete() &&
                            getFileStreamPath(concreteMeasure.userFileName).delete()) {
                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                return true;
            case R.id.menu_save_converter:
                ActivityCompat.requestPermissions(this,
                        getReadAndWritePermissionsStorage(), REQUEST_SAVE_TO_DOWNLOAD);
                return true;
            case R.id.menu_upload_converter:
                submitMeasure();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_SAVE_TO_DOWNLOAD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && isExternalStorageWritable()) {
                saveToDownloads();
            } else {
                showError(this, R.string.error_no_permissions_to_write_file);
            }
        }
    }

    private void saveToDownloads() {
        try {
            FileInputStream in = openFileInput(concreteMeasure.userFileName);
            Reader reader = new BufferedReader(new InputStreamReader(in));
            Gson gson = getGson();
            String json = gson.toJson(gson.fromJson(reader, Measure.class));

            OutputStream out = getContentResolver().openOutputStream(getFileUri(concreteMeasure.getName(userMeasure.global)));
            if (out != null) {
                out.write(json.getBytes());
                out.close();
                showSuccess(this, R.string.success_save_to_downloads);
            } else showError(this, R.string.error_create_file);
        } catch (Exception e) {
            e.printStackTrace();
            showError(this, R.string.error_create_file);
        }
    }

    private void submitMeasure() {
        String userId = getUid();
        if (userId == null) return;
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(EditMeasureActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            updateMeasure(userId, user.username, userMeasure.getName(getLangCode(EditMeasureActivity.this)), concreteMeasure.getUnitsOrder());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    private void updateMeasure(String userId, String username, String title, String body) {
        if (userMeasure.cloudID.equals("")) {
            String key = mDatabase.child("measures").push().getKey();
            CloudMeasure cloudMeasure = new CloudMeasure(userId, username, title, body, body, 1L);
            doUpdateMeasure(key, cloudMeasure);
        } else {
            Query query = mDatabase.child("measures");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.hasChild(userMeasure.cloudID)) {
                        CloudMeasure cloudMeasure = snapshot.child(userMeasure.cloudID).getValue(CloudMeasure.class);
                        if (cloudMeasure.uid.equals(getUid())) {
                            cloudMeasure.version++;
                            cloudMeasure.title = title;
                            cloudMeasure.units_symbols = body;
                            cloudMeasure.units_names = body;
                            doUpdateMeasure(userMeasure.cloudID, cloudMeasure);
                        } else {
                            String key = mDatabase.child("measures").push().getKey();
                            cloudMeasure = new CloudMeasure(userId, username, title, body, body, 1L);
                            doUpdateMeasure(key, cloudMeasure);
                        }
                    } else {
                        String key = mDatabase.child("measures").push().getKey();
                        CloudMeasure cloudMeasure = new CloudMeasure(userId, username, title, body, body, 1L);
                        doUpdateMeasure(key, cloudMeasure);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    private void doUpdateMeasure(String key, CloudMeasure cloudMeasure) {
        cloudMeasure.file = concreteMeasure.userFileName;
        Map<String, Object> postValues = cloudMeasure.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/measures/" + key, postValues);
        childUpdates.put("/user-measures/" + cloudMeasure.uid + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
        DatabaseReference ref = mDatabase.child("measures").child(key).child("version");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long version = dataSnapshot.getValue(Long.class);
                userMeasure.cloudID = key;
                userMeasure.version = version;
                onSave(false);
                File file = new File(getFilesDir() + "/" + concreteMeasure.userFileName);
                uploadFromUri(Uri.parse(file.toURI().toString()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void uploadFromUri(Uri fileUri) {
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .putExtra(MyUploadService.EXTRA_FILE_USER, getUid())
                .putExtra(MyUploadService.EXTRA_FILE_CONCRETE, concreteMeasure.concreteFileName)
                .setAction(MyUploadService.ACTION_UPLOAD));

        showProgressDialog(getString(R.string.progress_uploading));
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

}

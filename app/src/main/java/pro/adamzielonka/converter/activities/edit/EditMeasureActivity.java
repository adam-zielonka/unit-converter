package pro.adamzielonka.converter.activities.edit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import pro.adamzielonka.converter.R;
import pro.adamzielonka.converter.activities.StartActivity;
import pro.adamzielonka.converter.adapters.UnitsAdapter;
import pro.adamzielonka.converter.units.user.Measure;
import pro.adamzielonka.converter.units.user.Unit;

import static pro.adamzielonka.converter.tools.Code.REQUEST_EDIT_ACTIVITY;
import static pro.adamzielonka.converter.tools.FileTools.getFileUri;
import static pro.adamzielonka.converter.tools.FileTools.getGson;
import static pro.adamzielonka.converter.tools.FileTools.isExternalStorageWritable;
import static pro.adamzielonka.converter.tools.Message.showError;
import static pro.adamzielonka.converter.tools.Message.showSuccess;
import static pro.adamzielonka.converter.tools.Permissions.getReadAndWritePermissionsStorage;

public class EditMeasureActivity extends EditActivity implements ListView.OnItemClickListener {

    private UnitsAdapter unitsAdapter;

    private View editMeasureNameView;
    private View editUnitOrder;
    private View addUnit;

    private static final int REQUEST_SAVE_TO_DOWNLOAD = 1;

    @Override
    public void onLoad() throws FileNotFoundException {
        super.onLoad();
        unitsAdapter = new UnitsAdapter(getApplicationContext(), userMeasure.getUnits());
        listView.setAdapter(unitsAdapter);
        listView.setOnItemClickListener(this);

        listView.addHeaderTitle(getString(R.string.list_title_Measure));
        editMeasureNameView = listView.addHeaderItem(getString(R.string.list_item_name), userMeasure.getName());
        editUnitOrder = listView.addHeaderItem(getString(R.string.list_item_units_order), concreteMeasure.getUnitsOrder());
        listView.addHeaderTitle(getString(R.string.list_title_units));
        addUnit = listView.addFooterItem(getString(R.string.list_item_add_unit));
    }

    @Override
    public void onReload() throws FileNotFoundException {
        super.onReload();
        ((TextView) editMeasureNameView.findViewById(R.id.textSecondary)).setText(userMeasure.getName());
        ((TextView) editUnitOrder.findViewById(R.id.textSecondary)).setText(concreteMeasure.getUnitsOrder());
        unitsAdapter.clear();
        unitsAdapter.addAll(userMeasure.getUnits());
        unitsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (isUnderItemClick(position, listView.getCountHeaderItems(), userMeasure.getUnits().size())) {
            Unit unit = unitsAdapter.getItem(position - listView.getCountHeaderItems());
            Intent intent = new Intent(getApplicationContext(), EditUnitActivity.class);
            intent.putExtra("measureFileName", concreteMeasure.getConcreteFileName());
            intent.putExtra("unitName", unit != null ? unit.getSymbol() : "");
            startActivityForResult(intent, REQUEST_EDIT_ACTIVITY);
        } else {
            if (view.equals(editMeasureNameView)) {
                View layout = getLayoutInflater().inflate(R.layout.layout_dialog_edit_text, null);
                EditText editText = getDialogEditText(layout, userMeasure.getName());
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_measure_name)
                        .setView(layout)
                        .setCancelable(true)
                        .setPositiveButton(R.string.dialog_save, (dialog, which) -> {
                            userMeasure.setName(editText.getText().toString());
                            onSave();
                        }).setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                }).show();

            } else if (view.equals(editUnitOrder)) {
                Intent intent = new Intent(getApplicationContext(), EditOrderUnitsActivity.class);
                intent.putExtra("measureFileName", concreteMeasure.getConcreteFileName());
                startActivityForResult(intent, REQUEST_EDIT_ACTIVITY);

            } else if (view.equals(addUnit)) {
                View layout = getLayoutInflater().inflate(R.layout.layout_dialog_edit_text, null);
                EditText editText = getDialogEditText(layout, "");
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_unit_symbol)
                        .setView(layout)
                        .setCancelable(true)
                        .setPositiveButton(R.string.dialog_save, (dialog, which) -> {
                            String newUnitName = editText.getText().toString();
                            if (!isSymbolUnitExist(newUnitName, userMeasure.getUnits())) {
                                Unit newUnit = new Unit();
                                newUnit.setSymbol(newUnitName);
                                userMeasure.getUnits().add(newUnit);
                                onSave(false);
                                Intent intent = new Intent(getApplicationContext(), EditUnitActivity.class);
                                intent.putExtra("measureFileName", concreteMeasure.getConcreteFileName());
                                intent.putExtra("unitName", newUnitName);
                                startActivityForResult(intent, REQUEST_EDIT_ACTIVITY);
                            } else {
                                showError(this, R.string.error_symbol_unit_already_exist);
                            }
                        }).setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                }).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        finish();
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
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_delete_converter:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_measure_title)
                        .setCancelable(true)
                        .setPositiveButton(R.string.dialog_delete, (dialog, which) -> {
                            if (getFileStreamPath(concreteMeasure.getConcreteFileName()).delete() &&
                                    getFileStreamPath(concreteMeasure.getUserFileName()).delete()) {
                                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                }).show();
                return true;
            case R.id.menu_save_converter:
                ActivityCompat.requestPermissions(this,
                        getReadAndWritePermissionsStorage(), REQUEST_SAVE_TO_DOWNLOAD);
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
            FileInputStream in = openFileInput(concreteMeasure.getUserFileName());
            Reader reader = new BufferedReader(new InputStreamReader(in));
            Gson gson = getGson();
            String json = gson.toJson(gson.fromJson(reader, Measure.class));

            OutputStream out = getContentResolver().openOutputStream(getFileUri(concreteMeasure.getName()));
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
}

package pro.adamzielonka.converter.activities.edit;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import pro.adamzielonka.converter.R;
import pro.adamzielonka.converter.activities.abstractes.EditActivity;

import static pro.adamzielonka.converter.tools.Message.showError;
import static pro.adamzielonka.converter.tools.Number.doubleToString;
import static pro.adamzielonka.converter.tools.Number.stringToDouble;

public class EditPrefixActivity extends EditActivity implements ListView.OnItemClickListener {

    private View prefixNameView;
    private View prefixDescriptionView;
    private View prefixExponentView;

    @Override
    public void onLoad() throws Exception {
        super.onLoad();
        listView.setEmptyAdapter();
        listView.setOnItemClickListener(this);

        listView.addHeaderTitle(getString(R.string.list_title_prefix));
        prefixNameView = listView.addHeaderItem(getString(R.string.list_item_symbol));
        prefixDescriptionView = listView.addHeaderItem(getString(R.string.list_item_description));
        prefixExponentView = listView.addHeaderItem(getString(R.string.list_item_exponent));
    }

    @Override
    public void onUpdate() throws Exception {
        super.onUpdate();
        updateView(prefixNameView, prefix.getSymbol());
        updateView(prefixDescriptionView, prefix.getDescription());
        updateView(prefixExponentView, doubleToString(prefix.getExp()));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (view.equals(prefixNameView)) {
            EditText editText = getDialogEditText(prefix.getSymbol());
            getAlertDialogSave(R.string.dialog_prefix_symbol, editText.getRootView(), (dialog, which) -> {
                String newName = editText.getText().toString();
                if (!newName.equals(prefixName)) {
                    if (!isSymbolPrefixExist(newName, unit.getPrefixes())) {
                        prefix.setSymbol(newName);
                        prefixName = newName;
                        onSave();
                    } else {
                        showError(this, R.string.error_symbol_prefix_already_exist);
                    }
                }
            }).show();

        } else if (view.equals(prefixDescriptionView)) {
            EditText editText = getDialogEditText(prefix.getDescription());
            getAlertDialogSave(R.string.dialog_prefix_description, editText.getRootView(), (dialog, which) -> {
                prefix.setDescription(editText.getText().toString());
                onSave();
            }).show();

        } else if (view.equals(prefixExponentView)) {
            EditText editText = getDialogEditNumber(prefix.getExp());
            getAlertDialogSave(R.string.dialog_prefix_exponent, editText.getRootView(), (dialog, which) -> {
                prefix.setExp(stringToDouble(editText.getText().toString()));
                onSave();
            }).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_delete:
                getAlertDialogDelete(R.string.delete_unit_title, (dialog, which) -> {
                    unit.getPrefixes().remove(prefix);
                    onSave(false);
                    onBackPressed();
                }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

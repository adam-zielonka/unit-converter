package pro.adamzielonka.converter.activities.abstractes;

import android.content.Intent;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import pro.adamzielonka.converter.R;
import pro.adamzielonka.converter.models.concrete.ConcreteMeasure;
import pro.adamzielonka.converter.models.user.Measure;
import pro.adamzielonka.converter.models.user.Prefix;
import pro.adamzielonka.converter.models.user.Unit;
import pro.adamzielonka.converter.tools.FileTools;

import static pro.adamzielonka.converter.tools.FileTools.getGson;
import static pro.adamzielonka.converter.tools.Message.showError;

public abstract class EditActivity extends ListActivity {

    protected Measure userMeasure;
    protected ConcreteMeasure concreteMeasure;
    protected Unit unit;
    protected Prefix prefix;

    private String measureFileName;
    protected String unitName;
    protected String prefixName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            onUpdate();
        } catch (Exception e) {
            finish();
        }
    }

    protected void onSave() {
        onSave(true);
    }

    protected void onSave(boolean reload) {
        try {
            FileTools.saveMeasure(this, concreteMeasure,  userMeasure);
            setResultCode(RESULT_OK);
            if (reload) onUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            showError(this, R.string.error_could_not_save_changes);
        }
    }

    protected void onLoad() throws Exception {
        Intent intent = getIntent();
        measureFileName = intent.getStringExtra("measureFileName");
        unitName = intent.getStringExtra("unitName");
        prefixName = intent.getStringExtra("prefixName");
        super.onLoad();
        onOpen();
    }

    protected void onUpdate() throws Exception {
        onOpen();
    }

    private void onOpen() throws Exception {
        concreteMeasure = openConcreteMeasure(measureFileName);
        userMeasure = openMeasure(concreteMeasure.getUserFileName());
        unit = userMeasure != null ? openUnit(unitName, userMeasure) : null;
        prefix = unit != null ? openPrefix(prefixName, unit) : null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK) {
            try {
                setResultCode(RESULT_OK);
                onUpdate();
            } catch (Exception e) {
                finish();
            }
        }
    }

    //region open and save
    private ConcreteMeasure openConcreteMeasure(String fileName) throws FileNotFoundException {
        FileInputStream in = openFileInput(fileName);
        Reader reader = new BufferedReader(new InputStreamReader(in));
        return getGson().fromJson(reader, ConcreteMeasure.class);
    }

    private Measure openMeasure(String fileName) throws FileNotFoundException {
        FileInputStream in = openFileInput(fileName);
        Reader reader = new BufferedReader(new InputStreamReader(in));
        return getGson().fromJson(reader, Measure.class);
    }

    private Unit openUnit(String unitName, Measure measure) {
        for (Unit unit : measure.getUnits()) {
            if (unit.getSymbol().equals(unitName))
                return unit;
        }
        return null;
    }

    private Prefix openPrefix(String prefixName, Unit unit) {
        for (Prefix prefix : unit.getPrefixes()) {
            if (prefix.getSymbol().equals(prefixName))
                return prefix;
        }
        return null;
    }
    //endregion

    //region isTrueOrFalse
    protected boolean isSymbolPrefixExist(String newName, List<Prefix> prefixes) {
        for (Prefix prefix : prefixes) {
            if (prefix.getSymbol().equals(newName)) return true;
        }
        return false;
    }

    protected boolean isSymbolUnitExist(String newName, List<Unit> units) {
        for (Unit unit : units) {
            if (unit.getSymbol().equals(newName)) return true;
        }
        return false;
    }
    //endregion

    protected Intent setEditIntent(Class<?> cls) {
        Intent intent = new Intent(getApplicationContext(), cls);
        intent.putExtra("measureFileName", concreteMeasure.getConcreteFileName());
        intent.putExtra("unitName", unit != null ? unit.getSymbol() : "");
        intent.putExtra("prefixName", prefix != null ? prefix.getSymbol() : "");
        return intent;
    }
}